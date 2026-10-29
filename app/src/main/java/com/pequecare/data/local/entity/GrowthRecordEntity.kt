package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "growth_records",
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
data class GrowthRecordEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val date: String, // Formato: YYYY-MM-DD
    val ageMonths: Int,
    val weightKg: Double,
    val heightCm: Double,
    val headCircumferenceCm: Double? = null,
    val bmi: Double,
    val weightPercentile: Int = 50,
    val heightPercentile: Int = 50,
    val bmiPercentile: Int = 50,
    val weightZScore: Double = 0.0,
    val heightZScore: Double = 0.0,
    val bmiZScore: Double = 0.0,
    val weightStatus: String = "Normal OMS",
    val heightStatus: String = "Normal OMS",
    val bmiStatus: String = "Normal OMS",
    val notes: String? = null
)
