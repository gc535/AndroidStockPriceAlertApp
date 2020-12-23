package stock.price.alert.application.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import androidx.core.content.res.ColorStateListInflaterCompat.inflate
import androidx.core.content.res.ComplexColorCompat.inflate
import kotlinx.android.synthetic.main.listview_layout_watchlist.view.*
import stock.price.alert.application.R

class WatchListViewAdapter(private val mContext : Context, private val mDataArray : ArrayList<WatchListEntry>)
    : ArrayAdapter<WatchListEntry>(mContext, R.layout.listview_layout_watchlist, mDataArray) {
    private val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
        val uninitStr = "--"
        val rowView : View = inflater.inflate(R.layout.listview_layout_watchlist, parent, false)
        rowView.symbol.text = mDataArray[pos].getSymbol()
        rowView.lowerbound.text =
            if (mDataArray[pos].getLowerBound() != null) "%.2f".format(mDataArray[pos].getLowerBound()) else uninitStr
        rowView.upperbound.text =
            if (mDataArray[pos].getUpperBound() != null) "%.2f".format(mDataArray[pos].getUpperBound()) else uninitStr
        rowView.price.text =
            if (mDataArray[pos].getPrice() != null) "%.2f".format(mDataArray[pos].getPrice()) else uninitStr

        // set indicator images for lower bound
        if ((mDataArray[pos].getPrice() != null && mDataArray[pos].getLowerBound() != null)
            && mDataArray[pos].getPrice()!! < mDataArray[pos].getLowerBound()!!) {
            rowView.lower_indicator.setImageResource(R.drawable.ic_home_watchlist_down_24)
        } else {
            rowView.lower_indicator.setImageResource(R.drawable.ic_home_watchlist_wait_24)
        }

        // set indicator images for upper bound
        if ((mDataArray[pos].getPrice() != null && mDataArray[pos].getUpperBound() != null)
            && mDataArray[pos].getPrice()!! > mDataArray[pos].getUpperBound()!!) {
            rowView.upper_indicator.setImageResource(R.drawable.ic_home_watchlist_up_24)
        } else {
            rowView.upper_indicator.setImageResource(R.drawable.ic_home_watchlist_wait_24)
        }

        return rowView
    }

    override fun getCount(): Int {
        return mDataArray.size
    }

    override fun getItem(position: Int): WatchListEntry? {
        return mDataArray[position]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

}

class WatchListEntry(val mSymbol: String, val mTicker: String, val mLowerBound: Float?, val mUpperBound: Float?) {
    var mPrice : Float? = null

    fun setPrice(price: Float) { mPrice = price }

    fun getSymbol(): String { return mSymbol}
    fun getName(): String { return mTicker }
    fun getLowerBound(): Float? { return mLowerBound }
    fun getUpperBound(): Float? { return mUpperBound }
    fun getPrice(): Float? { return mPrice }
}
