package stock.price.alert.application.service.RealTimePriceAlert

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONObject
import stock.price.alert.application.ui.notifications.NotificationBuilder

class RealTimePriceAlertService  : JobIntentService() {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    fun cleanUp() {
        // Cancel the scope to cancel ongoing coroutines work
        scope.cancel()
    }

    companion object {
        // Job-ID must be unique across your whole app.
        private val cUnique_Service_ID : Int = "RealTimePriceSerice".toInt()

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, RealTimePriceAlertService::class.java,
                cUnique_Service_ID, intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        Log.d("Service", "RealTimePriceAlertService Created.")
    }


    override fun onHandleWork(intent: Intent) {
        if (intent.hasExtra("symbols")
            && intent.hasExtra("limit")
            && intent.hasExtra("lowerBound"))
        {
            // fetch param from intent
            val symbol = intent.getStringExtra("symbols")!!
            val lowerBound = intent.getBooleanExtra("lowerBound", false)
            val limit = if (lowerBound) intent.getFloatExtra("limit", Float.MIN_VALUE)
                else intent.getFloatExtra("limit", Float.MAX_VALUE)

            checkPriceTriggerInBackGround(symbol, limit, lowerBound)
        }

    }

    private val qUrl : String = "https://query1.finance.yahoo.com/v8/finance/chart/"
    private val qOpt : String = "?region=US&lang=en-US&includePrePost=false&interval=1m&range=1d&corsDomain=search.yahoo.com"
    private fun checkPriceTriggerInBackGround(symbol : String, limit : Float, lowerBound : Boolean) {
        val requestQueue = Volley.newRequestQueue(this)
        val queryStr = qUrl + symbol + qOpt
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, queryStr, null,
            Response.Listener { response->
                // launch parsing and notification logic in worker thread to avoid occupying UI thread
                scope.launch {
                    parseResponse(response)?.let { price ->
                        // trigger lower bound check notification
                        if (lowerBound && price < limit) {
                            val notificationStr = "Price for $symbol dropped below $limit"
                            createPriceAlertNotification(notificationStr)
                        }
                        // trigger upper bound check notification
                        else if (!lowerBound && price > limit ){
                            val notificationStr = "Price for $symbol is now above $limit"
                            createPriceAlertNotification(notificationStr)
                        }
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


    // Init notification channel used by this service
    private val cPriceAlertChName = "Price Alert Notification"
    private val cPriceAlertChId = "price_alert_notification_channel"
    private fun setupNotificationChannel() {
        val priceAlertChDesc = "Notify when any of user's price limits have been triggered"
        NotificationBuilder().CreateNotificationChannel(
            this,
            cPriceAlertChName,
            cPriceAlertChId,
            priceAlertChDesc
        )
    }

    // generate actual price alert notification
    private fun createPriceAlertNotification(content : String) {
        val builder = NotificationCompat.Builder(this, cPriceAlertChId)
            .setSmallIcon(stock.price.alert.application.R.drawable.ic_baseline_show_chart_24)
            .setContentTitle("Price Alert Notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(content)
            )
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val uniqueID : Int = cPriceAlertChId.toInt()
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(uniqueID, builder.build())
        }
    }


}