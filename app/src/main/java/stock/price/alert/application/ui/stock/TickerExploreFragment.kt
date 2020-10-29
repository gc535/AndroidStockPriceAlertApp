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
import androidx.navigation.fragment.findNavController
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


class TickerExploreFragment : Fragment(), View.OnTouchListener {
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
        tickerViewModel = ViewModelProviders.of(requireActivity()).get(TickerViewModel::class.java)
        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        rootView = inflater.inflate(R.layout.fragment_ticker_explore, container, false)

        val name : String = mainViewModel.mCur_ticker_name.value.toString()
        val symbol : String = mainViewModel.mCur_ticker_symbol.value.toString()
        Log.d("hist", "$name, $symbol")
        Log.d("HasHist", mainViewModel.mHasHistory.toString())
        Log.d("DataHist", tickerViewModel.mSymbol)
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //findNavController().popBackStack()

        if (args.tickerName != null && args.tickerSymbol != null) {
            name = args.tickerName as String
            symbol = args.tickerSymbol as String

            // init query APIs
            queryAPIs = StockDataQueryAPIs(requireContext(), symbol)

            // setup viewModel to observe price series data
            tickerViewModel.MaybeRefresh(symbol, name, queryAPIs)
            observeViewModel()

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
        symbol_TextView.text = tickerViewModel.mSymbol
        name_TextView.text = tickerViewModel.mName

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
                var pricePloter = PricePloter(priceChart)
                pricePloter.PlotData(priceSeries)
            }
        )
    }

    /////////////////////// button related ///////////////////////
    private fun initPriceButtons() {
        if (tickerViewModel.mCurType != "Null") {
            when (tickerViewModel.mCurType) {
                "day" -> {button_1d.isPressed = true; button_1d.isSelected = true}
                "week" -> {button_1w.isPressed = true; button_1w.isSelected = true}
                "month" -> {button_1m.isPressed = true; button_1m.isSelected = true}
                "3month" -> {button_3m.isPressed = true; button_3m.isSelected = true}
                "year" -> {button_1y.isPressed = true; button_1y.isSelected = true}
                "5year" -> {button_5y.isPressed = true; button_5y.isSelected = true}
                else -> {}
            }
        }
        else {
            // by default, 1d button is pressed on init
            button_1d.isPressed = true
            button_1d.isSelected = true
        }

        button_1d.setOnTouchListener(this)
        button_1w.setOnTouchListener(this)
        button_1m.setOnTouchListener(this)
        button_3m.setOnTouchListener(this)
        button_1y.setOnTouchListener(this)
        button_5y.setOnTouchListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // clear all buttons' states
            clearButtonState(button_1d)
            clearButtonState(button_1w)
            clearButtonState(button_1m)
            clearButtonState(button_3m)
            clearButtonState(button_1y)
            clearButtonState(button_5y)

            // set state of clicked button
            when(view.id) {
                R.id.button_1d -> {setButtonState(button_1d); tickerViewModel.UpdatePriceInBackGround("day", queryAPIs)}
                R.id.button_1w -> {setButtonState(button_1w); tickerViewModel.UpdatePriceInBackGround("week", queryAPIs)}
                R.id.button_1m -> {setButtonState(button_1m); tickerViewModel.UpdatePriceInBackGround("month", queryAPIs)}
                R.id.button_3m -> {setButtonState(button_3m); tickerViewModel.UpdatePriceInBackGround("3month", queryAPIs)}
                R.id.button_1y -> {setButtonState(button_1y); tickerViewModel.UpdatePriceInBackGround("year", queryAPIs)}
                R.id.button_5y -> {setButtonState(button_5y); tickerViewModel.UpdatePriceInBackGround("5year", queryAPIs)}
                else -> {}
            }
            return true
        }

        if (event.action != MotionEvent.ACTION_UP) {
            return false
        }
        return true
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


