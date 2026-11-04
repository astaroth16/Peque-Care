package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pequecare.data.local.entity.SleepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records WHERE childId = :childId ORDER BY date DESC, startTime DESC")
    fun getSleepRecords(childId: String): Flow<List<SleepEntity>>

    @Query("SELECT * FROM sleep_records WHERE childId = :childId ORDER BY date DESC, startTime DESC")
    suspend fun getSleepRecordsOnce(childId: String): List<SleepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SleepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<SleepEntity>)

    @Query("SELECT * FROM sleep_records ORDER BY date DESC, startTime DESC")
    suspend fun getAllRecordsOnce(): List<SleepEntity>

    @Query("DELETE FROM sleep_records WHERE childId = :childId")
    suspend fun deleteByChildId(childId: String)

    @Update
    suspend fun update(record: SleepEntity)

    @Delete
    suspend fun delete(record: SleepEntity)
}
