package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Salah"
        showNotification(context, prayerName)
    }

    private fun showNotification(context: Context, prayerName: String) {
        val channelId = "imad_prayer_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Imad Prayer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification alerts for daily salah times"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val preferences = context.getSharedPreferences("emad_settings", Context.MODE_PRIVATE)
        val fullLocName = preferences.getString("location_name", "Aleppo, Syria") ?: "Aleppo, Syria"
        val locName = fullLocName.split(",").first().trim()

        val title: String
        val content: String

        if (prayerName.equals("Sunrise", ignoreCase = true)) {
            title = "شروق الشمس - انتهاء وقت الفجر"
            content = "لقد حان وقت شروق الشمس بتوقيت $locName وانتهى وقت صلاة الفجر."
        } else {
            val prayerAr = when (prayerName) {
                "Fajr" -> "الفجر"
                "Dhuhr" -> "الظهر"
                "Asr" -> "العصر"
                "Maghrib" -> "المغرب"
                "Isha" -> "العشاء"
                else -> prayerName
            }
            title = "صلاة $prayerAr"
            content = "حان الآن وقت صلاة $prayerAr بتوقيت $locName"
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using built-in drawable first, we can refine
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }
}
