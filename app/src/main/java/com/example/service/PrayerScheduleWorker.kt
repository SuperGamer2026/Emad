package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.PrayerTimeCalculator
import com.example.data.local.db.AppDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class PrayerScheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("PrayerScheduleWorker", "Scheduling prayer alarms...")
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.prayerDao()

            // Fetch coordinates from SharedPreferences
            val preferences = applicationContext.getSharedPreferences("emad_settings", Context.MODE_PRIVATE)
            val lat = preferences.getFloat("latitude", 36.2021f).toDouble()
            val lon = preferences.getFloat("longitude", 37.1343f).toDouble()
            val tz = preferences.getFloat("timezone", 3.0f).toDouble()

            // Fetch custom user-set offsets
            val offsets = dao.getAllOffsets().first().associate { it.prayerKey to it.offsetMinutes }

            // Calculate prayer times for today and tomorrow
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            scheduleForDate(today, offsets, lat, lon, tz)
            scheduleForDate(tomorrow, offsets, lat, lon, tz)

            return Result.success()
        } catch (e: Exception) {
            Log.e("PrayerScheduleWorker", "Error scheduling prayers", e)
            return Result.retry()
        }
    }

    private fun scheduleForDate(date: LocalDate, offsets: Map<String, Int>, lat: Double, lon: Double, tz: Double) {
        val baseTimes = PrayerTimeCalculator.calculateTimes(date, latitude = lat, longitude = lon, timezone = tz)

        val prayers = listOf(
            "Fajr" to baseTimes.fajr,
            "Sunrise" to baseTimes.sunrise,
            "Dhuhr" to baseTimes.dhuhr,
            "Asr" to baseTimes.asr,
            "Maghrib" to baseTimes.maghrib,
            "Isha" to baseTimes.isha
        )

        val preferences = applicationContext.getSharedPreferences("emad_settings", Context.MODE_PRIVATE)
        val globalEnabled = preferences.getBoolean("notifications_enabled_global", true)

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for ((name, initialTime) in prayers) {
            val isEnabled = if (name == "Sunrise") {
                preferences.getBoolean("notif_Fajr", true)
            } else {
                preferences.getBoolean("notif_$name", true)
            }

            val requestCode = (date.hashCode() + name.hashCode()).hashCode()
            val intent = Intent(applicationContext, PrayerAlarmReceiver::class.java).apply {
                putExtra("PRAYER_NAME", name)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (!globalEnabled || !isEnabled) {
                alarmManager.cancel(pendingIntent)
                Log.d("PrayerScheduleWorker", "Skipped/Cancelled alarm for $name on $date (disabled)")
                continue
            }

            val offsetMinutes = if (name == "Sunrise") {
                offsets["SUNRISE"] ?: 0
            } else {
                offsets[name.uppercase()] ?: 0
            }
            val scheduledTime = initialTime.plusMinutes(offsetMinutes.toLong())

            val now = LocalDateTime.now()
            if (scheduledTime.isAfter(now)) {
                val triggerAtMillis = scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        // Fallback to standard non-exact alarm if permission not granted
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
                Log.d("PrayerScheduleWorker", "Scheduled $name at $scheduledTime")
            }
        }
    }
}
