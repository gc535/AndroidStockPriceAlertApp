package stock.price.alert.application.ui.stock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_ticker_explore.*
import kotlinx.coroutines.*
import org.json.JSONObject
import stock.price.alert.application.MainViewModel
import stock.price.alert.application.R
import stock.price.alert.application.service.RealTimePriceAlert.RealTimePriceAlertAlarmReceiver
import stock.price.alert.application.ui.home.HomeViewModel


class TickerExploreFragment : Fragment() {
    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var queryAPIs: StockDataQueryAPIs
    private lateinit var symbol : String
    private lateinit var name : String
    private lateinit var rootView: View
    private lateinit var tickerViewModel : TickerViewModel
    private lateinit var mainViewModel : MainViewModel
    private val args : TickerExploreFragmentArgs by navArgs()
    // test
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tickerViewModel = ViewModelProviders.of(this).get(TickerViewModel::class.java)
        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        rootView = inflater.inflate(R.layout.fragment_ticker_explore, container, false)

        val name : String = mainViewModel.mCur_ticker_name.value.toString()
        val symbol : String = mainViewModel.mCur_ticker_symbol.value.toString()
        Log.d("hist", "$name, $symbol")
        Log.d("HasHist", mainViewModel.mHasHistory.toString())
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.tickerName != null && args.tickerSymbol != null) {
            name = args.tickerName as String
            symbol = args.tickerSymbol as String

            // init query APIs
            queryAPIs = StockDataQueryAPIs(requireContext(), symbol)

            // setup viewModel to observe price series data
            tickerViewModel.MaybeRefresh(symbol, name)
            observeViewModel()

            // update and load ticker price data in background
            // todo: if data already exist then dont update in background
            tickerViewModel.UpdatePriceInBackGround("day", queryAPIs)
            //tickerViewModel.LoadPriceInBackGround("week", queryAPIs)
            //tickerViewModel.LoadPriceInBackGround("3month", queryAPIs)
            //tickerViewModel.LoadPriceInBackGround("5year", queryAPIs)

            // init buttons
            initPriceButtons()
        }


        // test watch button
        watch_Button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                checkPriceTriggerInBackGround("KO")

                // test create price check alarm

                val alarmMgr = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val broadcastIntent =
                    Intent(Intent(requireContext(),
                        RealTimePriceAlertAlarmReceiver::class.java)).apply {
                        action = "android.intent.action.SET_REALTIME_PRICE_CHECK_ALARM"
                    }
                var pendingIntent = PendingIntent.getBroadcast(
                    requireContext(), 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                // set repeat clock every 5 sec
                val interval : Long = 5* 1000
                alarmMgr.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + interval,
                    interval,
                    pendingIntent
                )

                // display set price alert dialog
                var setAlertDialog = SetAlertDialogFragment()
                setAlertDialog.show(childFragmentManager, "setAlertDialog")

            }
        })
    }

    // define subscription to view model data
    private fun observeViewModel() {
        tickerViewModel.mSymbol.observe(
            viewLifecycleOwner, Observer { symbolStr ->
                symbol_TextView.text = symbolStr
            }
        )
        tickerViewModel.mName.observe(
            viewLifecycleOwner, Observer { nameStr ->
                name_TextView.text = nameStr
            }
        )
        tickerViewModel.mPrice.observe(
            viewLifecycleOwner, Observer { priceStr ->
                price_TextView.text = priceStr
            }
        )

        tickerViewModel.mPriceSeries.observe(
            viewLifecycleOwner, Observer{ priceSeries ->
                // update plot
                Log.d("OB", priceSeries.toString())
                val priceChart: LineChart = rootView.findViewById(R.id.pricePlot)
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

    /// test ////

    private val qUrl : String = "https://query1.finance.yahoo.com/v8/finance/chart/"
    private val qOpt : String = "?region=US&lang=en-US&includePrePost=false&interval=1m&range=1d&corsDomain=search.yahoo.com"
    private fun checkPriceTriggerInBackGround(symbol : String) {
        val requestQueue = Volley.newRequestQueue(requireContext())
        val queryStr = qUrl + symbol + qOpt
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, queryStr, null,
            Response.Listener { response->
                // launch parsing and notification logic in worker thread to avoid occupying UI thread
                scope.launch {
                    parseResponse(response)?.let { price ->
                        Log.d("PRICECHECK", price.toString())
                    } // null check
                } // coroutine scope
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
            }
        )

        // Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest)
    }

    // parse latest price from response
    private fun parseResponse(response : JSONObject) : Float? {
        response.optJSONObject("chart")?.let { data ->
            data.optJSONArray ("result")?.let { array ->
                if (array.length() > 0) {
                    array.getJSONObject(0).optJSONObject("meta")?.let { meta ->
                        val priceStr = meta.optString("regularMarketPrice")
                        if (priceStr.isNotEmpty()) {
                            return priceStr.toFloat()
                        }
                    }
                }
            }
        }
        return null
    }

}



