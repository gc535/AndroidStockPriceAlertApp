package stock.price.alert.application

class QueryAPI {
    private var cQueryFuncs = HashMap<String, String>()
    init {
        cQueryFuncs["day"] = "TIME_SERIES_INTRADAY" // full 1 min
        cQueryFuncs["week"] = "TIME_SERIES_INTRADAY" // full 15 mins
        cQueryFuncs["month"] = "TIME_SERIES_DAILY_ADJUSTED" // compact
        cQueryFuncs["3month"] = "TIME_SERIES_DAILY_ADJUSTED" // compact
        cQueryFuncs["year"] = "TIME_SERIES_DAILY_ADJUSTED"
        cQueryFuncs["5year"] = "TIME_SERIES_WEEKLY_ADJUSTED" // compact
    }

    fun GenAllQueryStr(ticket : String) :MutableList<String> {
        var queryStrArray : MutableList<String> = ArrayList()
        for (func in cQueryFuncs.values) {
            queryStrArray.add(GenQueryStr(func, ticket))
        }
        return queryStrArray
    }

    fun GenQueryStr(func : String, ticker : String) : String {
        val optinal_interval = when(func) {
            "day" -> "interval=1min"
            "week" -> "interval=15min"
            else -> "" } + delim

        val queryURL = rootURL + getFuncStr(func) + delim +
                       "symbol=" + ticker + delim +
                       optinal_interval + key

        return queryURL
    }

    private fun getFuncStr(func: String): String {
        val prefix = "function="
        if (!cQueryFuncs.containsKey(func)) {
            throw Exception("Error: Unsupported query")
        }
        return prefix + cQueryFuncs[func]
    }


    private val key = "apikey=5B4ZQG1WXCAB5L1N"
    private val rootURL = "https://www.alphavantage.co/query?"
    private val delim = "&"

}