package com.example.data.local.db

import androidx.room.*
import com.example.data.local.model.PrayerRecord
import com.example.data.local.model.PrayerOffset
import com.example.data.local.model.QadaTally
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    // Prayer Records
    @Query("SELECT * FROM prayer_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<PrayerRecord>>

    @Query("SELECT * FROM prayer_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<PrayerRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PrayerRecord)

    // Prayer Offsets
    @Query("SELECT * FROM prayer_offsets")
    fun getAllOffsets(): Flow<List<PrayerOffset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffset(offset: PrayerOffset)

    // Qada Tally
    @Query("SELECT * FROM qada_tally")
    fun getAllQadaTallies(): Flow<List<QadaTally>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQadaTally(tally: QadaTally)
}
