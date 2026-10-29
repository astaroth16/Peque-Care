package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medical_appointments",
    foreignKeys = [
        ForeignKey(
            entity = ChildProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"]), Index(value = ["date"])]
)
data class MedicalAppointmentEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val date: String, // Formato: YYYY-MM-DD
    val time: String, // Formato: HH:mm
    val doctorName: String,
    val specialty: String,
    val clinic: String,
    val reason: String,
    val diagnosis: String = "",
    val indications: String = "",
    val prescriptions: String = "", // Recetas / medicamentos prescritos separados por coma
    val studiesRequested: String = "", // Estudios o análisis solicitados
    val nextFollowUpDate: String? = null, // Formato: YYYY-MM-DD
    val status: String, // "programada" | "completada" | "cancelada"
    val notes: String? = null
)
