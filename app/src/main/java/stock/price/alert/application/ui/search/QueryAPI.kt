package stock.price.alert.application.ui.search

import android.content.Context
import android.util.Log
import androidx.annotation.RestrictTo
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class QueryAPI {
    /* Functions for ticker info probing */
    private val probQuestUrl = "https://query2.finance.yahoo.com/v1/finance/"
    private val realtimePirceQueryUrl : String = "https://query1.finance.yahoo.com/v8/finance/chart/"
    private val realtimePirceQueryOpt : String = "?region=US&lang=en-US&includePrePost=false&interval=1m&range=1d&corsDomain=search.yahoo.com"
    private val delim = "&"

    fun ProbTicker(search : String) : String {
        val query = "search?q=" + search
        val probURL = probQuestUrl + query + delim + initProbOptions()
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

                // check symbol syntax, only add valid symbol (characters only)
                val pattern = "[a-zA-Z]+".toRegex()
                if (pattern.matches(symbol)) {
                    ret.add("$name : $symbol")
                }
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

    /* functions for fetching real time price */
    // TODO: Add callback routine as argument, and test callback with watch button in ticker explorer
    private fun checkPriceTriggerInBackGround(context : Context, scope : CoroutineScope, symbol : String) {
        val requestQueue = Volley.newRequestQueue(context)
        val queryStr = realtimePirceQueryUrl + symbol + realtimePirceQueryOpt
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