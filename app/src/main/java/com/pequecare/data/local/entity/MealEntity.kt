package com.pequecare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_records",
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
data class MealEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val date: String, // Formato: YYYY-MM-DD
    val time: String, // Formato: HH:mm
    val mealType: String, // "desayuno" | "media mañana" | "almuerzo" | "merienda" | "cena" | "colación"
    val foods: String, // Lista de alimentos separada por comas: "Leche, Avena, Banana"
    val waterGlasses: Int = 0, // Vasitos de agua de 200 ml
    val appetite: String, // "excelente" | "bueno" | "regular" | "poco" | "rechazó"
    val notes: String? = null
)
