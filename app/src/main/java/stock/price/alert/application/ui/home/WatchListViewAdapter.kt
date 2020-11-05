package stock.price.alert.application.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.res.ColorStateListInflaterCompat.inflate
import androidx.core.content.res.ComplexColorCompat.inflate
import kotlinx.android.synthetic.main.listview_layout_watchlist.view.*
import stock.price.alert.application.R

class WatchListViewAdapter(private val mContext : Context, private val mDataArray : ArrayList<String>) :  BaseAdapter() {
    private val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
        val rowView : View = inflater.inflate(R.layout.listview_layout_watchlist, parent, false)
        val fields = mDataArray[pos].split(" : ")
        rowView.symbol.text = fields[1]
        rowView.lowerbound.text = fields[2]
        rowView.upperbound.text = fields[3]
        rowView.price.text = fields[4]

        // set indicator images for lower bound
        if (fields[4].toFloat() < fields[2].toFloat()) {
            rowView.lower_indicator.setImageResource(R.drawable.ic_home_watchlist_down_24)
        } else {
            rowView.lower_indicator.setImageResource(R.drawable.ic_home_watchlist_wait_24)
        }

        // set indicator images for upper bound
        if (fields[4].toFloat() > fields[3].toFloat()) {
            rowView.upper_indicator.setImageResource(R.drawable.ic_home_watchlist_up_24)
        } else {
            rowView.upper_indicator.setImageResource(R.drawable.ic_home_watchlist_wait_24)
        }

        return rowView
    }

    override fun getCount(): Int {
        return mDataArray.size
    }

    override fun getItem(position: Int): Any {
        return mDataArray[position]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }
}