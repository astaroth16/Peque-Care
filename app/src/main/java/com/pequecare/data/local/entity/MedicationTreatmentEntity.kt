package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_treatments",
    foreignKeys = [
        ForeignKey(
            entity = ChildProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"]), Index(value = ["status"])]
)
data class MedicationTreatmentEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val name: String, // Nombre comercial, ej: "Ibuprofeno Suspensión 2%"
    val formula: String, // Fórmula / principio activo, ej: "Ibuprofeno"
    val concentration: String, // Concentración, ej: "2% (100 mg / 5 ml)"
    val dosage: String, // Dosis prescrita, ej: "6.5 ml"
    val unit: String, // Unidad de medida, ej: "ml", "mg", "gotas", "puff"
    val purpose: String, // Propósito o indicación médica, ej: "Antitérmico para fiebre"
    val frequencyHours: Int, // Frecuencia de toma en horas, ej: 8
    val startDate: String, // Fecha de inicio: YYYY-MM-DD
    val endDate: String, // Fecha de finalización: YYYY-MM-DD
    val durationDays: Int, // Duración total en días del tratamiento
    val nextDoseTime: String? = null, // Próxima dosis programada: YYYY-MM-DDTHH:mm
    val status: String, // Estado del tratamiento: "ACTIVO" | "COMPLETADO" | "SUSPENDIDO"
    val isActive: Boolean = true,
    val notes: String? = null
)
