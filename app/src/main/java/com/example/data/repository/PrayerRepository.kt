package com.example.data.repository

import com.example.data.local.db.PrayerDao
import com.example.data.local.model.PrayerOffset
import com.example.data.local.model.PrayerRecord
import com.example.data.local.model.QadaTally
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrayerRepository(private val prayerDao: PrayerDao) {

    // Retrieve all prayer records
    val allRecords: Flow<List<PrayerRecord>> = prayerDao.getAllRecords()

    // Retrieve offsets as a map of key -> minutes
    val offsetsMap: Flow<Map<String, Int>> = prayerDao.getAllOffsets().map { list ->
        list.associate { it.prayerKey to it.offsetMinutes }
    }

    // Retrieve Qada tallies as a map of key -> count
    val qadaTalliesMap: Flow<Map<String, Int>> = prayerDao.getAllQadaTallies().map { list ->
        list.associate { it.prayerKey to it.count }
    }

    fun getRecordsForDate(date: String): Flow<List<PrayerRecord>> {
        return prayerDao.getRecordsForDate(date)
    }

    suspend fun saveRecord(record: PrayerRecord) = withContext(Dispatchers.IO) {
        // Retrieve previous state for this specific record to check if QADA status changed
        val dateRecords = prayerDao.getRecordsForDate(record.date).first()
        val previous = dateRecords.find { it.prayerKey == record.prayerKey }
        
        // If transitioning TO or FROM Qada, we can intelligently adjust Qada tally,
        // but since the user might want direct control, let's automate it:
        // Transition from other -> QADA: increment tally
        // Transition from QADA -> other: decrement tally (of course, they can also do manual adjustments in the Forge)
        if (record.status == "QADA" && (previous == null || previous.status != "QADA")) {
            // Auto-increment if candidate for QADA is Fard
            val baseKey = getBasePrayerKey(record.prayerKey)
            if (baseKey != null) {
                incrementQadaTally(baseKey, 1)
            }
        } else if (previous != null && previous.status == "QADA" && record.status != "QADA") {
            // Auto-decrement
            val baseKey = getBasePrayerKey(record.prayerKey)
            if (baseKey != null) {
                incrementQadaTally(baseKey, -1)
            }
        }

        prayerDao.insertRecord(record)
    }

    suspend fun saveOffset(prayerKey: String, offsetMinutes: Int) = withContext(Dispatchers.IO) {
        prayerDao.insertOffset(PrayerOffset(prayerKey, offsetMinutes))
    }

    suspend fun incrementQadaTally(prayerKey: String, delta: Int) = withContext(Dispatchers.IO) {
        val currentTallies = prayerDao.getAllQadaTallies().first()
        val currentTally = currentTallies.find { it.prayerKey == prayerKey }?.count ?: 0
        val newCount = (currentTally + delta).coerceAtLeast(0)
        prayerDao.insertQadaTally(QadaTally(prayerKey, newCount))
    }

    suspend fun setQadaTally(prayerKey: String, count: Int) = withContext(Dispatchers.IO) {
        prayerDao.insertQadaTally(QadaTally(prayerKey, count.coerceAtLeast(0)))
    }

    private fun getBasePrayerKey(fullKey: String): String? {
        val upper = fullKey.uppercase()
        return when {
            upper.contains("FAJR_FARD") -> "FAJR"
            upper.contains("DHUHR_FARD") -> "DHUHR"
            upper.contains("ASR_FARD") -> "ASR"
            upper.contains("MAGHRIB_FARD") -> "MAGHRIB"
            upper.contains("ISHA_FARD") -> "ISHA"
            else -> null
        }
    }
}
