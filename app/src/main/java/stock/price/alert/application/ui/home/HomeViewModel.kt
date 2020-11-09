package stock.price.alert.application.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import stock.price.alert.application.ui.search.QueryAPI
import kotlin.random.Random

class HomeViewModel : ViewModel() {


    private var watchedTickers : ArrayList<String> = arrayListOf("KO", "JETS", "AMZN")
    private var queuedPriceQuery = mutableSetOf<String>()
    private var tickersWatchList = ArrayList<WatchListEntry>()

    private var tickersWatchListLiveData = MutableLiveData<ArrayList<WatchListEntry>>()
    val mTickersWatchListLiveData : LiveData<ArrayList<WatchListEntry>> get() = tickersWatchListLiveData

    fun Init() {
        loadWatchList()
        tickersWatchListLiveData = MutableLiveData(tickersWatchList)
    }


    fun StartBackgroundUpdate(context: Context) {
        viewModelScope.launch {
            while (true) {
                tickersWatchList.forEachIndexed { index, watchListEntry ->
                    val symbol: String = watchListEntry.getSymbol()
                    if (queuedPriceQuery.contains(symbol)) {
                        checkPriceInBackground(context, symbol, index)
                        queuedPriceQuery.remove(symbol)
                    }
                }
                // do price check every 2 seconds
                delay(2000)
            }
        }
    }

    private fun checkPriceInBackground(context: Context, symbol : String, index: Int) {
        if (tickersWatchList[index].getSymbol() == symbol) {
            QueryAPI().CheckPriceTriggerInBackGround(context, viewModelScope, symbol,
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
        tickersWatchList.add(WatchListEntry("KO", "CocaCola", null, 55.5.toFloat()))
        tickersWatchList.add(WatchListEntry("AMZN", "Amazon", 2900.toFloat(), 3300.toFloat()))
        tickersWatchList.add(WatchListEntry("JETS", "Global Jets", 14.toFloat(), 17.toFloat()))
    }

}