package stock.price.alert.application.Data

import android.content.Context
import android.util.Log
import stock.price.alert.application.R

class WatchListDBHandler(private val mContext: Context) {
    /**  Description:
    *    1. Database entry is like: symbol  ->   name   : lower bound : upper bound
    *                                  KO   -> Cocacola :     48.08   :   --
    *
    *
    * */


    val DELIM = " : "
    val UNINIT = "--"
    val sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.watchlist_database_name), Context.MODE_PRIVATE)

    fun GetAllSymbol(): Map<String, String> {
        val all = sharedPref.all as Map<String, String>
        return all
    }

    fun GetVal(key : String) : String? {
        return sharedPref.getString(key, null)
    }


    fun GetTickerName(key : String) : String? {
        if (sharedPref.contains(key)){
            val name = sharedPref.getString(key, null)!!.split(DELIM)[0]
            return name
        }
        return null
    }


    fun GetLowerBound(key : String) : Float? {
        if (sharedPref.contains(key)){
            val lb_val = sharedPref.getString(key, null)!!.split(DELIM)[1]
            if (lb_val != UNINIT) {
                return lb_val.toFloat()
            }
        }
        return null
    }


    fun GetUpperBound(key : String) : Float? {
        if (sharedPref.contains(key)){
            val ub_val = sharedPref.getString(key, null)!!.split(DELIM)[2]
            if (ub_val != UNINIT) {
                return ub_val.toFloat()
            }
        }
        return null
    }


    fun PutVal(key: String, value: String) {
        if (isValValid(value)) {
            safePutVal(key, value)
        } else {
            Exception("Error: Trying to put invalid watchlist value: $value for symbol: $key")
        }
    }

    fun safePutVal(key: String, value: String) {
        with (sharedPref.edit()) {
            putString(key, value)
            commit()
        }
    }

    fun RemoveKey(symbol : String) {
        if (sharedPref.contains(symbol)) {
            safeRemoveKey(symbol)
        } else {
            Exception("Error: Trying to remove a symbol: $symbol which does not exist")
        }
    }

    fun safeRemoveKey(symbol : String){
        with (sharedPref.edit()) {
            remove(symbol)
            commit()
        }
    }

    fun PutLowerBound(symbol: String, name: String, value: Float?) {
        val hasSymbol = sharedPref.contains(symbol)
        val old_lb = if (hasSymbol) sharedPref.getString(symbol, null)!!.split(DELIM)[1] else UNINIT
        val old_ub = if (hasSymbol) sharedPref.getString(symbol, null)!!.split(DELIM)[2] else UNINIT


        if (value != null) {
            val new_value = name + DELIM + value.toString() + DELIM + old_ub
            safePutVal(symbol, new_value)
        }
        else {
            if (hasSymbol && old_ub == UNINIT) {
                safeRemoveKey(symbol)
            }
            else if (hasSymbol) {
                val new_value = name + DELIM + UNINIT + DELIM + old_ub
                safePutVal(symbol, new_value)
            }
        }
    }

    fun PutUpperBound(symbol: String, name: String, value: Float?) {
        val hasSymbol = sharedPref.contains(symbol)
        val old_lb = if (hasSymbol) sharedPref.getString(symbol, null)!!.split(DELIM)[1] else UNINIT
        val old_ub = if (hasSymbol) sharedPref.getString(symbol, null)!!.split(DELIM)[2] else UNINIT

        if (value != null) {
            val new_value = name + DELIM + old_lb + DELIM + value.toString()
            safePutVal(symbol, new_value)
        }
        else {
            if (hasSymbol && old_lb == UNINIT) {
                safeRemoveKey(symbol)
            }
            else if (hasSymbol) {
                val new_value = name + DELIM + old_lb + DELIM + UNINIT
                safePutVal(symbol, new_value)
            }
        }
        //Log.d("WATCHLISTDB", "")
    }


    fun Flush() {
        with (sharedPref.edit()) {
            apply()
            commit()
        }
    }

    private fun isValValid(value : String) : Boolean {
        if (value.split(DELIM).size != 3) {
            return false
        }
        return true
    }



}