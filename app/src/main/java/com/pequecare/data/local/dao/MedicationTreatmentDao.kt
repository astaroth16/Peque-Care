package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationTreatmentDao {
    @Query("SELECT * FROM medication_treatments WHERE childId = :childId AND isActive = 1 ORDER BY startDate DESC")
    fun getActiveMedications(childId: String): Flow<List<MedicationTreatmentEntity>>

    @Query("SELECT * FROM medication_treatments WHERE childId = :childId ORDER BY startDate DESC")
    fun getAllMedications(childId: String): Flow<List<MedicationTreatmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(treatment: MedicationTreatmentEntity)

    @Update
    suspend fun update(treatment: MedicationTreatmentEntity)

    @Delete
    suspend fun delete(treatment: MedicationTreatmentEntity)
}
