package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_records",
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
data class SleepEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val date: String, // Formato: YYYY-MM-DD
    val startTime: String, // Formato: HH:mm
    val endTime: String, // Formato: HH:mm
    val durationMinutes: Int,
    val type: String, // "noche" | "siesta"
    val quality: String, // "tranquilo" | "inquieto" | "con pesadillas" | "despertares frecuentes"
    val notes: String? = null
)
