package stock.price.alert.application

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _cur_ticker_name = MutableLiveData<String>()
    private val _cur_ticker_symbol = MutableLiveData<String>()

    val mCur_ticker_name: LiveData<String> = _cur_ticker_name
    val mCur_ticker_symbol: LiveData<String> = _cur_ticker_symbol
    var mHasHistory : Boolean = false

    fun ChangeTicker(name : String, symbol : String) {
        _cur_ticker_name.postValue(name)
        _cur_ticker_symbol.postValue(symbol)
    }
}