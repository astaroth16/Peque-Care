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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: MedicalAppointmentEntity)

    @Update
    suspend fun update(appointment: MedicalAppointmentEntity)

    @Delete
    suspend fun delete(appointment: MedicalAppointmentEntity)
}
