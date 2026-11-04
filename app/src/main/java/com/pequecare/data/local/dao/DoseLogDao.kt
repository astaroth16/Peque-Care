package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pequecare.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs WHERE treatmentId = :treatmentId ORDER BY timestamp DESC")
    fun getDoseLogs(treatmentId: String): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE treatmentId IN (:treatmentIds)")
    suspend fun getDoseLogsForTreatmentsOnce(treatmentIds: List<String>): List<DoseLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DoseLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DoseLogEntity>)

    @Delete
    suspend fun delete(log: DoseLogEntity)
}
