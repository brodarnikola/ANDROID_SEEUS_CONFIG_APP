package hr.sil.android.seeusadmin.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import androidx.core.app.NotificationCompat
import hr.sil.android.seeusadmin.R


class NotificationHelper(val mContext: Context) {

    lateinit var mNotificationManager: NotificationManager
    val NOTIFICATION_CHANNEL_ID = "10001"
    fun createNotification(title: String?, message: String?, klazz: Class<*>) {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(mContext, klazz)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_IMMUTABLE)
        val mBuilder = NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)

        mBuilder.setSmallIcon(R.drawable.ic_fab)
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)

        mNotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build())
    }

    fun clearNotification (){
        mNotificationManager.cancelAll()
    }

}