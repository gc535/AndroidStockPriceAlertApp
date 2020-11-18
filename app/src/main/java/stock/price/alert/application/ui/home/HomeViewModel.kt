package stock.price.alert.application.ui.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.utils.Utils.init
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import stock.price.alert.application.Data.WatchListDBHandler
import stock.price.alert.application.ui.search.QueryAPI
import kotlin.random.Random

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private var isInited : Boolean = false
    private var queuedPriceQuery = mutableSetOf<String>()
    private var tickersWatchList = ArrayList<WatchListEntry>()

    private var tickersWatchListLiveData = MutableLiveData<ArrayList<WatchListEntry>>()
    val mTickersWatchListLiveData : LiveData<ArrayList<WatchListEntry>> get() = tickersWatchListLiveData

    init {
        loadWatchList()
        tickersWatchListLiveData = MutableLiveData(tickersWatchList)

        // add price update job for all watched tickers
        tickersWatchList.forEach {
            queuedPriceQuery.add(it.getSymbol())
        }
    }


    fun StartBackgroundUpdate(context: Context) {
        viewModelScope.launch {
            val requestQueue = Volley.newRequestQueue(context)
            while (true) {
                tickersWatchList.forEachIndexed { index, watchListEntry ->
                    val symbol: String = watchListEntry.getSymbol()
                    if (queuedPriceQuery.contains(symbol)) {
                        checkPriceInBackground(requestQueue, symbol, index)
                        queuedPriceQuery.remove(symbol)
                    }
                }
                // do price check every 2 seconds
                delay(2000)
            }
        }
    }


    private fun checkPriceInBackground(volleyRequestQueue: RequestQueue, symbol : String, index: Int) {
        if (tickersWatchList[index].getSymbol() == symbol) {
            Log.d("HOMEVW", "Start checking $symbol")
            QueryAPI().CheckPriceTriggerInBackGround(volleyRequestQueue, viewModelScope, symbol,
                { symbol: String, price: Float ->
                    val priceStr = price.toString()
                    Log.d("HOMEVW", "got $symbol price: $priceStr")
                    val testPrice: Float = price + Random.nextFloat()
                    tickersWatchList[index].setPrice(testPrice)
                    tickersWatchListLiveData.postValue(tickersWatchList)
                    viewModelScope.launch { delay(5000) /* wait at least 5 sec before next refresh*/ }
                    queuedPriceQuery.add(symbol)
                }, // callback on response
                {
                    viewModelScope.launch { delay(5000) /* wait at least 5 sec before next refresh*/ }
                    queuedPriceQuery.add(symbol)
                } // callback on error
            )

        }
    }


    private fun loadWatchList() {
        viewModelScope.launch {
            //tickersWatchList.add(WatchListEntry("KO", "CocaCola", null, 55.5.toFloat()))
            //tickersWatchList.add(WatchListEntry("AMZN", "Amazon", 2900.toFloat(), 3300.toFloat()))
            //tickersWatchList.add(WatchListEntry("JETS", "Global Jets", 14.toFloat(), 17.toFloat()))

            val context = getApplication<Application>().applicationContext
            val watchListDBHandler = WatchListDBHandler(context)
            for ((symbol, value) in watchListDBHandler.GetAllSymbol()) {
                tickersWatchList.add(WatchListEntry(
                    symbol,
                    watchListDBHandler.GetTickerName(symbol)!!,
                    watchListDBHandler.GetLowerBound(symbol),
                    watchListDBHandler.GetUpperBound(symbol)))
            }
        }
    }


    fun Clear() = onCleared()
    override fun onCleared() {
        viewModelScope.cancel()
        Log.d("HOMEVW", "ViewModel cleared successfull")
    }

}