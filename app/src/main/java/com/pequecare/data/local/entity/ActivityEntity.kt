package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_records",
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
data class ActivityEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val date: String, // Formato: YYYY-MM-DD
    val time: String, // Formato: HH:mm
    val title: String,
    val category: String, // "al aire libre" | "educativa" | "motricidad" | "lectura" | "pantalla" | "recreativa"
    val durationMinutes: Int,
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null, // Formato: HH:mm
    val notes: String? = null
)
