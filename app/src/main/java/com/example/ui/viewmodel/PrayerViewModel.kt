package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.PrayerTimeCalculator
import com.example.data.local.db.AppDatabase
import com.example.data.local.model.PrayerRecord
import com.example.data.local.model.QadaTally
import com.example.data.repository.PrayerRepository
import com.example.service.PrayerScheduleWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = PrayerRepository(database.prayerDao())

    private val preferences = application.getSharedPreferences("emad_settings", Context.MODE_PRIVATE)

    // User settings stored in SharedPreferences
    val onboardingCompleted = MutableStateFlow(preferences.getBoolean("onboarding_completed", false))
    val themePreset = MutableStateFlow(preferences.getString("theme_preset", "EXPRESSIVE") ?: "EXPRESSIVE")
    val appIconPreset = MutableStateFlow(preferences.getString("app_icon_preset", "EXPRESSIVE") ?: "EXPRESSIVE")
    val locationName = MutableStateFlow(preferences.getString("location_name", "Aleppo, Syria") ?: "Aleppo, Syria")
    val latitude = MutableStateFlow(preferences.getFloat("latitude", 36.2021f).toDouble())
    val longitude = MutableStateFlow(preferences.getFloat("longitude", 37.1343f).toDouble())
    val timezone = MutableStateFlow(preferences.getFloat("timezone", 3.0f).toDouble())

    // Custom forge baseline target override (if set by user, else null for dynamic totalPending)
    val customForgeTarget = MutableStateFlow<Int?>(
        if (preferences.contains("custom_forge_target")) {
            val v = preferences.getInt("custom_forge_target", -1)
            if (v == -1) null else v
        } else null
    )

    // Dynamic forge target max (high-water mark of total pending Qadas, used as target if customForgeTarget is null)
    val maxQadaTarget = MutableStateFlow(preferences.getInt("max_qada_target", 0))

    fun saveCustomForgeTarget(target: Int?) {
        viewModelScope.launch {
            if (target == null) {
                preferences.edit().remove("custom_forge_target").apply()
                customForgeTarget.value = null
            } else {
                preferences.edit().putInt("custom_forge_target", target).apply()
                customForgeTarget.value = target
            }
        }
    }

    fun resetMaxQadaTargetToCurrent() {
        viewModelScope.launch {
            val prayersList = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
            val currentPending = prayersList.sumOf { qadaTallies.value[it] ?: 0 }
            preferences.edit().putInt("max_qada_target", currentPending).apply()
            maxQadaTarget.value = currentPending
        }
    }

    // Notification states for the five daily prayers
    val fahrNotifEnabled = MutableStateFlow(preferences.getBoolean("notif_Fajr", true))
    val dhuhrNotifEnabled = MutableStateFlow(preferences.getBoolean("notif_Dhuhr", true))
    val asrNotifEnabled = MutableStateFlow(preferences.getBoolean("notif_Asr", true))
    val maghribNotifEnabled = MutableStateFlow(preferences.getBoolean("notif_Maghrib", true))
    val ishaNotifEnabled = MutableStateFlow(preferences.getBoolean("notif_Isha", true))

    fun isPrayerNotificationEnabled(prayerName: String): Boolean {
        return when (prayerName) {
            "Fajr" -> fahrNotifEnabled.value
            "Dhuhr" -> dhuhrNotifEnabled.value
            "Asr" -> asrNotifEnabled.value
            "Maghrib" -> maghribNotifEnabled.value
            "Isha" -> ishaNotifEnabled.value
            else -> false
        }
    }

    fun togglePrayerNotification(prayerName: String) {
        viewModelScope.launch {
            val currentVal = isPrayerNotificationEnabled(prayerName)
            val newVal = !currentVal
            preferences.edit().putBoolean("notif_$prayerName", newVal).apply()
            
            when (prayerName) {
                "Fajr" -> fahrNotifEnabled.value = newVal
                "Dhuhr" -> dhuhrNotifEnabled.value = newVal
                "Asr" -> asrNotifEnabled.value = newVal
                "Maghrib" -> maghribNotifEnabled.value = newVal
                "Isha" -> ishaNotifEnabled.value = newVal
            }

            // Reschedule alarms immediately to reflect toggle
            try {
                WorkManager.getInstance(getApplication()).enqueue(
                    OneTimeWorkRequestBuilder<PrayerScheduleWorker>().build()
                )
            } catch (e: Exception) {
                android.util.Log.e("PrayerViewModel", "Failed to reschedule alarms after toggle", e)
            }
        }
    }

    // Date state
    val currentDate = MutableStateFlow(LocalDate.now())

    // Historic records
    val allRecords = repository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current date records
    val todayRecords = currentDate.flatMapLatest { date ->
        repository.getRecordsForDate(date.toString())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Manual offset overrides (e.g., "FAJR" -> minutes offset)
    val offsets = repository.offsetsMap.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Qada tallies map (e.g., "FAJR" -> count)
    val qadaTallies = repository.qadaTalliesMap.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Calculated prayer times for current date (updated with user offsets and custom location)
    val prayerTimes = combine(currentDate, offsets, latitude, longitude, timezone) { date, offsetMap, lat, lon, tz ->
        val base = PrayerTimeCalculator.calculateTimes(date, latitude = lat, longitude = lon, timezone = tz)
        PrayerTimeCalculator.PrayerTimes(
            fajr = base.fajr.plusMinutes((offsetMap["FAJR"] ?: 0).toLong()),
            sunrise = base.sunrise.plusMinutes((offsetMap["SUNRISE"] ?: 0).toLong()),
            dhuhr = base.dhuhr.plusMinutes((offsetMap["DHUHR"] ?: 0).toLong()),
            asr = base.asr.plusMinutes((offsetMap["ASR"] ?: 0).toLong()),
            maghrib = base.maghrib.plusMinutes((offsetMap["MAGHRIB"] ?: 0).toLong()),
            isha = base.isha.plusMinutes((offsetMap["ISHA"] ?: 0).toLong())
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrayerTimeCalculator.calculateTimes(LocalDate.now())
    )

    // Countdown state (Next Prayer name, remaining time formatted, depleting ratio)
    data class CountdownState(
        val nextPrayerName: String = "Fajr",
        val formattedRemaining: String = "00:00:00",
        val nextPrayerTime: String = "00:00",
        val depletingRatio: Float = 1.0f
    )

    private val _countdownState = MutableStateFlow(CountdownState())
    val countdownState: StateFlow<CountdownState> = _countdownState.asStateFlow()

    init {
        // Monitor total pending Qadas to dynamically grow the high-water mark target maxQadaTarget
        viewModelScope.launch {
            val prayersList = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
            qadaTallies.collect { tallies ->
                val total = prayersList.sumOf { tallies[it] ?: 0 }
                val currentMax = maxQadaTarget.value
                if (total == 0) {
                    preferences.edit().remove("custom_forge_target").putInt("max_qada_target", 0).apply()
                    customForgeTarget.value = null
                    maxQadaTarget.value = 0
                } else if (total > currentMax) {
                    preferences.edit().putInt("max_qada_target", total).apply()
                    maxQadaTarget.value = total
                }
            }
        }

        // Trigger live countdown timer tick every second
        viewModelScope.launch {
            while (true) {
                updateCountdown()
                delay(1000)
            }
        }

        // Initialize default offsets & Qada structures if not existing in DB
         viewModelScope.launch {
             // Invalidate/trigger work manager to ensure sync is live on first launcher load
             try {
                 WorkManager.getInstance(getApplication()).enqueue(
                     OneTimeWorkRequestBuilder<PrayerScheduleWorker>().build()
                 )
             } catch (e: Exception) {
                 android.util.Log.e("PrayerViewModel", "Failed to enqueue initial prayer schedule work", e)
             }
         }
    }

    private suspend fun updateCountdown() {
        val now = LocalDateTime.now()
        val offsetsVal = offsets.value
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        val lat = latitude.value
        val lon = longitude.value
        val tz = timezone.value

        val timesToday = PrayerTimeCalculator.calculateTimes(today, latitude = lat, longitude = lon, timezone = tz)
        val timesTomorrow = PrayerTimeCalculator.calculateTimes(tomorrow, latitude = lat, longitude = lon, timezone = tz)

        fun applyOffset(time: LocalDateTime, key: String): LocalDateTime {
            return time.plusMinutes((offsetsVal[key] ?: 0).toLong())
        }

        val fajrToday = applyOffset(timesToday.fajr, "FAJR")
        val sunriseToday = applyOffset(timesToday.sunrise, "SUNRISE")
        val fajrTomorrow = applyOffset(timesTomorrow.fajr, "FAJR")
        val sunriseTomorrow = applyOffset(timesTomorrow.sunrise, "SUNRISE")

        val prayerSequence = listOf(
            "Fajr" to fajrToday,
            "Dhuhr" to applyOffset(timesToday.dhuhr, "DHUHR"),
            "Asr" to applyOffset(timesToday.asr, "ASR"),
            "Maghrib" to applyOffset(timesToday.maghrib, "MAGHRIB"),
            "Isha" to applyOffset(timesToday.isha, "ISHA"),
            "Fajr (Tomorrow)" to fajrTomorrow
        )

        // Find next prayer
        var nextPair = prayerSequence.find { it.second.isAfter(now) }
        var prevPair = prayerSequence.findLast { it.second.isBefore(now) }

        if (nextPair == null) {
            // Edge case: after Isha today
            nextPair = "Fajr" to fajrTomorrow
        }
        if (prevPair == null) {
            // Edge case: before Fajr today, previous is Isha yesterday
            val yesterday = today.minusDays(1)
            val timesYesterday = PrayerTimeCalculator.calculateTimes(yesterday, latitude = lat, longitude = lon, timezone = tz)
            prevPair = "Isha" to applyOffset(timesYesterday.isha, "ISHA")
        }

        var nextName = nextPair.first.replace(" (Tomorrow)", "")
        var nextTime = nextPair.second
        var prevTime = prevPair.second

        // Intercept specifically if we are in Fajr period (between Fajr and Sunrise)
        // to show "Sunrise" as the countdown target with progress from Fajr to Sunrise.
        if (!now.isBefore(fajrToday) && now.isBefore(sunriseToday)) {
            nextName = "Sunrise"
            nextTime = sunriseToday
            prevTime = fajrToday
        } else if (!now.isBefore(fajrTomorrow) && now.isBefore(sunriseTomorrow)) {
            nextName = "Sunrise"
            nextTime = sunriseTomorrow
            prevTime = fajrTomorrow
        }

        val durationRemaining = Duration.between(now, nextTime)
        val totalDuration = Duration.between(prevTime, nextTime)

        val secsRemaining = durationRemaining.seconds.coerceAtLeast(0)
        val hours = secsRemaining / 3600
        val mins = (secsRemaining % 3600) / 60
        val secs = secsRemaining % 60

        val formatted = String.format("%02d:%02d:%02d", hours, mins, secs)

        val ratio = if (totalDuration.seconds > 0) {
            (secsRemaining.toFloat() / totalDuration.seconds.toFloat()).coerceIn(0.0f, 1.0f)
        } else {
            1.0f
        }

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        _countdownState.value = CountdownState(
            nextPrayerName = nextName,
            formattedRemaining = formatted,
            nextPrayerTime = nextTime.format(timeFormatter),
            depletingRatio = ratio
        )
    }

    fun setOnboardingCompleted(
        completed: Boolean,
        initialTheme: String,
        initialLocName: String,
        lat: Double,
        lon: Double,
        tz: Double,
        notificationsEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            preferences.edit()
                .putBoolean("onboarding_completed", completed)
                .putString("theme_preset", initialTheme)
                .putString("location_name", initialLocName)
                .putFloat("latitude", lat.toFloat())
                .putFloat("longitude", lon.toFloat())
                .putFloat("timezone", tz.toFloat())
                .putBoolean("notifications_enabled_global", notificationsEnabled)
                .putBoolean("notif_Fajr", notificationsEnabled)
                .putBoolean("notif_Dhuhr", notificationsEnabled)
                .putBoolean("notif_Asr", notificationsEnabled)
                .putBoolean("notif_Maghrib", notificationsEnabled)
                .putBoolean("notif_Isha", notificationsEnabled)
                .apply()

            onboardingCompleted.value = completed
            themePreset.value = initialTheme
            updateAppIcon(initialTheme)
            locationName.value = initialLocName
            latitude.value = lat
            longitude.value = lon
            timezone.value = tz

            fahrNotifEnabled.value = notificationsEnabled
            dhuhrNotifEnabled.value = notificationsEnabled
            asrNotifEnabled.value = notificationsEnabled
            maghribNotifEnabled.value = notificationsEnabled
            ishaNotifEnabled.value = notificationsEnabled

            // Reschedule alarms immediately
            try {
                WorkManager.getInstance(getApplication()).enqueue(
                    OneTimeWorkRequestBuilder<PrayerScheduleWorker>().build()
                )
            } catch (e: Exception) {
                android.util.Log.e("PrayerViewModel", "Failed to enqueue onboarding prayer schedule work", e)
            }
        }
    }

    private fun updateAppIcon(theme: String, restartApp: Boolean = false) {
        val app = getApplication<Application>()
        val packageManager = app.packageManager
        val packageName = app.packageName

        val aliases = listOf(
            "Expressive", "Cosmic", "Teal", "Rose", "Golden", "Emerald", 
            "Ocean", "Amythist", "Sunfire", "Sapphire", "Lavender", "Mint", 
            "Forest", "Blood_moon", "Monochrome", "Sakura", "Nebula", 
            "Desert", "Aurora"
        )
        
        // Capitalize the first letter and lowercase the rest to match alias naming
        val activeAlias = if (theme == "BLOOD_MOON") "Blood_moon" else theme.lowercase().replaceFirstChar { it.uppercase() }

        try {
            for (alias in aliases) {
                val componentName = android.content.ComponentName(
                    packageName,
                    "com.example.MainActivityAlias$alias"
                )
                val isTarget = alias == activeAlias
                val newState = if (isTarget) {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                val currentState = packageManager.getComponentEnabledSetting(componentName)
                if (currentState != newState) {
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        newState,
                        android.content.pm.PackageManager.DONT_KILL_APP
                    )
                }
            }
            
            if (restartApp) {
                // Manually restart the app to apply the alias change cleanly
                var intent = packageManager.getLaunchIntentForPackage(packageName)
                if (intent == null) {
                    intent = android.content.Intent().apply {
                        setClassName(packageName, "com.example.MainActivity")
                    }
                }
                intent.putExtra("START_DESTINATION", "finetune")
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                app.startActivity(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("PrayerViewModel", "Failed to update dynamic launcher icon", e)
        }
    }

    fun setThemePreset(preset: String) {
        preferences.edit().putString("theme_preset", preset).apply()
        themePreset.value = preset
    }

    fun setAppIconPreset(preset: String) {
        preferences.edit().putString("app_icon_preset", preset).apply()
        appIconPreset.value = preset
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            updateAppIcon(preset, restartApp = true)
        }
    }

    fun setLocation(name: String, lat: Double, lon: Double, tz: Double) {
        viewModelScope.launch {
            preferences.edit()
                .putString("location_name", name)
                .putFloat("latitude", lat.toFloat())
                .putFloat("longitude", lon.toFloat())
                .putFloat("timezone", tz.toFloat())
                .apply()

            locationName.value = name
            latitude.value = lat
            longitude.value = lon
            timezone.value = tz

            // Reschedule alarms immediately
            try {
                WorkManager.getInstance(getApplication()).enqueue(
                    OneTimeWorkRequestBuilder<PrayerScheduleWorker>().build()
                )
            } catch (e: Exception) {
                android.util.Log.e("PrayerViewModel", "Failed to enqueue location prayer schedule work", e)
            }
        }
    }

    // Streak count State
    val streakCount: StateFlow<Int> = allRecords.map { list ->
        calculateStreak(list)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Weekly completion percentage for fards
    val consistencyMetrics: StateFlow<Map<Int, Double>> = allRecords.map { list ->
        // Return percentage for 7-day and 30-day
        val nowStr = LocalDate.now()
        val fardKeys = listOf("FAJR_FARD", "DHUHR_FARD", "ASR_FARD", "MAGHRIB_FARD", "ISHA_FARD")
        
        fun calculateRatio(daysLimit: Long): Double {
            val sinceDate = nowStr.minusDays(daysLimit)
            val filteredRecords = list.filter {
                val rDate = LocalDate.parse(it.date)
                (rDate.isEqual(sinceDate) || rDate.isAfter(sinceDate)) && fardKeys.contains(it.prayerKey)
            }
            if (filteredRecords.isEmpty()) return 0.0
            val successfulCount = filteredRecords.count { it.status == "PRAYED_ON_TIME" || it.status == "PRAYED_LATE" }
            return (successfulCount.toDouble() / (daysLimit * 5).toDouble()).coerceIn(0.0, 1.0)
        }

        mapOf(
            7 to calculateRatio(7),
            30 to calculateRatio(30)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = mapOf(7 to 0.0, 30 to 0.0)
    )

    // User consistency level title
    val consistencyLevelTitle: StateFlow<String> = consistencyMetrics.map { metrics ->
        val score = metrics[30] ?: 0.0
        when {
            score >= 0.90 -> "Guardian of the Pillar"
            score >= 0.70 -> "Steadfast"
            score >= 0.40 -> "Musalat"
            else -> "Seeker of Light"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Seeker of Light"
    )

    // Save status for a prayer component
    fun updatePrayerStatus(prayerKey: String, status: String) {
        viewModelScope.launch {
            val dateStr = currentDate.value.toString()
            val id = "${dateStr}|$prayerKey"
            repository.saveRecord(
                PrayerRecord(
                    id = id,
                    date = dateStr,
                    prayerKey = prayerKey,
                    status = status
                )
            )
        }
    }

    // Save manual offset adjustment
    fun updateOffset(prayerKey: String, offsetMinutes: Int) {
        viewModelScope.launch {
            repository.saveOffset(prayerKey, offsetMinutes)
            // Trigger alarm rescheduling immediately upon fine-tuning
            try {
                WorkManager.getInstance(getApplication()).enqueue(
                    OneTimeWorkRequestBuilder<PrayerScheduleWorker>().build()
                )
            } catch (e: Exception) {
                android.util.Log.e("PrayerViewModel", "Failed to enqueue offset fine-tuning prayer schedule work", e)
            }
        }
    }

    // Direct Qada update
    fun updateQadaCount(prayerKey: String, delta: Int) {
        viewModelScope.launch {
            repository.incrementQadaTally(prayerKey, delta)
        }
    }

    fun setQadaCount(prayerKey: String, count: Int) {
        viewModelScope.launch {
            repository.setQadaTally(prayerKey, count)
        }
    }

    private fun calculateStreak(records: List<PrayerRecord>): Int {
        val grouped = records.groupBy { it.date }
        var streak = 0
        var checkDate = LocalDate.now()

        val fardList = listOf("FAJR_FARD", "DHUHR_FARD", "ASR_FARD", "MAGHRIB_FARD", "ISHA_FARD")

        // Helper to check if a specific date has all 5 fard completed (either ON_TIME or LATE)
        fun isDayFullyCompleted(date: LocalDate): Boolean {
            val dateStr = date.toString()
            val dayRecs = grouped[dateStr] ?: return false
            return fardList.all { fard ->
                val rec = dayRecs.find { it.prayerKey == fard }
                rec != null && (rec.status == "PRAYED_ON_TIME" || rec.status == "PRAYED_LATE")
            }
        }

        // If today is complete, streak includes today. If not, check if yesterday was.
        if (!isDayFullyCompleted(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        while (true) {
            if (isDayFullyCompleted(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    // Weekly pillar chart stats for Fardhs & Sunnahs over the current week (Saturday to Friday)
    val weeklyPillarStats: StateFlow<List<DailyPillarData>> = allRecords.map { list ->
        val fardKeys = listOf("FAJR_FARD", "DHUHR_FARD", "ASR_FARD", "MAGHRIB_FARD", "ISHA_FARD")
        val sunnahKeys = listOf("FAJR_SUNNAH", "DHUHR_SUNNAH_BEFORE", "DHUHR_SUNNAH_AFTER", "MAGHRIB_SUNNAH", "ISHA_SUNNAH_BEFORE", "ISHA_SUNNAH_AFTER")
        
        val today = LocalDate.now()
        val daysToSubtract = when (today.dayOfWeek) {
            java.time.DayOfWeek.SATURDAY -> 0
            java.time.DayOfWeek.SUNDAY -> 1
            java.time.DayOfWeek.MONDAY -> 2
            java.time.DayOfWeek.TUESDAY -> 3
            java.time.DayOfWeek.WEDNESDAY -> 4
            java.time.DayOfWeek.THURSDAY -> 5
            java.time.DayOfWeek.FRIDAY -> 6
            else -> 0
        }
        val startSaturday = today.minusDays(daysToSubtract.toLong())

        (0..6).map { i ->
            val targetDate = startSaturday.plusDays(i.toLong())
            val dateStr = targetDate.toString()
            
            val dateRecords = list.filter { it.date == dateStr }
            val fardCount = dateRecords.count { fardKeys.contains(it.prayerKey) && (it.status == "PRAYED_ON_TIME" || it.status == "PRAYED_LATE") }
            val sunnahCount = dateRecords.count { sunnahKeys.contains(it.prayerKey) && (it.status == "PRAYED_ON_TIME" || it.status == "PRAYED_LATE") }
            
            val dayOfWeekName = when (targetDate.dayOfWeek) {
                java.time.DayOfWeek.SATURDAY -> "Sat"
                java.time.DayOfWeek.SUNDAY -> "Sun"
                java.time.DayOfWeek.MONDAY -> "Mon"
                java.time.DayOfWeek.TUESDAY -> "Tue"
                java.time.DayOfWeek.WEDNESDAY -> "Wed"
                java.time.DayOfWeek.THURSDAY -> "Thu"
                java.time.DayOfWeek.FRIDAY -> "Fri"
                else -> ""
            }
            
            DailyPillarData(
                dateLabel = dayOfWeekName,
                fardCompleted = fardCount,
                sunnahCompleted = sunnahCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

data class DailyPillarData(
    val dateLabel: String,
    val fardCompleted: Int,
    val sunnahCompleted: Int
)
