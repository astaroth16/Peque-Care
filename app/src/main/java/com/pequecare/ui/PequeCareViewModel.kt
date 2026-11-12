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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PequeCareViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private const val KEY_AR_STANDARDS_MIGRATED = "ar_standards_v1_applied"
    }

    private val db = PequeCareDatabase.getDatabase(application)
    private val childProfileDao = db.childProfileDao()
    private val growthRecordDao = db.growthRecordDao()
    private val medicationDao = db.medicationTreatmentDao()
    private val doseLogDao = db.doseLogDao()
    private val tempDao = db.temperatureRecordDao()
    private val appointmentDao = db.medicalAppointmentDao()
    private val sleepDao = db.sleepDao()
    private val mealDao = db.mealDao()
    private val activityDao = db.activityDao()

    private val prefs = application.getSharedPreferences("pequecare_prefs", android.content.Context.MODE_PRIVATE)

    val allProfiles: StateFlow<List<ChildProfileEntity>> = childProfileDao.getAllChildProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChildId = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("selected_child_id", null))
    val selectedChildId: StateFlow<String?> = _selectedChildId

    val childProfile: StateFlow<ChildProfileEntity?> = kotlinx.coroutines.flow.combine(allProfiles, _selectedChildId) { profiles, selId ->
        if (profiles.isEmpty()) null
        else {
            val found = profiles.find { it.id == selId }
            if (found != null) {
                found
            } else {
                val first = profiles.first()
                _selectedChildId.value = first.id
                prefs.edit().putString("selected_child_id", first.id).apply()
                first
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    init {
        recalculateGrowthRecordsWithArgentinaStandards()
    }

    private val _isDarkTheme = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    // Estado de sesión y bienvenida (Onboarding)
    // "UNSET": usuario nuevo que aún no ha elegido modo
    // "GUEST": modo invitado sin cuenta vinculada
    // "GOOGLE": usuario autenticado con Google
    private val _authMode = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("auth_mode", "UNSET") ?: "UNSET")
    val authMode: StateFlow<String> = _authMode

    private val _googleUserEmail = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("google_user_email", null))
    val googleUserEmail: StateFlow<String?> = _googleUserEmail

    private val _googleUserName = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("google_user_name", null))
    val googleUserName: StateFlow<String?> = _googleUserName

    // Configuración de frecuencia de respaldo automático ("daily", "weekly", "monthly", "none")
    private val _backupFrequency = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("backup_frequency", "daily") ?: "daily")
    val backupFrequency: StateFlow<String> = _backupFrequency

    private val _lastBackupDate = kotlinx.coroutines.flow.MutableStateFlow(prefs.getString("last_drive_backup_date", null))
    val lastBackupDate: StateFlow<String?> = _lastBackupDate

    private val _backupStateMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val backupStateMessage: StateFlow<String?> = _backupStateMessage

    private val _isBackupLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isBackupLoading: StateFlow<Boolean> = _isBackupLoading

    fun setAuthModeGuest() {
        _authMode.value = "GUEST"
        prefs.edit().putString("auth_mode", "GUEST").apply()
    }

    fun setAuthModeGoogle(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount) {
        _authMode.value = "GOOGLE"
        _googleUserEmail.value = account.email
        _googleUserName.value = account.displayName
        prefs.edit()
            .putString("auth_mode", "GOOGLE")
            .putString("google_user_email", account.email)
            .putString("google_user_name", account.displayName)
            .apply()

        // Programar backup automático de acuerdo a la frecuencia
        com.pequecare.util.BackupWorker.schedulePeriodicBackup(getApplication(), _backupFrequency.value)
    }

    fun logoutGoogle() {
        _authMode.value = "GUEST"
        _googleUserEmail.value = null
        _googleUserName.value = null
        prefs.edit()
            .putString("auth_mode", "GUEST")
            .remove("google_user_email")
            .remove("google_user_name")
            .apply()
        com.pequecare.util.BackupWorker.cancelPeriodicBackup(getApplication())
    }

    fun updateBackupFrequency(newFreq: String) {
        _backupFrequency.value = newFreq
        prefs.edit().putString("backup_frequency", newFreq).apply()
        if (_authMode.value == "GOOGLE") {
            com.pequecare.util.BackupWorker.schedulePeriodicBackup(getApplication(), newFreq)
        }
    }

    fun backupNowToGoogleDrive(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            _backupStateMessage.value = "Subiendo respaldo a Google Drive..."
            val repo = com.pequecare.data.remote.google.GoogleDriveBackupRepository(getApplication())
            val activeDb = com.pequecare.data.local.db.PequeCareDatabase.getDatabase(getApplication())
            val res = repo.uploadDatabaseBackup(account, activeDb)
            _isBackupLoading.value = false
            if (res.isSuccess) {
                val nowStr = java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER)
                _lastBackupDate.value = nowStr
                prefs.edit().putString("last_drive_backup_date", nowStr).apply()
                _backupStateMessage.value = "Copia familiar guardada con éxito"
            } else {
                _backupStateMessage.value = "Error al subir respaldo: ${res.exceptionOrNull()?.localizedMessage ?: "Desconocido"}"
            }
        }
    }

    fun backupChildToGoogleDrive(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        child: ChildProfileEntity
    ) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            _backupStateMessage.value = "Respaldando ficha de ${child.name} en Google Drive..."
            val repo = com.pequecare.data.remote.google.GoogleDriveBackupRepository(getApplication())
            val activeDb = com.pequecare.data.local.db.PequeCareDatabase.getDatabase(getApplication())
            val res = repo.uploadChildBackup(account, activeDb, child)
            _isBackupLoading.value = false
            if (res.isSuccess) {
                val nowStr = java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER)
                _lastBackupDate.value = nowStr
                prefs.edit().putString("last_drive_backup_date", nowStr).apply()
                _backupStateMessage.value = "Ficha de ${child.name} respaldada con éxito"
            } else {
                _backupStateMessage.value = "Error al subir ficha: ${res.exceptionOrNull()?.localizedMessage ?: "Desconocido"}"
            }
        }
    }

    fun listDriveBackups(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        onResult: (List<com.pequecare.data.remote.google.DriveBackupItem>) -> Unit
    ) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val repo = com.pequecare.data.remote.google.GoogleDriveBackupRepository(getApplication())
            val res = repo.listAvailableBackups(account)
            _isBackupLoading.value = false
            if (res.isSuccess) {
                onResult(res.getOrDefault(emptyList()))
            } else {
                val err = res.exceptionOrNull()?.localizedMessage ?: "Error al conectar con Drive"
                _backupStateMessage.value = err
                onResult(emptyList())
            }
        }
    }

    fun restoreDriveBackupItem(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        item: com.pequecare.data.remote.google.DriveBackupItem,
        onCompleted: (Boolean, String, Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val repo = com.pequecare.data.remote.google.GoogleDriveBackupRepository(getApplication())
            val activeDb = com.pequecare.data.local.db.PequeCareDatabase.getDatabase(getApplication())

            if (item.isFullBackup) {
                _backupStateMessage.value = "Descargando base de datos familiar..."
                val res = repo.downloadDatabaseBackup(account, activeDb, item.fileId)
                _isBackupLoading.value = false
                if (res.isSuccess) {
                    val restoredProfiles = res.getOrDefault(emptyList())
                    if (restoredProfiles.isNotEmpty()) {
                        val firstChildId = restoredProfiles.first().id
                        selectChild(firstChildId)
                    }
                    val count = restoredProfiles.size
                    val msg = "Base familiar restaurada ($count ${if (count == 1) "peque" else "peques"})"
                    _backupStateMessage.value = msg
                    onCompleted(true, msg, true)
                } else {
                    val err = res.exceptionOrNull()?.localizedMessage ?: "Error al restaurar"
                    _backupStateMessage.value = err
                    onCompleted(false, err, true)
                }
            } else {
                _backupStateMessage.value = "Descargando ficha de ${item.childName ?: "niño"}..."
                val res = repo.downloadChildBackup(account, item.fileId, activeDb)
                _isBackupLoading.value = false
                if (res.isSuccess) {
                    val restoredChild = res.getOrNull()
                    if (restoredChild != null) {
                        selectChild(restoredChild.id)
                    }
                    val msg = "Ficha de ${restoredChild?.name ?: "niño"} restaurada con éxito"
                    _backupStateMessage.value = msg
                    onCompleted(true, msg, false)
                } else {
                    val err = res.exceptionOrNull()?.localizedMessage ?: "Error al restaurar ficha"
                    _backupStateMessage.value = err
                    onCompleted(false, err, false)
                }
            }
        }
    }

    fun restoreFromGoogleDrive(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount, onCompleted: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            _backupStateMessage.value = "Descargando respaldo desde Google Drive..."
            val repo = com.pequecare.data.remote.google.GoogleDriveBackupRepository(getApplication())
            val activeDb = com.pequecare.data.local.db.PequeCareDatabase.getDatabase(getApplication())
            val res = repo.downloadDatabaseBackup(account, activeDb)
            _isBackupLoading.value = false
            if (res.isSuccess) {
                val restoredProfiles = res.getOrDefault(emptyList())
                if (restoredProfiles.isNotEmpty()) {
                    selectChild(restoredProfiles.first().id)
                }
                _backupStateMessage.value = "Restauración completada con éxito"
                onCompleted(true, "Datos familiares restaurados con éxito")
            } else {
                val err = res.exceptionOrNull()?.localizedMessage ?: "No se encontró respaldo"
                _backupStateMessage.value = "Error en restauración: $err"
                onCompleted(false, err)
            }
        }
    }

    fun clearBackupMessage() {
        _backupStateMessage.value = null
    }

    fun toggleDarkTheme() {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        prefs.edit().putBoolean("is_dark_theme", newTheme).apply()
    }

    fun selectChild(childId: String) {
        _selectedChildId.value = childId
        prefs.edit().putString("selected_child_id", childId).apply()
    }

    fun cloneChildProfile(source: ChildProfileEntity) {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val cloned = source.copy(
                id = newId,
                name = "${source.name} (Gemelo/a)",
                notes = "Perfil clonado de ${source.name}"
            )
            childProfileDao.insertOrUpdate(cloned)
            selectChild(newId)
        }
    }

    fun deleteChildProfile(profile: ChildProfileEntity) {
        viewModelScope.launch {
            // Cancelar alarmas programadas de tratamientos y citas de este peque
            try {
                val meds = medicationDao.getAllMedicationsOnce(profile.id)
                meds.forEach { med ->
                    com.pequecare.util.NotificationHelper.cancelReminder(getApplication(), med.id)
                }
                val appts = appointmentDao.getAppointmentsOnce(profile.id)
                appts.forEach { appt ->
                    com.pequecare.util.NotificationHelper.cancelReminder(getApplication(), appt.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Eliminar registros de este niño en todas las tablas
            try {
                growthRecordDao.deleteByChildId(profile.id)
                val meds = medicationDao.getAllMedicationsOnce(profile.id)
                if (meds.isNotEmpty()) {
                    doseLogDao.deleteByTreatmentIds(meds.map { it.id })
                }
                medicationDao.deleteByChildId(profile.id)
                appointmentDao.deleteByChildId(profile.id)
                tempDao.deleteByChildId(profile.id)
                sleepDao.deleteByChildId(profile.id)
                mealDao.deleteByChildId(profile.id)
                activityDao.deleteByChildId(profile.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Eliminar el perfil del niño
            childProfileDao.delete(profile)

            val remaining = childProfileDao.getAllChildProfilesOnce().filter { it.id != profile.id }
            val nextId = remaining.firstOrNull()?.id
            _selectedChildId.value = nextId
            if (nextId != null) {
                prefs.edit().putString("selected_child_id", nextId).apply()
            } else {
                prefs.edit().remove("selected_child_id").apply()
                // Si ya no quedan niños registrados, purgar por completo la base de datos
                val db = com.pequecare.data.local.db.PequeCareDatabase.getDatabase(getApplication())
                db.clearAllTables()
            }
        }
    }

    fun saveChildProfile(
        childId: String? = null,
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
            val isEditing = childId != null
            val existing = if (isEditing) childProfileDao.getChildProfileByIdOnce(childId!!) else null
            val id = childId ?: UUID.randomUUID().toString()
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
                insuranceNumber = existing?.insuranceNumber ?: "",
                notes = notes?.trim() ?: existing?.notes,
                avatarUrl = existing?.avatarUrl
            )
            childProfileDao.insertOrUpdate(profile)
            selectChild(id)
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
                heightCm = heightCm,
                headCircumferenceCm = headCircumferenceCm
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
        notes: String?,
        headCircumferenceCm: Double? = record.headCircumferenceCm
    ) {
        viewModelScope.launch {
            val profile = childProfile.value ?: return@launch
            val ageMonths = WhoGrowthStandards.calculateAgeMonths(profile.birthDate, date)
            val eval = WhoGrowthStandards.evaluate(
                gender = profile.gender,
                ageMonths = ageMonths,
                weightKg = weightKg,
                heightCm = heightCm,
                headCircumferenceCm = headCircumferenceCm
            )

            val updated = record.copy(
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
                heightCm = record.heightCm,
                headCircumferenceCm = record.headCircumferenceCm
            )

            val duplicated = GrowthRecordEntity(
                id = UUID.randomUUID().toString(),
                childId = profile.id,
                date = today,
                ageMonths = ageMonths,
                weightKg = record.weightKg,
                heightCm = record.heightCm,
                headCircumferenceCm = record.headCircumferenceCm,
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

    private fun recalculateGrowthRecordsWithArgentinaStandards(force: Boolean = false) {
        if (!force && prefs.getBoolean(KEY_AR_STANDARDS_MIGRATED, false)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profiles = childProfileDao.getAllChildProfilesOnce().associateBy { it.id }
                val allGrowth = growthRecordDao.getAllRecordsOnce()
                var updatedCount = 0
                var deletedCount = 0

                for (record in allGrowth) {
                    val profile = profiles[record.childId]
                    // Si el niño no existe o los datos son inválidos/inconsistentes (< 0 o nulos), eliminar por conflicto
                    if (profile == null || record.weightKg <= 0.0 || record.heightCm <= 0.0) {
                        growthRecordDao.delete(record)
                        deletedCount++
                        continue
                    }

                    val ageMonths = WhoGrowthStandards.calculateAgeMonths(profile.birthDate, record.date)
                    val eval = WhoGrowthStandards.evaluate(
                        gender = profile.gender,
                        ageMonths = ageMonths,
                        weightKg = record.weightKg,
                        heightCm = record.heightCm,
                        headCircumferenceCm = record.headCircumferenceCm
                    )

                    // Verificar si hubo cambios en percentiles, z-scores o clasificaciones de Argentina
                    if (record.weightStatus != eval.weightStatus ||
                        record.heightStatus != eval.heightStatus ||
                        record.bmiStatus != eval.bmiStatus ||
                        record.weightPercentile != eval.weightPercentile ||
                        record.heightPercentile != eval.heightPercentile ||
                        record.bmiPercentile != eval.bmiPercentile ||
                        record.bmi != eval.bmi ||
                        record.ageMonths != ageMonths
                    ) {
                        val updated = record.copy(
                            ageMonths = ageMonths,
                            bmi = eval.bmi,
                            weightPercentile = eval.weightPercentile,
                            heightPercentile = eval.heightPercentile,
                            bmiPercentile = eval.bmiPercentile,
                            weightZScore = eval.weightZScore,
                            heightZScore = eval.heightZScore,
                            bmiZScore = eval.bmiZScore,
                            weightStatus = eval.weightStatus,
                            heightStatus = eval.heightStatus,
                            bmiStatus = eval.bmiStatus
                        )
                        growthRecordDao.update(updated)
                        updatedCount++
                    }
                }
                prefs.edit().putBoolean(KEY_AR_STANDARDS_MIGRATED, true).apply()
                android.util.Log.d("PequeCare", "Estandares oficiales de Argentina aplicados. Actualizados: $updatedCount, Eliminados por conflicto: $deletedCount")
            } catch (e: Exception) {
                android.util.Log.e("PequeCare", "Error recalculando registros de crecimiento con estándares de Argentina", e)
            }
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
            com.pequecare.util.NotificationHelper.cancelReminder(getApplication(), med.id)
        }
    }

    // --- TEMPERATURA / FIEBRE ---
    fun addTemperature(
        celsius: Double,
        symptoms: String,
        medicationGiven: String? = null,
        notes: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = childProfile.value ?: return@launch
            val nowTime = LocalTime.now().format(TIME_FORMATTER)
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
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            val nowTime = LocalTime.now().format(TIME_FORMATTER)
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
            com.pequecare.util.NotificationHelper.cancelReminder(getApplication(), appointment.id)
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
        viewModelScope.launch(Dispatchers.IO) {
            val profile = childProfile.value ?: return@launch
            val nowTime = LocalTime.now().format(TIME_FORMATTER)
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
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            val nowTime = LocalTime.now().format(TIME_FORMATTER)
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
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            val profile = childProfile.value ?: return@launch
            val nowTime = LocalTime.now().format(TIME_FORMATTER)
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
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            val nowTime = LocalTime.now().format(TIME_FORMATTER)
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
