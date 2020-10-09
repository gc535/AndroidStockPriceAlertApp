package stock.price.alert.application.ui.search

import org.json.JSONObject

class QueryAPI {
    /* Functions for ticker info probing */
    private val probQuestURL = "https://query2.finance.yahoo.com/v1/finance/"
    private val delim = "&"

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

    /* functions for fetching time price */



}