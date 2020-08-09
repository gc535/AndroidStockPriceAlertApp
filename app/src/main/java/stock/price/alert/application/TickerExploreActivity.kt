package stock.price.alert.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.charts.Cartesian
import com.anychart.graphics.vector.Stroke


class TickerExploreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_ticker_explore)


        var priceView : AnyChartView = findViewById(R.id.pricePlot)
        var cartesian : Cartesian = initPlot()


        // create dummy data







    }

    private fun initPlot() : Cartesian {
        var cartesian : Cartesian = AnyChart.line()
        cartesian.animation(true)
        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair().displayMode("float")
        cartesian.crosshair()
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)

        return cartesian
    }
}