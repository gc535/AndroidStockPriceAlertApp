package stock.price.alert.application.ui.stock

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.fragment_ticker_explore.*
import stock.price.alert.application.R


class TickerExploreActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var queryAPIs: StockDataQueryAPIs
    private lateinit var symbol : String
    private lateinit var name : String
    private lateinit var tickerViewModel : TickerViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tickerViewModel =
            ViewModelProviders.of(this).get(TickerViewModel::class.java)
        setContentView(R.layout.fragment_ticker_explore)

        // init private member variables
        tickerViewModel =
            ViewModelProviders.of(this).get(TickerViewModel::class.java)
        name = intent.getSerializableExtra("name") as String
        symbol = intent.getSerializableExtra("symbol") as String
        queryAPIs = StockDataQueryAPIs(
            this,
            symbol
        )

        // setup viewModel to observe price series data
        tickerViewModel.MaybeRefresh(symbol, name)
        observeViewModel()

        // update and load ticker price data in background
        tickerViewModel.UpdatePriceInBackGround("day", queryAPIs)
        //tickerViewModel.LoadPriceInBackGround("week", queryAPIs)
        //tickerViewModel.LoadPriceInBackGround("3month", queryAPIs)
        //tickerViewModel.LoadPriceInBackGround("5year", queryAPIs)

        // init buttons
        initPriceButtons()
    }

    // define subscription to view model data
    private fun observeViewModel() {
        tickerViewModel.mSymbol.observe(
            this, Observer { symbolStr ->
                symbol_TextView.text = symbolStr
            }
        )
        tickerViewModel.mName.observe(
            this, Observer { nameStr ->
                name_TextView.text = nameStr
            }
        )
        tickerViewModel.mPrice.observe(
            this, Observer { priceStr ->
                price_TextView.text = priceStr
            }
        )

        tickerViewModel.mPriceSeries.observe(
            this, Observer{ priceSeries ->
                // update plot
                Log.d("OB", priceSeries.toString())
                val priceChart: LineChart = findViewById(R.id.pricePlot)
                var pricePloter =
                    PricePloter(priceChart)
                pricePloter.PlotData(priceSeries)
            }
        )
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
                    tickerViewModel.UpdatePriceInBackGround("day", queryAPIs)
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
                    tickerViewModel.UpdatePriceInBackGround("week", queryAPIs)
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
                    tickerViewModel.UpdatePriceInBackGround("month", queryAPIs)
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
                    tickerViewModel.UpdatePriceInBackGround("3month", queryAPIs)
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
                    tickerViewModel.UpdatePriceInBackGround("year", queryAPIs)
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
                    tickerViewModel.UpdatePriceInBackGround("5year", queryAPIs)
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



