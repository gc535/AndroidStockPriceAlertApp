package stock.price.alert.application.ui.stock

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import stock.price.alert.application.Data.WatchListDBHandler
import java.time.LocalDate
import java.util.*

class TickerViewModel : ViewModel() {
    private var price = MutableLiveData<String> ()
    private var priceSeries = MutableLiveData<Vector<Pair<String, Float>>>()
    private var alertPrices = MutableLiveData<ArrayList<Pair<String, Float>>>()

    var mCurType : String = "Null"
    var mSymbol : String = "Null"
    var mName : String = "Null"
    private var upperbound = Float.NaN
    private var lowerbound = Float.NaN
    private var priceData = HashMap<String, Vector<Pair<String, Float>>>()

    val mPrice : LiveData<String> get() = price
    val mPriceSeries : LiveData<Vector<Pair<String, Float>>> get() = priceSeries
    val mAlertPrices : LiveData<ArrayList<Pair<String, Float>>> get() = alertPrices


    fun MaybeRefresh(new_symbol : String, new_name : String, currentContext : Context) : Boolean {
        if (new_symbol != mSymbol) {
            // clear old ticker data
            Log.d("TICKERVW", "update requested, old: $mSymbol, new: $new_symbol")
            mSymbol = new_symbol
            mName = new_name
            priceData.clear()
            priceSeries = MutableLiveData()
            alertPrices = MutableLiveData(ArrayList<Pair<String, Float>>())
            price.postValue(" ")

            // load alert prices
            val watchListDBHandler = WatchListDBHandler(currentContext)
            LoadAlertPrices(watchListDBHandler)

            // pre-load all data in background for better user experience
            val queryAPI = StockDataQueryAPIs(currentContext, new_symbol)
            loadPriceInBackGround("day", queryAPI, true)
            loadPriceInBackGround("week", queryAPI, false)
            loadPriceInBackGround("month", queryAPI, false)
            loadPriceInBackGround("3month", queryAPI, false)
            loadPriceInBackGround("year", queryAPI, false)
            loadPriceInBackGround("5year", queryAPI, false)
            mCurType = "day"
            return true
        }
        return false
    }

    // load users alert price of current ticker, if there is any
    fun LoadAlertPrices(watchlistDBHandler : WatchListDBHandler) {
        var alertPriceList = ArrayList<Pair<String, Float>>()
         watchlistDBHandler.GetUpperBound(mSymbol)?.let { ub_price ->
             upperbound = ub_price
             alertPriceList.add(Pair("upper bound", ub_price))
         }
        watchlistDBHandler.GetLowerBound(mSymbol)?.let { lb_price ->
            lowerbound = lb_price
            alertPriceList.add(Pair("lower bound", lb_price))
        }
        alertPrices.postValue(alertPriceList)
    }

    fun UpdatePriceInBackGround(type : String, apis : StockDataQueryAPIs) {
        if (type != mCurType) {
            mCurType = type
            // if dataset is ready, just update livedata
            if (priceData.containsKey(type)) {
                SetPriceSeries(type)
            }
            // else load price series in background and update livedata when ready
            else {
                loadPriceInBackGround(type, apis, true)
            }
        }
    }

    // update price and dataset for displaying
    fun SetPriceSeries(type : String) {
        Log.d("UPDATE", priceData[type].toString())
        val priceStr : String = "$" + "%.2f".format(priceData[type]!!.lastElement().second)
        price.postValue(priceStr)
        priceSeries.postValue(priceData[type])
    }

    fun GetPriceSeries() : LiveData<Vector<Pair<String, Float>>> {
        return mPriceSeries
    }

    private fun loadPriceInBackGround(
        type : String,
        apis : StockDataQueryAPIs,
        update : Boolean = false)
    {
        // return if dataset already exists
        if (priceData.containsKey(type)) {
            if (update) {
                SetPriceSeries(type)
            }
            return
        }
        else {
            apis.GetPriceDataInBackGround(type, viewModelScope) { data ->
                Log.d("Yahoo CallBack:", data.toString())
                priceData[type] = data
                if (update) {
                    SetPriceSeries(type)
                }
            }
        }
    }



/*  Old Alphavantage API implementation. No longer used.

    @RequiresApi(Build.VERSION_CODES.O)
    fun ProcessReponse(type : String, resp : JSONObject) {
        val respKey = getFuncType(type)
        // save JsonResponse if not save yet
        if (!respMap.containsKey(respKey)) {
            respMap[respKey] = resp
        }

        parseResponse(type)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseResponse(type : String) {
        var dataKey : String  // data field tag
        var valueKey : String // data point tag
        var range : Int
        var freq : Int

        when(type) {
            "day" -> {
                dataKey = "Time Series (5min)"; valueKey = "4. close"
                range = 1; freq = 1
            }
            "week" -> {
                dataKey = "Time Series (60min)"; valueKey = "4. close"
                range = 7; freq = 1
            }
            "month" -> {
                dataKey = "Time Series (Daily)"; valueKey = "5. adjusted close"
                range = 30; freq = 1}
            "3month" -> {
                dataKey = "Time Series (Daily)"; valueKey = "5. adjusted close"
                range = 90; freq = 1
            }
            "year" -> {
                dataKey = "Weekly Adjusted Time Series"; valueKey = "5. adjusted close"
                range = 365; freq = 1
            }
            "5year" -> {
                dataKey = "Weekly Adjusted Time Series"; valueKey = "5. adjusted close"
                range = 825; freq = 1
            }
            else -> throw Exception("Error: Try to parse invalid data type")
        }

        Log.d("TOPARSE", respMap[getFuncType(type)].toString())
        // construct time range by computing start date of sampling
        val timeStampStr = respMap[getFuncType(type)]?.getJSONObject("Meta Data")
            ?.getString("3. Last Refreshed")
            ?: throw Exception("Error: Cannot get data timestamp")
        val endDateStr : String = timeStampStr.split(" ")[0]
        val endDate = LocalDate.parse(endDateStr)
        val startDate = endDate.plusDays((-range).toLong())
        Log.d("STARTDATE", startDate.toString())

        priceData[type] = Vector()
        var pickCnt = 0  // only sample at desired frequency
        val data = respMap.get(getFuncType(type))!!.getJSONObject(dataKey)
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
                    priceData[type]!!.add(0, Pair(timeStr, step.getString(valueKey).toFloat()))
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
            "day" -> "TIME_SERIES_INTRADAY(5min)"
            "week" -> "TIME_SERIES_INTRADAY(60min)"
            "month" -> "TIME_SERIES_DAILY_ADJUSTED"
            "3month" -> "TIME_SERIES_DAILY_ADJUSTED"
            "year" -> "TIME_SERIES_WEEKLY_ADJUSTED"
            "5year" -> "TIME_SERIES_WEEKLY_ADJUSTED"
            else -> throw Exception("Error: Invalid FuncType key.")
        } //
    }

 */
}