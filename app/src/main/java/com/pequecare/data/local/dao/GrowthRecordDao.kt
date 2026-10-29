package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pequecare.data.local.entity.GrowthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthRecordDao {
    @Query("SELECT * FROM growth_records WHERE childId = :childId ORDER BY date DESC")
    fun getAllGrowthRecords(childId: String): Flow<List<GrowthRecordEntity>>

    @Query("SELECT * FROM growth_records WHERE childId = :childId ORDER BY date DESC LIMIT 1")
    fun getLatestGrowthRecord(childId: String): Flow<GrowthRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: GrowthRecordEntity)

    @Update
    suspend fun update(record: GrowthRecordEntity)

    @Delete
    suspend fun delete(record: GrowthRecordEntity)
}
