package stock.price.alert.application

import androidx.lifecycle.ViewModel
import java.util.*

class TickerViewModel : ViewModel() {
    lateinit var mSymbol : String
    private var mPriceSeries = hashMapOf<String, Vector<Pair<String, Float>>>()

    private enum class PriceType {
        DAY, WEEK, MONTH, MONTH3, YEAR, YEAR5
    }

    fun GetPriceSeries(type : String) {

    }

}