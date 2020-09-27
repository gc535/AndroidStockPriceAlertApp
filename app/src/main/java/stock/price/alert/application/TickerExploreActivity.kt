package stock.price.alert.application

import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
import com.anychart.core.ui.DataArea
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.graphics.vector.Stroke
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_ticker_explore.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*


class TickerExploreActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var queryAPIs: StockDataQueryAPIs
    private lateinit var symbol : String
    private val viewModel : TickerViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_ticker_explore)

        // init private member variables
        symbol = intent.getSerializableExtra("symbol") as String
        queryAPIs = StockDataQueryAPIs(this, symbol)

        // setup viewModel to observe price series data
        viewModel.MaybeRefresh(symbol)
        viewModel.mPriceSeries.observe(
            this, Observer{ priceSeries ->
                // update plot
                Log.d("OB", priceSeries.toString())
                val priceChart: LineChart = findViewById(R.id.pricePlot)
                var pricePloter = PricePloter(priceChart)
                pricePloter.PlotData(priceSeries)
            }
        )

        // update and load ticker price data in background
        viewModel.UpdatePriceInBackGround("day", queryAPIs)
        viewModel.LoadPriceInBackGround("week", queryAPIs)
        viewModel.LoadPriceInBackGround("3month", queryAPIs)
        viewModel.LoadPriceInBackGround("5year", queryAPIs)

        // init buttons
        initPriceButtons()


    }


    private fun initPriceButtons() {
        // by default, 1d button is pressed on init
        button_1d.isPressed = true
        button_1d.isSelected = true

        // 1 day
        button_1d.setOnTouchListener (object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    clearButtonState(button_1w)
                    clearButtonState(button_1m)
                    clearButtonState(button_3m)
                    clearButtonState(button_1y)
                    clearButtonState(button_5y)
                    setButtonState(button_1d)
                    viewModel.UpdatePriceInBackGround("day", queryAPIs)
                    return true
                }

                if (event.action != MotionEvent.ACTION_UP) {
                    return false
                }
                return true
            }
        })

        // 1 week
        button_1w.setOnTouchListener (object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    clearButtonState(button_1d)
                    clearButtonState(button_1m)
                    clearButtonState(button_3m)
                    clearButtonState(button_1y)
                    clearButtonState(button_5y)
                    setButtonState(button_1w)
                    viewModel.UpdatePriceInBackGround("week", queryAPIs)
                    return true
                }

                if (event.action != MotionEvent.ACTION_UP) {
                    return false
                }
                return true
            }
        })

        // 1 month
        button_1m.setOnTouchListener (object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    clearButtonState(button_1d)
                    clearButtonState(button_1w)
                    clearButtonState(button_3m)
                    clearButtonState(button_1y)
                    clearButtonState(button_5y)
                    setButtonState(button_1m)
                    viewModel.UpdatePriceInBackGround("month", queryAPIs)
                    return true
                }

                if (event.action != MotionEvent.ACTION_UP) {
                    return false
                }
                return true
            }
        })

        // 3 month
        button_3m.setOnTouchListener (object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    clearButtonState(button_1d)
                    clearButtonState(button_1w)
                    clearButtonState(button_1m)
                    clearButtonState(button_1y)
                    clearButtonState(button_5y)
                    setButtonState(button_3m)
                    viewModel.UpdatePriceInBackGround("3month", queryAPIs)
                    return true
                }

                if (event.action != MotionEvent.ACTION_UP) {
                    return false
                }
                return true
            }
        })

        // 1 year
        button_1y.setOnTouchListener (object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    clearButtonState(button_1d)
                    clearButtonState(button_1w)
                    clearButtonState(button_1m)
                    clearButtonState(button_3m)
                    clearButtonState(button_5y)
                    setButtonState(button_1y)
                    viewModel.UpdatePriceInBackGround("year", queryAPIs)
                    return true
                }

                if (event.action != MotionEvent.ACTION_UP) {
                    return false
                }
                return true
            }
        })

        // 5 years
        button_5y.setOnTouchListener (object : View.OnTouchListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    clearButtonState(button_1d)
                    clearButtonState(button_1w)
                    clearButtonState(button_1m)
                    clearButtonState(button_3m)
                    clearButtonState(button_1y)
                    setButtonState(button_5y)
                    viewModel.UpdatePriceInBackGround("5year", queryAPIs)
                    return true
                }

                if (event.action != MotionEvent.ACTION_UP) {
                    return false
                }
                return true
            }
        })
    }

    private fun clearButtonState(button: Button) {
        button.isSelected = false
        button.isPressed = false
    }

    private fun setButtonState(button: Button) {
        button.isSelected = true
        button.isPressed = true
    }

}



