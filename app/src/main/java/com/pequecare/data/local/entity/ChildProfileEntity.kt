package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val lastName: String = "",
    val birthDate: String, // Formato: YYYY-MM-DD
    val gender: String, // "female" | "male"
    val bloodType: String = "",
    val allergies: String = "", // Coma-separated string: "Penicilina, Polen"
    val pediatricianName: String = "",
    val pediatricianPhone: String = "",
    val clinicName: String = "",
    val insuranceNumber: String = "",
    val notes: String? = null,
    val avatarUrl: String? = null
)
