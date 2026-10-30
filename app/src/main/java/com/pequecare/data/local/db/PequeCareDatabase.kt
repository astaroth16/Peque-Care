package com.pequecare.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pequecare.data.local.dao.ActivityDao
import com.pequecare.data.local.dao.ChildProfileDao
import com.pequecare.data.local.dao.DoseLogDao
import com.pequecare.data.local.dao.GrowthRecordDao
import com.pequecare.data.local.dao.MealDao
import com.pequecare.data.local.dao.MedicalAppointmentDao
import com.pequecare.data.local.dao.MedicationTreatmentDao
import com.pequecare.data.local.dao.SleepDao
import com.pequecare.data.local.dao.TemperatureRecordDao
import com.pequecare.data.local.entity.ActivityEntity
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.DoseLogEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MealEntity
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.SleepEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ChildProfileEntity::class,
        GrowthRecordEntity::class,
        MedicationTreatmentEntity::class,
        DoseLogEntity::class,
        TemperatureRecordEntity::class,
        MedicalAppointmentEntity::class,
        SleepEntity::class,
        MealEntity::class,
        ActivityEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PequeCareDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun medicationTreatmentDao(): MedicationTreatmentDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun temperatureRecordDao(): TemperatureRecordDao
    abstract fun medicalAppointmentDao(): MedicalAppointmentDao
    abstract fun sleepDao(): SleepDao
    abstract fun mealDao(): MealDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: PequeCareDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medication_treatments ADD COLUMN frequencyMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medication_treatments ADD COLUMN startTime TEXT NOT NULL DEFAULT '08:00'")
            }
        }

        fun getDatabase(context: Context): PequeCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PequeCareDatabase::class.java,
                    "pequecare_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
