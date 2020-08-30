package stock.price.alert.application

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.graphics.vector.Stroke
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_search.*


class TickerExploreActivity : AppCompatActivity() {
    private var dataHandler : StockDataHandler? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_ticker_explore)

        //var canvas : Cartesian = initCanvas()
        //val seriesMapping : Mapping = getStockDataMapping()
        //generatePlot(seriesMapping, canvas)

        //testMPChart()

        // obtain the query respond from the search activity
        val textView: TextView = findViewById(R.id.responseTextView)
        val symbol = intent.getSerializableExtra("symbol") as String

        val queryMap = HashMap<String, String>()

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, QueryAPI().GenQueryStr("day", symbol), null,
            Response.Listener { response->
                textView.text = "Response: %s".format(response.toString())
                Toast.makeText(applicationContext, "success", Toast.LENGTH_SHORT).show()
                queryMap.put("day", response.toString())

                // process response
                dataHandler = StockDataHandler(queryMap)

                textView.text = "Response: %s".format(dataHandler?.GetResponse("day"))
                val data = dataHandler!!.GetData("day")
                Log.d("READBACK", data.toString())

                val priceChart : LineChart = findViewById(R.id.pricePlot)
                var pricePloter = PricePloter(priceChart)
                pricePloter.PlotData(data)
                //textView.text = "Response: %s".format(queryMap["day"])
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
                textView.text = "Response: %s".format(error.toString())
                Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()

            }
        )
        // Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest)

        //dataHandler = StockDataHandler(queryMap)

        //textView.text = "Response: %s".format(dataHandler?.GetResponse("day"))
        //val data = dataHandler!!.GetData("day")
        //Log.d("READBACK", data.toString())

        //val priceChart : LineChart = findViewById(R.id.pricePlot)
        //var pricePloter = PricePloter(priceChart)
        //pricePloter.PlotData(data)
    }

    private fun initCanvas() : Cartesian {
        var cartesian : Cartesian = AnyChart.line()
        cartesian.background().fill("rgb(300,300,300)", 1)
        cartesian.credits().text("false")
        cartesian.animation(true)
        cartesian.padding(0.0, 20.0, 0.0, 20.0)
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
            .offsetX(0.0)
            .offsetY(5.0)

        priceView.setChart(canvas)
    }

    private fun testMPChart() {
        val priceChart : LineChart = findViewById(R.id.pricePlot)
        //priceChart.setBackgroundColor(Color.WHITE)
        priceChart.setDrawBorders(false)
        priceChart.description.isEnabled = false
        priceChart.isDragEnabled = false
        priceChart.isScaleXEnabled = false
        priceChart.isScaleYEnabled = false
        priceChart.setTouchEnabled(true)
        priceChart.setDrawGridBackground(false)

        priceChart.axisRight.setDrawGridLines(false)
        priceChart.axisRight.setDrawAxisLine(false)
        priceChart.axisRight.setDrawLabels(false)

        priceChart.axisLeft.setDrawGridLines(false)
        priceChart.axisLeft.setDrawAxisLine(false)
        priceChart.axisLeft.setDrawLabels(false)

        priceChart.xAxis.setDrawGridLines(false)
        priceChart.xAxis.setDrawLabels(false)
        priceChart.xAxis.setDrawAxisLine(false)
        priceChart.legend.isEnabled = false



        var yValue : ArrayList<Entry> = ArrayList()
        yValue.add(Entry(1f, 10f))
        yValue.add(Entry(2f, 20f))
        yValue.add(Entry(3f, 30f))
        yValue.add(Entry(4f, 40f))
        yValue.add(Entry(5f, 50f))
        yValue.add(Entry(6f, 60f))
        yValue.add(Entry(7f, 70f))

        val set1 : LineDataSet = LineDataSet(yValue, "set1")
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
        set1.setColor(Color.GREEN)
        set1.setDrawVerticalHighlightIndicator(true)
        set1.highLightColor = Color.GRAY
        set1.highlightLineWidth = 1f
        set1.setDrawHorizontalHighlightIndicator(false)
        set1.lineWidth = 2f
        val dataSet = ArrayList<ILineDataSet>()
        dataSet.add(set1)
        val data = LineData(dataSet)
        priceChart.data = data


    }

}