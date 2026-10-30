package com.pequecare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pequecare.data.local.db.PequeCareDatabase
import com.pequecare.data.local.entity.ActivityEntity
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MealEntity
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.SleepEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import com.pequecare.util.WhoGrowthStandards
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PequeCareViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PequeCareDatabase.getDatabase(application)
    private val childProfileDao = db.childProfileDao()
    private val growthRecordDao = db.growthRecordDao()
    private val medicationDao = db.medicationTreatmentDao()
    private val tempDao = db.temperatureRecordDao()
    private val appointmentDao = db.medicalAppointmentDao()
    private val sleepDao = db.sleepDao()
    private val mealDao = db.mealDao()
    private val activityDao = db.activityDao()

    val childProfile: StateFlow<ChildProfileEntity?> = childProfileDao.getChildProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val growthRecords: StateFlow<List<GrowthRecordEntity>> = childProfile.flatMapLatest { profile ->
        if (profile != null) growthRecordDao.getAllGrowthRecords(profile.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestGrowth: StateFlow<GrowthRecordEntity?> = childProfile.flatMapLatest { profile ->
        if (profile != null) growthRecordDao.getLatestGrowthRecord(profile.id)
        else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMedications: StateFlow<List<MedicationTreatmentEntity>> = childProfile.flatMapLatest { profile ->
        if (profile != null) medicationDao.getActiveMedications(profile.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<MedicalAppointmentEntity>> = childProfile.flatMapLatest { profile ->
        if (profile != null) appointmentDao.getAppointments(profile.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val temperatures: StateFlow<List<TemperatureRecordEntity>> = childProfile.flatMapLatest { profile ->
        if (profile != null) tempDao.getTemperatureRecords(profile.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepRecords: StateFlow<List<SleepEntity>> = childProfile.flatMapLatest { profile ->
        if (profile != null) sleepDao.getSleepRecords(profile.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealRecords: StateFlow<List<MealEntity>> = childProfile.flatMapLatest { profile ->
        if (profile != null) mealDao.getMealRecords(profile.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityRecords: StateFlow<List<ActivityEntity>> = childProfile.flatMapLatest { profile ->
        if (profile != null) activityDao.getActivityRecords(profile.id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val prefs = application.getSharedPreferences("pequecare_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkTheme = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun toggleDarkTheme() {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        prefs.edit().putBoolean("is_dark_theme", newTheme).apply()
    }

    fun saveChildProfile(
        name: String,
        lastName: String,
        birthDate: String,
        gender: String,
        bloodType: String = "",
        allergies: String = "",
        pediatricianName: String = "",
        pediatricianPhone: String = "",
        clinicName: String = "",
        notes: String? = null
    ) {
        viewModelScope.launch {
            val current = childProfile.value
            val id = current?.id ?: UUID.randomUUID().toString()
            val profile = ChildProfileEntity(
                id = id,
                name = name.trim(),
                lastName = lastName.trim(),
                birthDate = birthDate.trim(),
                gender = gender.trim(),
                bloodType = bloodType.trim(),
                allergies = allergies.trim(),
                pediatricianName = pediatricianName.trim(),
                pediatricianPhone = pediatricianPhone.trim(),
                clinicName = clinicName.trim(),
                notes = notes?.trim()
            )
            childProfileDao.insertOrUpdate(profile)
        }
    }

    fun addGrowthRecord(
        weightKg: Double,
        heightCm: Double,
        headCircumferenceCm: Double? = null,
        date: String = LocalDate.now().toString(),
        notes: String? = null
    ) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val ageMonths = WhoGrowthStandards.calculateAgeMonths(profile.birthDate, date)
            val eval = WhoGrowthStandards.evaluate(
                gender = profile.gender,
                ageMonths = ageMonths,
                weightKg = weightKg,
                heightCm = heightCm
            )

            val record = GrowthRecordEntity(
                id = UUID.randomUUID().toString(),
                childId = profile.id,
                date = date,
                ageMonths = ageMonths,
                weightKg = weightKg,
                heightCm = heightCm,
                headCircumferenceCm = headCircumferenceCm,
                bmi = eval.bmi,
                weightPercentile = eval.weightPercentile,
                heightPercentile = eval.heightPercentile,
                bmiPercentile = eval.bmiPercentile,
                weightZScore = eval.weightZScore,
                heightZScore = eval.heightZScore,
                bmiZScore = eval.bmiZScore,
                weightStatus = eval.weightStatus,
                heightStatus = eval.heightStatus,
                bmiStatus = eval.bmiStatus,
                notes = notes
            )
            growthRecordDao.insert(record)
        }
    }

    fun updateGrowthRecord(
        record: GrowthRecordEntity,
        weightKg: Double,
        heightCm: Double,
        date: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val ageMonths = WhoGrowthStandards.calculateAgeMonths(profile.birthDate, date)
            val eval = WhoGrowthStandards.evaluate(
                gender = profile.gender,
                ageMonths = ageMonths,
                weightKg = weightKg,
                heightCm = heightCm
            )

            val updated = record.copy(
                date = date,
                ageMonths = ageMonths,
                weightKg = weightKg,
                heightCm = heightCm,
                headCircumferenceCm = null,
                bmi = eval.bmi,
                weightPercentile = eval.weightPercentile,
                heightPercentile = eval.heightPercentile,
                bmiPercentile = eval.bmiPercentile,
                weightZScore = eval.weightZScore,
                heightZScore = eval.heightZScore,
                bmiZScore = eval.bmiZScore,
                weightStatus = eval.weightStatus,
                heightStatus = eval.heightStatus,
                bmiStatus = eval.bmiStatus,
                notes = notes
            )
            growthRecordDao.update(updated)
        }
    }

    fun duplicateGrowthRecord(record: GrowthRecordEntity) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val today = LocalDate.now().toString()
            val ageMonths = WhoGrowthStandards.calculateAgeMonths(profile.birthDate, today)
            val eval = WhoGrowthStandards.evaluate(
                gender = profile.gender,
                ageMonths = ageMonths,
                weightKg = record.weightKg,
                heightCm = record.heightCm
            )

            val duplicated = GrowthRecordEntity(
                id = UUID.randomUUID().toString(),
                childId = profile.id,
                date = today,
                ageMonths = ageMonths,
                weightKg = record.weightKg,
                heightCm = record.heightCm,
                headCircumferenceCm = null,
                bmi = eval.bmi,
                weightPercentile = eval.weightPercentile,
                heightPercentile = eval.heightPercentile,
                bmiPercentile = eval.bmiPercentile,
                weightZScore = eval.weightZScore,
                heightZScore = eval.heightZScore,
                bmiZScore = eval.bmiZScore,
                weightStatus = eval.weightStatus,
                heightStatus = eval.heightStatus,
                bmiStatus = eval.bmiStatus,
                notes = record.notes?.let { "Copia: $it" } ?: "Registro duplicado"
            )
            growthRecordDao.insert(duplicated)
        }
    }

    fun deleteGrowthRecord(record: GrowthRecordEntity) {
        viewModelScope.launch {
            growthRecordDao.delete(record)
        }
    }

    // --- MEDICAMENTOS / TRATAMIENTOS ---
    fun addMedication(
        name: String,
        formula: String,
        dosage: String,
        startDate: String = LocalDate.now().toString(),
        startTime: String = "08:00",
        frequencyHours: Int = 8,
        frequencyMinutes: Int = 0,
        durationDays: Int = 7,
        status: String = "ACTIVO",
        notes: String? = null
    ): String {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val start = try { LocalDate.parse(startDate) } catch (_: Exception) { LocalDate.now() }
            val end = start.plusDays(durationDays.toLong())

            val med = MedicationTreatmentEntity(
                id = newId,
                childId = profile.id,
                name = name.trim(),
                formula = formula.trim(),
                concentration = "",
                dosage = dosage.trim(),
                unit = "ml",
                purpose = "Tratamiento pediátrico",
                frequencyHours = frequencyHours,
                frequencyMinutes = frequencyMinutes,
                startDate = start.toString(),
                startTime = startTime.trim(),
                endDate = end.toString(),
                durationDays = durationDays,
                status = status.trim(),
                isActive = (status.trim().uppercase() == "ACTIVO"),
                notes = notes?.trim()
            )
            medicationDao.insert(med)
        }
        return newId
    }

    fun updateMedication(
        med: MedicationTreatmentEntity,
        name: String,
        formula: String,
        dosage: String,
        startDate: String = med.startDate,
        startTime: String = med.startTime,
        frequencyHours: Int = med.frequencyHours,
        frequencyMinutes: Int = med.frequencyMinutes,
        durationDays: Int = med.durationDays,
        status: String = med.status,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val start = try { LocalDate.parse(startDate) } catch (_: Exception) { LocalDate.now() }
            val end = start.plusDays(durationDays.toLong())
            val updated = med.copy(
                name = name.trim(),
                formula = formula.trim(),
                dosage = dosage.trim(),
                startDate = start.toString(),
                startTime = startTime.trim(),
                endDate = end.toString(),
                frequencyHours = frequencyHours,
                frequencyMinutes = frequencyMinutes,
                durationDays = durationDays,
                status = status.trim(),
                isActive = (status.trim().uppercase() == "ACTIVO"),
                notes = notes?.trim()
            )
            medicationDao.update(updated)
        }
    }

    fun duplicateMedication(med: MedicationTreatmentEntity) {
        viewModelScope.launch {
            val start = LocalDate.now()
            val end = start.plusDays(med.durationDays.toLong())
            val duplicated = med.copy(
                id = UUID.randomUUID().toString(),
                startDate = start.toString(),
                endDate = end.toString(),
                notes = med.notes?.let { "Copia: $it" } ?: "Tratamiento duplicado"
            )
            medicationDao.insert(duplicated)
        }
    }

    fun deleteMedication(med: MedicationTreatmentEntity) {
        viewModelScope.launch {
            medicationDao.delete(med)
        }
    }

    // --- TEMPERATURA / FIEBRE ---
    fun addTemperature(
        celsius: Double,
        symptoms: String,
        medicationGiven: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val temp = TemperatureRecordEntity(
                id = UUID.randomUUID().toString(),
                childId = profile.id,
                date = LocalDate.now().toString(),
                time = nowTime,
                celsius = celsius,
                symptoms = symptoms.trim(),
                medicationGiven = medicationGiven?.trim(),
                notes = notes?.trim()
            )
            tempDao.insert(temp)
        }
    }

    fun updateTemperature(
        record: TemperatureRecordEntity,
        celsius: Double,
        symptoms: String,
        medicationGiven: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val updated = record.copy(
                celsius = celsius,
                symptoms = symptoms.trim(),
                medicationGiven = medicationGiven?.trim(),
                notes = notes?.trim()
            )
            tempDao.update(updated)
        }
    }

    fun duplicateTemperature(record: TemperatureRecordEntity) {
        viewModelScope.launch {
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val duplicated = record.copy(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now().toString(),
                time = nowTime,
                notes = record.notes?.let { "Copia: $it" } ?: "Registro duplicado"
            )
            tempDao.insert(duplicated)
        }
    }

    fun deleteTemperature(record: TemperatureRecordEntity) {
        viewModelScope.launch {
            tempDao.delete(record)
        }
    }

    // --- TURNOS Y CONSULTAS MÉDICAS ---
    fun addAppointment(
        doctorName: String,
        specialty: String,
        clinic: String,
        reason: String,
        date: String,
        time: String,
        status: String = "pendiente",
        notes: String? = null
    ): String {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val appt = MedicalAppointmentEntity(
                id = newId,
                childId = profile.id,
                date = date.trim(),
                time = time.trim(),
                doctorName = doctorName.trim(),
                specialty = specialty.trim(),
                clinic = clinic.trim(),
                reason = reason.trim(),
                status = status.trim(),
                notes = notes?.trim()
            )
            appointmentDao.insert(appt)
        }
        return newId
    }

    fun updateAppointment(
        appointment: MedicalAppointmentEntity,
        doctorName: String,
        specialty: String,
        clinic: String,
        reason: String,
        date: String,
        time: String,
        status: String = appointment.status,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val updated = appointment.copy(
                doctorName = doctorName.trim(),
                specialty = specialty.trim(),
                clinic = clinic.trim(),
                reason = reason.trim(),
                date = date.trim(),
                time = time.trim(),
                status = status.trim(),
                notes = notes?.trim()
            )
            appointmentDao.update(updated)
        }
    }

    fun duplicateAppointment(appointment: MedicalAppointmentEntity) {
        viewModelScope.launch {
            val duplicated = appointment.copy(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now().toString(),
                notes = appointment.notes?.let { "Copia: $it" } ?: "Turno duplicado"
            )
            appointmentDao.insert(duplicated)
        }
    }

    fun deleteAppointment(appointment: MedicalAppointmentEntity) {
        viewModelScope.launch {
            appointmentDao.delete(appointment)
        }
    }

    // --- SUEÑO Y SIESTAS ---
    fun addSleep(
        startTime: String,
        endTime: String,
        type: String, // "siesta" or "noche"
        quality: String,
        date: String = LocalDate.now().toString(),
        notes: String? = null
    ) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val durationMinutes = calculateDuration(startTime, endTime)
            val sleep = SleepEntity(
                id = UUID.randomUUID().toString(),
                childId = profile.id,
                date = date.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                durationMinutes = durationMinutes,
                type = type.trim(),
                quality = quality.trim(),
                notes = notes?.trim()
            )
            sleepDao.insert(sleep)
        }
    }

    fun updateSleep(
        record: SleepEntity,
        startTime: String,
        endTime: String,
        type: String,
        quality: String,
        date: String = record.date,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val durationMinutes = calculateDuration(startTime, endTime)
            val updated = record.copy(
                date = date.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                durationMinutes = durationMinutes,
                type = type.trim(),
                quality = quality.trim(),
                notes = notes?.trim()
            )
            sleepDao.update(updated)
        }
    }

    fun duplicateSleep(record: SleepEntity) {
        viewModelScope.launch {
            val duplicated = record.copy(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now().toString(),
                notes = record.notes?.let { "Copia: $it" } ?: "Registro duplicado"
            )
            sleepDao.insert(duplicated)
        }
    }

    fun deleteSleep(record: SleepEntity) {
        viewModelScope.launch {
            sleepDao.delete(record)
        }
    }

    // --- ALIMENTACIÓN / COMIDAS ---
    fun addMeal(
        mealType: String,
        foods: String,
        appetite: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val meal = MealEntity(
                id = UUID.randomUUID().toString(),
                childId = profile.id,
                date = LocalDate.now().toString(),
                time = nowTime,
                mealType = mealType,
                foods = foods.trim(),
                waterGlasses = 0,
                appetite = appetite,
                notes = notes
            )
            mealDao.insert(meal)
        }
    }

    fun updateMeal(
        record: MealEntity,
        mealType: String,
        foods: String,
        appetite: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val updated = record.copy(
                mealType = mealType,
                foods = foods.trim(),
                appetite = appetite,
                notes = notes
            )
            mealDao.update(updated)
        }
    }

    fun duplicateMeal(record: MealEntity) {
        viewModelScope.launch {
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val duplicated = record.copy(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now().toString(),
                time = nowTime,
                notes = record.notes?.let { "Copia: $it" } ?: "Registro duplicado"
            )
            mealDao.insert(duplicated)
        }
    }

    fun deleteMeal(record: MealEntity) {
        viewModelScope.launch {
            mealDao.delete(record)
        }
    }

    // --- ACTIVIDADES Y DESARROLLO ---
    fun addActivity(
        title: String,
        category: String,
        durationMinutes: Int,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val act = ActivityEntity(
                id = UUID.randomUUID().toString(),
                childId = profile.id,
                date = LocalDate.now().toString(),
                time = nowTime,
                title = title.trim(),
                category = category,
                durationMinutes = durationMinutes,
                notes = notes
            )
            activityDao.insert(act)
        }
    }

    fun updateActivity(
        record: ActivityEntity,
        title: String,
        category: String,
        durationMinutes: Int,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val updated = record.copy(
                title = title.trim(),
                category = category,
                durationMinutes = durationMinutes,
                notes = notes
            )
            activityDao.update(updated)
        }
    }

    fun duplicateActivity(record: ActivityEntity) {
        viewModelScope.launch {
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val duplicated = record.copy(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now().toString(),
                time = nowTime,
                notes = record.notes?.let { "Copia: $it" } ?: "Registro duplicado"
            )
            activityDao.insert(duplicated)
        }
    }

    fun deleteActivity(record: ActivityEntity) {
        viewModelScope.launch {
            activityDao.delete(record)
        }
    }

    private fun calculateDuration(start: String, end: String): Int {
        return try {
            val t1 = LocalTime.parse(start)
            val t2 = LocalTime.parse(end)
            var min = Duration.between(t1, t2).toMinutes().toInt()
            if (min < 0) min += 24 * 60
            min
        } catch (e: Exception) {
            60
        }
    }
}
