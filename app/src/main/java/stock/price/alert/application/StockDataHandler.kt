package stock.price.alert.application

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.sync.Mutex
import org.json.JSONObject
import java.time.LocalDate
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashMap
import kotlin.concurrent.withLock


@RequiresApi(Build.VERSION_CODES.O)
class StockDataHandler(val context : Context, val symbol: String) {
    private var queryMap = HashMap<String, JSONObject>()
    private var parsedResponse = hashMapOf<String, Vector<Pair<String, Float>>>()
    private var dataLock : HashMap<String, ReentrantLock>
    private var lockCond = hashMapOf<String, Condition>()

    init { // init lock condition
        dataLock = hashMapOf(
            "day" to ReentrantLock(),
            "week" to ReentrantLock(),
            "3week" to ReentrantLock(),
            "month" to ReentrantLock(),
            "3month" to ReentrantLock(),
            "year" to ReentrantLock(),
            "5year" to ReentrantLock()
        )
        for ((k, v) in dataLock) {
            lockCond[k] = v.newCondition()
        }
    }

    fun QueryDataThenPlot(type : String, ploter: PricePloter)  {
        // if lock is in used, wait for data before ploting
        if (dataLock[type]?.isLocked!!) {
            dataLock[type]?.withLock {
                ploter.PlotData(parsedResponse[type])
            }
            return
        }

        // if lock not in used, check if result already exists.
        if (parsedResponse.containsKey(type)) {
            ploter.PlotData(parsedResponse[type])
        }
        // if result not exists yet, check of json response received yet
        else {
            if (queryMap.containsKey(QueryAPI().cQueryFuncs[type])) {
                dataLock[type]!!.withLock {
                    SetResponseData(type)
                }
                ploter.PlotData(parsedResponse[type])
            }
            else {
                // if both data and response not exists, make query with ploter call back
                MakeQuery(type, ploter)
            }
        }
    }

    fun QueryData(type : String) {
        // if lock in use, do noting and wait for result
        if (dataLock[type]?.isLocked!!) {
            return
        }

        // if lock not in use and result is not ready, make query now
        if (!parsedResponse.containsKey(type)) {
            // if data set not exists but url response exists, parse it
            if (queryMap.containsKey(QueryAPI().cQueryFuncs[type])) {
                dataLock[type]!!.withLock {
                    SetResponseData(type)
                }
            }
            // no data and not response exists yet
            else {
                MakeQuery(type, null)
            }
        }
    }


    fun MakeQuery(type : String, ploter : PricePloter?) {
        val textView : TextView? = (context as Activity).findViewById(R.id.responseTextView)
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, QueryAPI().GenQueryStr(type, symbol), null,
            Response.Listener { response ->
                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()

                // for debug, print out
                textView?.text = "Response: %s".format(response.toString())

                // process data
                dataLock[type]?.withLock {
                    // skip if data already saved
                    if (!parsedResponse.containsKey(type))
                    {
                        Log.d("RESP", response.toString())
                        // save response to map
                        AddResponse(QueryAPI().cQueryFuncs[type]!!, response.toString())
                        // parse current response into data set
                        SetResponseData(type)
                        Log.d("CHECKDATA", parsedResponse[type].toString())
                        lockCond[type]?.signalAll()
                    }
                }

                // make ploter callback if available, plot update UI in main thread
                ploter?.let{
                    val mainUI = Handler(context.mainLooper)
                    mainUI.post {
                        ploter.PlotData(parsedResponse[type])
                    }
                }
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
                textView?.text = "Response: %s".format(error.toString())
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()

            }
        )
        // Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest)
    }

    fun AddResponse(queryFunc : String, resp : String) {
        // save type->resp pair to map
        val metaKey = "Meta Data"
        val respJSON = JSONObject(resp)
        if (respJSON.opt(metaKey) == null) {
            throw Exception("Error: Invalid query response: $queryFunc")
        }
        queryMap.set(queryFunc, respJSON)
    }


    // pasre and cache response data
    @RequiresApi(Build.VERSION_CODES.O)
    fun SetResponseData(type : String) : Vector<Pair<String, Float>>? {
        var range : Int
        var freq : Int
        when(type) {
            "day" -> { range = 1; freq = 1 }
            "week" -> { range = 7; freq = 1 }
            "month" -> { range = 30; freq = 1 }
            "3month" -> { range = 90; freq = 1 }
            "year" -> { range = 365; freq = 1 }
            "5year" -> { range = 825; freq = 1 }
            else -> throw Exception("Error: Invalid data type requested")
        }

        if (!parsedResponse.containsKey(type)) {
            parseResponse(type, range, freq)
        }
        return parsedResponse[type]
    }

    fun GetResponse(type : String) : String? {
        return queryMap.get(getFuncType(type)).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseResponse(type : String, range : Int, freq : Int) {
        var dataKey : String  // data field tag
        var valueKey : String // data point tag
        when(type) {
            "day" -> { dataKey = "Time Series (5min)"; valueKey = "4. close" }
            "week" -> { dataKey = "Time Series (60min)"; valueKey = "4. close" }
            "month" -> { dataKey = "Time Series (Daily)"; valueKey = "5. adjusted close" }
            "3month" -> { dataKey = "Time Series (Daily)"; valueKey = "5. adjusted close" }
            "year" -> { dataKey = "Weekly Adjusted Time Series"; valueKey = "5. adjusted close" }
            "5year" -> { dataKey = "Weekly Adjusted Time Series"; valueKey = "5. adjusted close" }
            else -> throw Exception("Error: Try to parse invalid data type")
        }

        Log.d("TOPARSE", queryMap[getFuncType(type)].toString())
        // construct time range by computing start date of sampling
        val timeStampStr = queryMap[getFuncType(type)]?.getJSONObject("Meta Data")
            ?.getString("3. Last Refreshed")
            ?: throw Exception("Error: Cannot get data timestamp")
        val endDateStr : String = timeStampStr.split(" ")[0]
        val endDate = LocalDate.parse(endDateStr)
        val startDate = endDate.plusDays((-range).toLong())
        Log.d("STARTDATE", startDate.toString())

        parsedResponse[type] = Vector()
        var pickCnt = 0  // only sample at desired frequency
        val data = queryMap.get(getFuncType(type))!!.getJSONObject(dataKey)
        val timePoint = data.keys() // keys should be an ordered list of time string
        while(timePoint.hasNext()) {
            // extract sample's date info
            val timeStr : String = timePoint.next()
            val dateStr = timeStr.split(" ")[0]
            val date = LocalDate.parse(dateStr)

            val step = data.getJSONObject(timeStr)
            // date range check
            if (date.isAfter(startDate)) {
                pickCnt++
                if (pickCnt == freq) { // sample at freq
                    parsedResponse[type]!!.add(0, Pair(timeStr, step.getString(valueKey).toFloat()))
                    pickCnt = 0
                }
            }
            else {
                // reach start date, stop sampling
                break
            }
        } // while
    }

        private fun getFuncType(type : String) : String {
            return when(type) {
                "day" -> "TIME_SERIES_INTRADAY" // compact 5 min
                "week" -> "TIME_SERIES_INTRADAY" // compact 60 mins
                "month" -> "TIME_SERIES_DAILY_ADJUSTED" // compact daily adjusted 30
                "3month" -> "TIME_SERIES_DAILY_ADJUSTED" // compact daily adjusted 90
                "year" -> "TIME_SERIES_WEEKLY_ADJUSTED" //
                "5year" -> "TIME_SERIES_WEEKLY_ADJUSTED"
                else -> throw Exception("Error: Invalid FuncType key.")
            } //
        }

}