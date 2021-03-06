package stock.price.alert.application.ui.stock

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.fragment_ticker_explore.*
import kotlinx.android.synthetic.main.listview_layout_alertlist.view.*
import kotlinx.android.synthetic.main.listview_layout_watchlist.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import org.json.JSONObject
import stock.price.alert.application.Data.WatchListDBHandler
import stock.price.alert.application.MainViewModel
import stock.price.alert.application.R
import stock.price.alert.application.ui.home.HomeFragmentDirections
import stock.price.alert.application.ui.home.WatchListEntry
import stock.price.alert.application.ui.home.WatchListViewAdapter
import java.util.*


class TickerExploreFragment : Fragment(), View.OnTouchListener {


    private lateinit var symbol : String
    private lateinit var name : String
    private lateinit var rootView: View
    private lateinit var alertlistView : ListView

    private lateinit var tickerViewModel : TickerViewModel
    private lateinit var mainViewModel : MainViewModel
    private val args : TickerExploreFragmentArgs by navArgs()

    // test
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    //todo:
    // 1. backstack can be imporved if jump here from notification

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tickerViewModel = ViewModelProviders.of(requireActivity()).get(TickerViewModel::class.java)
        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        rootView = inflater.inflate(R.layout.fragment_ticker_explore, container, false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("old viewmodel ticker", "${tickerViewModel.mSymbol}")
        Log.d("new request ticker", args.tickerName.toString())

        if (args.tickerName != null && args.tickerSymbol != null) {
            name = args.tickerName as String
            symbol = args.tickerSymbol as String

            // setup viewModel to observe live data in ViewModel
            mainViewModel.ChangeTicker(name, symbol)
            mainViewModel.mHasHistory = true
            tickerViewModel.MaybeRefresh(symbol, name, requireContext())
            reObserveViewModel()

            // init buttons
            initButtonLogic()
            // init price alert field
            initAlertListView()
        }

        // Back pressed callback, always go to last page
        val backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainViewModel.ChangeTicker(null, null)
            findNavController().popBackStack()
        }
        backPressedCallback.isEnabled = true
    }

    // reload button state when view re-attached
    override fun onResume() {
        super.onResume()
        Log.d("state:", "onResume")
        updateButtonState()
    }


    // Define ViewModel observe behaviour
    private val priceObserver = Observer<String> { priceStr ->
        price_TextView.text = priceStr
    }
    private val priceSeriesObserver = Observer<Vector<Pair<String, Float>>> { priceSeries ->
        // update plot
        Log.d("OB", priceSeries.toString())
        val priceChart: LineChart = rootView.findViewById(R.id.pricePlot)
        var pricePloter = PricePloter(priceChart)
        pricePloter.PlotData(priceSeries)
    }
    private val alertPricesObserver = Observer<ArrayList<Pair<String, Float>>> { alertPrices ->
        reloadAlertListView(alertPrices)
    }
    // define subscription to view model data {  }
    private fun reObserveViewModel() {
        symbol_TextView.text = tickerViewModel.mSymbol
        name_TextView.text = tickerViewModel.mName

        tickerViewModel.mPrice.removeObserver(priceObserver)
        tickerViewModel.mPrice.observe(viewLifecycleOwner, priceObserver)

        tickerViewModel.mPriceSeries.removeObserver(priceSeriesObserver)
        tickerViewModel.mPriceSeries.observe(viewLifecycleOwner, priceSeriesObserver)

        tickerViewModel.mAlertPrices.removeObserver(alertPricesObserver)
        tickerViewModel.mAlertPrices.observe(viewLifecycleOwner, alertPricesObserver)
    }

    private fun initAlertListView() {
        alertlistView = rootView.findViewById(R.id.saved_alert_ListView)
        alertlistView.adapter =
            AlertListViewAdapter(requireContext(), tickerViewModel.mAlertPrices.value!!)
        alertlistView.emptyView = rootView.findViewById(R.id.empty_alertlist_textview)
        alertlistView.dividerHeight = 0

        alertlistView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                invokeSetAlertDialog()
            }
    }

    // TODO: this will be called by ListView observable when updated
    private fun reloadAlertListView(alertPrices: ArrayList<Pair<String, Float>>) {
        // create alert list view adapter
        alertlistView.adapter =
            AlertListViewAdapter(requireContext(), alertPrices)
        (alertlistView.adapter as AlertListViewAdapter).notifyDataSetChanged()
    }


    private fun invokeSetAlertDialog() {
        var setAlertDialog = SetAlertDialogFragment()

        // create on dismiss listener for SetAlertDialogFragment
        val listener = object : SetAlertDismissListener {
            override fun handleDismiss() {
                tickerViewModel.LoadAlertPrices(WatchListDBHandler(requireContext()))
            }
        }

        // prepare args to be passed to DialogFragment
        val bundleArgs = Bundle()
        bundleArgs.putString("symbol", symbol)
        bundleArgs.putString("name", name)

        // display set price alert dialog
        setAlertDialog.setArguments(bundleArgs)
        setAlertDialog.SetDismissListener(listener)
        setAlertDialog.show(childFragmentManager, "setAlertDialog")
    }

    /////////////////////// button related ///////////////////////
    private fun initButtonLogic() {
        // watch button
        watch_Button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                invokeSetAlertDialog()
            }
        })

        updateButtonState()

        // init button touch logic
        button_1d.setOnTouchListener(this)
        button_1w.setOnTouchListener(this)
        button_1m.setOnTouchListener(this)
        button_3m.setOnTouchListener(this)
        button_1y.setOnTouchListener(this)
        button_5y.setOnTouchListener(this)
    }

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
            val queryAPIs = StockDataQueryAPIs(requireContext(), symbol)
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

    private fun updateButtonState(){
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

// alert ListView related
class AlertListViewAdapter(private val mContext: Context, private val mAlertPrices: ArrayList<Pair<String, Float>>)
    : ArrayAdapter<Pair<String, Float>>(mContext, R.layout.listview_layout_alertlist, mAlertPrices) {
    private val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
        val rowView : View = inflater.inflate(R.layout.listview_layout_alertlist, parent, false)
        rowView.alert_tag.text = mAlertPrices[pos].first
        rowView.alert_price.text = "%.2f".format(mAlertPrices[pos].second)


        return rowView
    }

    override fun getCount(): Int {
        return mAlertPrices.size
    }

    override fun getItem(position: Int): Pair<String, Float>? {
        return mAlertPrices[position]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

}

