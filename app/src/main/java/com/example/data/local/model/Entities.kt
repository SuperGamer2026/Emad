package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_records")
data class PrayerRecord(
    @PrimaryKey val id: String, // format: "YYYY-MM-DD|PRAYER_KEY"
    val date: String,          // format: "YYYY-MM-DD"
    val prayerKey: String,     // e.g. "FAJR_FARD", "FAJR_SUNNAH", "DHUHR_FARD"
    val status: String,        // "PRAYED_ON_TIME", "PRAYED_LATE", "QADA", "MUTED_NOT_PRAYED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_offsets")
data class PrayerOffset(
    @PrimaryKey val prayerKey: String, // e.g. "FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA"
    val offsetMinutes: Int
)

@Entity(tableName = "qada_tally")
data class QadaTally(
    @PrimaryKey val prayerKey: String, // e.g. "FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA"
    val count: Int
)
