package com.pequecare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalAppointmentDao {
    @Query("SELECT * FROM medical_appointments WHERE childId = :childId ORDER BY date DESC, time DESC")
    fun getAppointments(childId: String): Flow<List<MedicalAppointmentEntity>>

    @Query("SELECT * FROM medical_appointments WHERE childId = :childId ORDER BY date DESC, time DESC")
    suspend fun getAppointmentsOnce(childId: String): List<MedicalAppointmentEntity>

    @Query("SELECT * FROM medical_appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: String): MedicalAppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: MedicalAppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<MedicalAppointmentEntity>)

    @Query("SELECT * FROM medical_appointments ORDER BY date DESC, time DESC")
    suspend fun getAllRecordsOnce(): List<MedicalAppointmentEntity>

    @Query("DELETE FROM medical_appointments WHERE childId = :childId")
    suspend fun deleteByChildId(childId: String)

    @Update
    suspend fun update(appointment: MedicalAppointmentEntity)

    @Delete
    suspend fun delete(appointment: MedicalAppointmentEntity)
}
