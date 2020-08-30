package stock.price.alert.application

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.*

class PricePloter (ploter : LineChart){
    var pricePloter = ploter
    // init routine setup the plot parameter
    init {
        pricePloter.setDrawBorders(false)
        pricePloter.description.isEnabled = false
        pricePloter.isDragEnabled = false
        pricePloter.isScaleXEnabled = false
        pricePloter.isScaleYEnabled = false
        pricePloter.setTouchEnabled(true)
        pricePloter.setDrawGridBackground(false)

        pricePloter.axisRight.setDrawGridLines(false)
        pricePloter.axisRight.setDrawAxisLine(false)
        pricePloter.axisRight.setDrawLabels(false)

        pricePloter.axisLeft.setDrawGridLines(false)
        pricePloter.axisLeft.setDrawAxisLine(false)
        pricePloter.axisLeft.setDrawLabels(false)

        pricePloter.xAxis.setDrawGridLines(false)
        pricePloter.xAxis.setDrawLabels(false)
        pricePloter.xAxis.setDrawAxisLine(false)
        pricePloter.legend.isEnabled = false
    }

    fun PlotData(data : Vector<Pair<String, Float>>?) {
        data?.let {
            // preprocess data to create dataset
            var xAxis: ArrayList<String> = ArrayList()
            var yValue: ArrayList<Entry> = ArrayList()
            for ((i, entry) in data.iterator().withIndex()) {
                xAxis.add(entry.first)
                yValue.add(Entry(i.toFloat(), entry.second))
            }

            // format dataset line drawing
            val lineset: LineDataSet = LineDataSet(yValue, "price")
            lineset.setDrawCircles(false)
            lineset.setDrawValues(false)
            lineset.setColor(Color.GREEN)
            lineset.setDrawVerticalHighlightIndicator(true)
            lineset.highLightColor = Color.GRAY
            lineset.highlightLineWidth = 1f
            lineset.setDrawHorizontalHighlightIndicator(false)
            lineset.lineWidth = 2f

            val dataSet = ArrayList<ILineDataSet>()
            dataSet.add(lineset)
            val data = LineData(dataSet)

            pricePloter.xAxis.valueFormatter = IndexAxisValueFormatter(xAxis)
            pricePloter.data = data
            pricePloter.invalidate()
        }
    }
}