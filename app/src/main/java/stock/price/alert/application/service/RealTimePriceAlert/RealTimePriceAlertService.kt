package stock.price.alert.application.service.RealTimePriceAlert

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONObject
import stock.price.alert.application.NotificationBuilder
import stock.price.alert.application.R

class RealTimePriceAlertService  : JobIntentService() {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    companion object {
        // Job-ID must be unique across your whole app.
        private val REALTIME_PRICE_JOB_ID : Int = 10
        private val REALTIME_PRICE_JOB_NAME = "RealTimePriceSerice"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, RealTimePriceAlertService::class.java, REALTIME_PRICE_JOB_ID, intent)
        }

        fun SetServiceAlarm(context : Context, interval : Int) {
            val broadcastIntent = createBoardcastIntent(context)
            var pendingIntent = PendingIntent.getBroadcast(
                context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            // set repeat clock interval (mins)
            val interval : Int = interval * 60 * 1000
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmMgr.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval,
                interval.toLong(),
                pendingIntent
            )
        }

        fun UnsetServiceAlarm(context: Context) {
            val broadcastIntent = createBoardcastIntent(context)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, broadcastIntent, PendingIntent.FLAG_NO_CREATE)

            if (pendingIntent != null) {
                val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmMgr.cancel(pendingIntent)
            }
        }

        private fun createBoardcastIntent(context : Context) : Intent {
            return  Intent(Intent(context, RealTimePriceAlertAlarmReceiver::class.java)).apply {
                        action = "android.intent.action.SET_REALTIME_PRICE_CHECK_ALARM"
                    }
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        Log.d("Service", "RealTimePriceAlertService Created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }


    override fun onHandleWork(intent: Intent) {
        Log.d("Service", "RealTimePriceAlertService job started.")
        showDebugNotification()

        if (intent.hasExtra("symbols")
            && intent.hasExtra("limit")
            && intent.hasExtra("lowerBound"))
        {
            // fetch param from intent
            //val symbol = intent.getStringExtra("symbols")!!
            //val lowerBound = intent.getBooleanExtra("lowerBound", false)
            //val limit = if (lowerBound) intent.getFloatExtra("limit", Float.MIN_VALUE)
            //    else intent.getFloatExtra("limit", Float.MAX_VALUE)

            //checkPriceTriggerInBackGround(symbol, limit, lowerBound)
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

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(REALTIME_PRICE_JOB_ID, builder.build())
        }
    }

    private fun showDebugNotification() {
        val builder = NotificationCompat.Builder(this, cPriceAlertChId)
            .setSmallIcon(stock.price.alert.application.R.drawable.ic_baseline_show_chart_24)
            .setContentTitle("Debug Notification")
            .setContentText("This is a debug notification")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText("This is a debug notification")
            )
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // create pending intent to jump to
        val pendingIntent = createShowPricePendingIntent("Coca cola jumped", "KO")
        pendingIntent?.let {
            builder.setContentIntent(it)
            builder.setAutoCancel(true)
        }

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(REALTIME_PRICE_JOB_ID+1, builder.build())
        }
    }

    // using deep link to creat pending intent for using Navigation Component.
    fun createShowPricePendingIntent(name : String, symbol : String): PendingIntent {
        val bundleArgs = Bundle()
        bundleArgs.putString("ticker_symbol", symbol)
        bundleArgs.putString("ticker_name", name)
        return NavDeepLinkBuilder(this)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.navigation_ticker_explorer)
            .setArguments(bundleArgs)
            .createPendingIntent()
    }

    //private fun createShowPricePendingIntent(ticker : String, symbol : String) : PendingIntent? {
    //    val showRealTimePriceIntent = Intent(this, TickerExploreActivity::class.java)
    //    showRealTimePriceIntent.putExtra("name", ticker)
    //    showRealTimePriceIntent.putExtra("symbol", symbol)
    //    val pendingIntent : PendingIntent? = TaskStackBuilder.create(this).run {
    //        // Add the intent, which inflates the back stack
    //        addNextIntentWithParentStack(showRealTimePriceIntent)
    //        // Get the PendingIntent containing the entire back stack
    //        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    //    }
//
    //    return pendingIntent
    //}
}