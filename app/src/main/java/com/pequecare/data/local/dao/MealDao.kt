package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pequecare.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_records WHERE childId = :childId ORDER BY date DESC, time DESC")
    fun getMealRecords(childId: String): Flow<List<MealEntity>>

    @Query("SELECT SUM(waterGlasses) FROM meal_records WHERE childId = :childId AND date = :date")
    fun getTodayWater(childId: String, date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MealEntity)

    @Update
    suspend fun update(record: MealEntity)

    @Delete
    suspend fun delete(record: MealEntity)
}
