package stock.price.alert.application.service.RealTimePriceAlert

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast

class RealTimePriceAlertAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("Broadcast", "Received Broadcast.")
        Toast.makeText(context, "Received Broadcast", Toast.LENGTH_SHORT).show()
        intent?.let {
            // restart periodic price check alarm if device is rebooted
            if(intent.action == "android.intent.action.BOOT_COMPLETED") {
                val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val broadcastIntent = Intent("android.intent.action.SET_REALTIME_PRICE_CHECK_ALARM")
                var pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, 0)

                // set repeat clock every 5 secs
                val interval : Long = 5 * 1000
                alarmMgr.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + interval,
                    interval,
                    pendingIntent
                )

            }

            // start background price check service if received broadcast signal
            if(intent.action == "android.intent.action.SET_REALTIME_PRICE_CHECK_ALARM") {
                // if need to setup?
                RealTimePriceAlertService.enqueueWork(context, intent)
            }

        }

    }
}