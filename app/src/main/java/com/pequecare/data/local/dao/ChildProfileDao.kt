package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pequecare.data.local.entity.ChildProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildProfileDao {
    @Query("SELECT * FROM child_profiles")
    fun getAllChildProfiles(): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profiles")
    suspend fun getAllChildProfilesOnce(): List<ChildProfileEntity>

    @Query("SELECT * FROM child_profiles WHERE id = :id")
    fun getChildProfileById(id: String): Flow<ChildProfileEntity?>

    @Query("SELECT * FROM child_profiles WHERE id = :id")
    suspend fun getChildProfileByIdOnce(id: String): ChildProfileEntity?

    @Query("SELECT * FROM child_profiles LIMIT 1")
    fun getChildProfile(): Flow<ChildProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: ChildProfileEntity)

    @Delete
    suspend fun delete(profile: ChildProfileEntity)

    @Query("DELETE FROM child_profiles WHERE id = :id")
    suspend fun deleteById(id: String)
}
