package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationTreatmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["treatmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["treatmentId"]), Index(value = ["timestamp"])]
)
data class DoseLogEntity(
    @PrimaryKey
    val id: String,
    val treatmentId: String,
    val timestamp: String, // ISO 8601 String o YYYY-MM-DDTHH:mm:ssZ
    val dosageGiven: String, // Dosis efectivamente administrada
    val status: String, // "administrado" | "omitido"
    val notes: String? = null
)
