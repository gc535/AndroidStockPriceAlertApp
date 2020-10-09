package stock.price.alert.application.ui.notifications

import android.app.Notification.DEFAULT_ALL
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_SOUND
import stock.price.alert.application.R

class NotificationBuilder {


    fun CreateNotificationChannel(context : Context,
                                  channel_name : String,
                                  channel_id: String,
                                  channel_desc: String)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channel_id, channel_name, importance).apply {
                description = channel_desc
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun CreateNotification(context : Context, channel_id : String) : NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channel_id)
            .setSmallIcon(R.drawable.ic_baseline_show_chart_24)
            .setContentTitle("Test Notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
            .setDefaults(DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
    }
}