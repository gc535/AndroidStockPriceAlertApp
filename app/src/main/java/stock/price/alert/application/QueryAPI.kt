package stock.price.alert.application

import android.util.Log
import com.android.volley.Response
import org.json.JSONObject

class QueryAPI {
    var cQueryFuncs = HashMap<String, String>()
    init {
        cQueryFuncs["day"] = "TIME_SERIES_INTRADAY" // compact 5 min
        cQueryFuncs["week"] = "TIME_SERIES_INTRADAY" // compact 60 mins
        cQueryFuncs["month"] = "TIME_SERIES_DAILY_ADJUSTED" // compact daily adjusted 30
        cQueryFuncs["3month"] = "TIME_SERIES_DAILY_ADJUSTED" // compact daily adjusted 90
        cQueryFuncs["year"] = "TIME_SERIES_WEEKLY_ADJUSTED" //
        cQueryFuncs["5year"] = "TIME_SERIES_WEEKLY_ADJUSTED" //
    }

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

    fun GenAllQueryStr(ticket : String) :MutableList<String> {
        var queryStrArray : MutableList<String> = ArrayList()
        for (func in cQueryFuncs.values) {
            queryStrArray.add(GenQueryStr(func, ticket))
        }
        return queryStrArray
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

    private val probQuestURL = "https://query2.finance.yahoo.com/v1/finance/"
    private val priceQueryURL = "https://www.alphavantage.co/query?"
    private val priceQueryKey = "apikey=5B4ZQG1WXCAB5L1N"
    private val delim = "&"


}