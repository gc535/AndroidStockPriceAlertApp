package stock.price.alert.application.service.RealTimePriceAlert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RealTimePriceAlertAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            if(intent.action == "android.intent.action.SET_REALTIME_PRICE_CHECK_ALARM"
                || intent.action == "android.intent.action.BOOT_COMPLETED")
            {
                TODO("Setup price check alarm")
                // if need to setup?
            }
        }

    }
}