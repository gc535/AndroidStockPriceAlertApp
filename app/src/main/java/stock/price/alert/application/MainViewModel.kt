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

    // set to null if any of two args is null
    fun ChangeTicker(name : String?, symbol : String?) {
        if (name != null && symbol != null) {
            _cur_ticker_name.postValue(name)
            _cur_ticker_symbol.postValue(symbol)
        }
        else {
            _cur_ticker_name.postValue(null)
            _cur_ticker_symbol.postValue(null)
        }

    }
}