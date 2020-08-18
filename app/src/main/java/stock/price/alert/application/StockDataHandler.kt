package stock.price.alert.application


class StockDataHandler(queries : HashMap<String, String>) {
    private lateinit var queryMap : HashMap<String, String>
    init {
        Reset(queries)
    }

    fun Reset(queries : HashMap<String, String>) {
        queryMap = hashMapOf()
        for ((key, value) in queries) {
            if (value == null) {
                throw Exception("Error: Invalid query response")
            }
            queryMap.set(key, value)
        }
    }

    fun GetResponse(type : String) : String? {
        return queryMap.get(type)
    }
}