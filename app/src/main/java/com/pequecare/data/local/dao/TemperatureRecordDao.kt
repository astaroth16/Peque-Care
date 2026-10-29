package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pequecare.data.local.entity.TemperatureRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemperatureRecordDao {
    @Query("SELECT * FROM temperature_records WHERE childId = :childId ORDER BY date DESC, time DESC")
    fun getTemperatureRecords(childId: String): Flow<List<TemperatureRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TemperatureRecordEntity)

    @Delete
    suspend fun delete(record: TemperatureRecordEntity)
}
