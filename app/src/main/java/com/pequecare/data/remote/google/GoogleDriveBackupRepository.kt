package com.pequecare.data.remote.google

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.withTransaction
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import com.pequecare.data.local.db.PequeCareDatabase
import com.pequecare.data.local.entity.ActivityEntity
import com.pequecare.data.local.entity.ChildProfileEntity
import com.pequecare.data.local.entity.DoseLogEntity
import com.pequecare.data.local.entity.GrowthRecordEntity
import com.pequecare.data.local.entity.MealEntity
import com.pequecare.data.local.entity.MedicalAppointmentEntity
import com.pequecare.data.local.entity.MedicationTreatmentEntity
import com.pequecare.data.local.entity.SleepEntity
import com.pequecare.data.local.entity.TemperatureRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections

/**
 * Representa una copia individual completa de la ficha médica de un niño/a.
 */
data class ChildBackupPackage(
    val version: Int = 1,
    val backupDate: String? = null,
    val profile: ChildProfileEntity? = null,
    val growthRecords: List<GrowthRecordEntity>? = null,
    val medications: List<MedicationTreatmentEntity>? = null,
    val doseLogs: List<DoseLogEntity>? = null,
    val appointments: List<MedicalAppointmentEntity>? = null,
    val temperatures: List<TemperatureRecordEntity>? = null,
    val sleepRecords: List<SleepEntity>? = null,
    val mealRecords: List<MealEntity>? = null,
    val activityRecords: List<ActivityEntity>? = null
)

/**
 * Representa una copia completa de toda la familia y todos los registros médicos.
 */
data class FamilyBackupPackage(
    val version: Int = 1,
    val backupDate: String? = null,
    val profiles: List<ChildProfileEntity>? = null,
    val growthRecords: List<GrowthRecordEntity>? = null,
    val medications: List<MedicationTreatmentEntity>? = null,
    val doseLogs: List<DoseLogEntity>? = null,
    val appointments: List<MedicalAppointmentEntity>? = null,
    val temperatures: List<TemperatureRecordEntity>? = null,
    val sleepRecords: List<SleepEntity>? = null,
    val mealRecords: List<MealEntity>? = null,
    val activityRecords: List<ActivityEntity>? = null
)

/**
 * Elemento de respaldo detectado en Google Drive.
 */
data class DriveBackupItem(
    val fileId: String,
    val title: String,
    val subtitle: String,
    val date: String,
    val isFullBackup: Boolean,
    val childId: String? = null,
    val childName: String? = null
)

class GoogleDriveBackupRepository(private val context: Context) {

    companion object {
        private const val FAMILY_BACKUP_FILE_NAME = "pequecare_family_backup.json"
        private const val LEGACY_BACKUP_FILE_NAME = "pequecare_backup.db"
        private const val MIME_TYPE_JSON = "application/json"
        private const val APP_DATA_FOLDER = "appDataFolder"
    }

    /**
     * Inicializa el cliente de Google Drive REST API utilizando la cuenta de Google Sign-In.
     */
    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("PequeCare").build()
    }

    /**
     * Lista todas las copias de seguridad disponibles en la carpeta AppData de Google Drive.
     */
    suspend fun listAvailableBackups(
        account: GoogleSignInAccount
    ): Result<List<DriveBackupItem>> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val result = driveService.files().list()
                .setSpaces(APP_DATA_FOLDER)
                .setQ("'$APP_DATA_FOLDER' in parents and trashed = false")
                .setFields("files(id, name, description, modifiedTime, size)")
                .execute()

            val items = mutableListOf<DriveBackupItem>()
            result.files?.forEach { file ->
                val name = file.name ?: ""
                val modDate = file.modifiedTime?.toStringRfc3339()?.take(10) ?: "Fecha desconocida"
                if (name == FAMILY_BACKUP_FILE_NAME || name == LEGACY_BACKUP_FILE_NAME || name.startsWith("pequecare_family") || name.startsWith("pequecare_backup")) {
                    items.add(
                        DriveBackupItem(
                            fileId = file.id,
                            title = "📦 Copia Completa Familiar",
                            subtitle = file.description ?: "Base de datos familiar ($modDate)",
                            date = modDate,
                            isFullBackup = true
                        )
                    )
                } else if (name.startsWith("pequecare_child_")) {
                    val childName = file.description?.substringBefore(" •") ?: "Niño/a"
                    items.add(
                        DriveBackupItem(
                            fileId = file.id,
                            title = "🧒 Ficha Individual: $childName",
                            subtitle = file.description ?: "Ficha médica individual ($modDate)",
                            date = modDate,
                            isFullBackup = false,
                            childName = childName
                        )
                    )
                }
            }
            Result.success(items)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error listando respaldos en Drive", e)
            Result.failure(e)
        }
    }

    /**
     * Sube una copia familiar completa en formato JSON a Google Drive.
     */
    suspend fun uploadDatabaseBackup(
        account: GoogleSignInAccount,
        roomDatabase: PequeCareDatabase
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val profiles = roomDatabase.childProfileDao().getAllChildProfilesOnce()
            val growth = roomDatabase.growthRecordDao().getAllRecordsOnce()
            val meds = roomDatabase.medicationTreatmentDao().getAllRecordsOnce()
            val doseLogs = roomDatabase.doseLogDao().getAllRecordsOnce()
            val appointments = roomDatabase.medicalAppointmentDao().getAllRecordsOnce()
            val temps = roomDatabase.temperatureRecordDao().getAllRecordsOnce()
            val sleeps = roomDatabase.sleepDao().getAllRecordsOnce()
            val meals = roomDatabase.mealDao().getAllRecordsOnce()
            val activities = roomDatabase.activityDao().getAllRecordsOnce()

            val nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val backupPackage = FamilyBackupPackage(
                version = 1,
                backupDate = nowStr,
                profiles = profiles,
                growthRecords = growth,
                medications = meds,
                doseLogs = doseLogs,
                appointments = appointments,
                temperatures = temps,
                sleepRecords = sleeps,
                mealRecords = meals,
                activityRecords = activities
            )

            val jsonString = Gson().toJson(backupPackage)
            val tempFile = File(context.cacheDir, "family_backup_upload.json")
            tempFile.writeText(jsonString)

            val driveService = getDriveService(account)
            val existingFileId = findFamilyBackupFileId(driveService) ?: findLegacyBackupFileId(driveService)
            val mediaContent = FileContent(MIME_TYPE_JSON, tempFile)
            val desc = "Copia familiar completa (${profiles.size} peques) • $nowStr"

            val resultId = if (existingFileId != null) {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = FAMILY_BACKUP_FILE_NAME
                    description = desc
                }
                val updatedFile = driveService.files().update(existingFileId, fileMetadata, mediaContent).execute()
                updatedFile.id
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = FAMILY_BACKUP_FILE_NAME
                    description = desc
                    parents = Collections.singletonList(APP_DATA_FOLDER)
                }
                val createdFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                createdFile.id
            }

            tempFile.delete()
            android.util.Log.d("DriveBackup", "Respaldo familiar subido exitosamente. File ID: $resultId")
            Result.success(resultId)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error al subir respaldo familiar a Google Drive", e)
            Result.failure(e)
        }
    }

    /**
     * Sube una copia individual de la ficha completa de un niño en formato JSON a Google Drive.
     */
    suspend fun uploadChildBackup(
        account: GoogleSignInAccount,
        roomDatabase: PequeCareDatabase,
        child: ChildProfileEntity
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val growth = roomDatabase.growthRecordDao().getAllGrowthRecordsOnce(child.id)
            val meds = roomDatabase.medicationTreatmentDao().getAllMedicationsOnce(child.id)
            val doseLogs = roomDatabase.doseLogDao().getDoseLogsForTreatmentsOnce(meds.map { it.id })
            val appointments = roomDatabase.medicalAppointmentDao().getAppointmentsOnce(child.id)
            val temps = roomDatabase.temperatureRecordDao().getTemperatureRecordsOnce(child.id)
            val sleeps = roomDatabase.sleepDao().getSleepRecordsOnce(child.id)
            val meals = roomDatabase.mealDao().getMealRecordsOnce(child.id)
            val activities = roomDatabase.activityDao().getActivityRecordsOnce(child.id)

            val nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val backupPackage = ChildBackupPackage(
                version = 1,
                backupDate = nowStr,
                profile = child,
                growthRecords = growth,
                medications = meds,
                doseLogs = doseLogs,
                appointments = appointments,
                temperatures = temps,
                sleepRecords = sleeps,
                mealRecords = meals,
                activityRecords = activities
            )

            val jsonString = Gson().toJson(backupPackage)
            val tempFile = File(context.cacheDir, "child_backup_${child.id}.json")
            tempFile.writeText(jsonString)

            val driveService = getDriveService(account)
            val fileName = "pequecare_child_${child.id}.json"
            val existingFileId = findFileIdByName(driveService, fileName)

            val mediaContent = FileContent(MIME_TYPE_JSON, tempFile)
            val desc = "${child.name} ${child.lastName}".trim() + " • $nowStr"

            val resultId = if (existingFileId != null) {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    description = desc
                }
                val updated = driveService.files().update(existingFileId, fileMetadata, mediaContent).execute()
                updated.id
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = fileName
                    description = desc
                    parents = Collections.singletonList(APP_DATA_FOLDER)
                }
                val created = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                created.id
            }

            tempFile.delete()
            android.util.Log.d("DriveBackup", "Ficha de ${child.name} subida a Drive. File ID: $resultId")
            Result.success(resultId)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error al respaldar niño individual", e)
            Result.failure(e)
        }
    }

    /**
     * Descarga e importa la copia de seguridad completa familiar desde Google Drive usando Room.
     * Es compatible tanto con el nuevo formato JSON como con archivos SQLite (.db) de versiones previas.
     */
    suspend fun downloadDatabaseBackup(
        account: GoogleSignInAccount,
        roomDatabase: PequeCareDatabase,
        targetFileId: String? = null
    ): Result<List<ChildProfileEntity>> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)

            val fileId = targetFileId ?: findFamilyBackupFileId(driveService) ?: findLegacyBackupFileId(driveService)
                ?: return@withContext Result.failure(Exception("No se encontró ningún respaldo en Google Drive."))

            val tempDownloadFile = File(context.cacheDir, "pequecare_download.tmp")
            FileOutputStream(tempDownloadFile).use { outputStream ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }

            if (!tempDownloadFile.exists() || tempDownloadFile.length() == 0L) {
                tempDownloadFile.delete()
                return@withContext Result.failure(Exception("El archivo descargado de Google Drive está vacío o dañado."))
            }

            // Detectar si el archivo es un archivo binario SQLite (header "SQLite format 3")
            val headerBytes = ByteArray(16)
            val isSqlite = try {
                FileInputStream(tempDownloadFile).use { it.read(headerBytes) }
                String(headerBytes, Charsets.US_ASCII).startsWith("SQLite format 3")
            } catch (e: Exception) {
                false
            }

            val result = if (isSqlite) {
                android.util.Log.d("DriveBackup", "Detectado respaldo en formato binario SQLite legacy.")
                restoreFromSqliteDatabase(tempDownloadFile, roomDatabase)
            } else {
                android.util.Log.d("DriveBackup", "Detectado respaldo en formato JSON.")
                val jsonContent = tempDownloadFile.readText(Charsets.UTF_8)
                restoreFromJsonContent(jsonContent, roomDatabase)
            }

            tempDownloadFile.delete()
            result
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error al restaurar respaldo de Google Drive", e)
            Result.failure(e)
        }
    }

    /**
     * Descarga e importa una ficha individual de un niño desde Google Drive sin borrar los datos existentes.
     */
    suspend fun downloadChildBackup(
        account: GoogleSignInAccount,
        fileId: String,
        roomDatabase: PequeCareDatabase
    ): Result<ChildProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val tempFile = File(context.cacheDir, "downloaded_child.json")
            FileOutputStream(tempFile).use { os ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(os)
            }

            val jsonString = tempFile.readText(Charsets.UTF_8)
            tempFile.delete()

            val pkg = Gson().fromJson(jsonString, ChildBackupPackage::class.java)
                ?: return@withContext Result.failure(Exception("Archivo de respaldo de niño corrupto o no válido."))

            val child = pkg.profile
                ?: return@withContext Result.failure(Exception("El archivo no contiene un perfil de niño válido."))

            val growth = pkg.growthRecords ?: emptyList()
            val meds = pkg.medications ?: emptyList()
            val doseLogs = pkg.doseLogs ?: emptyList()
            val appointments = pkg.appointments ?: emptyList()
            val temps = pkg.temperatures ?: emptyList()
            val sleeps = pkg.sleepRecords ?: emptyList()
            val meals = pkg.mealRecords ?: emptyList()
            val activities = pkg.activityRecords ?: emptyList()

            roomDatabase.withTransaction {
                roomDatabase.childProfileDao().insertOrUpdate(child)
                if (growth.isNotEmpty()) roomDatabase.growthRecordDao().insertAll(growth)
                if (meds.isNotEmpty()) roomDatabase.medicationTreatmentDao().insertAll(meds)
                if (doseLogs.isNotEmpty()) roomDatabase.doseLogDao().insertAll(doseLogs)
                if (appointments.isNotEmpty()) roomDatabase.medicalAppointmentDao().insertAll(appointments)
                if (temps.isNotEmpty()) roomDatabase.temperatureRecordDao().insertAll(temps)
                if (sleeps.isNotEmpty()) roomDatabase.sleepDao().insertAll(sleeps)
                if (meals.isNotEmpty()) roomDatabase.mealDao().insertAll(meals)
                if (activities.isNotEmpty()) roomDatabase.activityDao().insertAll(activities)
            }

            android.util.Log.d("DriveBackup", "Ficha individual de ${child.name} restaurada con éxito.")
            Result.success(child)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error restaurando ficha individual de niño", e)
            Result.failure(e)
        }
    }

    private suspend fun restoreFromJsonContent(
        content: String,
        roomDatabase: PequeCareDatabase
    ): Result<List<ChildProfileEntity>> {
        val trimmed = content.trim()
        if (!trimmed.startsWith("{")) {
            return Result.failure(Exception("El archivo descargado no tiene estructura JSON válida."))
        }

        // Intento 1: Paquete familiar completo
        val familyPkg = try {
            Gson().fromJson(trimmed, FamilyBackupPackage::class.java)
        } catch (e: Exception) {
            android.util.Log.w("DriveBackup", "Fallo al deserializar FamilyBackupPackage", e)
            null
        }

        if (familyPkg != null && (familyPkg.profiles != null || familyPkg.growthRecords != null || familyPkg.appointments != null)) {
            val profiles = familyPkg.profiles ?: emptyList()
            val growth = familyPkg.growthRecords ?: emptyList()
            val meds = familyPkg.medications ?: emptyList()
            val doseLogs = familyPkg.doseLogs ?: emptyList()
            val appts = familyPkg.appointments ?: emptyList()
            val temps = familyPkg.temperatures ?: emptyList()
            val sleeps = familyPkg.sleepRecords ?: emptyList()
            val meals = familyPkg.mealRecords ?: emptyList()
            val activities = familyPkg.activityRecords ?: emptyList()

            roomDatabase.withTransaction {
                if (profiles.isNotEmpty()) {
                    roomDatabase.childProfileDao().upsertAll(profiles)
                }
                if (growth.isNotEmpty()) {
                    roomDatabase.growthRecordDao().insertAll(growth)
                }
                if (meds.isNotEmpty()) {
                    roomDatabase.medicationTreatmentDao().insertAll(meds)
                }
                if (doseLogs.isNotEmpty()) {
                    roomDatabase.doseLogDao().insertAll(doseLogs)
                }
                if (appts.isNotEmpty()) {
                    roomDatabase.medicalAppointmentDao().insertAll(appts)
                }
                if (temps.isNotEmpty()) {
                    roomDatabase.temperatureRecordDao().insertAll(temps)
                }
                if (sleeps.isNotEmpty()) {
                    roomDatabase.sleepDao().insertAll(sleeps)
                }
                if (meals.isNotEmpty()) {
                    roomDatabase.mealDao().insertAll(meals)
                }
                if (activities.isNotEmpty()) {
                    roomDatabase.activityDao().insertAll(activities)
                }
            }

            android.util.Log.d("DriveBackup", "Base familiar JSON restaurada: ${profiles.size} peques")
            return Result.success(profiles)
        }

        // Intento 2: Ficha individual
        val singlePkg = try {
            Gson().fromJson(trimmed, ChildBackupPackage::class.java)
        } catch (e: Exception) {
            null
        }

        if (singlePkg?.profile != null) {
            val child = singlePkg.profile
            val growth = singlePkg.growthRecords ?: emptyList()
            val meds = singlePkg.medications ?: emptyList()
            val doseLogs = singlePkg.doseLogs ?: emptyList()
            val appts = singlePkg.appointments ?: emptyList()
            val temps = singlePkg.temperatures ?: emptyList()
            val sleeps = singlePkg.sleepRecords ?: emptyList()
            val meals = singlePkg.mealRecords ?: emptyList()
            val activities = singlePkg.activityRecords ?: emptyList()

            roomDatabase.withTransaction {
                roomDatabase.childProfileDao().insertOrUpdate(child)
                if (growth.isNotEmpty()) roomDatabase.growthRecordDao().insertAll(growth)
                if (meds.isNotEmpty()) roomDatabase.medicationTreatmentDao().insertAll(meds)
                if (doseLogs.isNotEmpty()) roomDatabase.doseLogDao().insertAll(doseLogs)
                if (appts.isNotEmpty()) roomDatabase.medicalAppointmentDao().insertAll(appts)
                if (temps.isNotEmpty()) roomDatabase.temperatureRecordDao().insertAll(temps)
                if (sleeps.isNotEmpty()) roomDatabase.sleepDao().insertAll(sleeps)
                if (meals.isNotEmpty()) roomDatabase.mealDao().insertAll(meals)
                if (activities.isNotEmpty()) roomDatabase.activityDao().insertAll(activities)
            }
            return Result.success(listOf(child))
        }

        return Result.failure(Exception("El archivo descargado no coincide con el formato de respaldo."))
    }

    private fun restoreFromSqliteDatabase(
        dbFile: File,
        roomDatabase: PequeCareDatabase
    ): Result<List<ChildProfileEntity>> {
        var sqliteDb: SQLiteDatabase? = null
        return try {
            sqliteDb = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            val profiles = mutableListOf<ChildProfileEntity>()
            val growthList = mutableListOf<GrowthRecordEntity>()
            val medsList = mutableListOf<MedicationTreatmentEntity>()
            val doseLogsList = mutableListOf<DoseLogEntity>()
            val apptsList = mutableListOf<MedicalAppointmentEntity>()
            val tempsList = mutableListOf<TemperatureRecordEntity>()
            val sleepsList = mutableListOf<SleepEntity>()
            val mealsList = mutableListOf<MealEntity>()
            val activitiesList = mutableListOf<ActivityEntity>()

            // 1. child_profiles
            if (tableExists(sqliteDb, "child_profiles")) {
                sqliteDb.rawQuery("SELECT * FROM child_profiles", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val nameIdx = c.getColumnIndex("name")
                    val lastIdx = c.getColumnIndex("lastName")
                    val birthIdx = c.getColumnIndex("birthDate")
                    val genderIdx = c.getColumnIndex("gender")
                    val bloodIdx = c.getColumnIndex("bloodType")
                    val allergiesIdx = c.getColumnIndex("allergies")
                    val pedIdx = c.getColumnIndex("pediatricianName")
                    val phoneIdx = c.getColumnIndex("pediatricianPhone")
                    val clinicIdx = c.getColumnIndex("clinicName")
                    val insIdx = c.getColumnIndex("insuranceNumber")
                    val notesIdx = c.getColumnIndex("notes")
                    val avatarIdx = c.getColumnIndex("avatarUrl")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && nameIdx != -1) {
                            profiles.add(
                                ChildProfileEntity(
                                    id = c.getString(idIdx),
                                    name = c.getString(nameIdx) ?: "",
                                    lastName = if (lastIdx != -1) c.getString(lastIdx) ?: "" else "",
                                    birthDate = if (birthIdx != -1) c.getString(birthIdx) ?: "" else "",
                                    gender = if (genderIdx != -1) c.getString(genderIdx) ?: "female" else "female",
                                    bloodType = if (bloodIdx != -1) c.getString(bloodIdx) ?: "" else "",
                                    allergies = if (allergiesIdx != -1) c.getString(allergiesIdx) ?: "" else "",
                                    pediatricianName = if (pedIdx != -1) c.getString(pedIdx) ?: "" else "",
                                    pediatricianPhone = if (phoneIdx != -1) c.getString(phoneIdx) ?: "" else "",
                                    clinicName = if (clinicIdx != -1) c.getString(clinicIdx) ?: "" else "",
                                    insuranceNumber = if (insIdx != -1) c.getString(insIdx) ?: "" else "",
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null,
                                    avatarUrl = if (avatarIdx != -1) c.getString(avatarIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 2. growth_records
            if (tableExists(sqliteDb, "growth_records")) {
                sqliteDb.rawQuery("SELECT * FROM growth_records", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val childIdx = c.getColumnIndex("childId")
                    val dateIdx = c.getColumnIndex("date")
                    val ageIdx = c.getColumnIndex("ageMonths")
                    val weightIdx = c.getColumnIndex("weightKg")
                    val heightIdx = c.getColumnIndex("heightCm")
                    val headIdx = c.getColumnIndex("headCircumferenceCm")
                    val bmiIdx = c.getColumnIndex("bmi")
                    val wPctIdx = c.getColumnIndex("weightPercentile")
                    val hPctIdx = c.getColumnIndex("heightPercentile")
                    val bPctIdx = c.getColumnIndex("bmiPercentile")
                    val wZIdx = c.getColumnIndex("weightZScore")
                    val hZIdx = c.getColumnIndex("heightZScore")
                    val bZIdx = c.getColumnIndex("bmiZScore")
                    val wStatIdx = c.getColumnIndex("weightStatus")
                    val hStatIdx = c.getColumnIndex("heightStatus")
                    val bStatIdx = c.getColumnIndex("bmiStatus")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && childIdx != -1) {
                            growthList.add(
                                GrowthRecordEntity(
                                    id = c.getString(idIdx),
                                    childId = c.getString(childIdx),
                                    date = if (dateIdx != -1) c.getString(dateIdx) ?: "" else "",
                                    ageMonths = if (ageIdx != -1) c.getInt(ageIdx) else 0,
                                    weightKg = if (weightIdx != -1) c.getDouble(weightIdx) else 0.0,
                                    heightCm = if (heightIdx != -1) c.getDouble(heightIdx) else 0.0,
                                    headCircumferenceCm = if (headIdx != -1 && !c.isNull(headIdx)) c.getDouble(headIdx) else null,
                                    bmi = if (bmiIdx != -1) c.getDouble(bmiIdx) else 0.0,
                                    weightPercentile = if (wPctIdx != -1) c.getInt(wPctIdx) else 50,
                                    heightPercentile = if (hPctIdx != -1) c.getInt(hPctIdx) else 50,
                                    bmiPercentile = if (bPctIdx != -1) c.getInt(bPctIdx) else 50,
                                    weightZScore = if (wZIdx != -1) c.getDouble(wZIdx) else 0.0,
                                    heightZScore = if (hZIdx != -1) c.getDouble(hZIdx) else 0.0,
                                    bmiZScore = if (bZIdx != -1) c.getDouble(bZIdx) else 0.0,
                                    weightStatus = if (wStatIdx != -1) c.getString(wStatIdx) ?: "" else "",
                                    heightStatus = if (hStatIdx != -1) c.getString(hStatIdx) ?: "" else "",
                                    bmiStatus = if (bStatIdx != -1) c.getString(bStatIdx) ?: "" else "",
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 3. medication_treatments
            if (tableExists(sqliteDb, "medication_treatments")) {
                sqliteDb.rawQuery("SELECT * FROM medication_treatments", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val childIdx = c.getColumnIndex("childId")
                    val nameIdx = c.getColumnIndex("name")
                    val formulaIdx = c.getColumnIndex("formula")
                    val concIdx = c.getColumnIndex("concentration")
                    val dosIdx = c.getColumnIndex("dosage")
                    val unitIdx = c.getColumnIndex("unit")
                    val purpIdx = c.getColumnIndex("purpose")
                    val fHIdx = c.getColumnIndex("frequencyHours")
                    val fMIdx = c.getColumnIndex("frequencyMinutes")
                    val durIdx = c.getColumnIndex("durationDays")
                    val startIdx = c.getColumnIndex("startDate")
                    val sTimeIdx = c.getColumnIndex("startTime")
                    val endIdx = c.getColumnIndex("endDate")
                    val nextIdx = c.getColumnIndex("nextDoseTime")
                    val statIdx = c.getColumnIndex("status")
                    val actIdx = c.getColumnIndex("isActive")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && childIdx != -1) {
                            medsList.add(
                                MedicationTreatmentEntity(
                                    id = c.getString(idIdx),
                                    childId = c.getString(childIdx),
                                    name = if (nameIdx != -1) c.getString(nameIdx) ?: "" else "",
                                    formula = if (formulaIdx != -1) c.getString(formulaIdx) ?: "" else "",
                                    concentration = if (concIdx != -1) c.getString(concIdx) ?: "" else "",
                                    dosage = if (dosIdx != -1) c.getString(dosIdx) ?: "" else "",
                                    unit = if (unitIdx != -1) c.getString(unitIdx) ?: "" else "",
                                    purpose = if (purpIdx != -1) c.getString(purpIdx) ?: "" else "",
                                    frequencyHours = if (fHIdx != -1) c.getInt(fHIdx) else 8,
                                    frequencyMinutes = if (fMIdx != -1) c.getInt(fMIdx) else 0,
                                    durationDays = if (durIdx != -1) c.getInt(durIdx) else 1,
                                    startDate = if (startIdx != -1) c.getString(startIdx) ?: "" else "",
                                    startTime = if (sTimeIdx != -1) c.getString(sTimeIdx) ?: "08:00" else "08:00",
                                    endDate = if (endIdx != -1) c.getString(endIdx) ?: "" else "",
                                    nextDoseTime = if (nextIdx != -1) c.getString(nextIdx) else null,
                                    status = if (statIdx != -1) c.getString(statIdx) ?: "ACTIVO" else "ACTIVO",
                                    isActive = if (actIdx != -1) c.getInt(actIdx) == 1 else true,
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 4. dose_logs
            if (tableExists(sqliteDb, "dose_logs")) {
                sqliteDb.rawQuery("SELECT * FROM dose_logs", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val treatIdx = c.getColumnIndex("treatmentId")
                    val timeIdx = c.getColumnIndex("timestamp")
                    val doseIdx = c.getColumnIndex("dosageGiven")
                    val statIdx = c.getColumnIndex("status")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && treatIdx != -1) {
                            doseLogsList.add(
                                DoseLogEntity(
                                    id = c.getString(idIdx),
                                    treatmentId = c.getString(treatIdx),
                                    timestamp = if (timeIdx != -1) c.getString(timeIdx) ?: "" else "",
                                    dosageGiven = if (doseIdx != -1) c.getString(doseIdx) ?: "" else "",
                                    status = if (statIdx != -1) c.getString(statIdx) ?: "administrado" else "administrado",
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 5. medical_appointments
            if (tableExists(sqliteDb, "medical_appointments")) {
                sqliteDb.rawQuery("SELECT * FROM medical_appointments", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val childIdx = c.getColumnIndex("childId")
                    val docIdx = c.getColumnIndex("doctorName")
                    val specIdx = c.getColumnIndex("specialty")
                    val clinIdx = c.getColumnIndex("clinic")
                    val dateIdx = c.getColumnIndex("date")
                    val timeIdx = c.getColumnIndex("time")
                    val reasonIdx = c.getColumnIndex("reason")
                    val statIdx = c.getColumnIndex("status")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && childIdx != -1) {
                            apptsList.add(
                                MedicalAppointmentEntity(
                                    id = c.getString(idIdx),
                                    childId = c.getString(childIdx),
                                    doctorName = if (docIdx != -1) c.getString(docIdx) ?: "" else "",
                                    specialty = if (specIdx != -1) c.getString(specIdx) ?: "" else "",
                                    clinic = if (clinIdx != -1) c.getString(clinIdx) ?: "" else "",
                                    date = if (dateIdx != -1) c.getString(dateIdx) ?: "" else "",
                                    time = if (timeIdx != -1) c.getString(timeIdx) ?: "" else "",
                                    reason = if (reasonIdx != -1) c.getString(reasonIdx) ?: "" else "",
                                    status = if (statIdx != -1) c.getString(statIdx) ?: "PENDIENTE" else "PENDIENTE",
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 6. temperature_records
            if (tableExists(sqliteDb, "temperature_records")) {
                sqliteDb.rawQuery("SELECT * FROM temperature_records", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val childIdx = c.getColumnIndex("childId")
                    val degIdx = c.getColumnIndex("celsius")
                    val dateIdx = c.getColumnIndex("date")
                    val timeIdx = c.getColumnIndex("time")
                    val sympIdx = c.getColumnIndex("symptoms")
                    val medIdx = c.getColumnIndex("medicationGiven")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && childIdx != -1) {
                            tempsList.add(
                                TemperatureRecordEntity(
                                    id = c.getString(idIdx),
                                    childId = c.getString(childIdx),
                                    celsius = if (degIdx != -1) c.getDouble(degIdx) else 37.0,
                                    date = if (dateIdx != -1) c.getString(dateIdx) ?: "" else "",
                                    time = if (timeIdx != -1) c.getString(timeIdx) ?: "" else "",
                                    symptoms = if (sympIdx != -1) c.getString(sympIdx) ?: "" else "",
                                    medicationGiven = if (medIdx != -1) c.getString(medIdx) else null,
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 7. sleep_records
            if (tableExists(sqliteDb, "sleep_records")) {
                sqliteDb.rawQuery("SELECT * FROM sleep_records", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val childIdx = c.getColumnIndex("childId")
                    val dateIdx = c.getColumnIndex("date")
                    val sTimeIdx = c.getColumnIndex("startTime")
                    val eTimeIdx = c.getColumnIndex("endTime")
                    val durIdx = c.getColumnIndex("durationMinutes")
                    val typeIdx = c.getColumnIndex("type")
                    val qualIdx = c.getColumnIndex("quality")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && childIdx != -1) {
                            sleepsList.add(
                                SleepEntity(
                                    id = c.getString(idIdx),
                                    childId = c.getString(childIdx),
                                    date = if (dateIdx != -1) c.getString(dateIdx) ?: "" else "",
                                    startTime = if (sTimeIdx != -1) c.getString(sTimeIdx) ?: "" else "",
                                    endTime = if (eTimeIdx != -1) c.getString(eTimeIdx) ?: "" else "",
                                    durationMinutes = if (durIdx != -1) c.getInt(durIdx) else 0,
                                    type = if (typeIdx != -1) c.getString(typeIdx) ?: "noche" else "noche",
                                    quality = if (qualIdx != -1) c.getString(qualIdx) ?: "tranquilo" else "tranquilo",
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 8. meal_records
            if (tableExists(sqliteDb, "meal_records")) {
                sqliteDb.rawQuery("SELECT * FROM meal_records", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val childIdx = c.getColumnIndex("childId")
                    val dateIdx = c.getColumnIndex("date")
                    val timeIdx = c.getColumnIndex("time")
                    val mTypeIdx = c.getColumnIndex("mealType")
                    val foodsIdx = c.getColumnIndex("foods")
                    val waterIdx = c.getColumnIndex("waterGlasses")
                    val appIdx = c.getColumnIndex("appetite")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && childIdx != -1) {
                            mealsList.add(
                                MealEntity(
                                    id = c.getString(idIdx),
                                    childId = c.getString(childIdx),
                                    date = if (dateIdx != -1) c.getString(dateIdx) ?: "" else "",
                                    time = if (timeIdx != -1) c.getString(timeIdx) ?: "" else "",
                                    mealType = if (mTypeIdx != -1) c.getString(mTypeIdx) ?: "almuerzo" else "almuerzo",
                                    foods = if (foodsIdx != -1) c.getString(foodsIdx) ?: "" else "",
                                    waterGlasses = if (waterIdx != -1) c.getInt(waterIdx) else 0,
                                    appetite = if (appIdx != -1) c.getString(appIdx) ?: "bueno" else "bueno",
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // 9. activity_records
            if (tableExists(sqliteDb, "activity_records")) {
                sqliteDb.rawQuery("SELECT * FROM activity_records", null).use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val childIdx = c.getColumnIndex("childId")
                    val dateIdx = c.getColumnIndex("date")
                    val timeIdx = c.getColumnIndex("time")
                    val titleIdx = c.getColumnIndex("title")
                    val catIdx = c.getColumnIndex("category")
                    val durIdx = c.getColumnIndex("durationMinutes")
                    val remEnIdx = c.getColumnIndex("reminderEnabled")
                    val remTimeIdx = c.getColumnIndex("reminderTime")
                    val notesIdx = c.getColumnIndex("notes")

                    while (c.moveToNext()) {
                        if (idIdx != -1 && childIdx != -1) {
                            activitiesList.add(
                                ActivityEntity(
                                    id = c.getString(idIdx),
                                    childId = c.getString(childIdx),
                                    date = if (dateIdx != -1) c.getString(dateIdx) ?: "" else "",
                                    time = if (timeIdx != -1) c.getString(timeIdx) ?: "" else "",
                                    title = if (titleIdx != -1) c.getString(titleIdx) ?: "" else "",
                                    category = if (catIdx != -1) c.getString(catIdx) ?: "recreativa" else "recreativa",
                                    durationMinutes = if (durIdx != -1) c.getInt(durIdx) else 0,
                                    reminderEnabled = if (remEnIdx != -1) c.getInt(remEnIdx) == 1 else false,
                                    reminderTime = if (remTimeIdx != -1) c.getString(remTimeIdx) else null,
                                    notes = if (notesIdx != -1) c.getString(notesIdx) else null
                                )
                            )
                        }
                    }
                }
            }

            // Inserción limpia en Room
            runBlocking {
                roomDatabase.withTransaction {
                    if (profiles.isNotEmpty()) roomDatabase.childProfileDao().upsertAll(profiles)
                    if (growthList.isNotEmpty()) roomDatabase.growthRecordDao().insertAll(growthList)
                    if (medsList.isNotEmpty()) roomDatabase.medicationTreatmentDao().insertAll(medsList)
                    if (doseLogsList.isNotEmpty()) roomDatabase.doseLogDao().insertAll(doseLogsList)
                    if (apptsList.isNotEmpty()) roomDatabase.medicalAppointmentDao().insertAll(apptsList)
                    if (tempsList.isNotEmpty()) roomDatabase.temperatureRecordDao().insertAll(tempsList)
                    if (sleepsList.isNotEmpty()) roomDatabase.sleepDao().insertAll(sleepsList)
                    if (mealsList.isNotEmpty()) roomDatabase.mealDao().insertAll(mealsList)
                    if (activitiesList.isNotEmpty()) roomDatabase.activityDao().insertAll(activitiesList)
                }
            }

            android.util.Log.d("DriveBackup", "Base SQLite legacy restaurada con éxito: ${profiles.size} peques")
            Result.success(profiles)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error extrayendo datos de SQLite legacy", e)
            Result.failure(e)
        } finally {
            sqliteDb?.close()
        }
    }

    private fun tableExists(db: SQLiteDatabase, tableName: String): Boolean {
        return try {
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(tableName)).use {
                it.count > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun findFamilyBackupFileId(driveService: Drive): String? {
        return findFileIdByName(driveService, FAMILY_BACKUP_FILE_NAME)
    }

    private fun findLegacyBackupFileId(driveService: Drive): String? {
        return findFileIdByName(driveService, LEGACY_BACKUP_FILE_NAME)
    }

    private fun findFileIdByName(driveService: Drive, fileName: String): String? {
        val query = "name = '$fileName' and '$APP_DATA_FOLDER' in parents and trashed = false"
        val result = driveService.files().list()
            .setSpaces(APP_DATA_FOLDER)
            .setQ(query)
            .setFields("files(id, name)")
            .execute()

        val files = result.files
        return if (!files.isNullOrEmpty()) files[0].id else null
    }
}
