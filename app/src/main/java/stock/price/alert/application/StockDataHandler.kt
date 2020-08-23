package stock.price.alert.application

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap


@RequiresApi(Build.VERSION_CODES.O)
class StockDataHandler(queries : HashMap<String, String>) {
    private lateinit var queryMap : HashMap<String, JSONObject>
    private var parsedResponse = hashMapOf<String, Vector<Pair<String, Float>>>()
    init {
        Reset(queries)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Reset(queries : HashMap<String, String>) {
        // TODO:
        //  1. reset function should call parse daily data by default
        queryMap = hashMapOf()
        for ((key, value) in queries) {
            if (value == null) {
                throw Exception("Error: Invalid query response")
            }
            val metaKey = "Meta Data"
            val respJSON = JSONObject(value)
            if (respJSON.opt(metaKey) == null) {
                throw Exception("Error: Invalid query response: $key")
            }
            queryMap.set(key, respJSON)
        }
        GetData("day")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun GetData(type : String) : Vector<Pair<String, Float>>? {
        var range : Int
        var freq : Int
        when(type) {
            "day" -> { range = 1; freq = 1 }
            "week" -> { range = 5; freq = 15 }
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
        return queryMap.get(type).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseResponse(type : String, range : Int, freq : Int) {
        var dataKey : String  // data field tag
        var valueKey : String // data point tag
        when(type) {
            "day" -> { dataKey = "Time Series (1min)"; valueKey = "4. close" }
            "week" -> { dataKey = "Time Series (1min)"; valueKey = "4. close" }
            "month" -> { dataKey = "Time Series (Daily)"; valueKey = "5. adjusted close" }
            "3month" -> { dataKey = "Time Series (Daily)"; valueKey = "5. adjusted close" }
            "year" -> { dataKey = "Time Series (Daily)"; valueKey = "5. adjusted close" }
            "5year" -> { dataKey = "Weekly Adjusted Time Series"; valueKey = "5. adjusted close" }
            else -> throw Exception("Error: Try to parse invalid data type")
        }

        // construct time range by computing start date of sampling
        val timeStampStr = queryMap[type]?.getJSONObject("Meta Data")
            ?.getString("3. Last Refreshed")
            ?: throw Exception("Error: Cannot get data timestamp")
        val endDateStr : String = timeStampStr.split(" ")[0]
        val endDate = LocalDate.parse(endDateStr)
        val startDate = endDate.plusDays((-range).toLong())

        parsedResponse[type] = Vector()
        var pickCnt = 0  // only sample at desired frequency
        val data = queryMap.get(type)!!.getJSONObject(dataKey)
        val timePoint = data.keys()
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


}