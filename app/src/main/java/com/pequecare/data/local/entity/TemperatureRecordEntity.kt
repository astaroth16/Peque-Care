package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "temperature_records",
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
data class TemperatureRecordEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val date: String, // Formato: YYYY-MM-DD
    val time: String, // Formato: HH:mm
    val celsius: Double, // Ejemplo: 38.3
    val symptoms: String, // Lista de síntomas separada por comas: "Fiebre, Mejillas sonrosadas, Apatía"
    val medicationGiven: String? = null, // Medicamento administrado en ese momento, ej: "Ibuprofeno 6.5 ml"
    val notes: String? = null
)
