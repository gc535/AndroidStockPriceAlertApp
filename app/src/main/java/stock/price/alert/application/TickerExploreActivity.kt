package stock.price.alert.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.graphics.vector.Stroke


class TickerExploreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_ticker_explore)

        var canvas : Cartesian = initCanvas()
        val seriesMapping : Mapping = getStockDataMapping()
        generatePlot(seriesMapping, canvas)

    }

    private fun initCanvas() : Cartesian {
        var cartesian : Cartesian = AnyChart.line()
        cartesian.background().fill("transparent", 0)

        cartesian.animation(true)
        cartesian.padding(10.0, 20.0, 5.0, 20.0)
        cartesian.xAxis(false)
        cartesian.yAxis(false)
        cartesian.crosshair().enabled(true)
        cartesian.crosshair().displayMode("float")
        cartesian.crosshair()
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)

        return cartesian
    }

    private fun getStockDataMapping() : Mapping {
        // create dummy data

        var seriesData: MutableList<DataEntry> = ArrayList()
        seriesData.add(ValueDataEntry("1987", 7.1))
        seriesData.add(ValueDataEntry("1987", 7.1))
        seriesData.add(ValueDataEntry("1988", 8.5))
        seriesData.add(ValueDataEntry("1989", 9.2))
        seriesData.add(ValueDataEntry("1990", 10.1))
        seriesData.add(ValueDataEntry("1991", 11.6))
        seriesData.add(ValueDataEntry("1992", 16.4))
        seriesData.add(ValueDataEntry("1993", 18.0))
        seriesData.add(ValueDataEntry("1994", 13.2))
        seriesData.add(ValueDataEntry("1995", 12.0))
        seriesData.add(ValueDataEntry("1996", 3.2))
        seriesData.add(ValueDataEntry("1997", 4.1))
        seriesData.add(ValueDataEntry("1998", 6.3))
        seriesData.add(ValueDataEntry("1999", 9.4))
        seriesData.add(ValueDataEntry("2000", 11.5))
        seriesData.add(ValueDataEntry("2001", 13.5))
        seriesData.add(ValueDataEntry("2002", 14.8))
        seriesData.add(ValueDataEntry("2003", 16.6))
        seriesData.add(ValueDataEntry("2004", 18.1))
        seriesData.add(ValueDataEntry("2005", 17.0))
        seriesData.add(ValueDataEntry("2006", 16.6))
        seriesData.add(ValueDataEntry("2007", 14.1))
        seriesData.add(ValueDataEntry("2008", 15.7))
        seriesData.add(ValueDataEntry("2009", 12.0))

        var data : Set = Set.instantiate()
        data.data(seriesData)

        return data.mapAs("{ x: 'x', value: 'value' }")
    }

    private fun generatePlot(seriesMapping : Mapping, canvas : Cartesian)  {
        var priceView : AnyChartView = findViewById(R.id.pricePlot)
        var series: Line = canvas.line(seriesMapping)
        series.hovered().markers().enabled(true)

        series.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        priceView.setChart(canvas)
    }

}