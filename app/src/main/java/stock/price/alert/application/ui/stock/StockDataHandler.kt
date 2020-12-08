package stock.price.alert.application.ui.stock

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


@RequiresApi(Build.VERSION_CODES.O)
class StockDataQueryAPIs(private val context : Context, private val symbol: String) {
    private val delim = "&"

    // Make network query of ticker price data in background threads
    fun DoBackgroundNetworkQuery(
        type : String,  // query type
        ticketViewModel : TickerViewModel, // callback for parsing response
        update : Boolean)
    {
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, GenQueryStr(type, symbol), null,
            Response.Listener { response ->
                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()

                // if response is valid, let viewModel parse response in background
                if (checkResponse(type, response)) {
                    ticketViewModel.viewModelScope.launch {
                        ticketViewModel.ProcessReponse(type, response)
                        if (update) {
                            ticketViewModel.SetPriceSeries(type)
                        }
                    } // viewModel coroutine launch
                } // if valid response
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }
        )
        // Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest)
    }

    fun GetPriceDataInBackGround(
        type : String,  // query type
        viewModelScope : CoroutineScope,
        updateCallback : (Vector<Pair<String, Float>>) -> Unit)
    {
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, genQueryStrYahoo(type), null,
            Response.Listener { response ->
                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()

                // parse response in background and then update passed live data
                viewModelScope.launch {
                    val data = parsePriceDataYahoo()
                    updateCallback(data)
                }
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }
        )
        // Access the RequestQueue through your singleton class.
        requestQueue.add(jsonObjectRequest)
    }

    fun parsePriceDataYahoo() : Vector<Pair<String, Float>> {
        return Vector<Pair<String, Float>>()
    }

    /* Yahoo Finance Data Query APIs*/
    private val priceQuery_Yahoo_URL = "https://query1.finance.yahoo.com/v8/finance/chart/"
    private val prefix_Yahoo_Query_Str = "?region=US&lang=en-US&includePrePost=true"
    private val sufix_Yahoo_Query_Str = "corsDomain=finance.yahoo.com&.tsrc=finance"
    private val cQueryTypeYahoo = hashMapOf(
        "day" to "interval=5m&range=1d",
        "week" to "interval=15m&range=5d",
        "month" to "interval=1d&range=1mo",
        "3month" to "interval=1d&range=3mo",
        "year" to "interval=1d&range=1y",
        "5year" to "interval=1wk&range=5y"
    )

    private fun genQueryStrYahoo(type : String) : String {
        if (!cQueryTypeYahoo.containsKey(type)) {
            throw Exception("Error: Unsupported query")
        }
        return priceQuery_Yahoo_URL + symbol + prefix_Yahoo_Query_Str +
               delim + cQueryTypeYahoo[type]!! + delim + sufix_Yahoo_Query_Str
    }




    /* Alphavantage Data Query APIs*/

    var cQueryFuncs = HashMap<String, String>()
    init {
        cQueryFuncs["day"] = "TIME_SERIES_INTRADAY" // compact 5 min
        cQueryFuncs["week"] = "TIME_SERIES_INTRADAY" // compact 60 mins
        cQueryFuncs["month"] = "TIME_SERIES_DAILY_ADJUSTED" // compact daily adjusted 30
        cQueryFuncs["3month"] = "TIME_SERIES_DAILY_ADJUSTED" // compact daily adjusted 90
        cQueryFuncs["year"] = "TIME_SERIES_WEEKLY_ADJUSTED" //
        cQueryFuncs["5year"] = "TIME_SERIES_WEEKLY_ADJUSTED" //
    }

    private val priceQueryURL = "https://www.alphavantage.co/query?"
    private val priceQueryKey = "apikey=5B4ZQG1WXCAB5L1N"

    fun checkResponse(type : String, resp : JSONObject) : Boolean {
        val validRespStr = "Meta Data"
        if (resp.opt(validRespStr) == null) {
            if (resp.opt("Note") != null) {
                val thankyouStr = "Thank you for using the app, your current status only allows" +
                        "1 ticker query per minutes."
                Toast.makeText(context, thankyouStr, Toast.LENGTH_SHORT).show()
                return false
            }
            else if (resp.opt("Error Message") != null) {
                val errorStr = "Invalid ticker information. Please check for correct ticker symbol"
                Toast.makeText(context, errorStr, Toast.LENGTH_SHORT).show()
                return false
            }
            else {
                Log.d("RESP", resp.toString())
                throw Exception("Error: Invalid query response: $type")
            }
        }
        return true
    }


    // sanity check of func is done by getFuncStr(func: String)
    fun GenQueryStr(func : String, ticker : String) : String {
        val optinal_interval = when(func) {
            // TODO: 1mins for intraday only covers utill 2pm, full size is too big
            "day" -> "interval=5min"
            "week" -> "interval=60min"
            else -> "" } + delim

        val queryURL = priceQueryURL + getFuncStr(func) + delim +
                "symbol=" + ticker + delim +
                optinal_interval + priceQueryKey

        return queryURL
    }

    // this interface also do the sanity check on query type
    private fun getFuncStr(func: String): String {
        val prefix = "function="
        if (!cQueryFuncs.containsKey(func)) {
            throw Exception("Error: Unsupported query")
        }
        return prefix + cQueryFuncs[func]
    }

    /* APIs for ticker probing */

    private val probQuestURL = "https://query2.finance.yahoo.com/v1/finance/"

    fun ProbTicker(search : String) : String {
        val query = "search?q=" + search
        val probURL = probQuestURL + query + delim + initProbOptions()
        return probURL
    }

    fun ParseProbResponse(response : JSONObject) : ArrayList<String> {
        var ret = ArrayList<String>()

        val companies = response.getJSONArray("quotes")
        for (i in 0 until companies.length()) {
            val company = companies.getJSONObject(i)
            if (company.has("shortname") && company.has("symbol")) {
                val name = company.getString("shortname")
                val symbol = company.getString("symbol")
                ret.add("$name : $symbol")
            }
        }
        return ret
    }

    private fun initProbOptions() : String {
        val quotes_count = "quotesCount=6"
        val news_count = "newsCount=0"
        val fussySearch = "enableFuzzyQuery=false"
        val queryOptions = "&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query"
        val otherOptions = "enableCb=true&enableEnhancedTrivialQuery=true"

        val optStr = quotes_count + delim + news_count + delim + fussySearch + delim +
                queryOptions + delim + otherOptions
        return optStr
    }

}