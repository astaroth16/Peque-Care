package com.pequecare.data.remote.google

import android.content.Context
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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections

/**
 * Representa una copia individual completa de la ficha médica de un niño/a.
 */
data class ChildBackupPackage(
    val version: Int = 1,
    val backupDate: String,
    val profile: ChildProfileEntity,
    val growthRecords: List<GrowthRecordEntity> = emptyList(),
    val medications: List<MedicationTreatmentEntity> = emptyList(),
    val doseLogs: List<DoseLogEntity> = emptyList(),
    val appointments: List<MedicalAppointmentEntity> = emptyList(),
    val temperatures: List<TemperatureRecordEntity> = emptyList(),
    val sleepRecords: List<SleepEntity> = emptyList(),
    val mealRecords: List<MealEntity> = emptyList(),
    val activityRecords: List<ActivityEntity> = emptyList()
)

/**
 * Representa una copia completa de toda la familia y todos los registros médicos.
 */
data class FamilyBackupPackage(
    val version: Int = 1,
    val backupDate: String,
    val profiles: List<ChildProfileEntity> = emptyList(),
    val growthRecords: List<GrowthRecordEntity> = emptyList(),
    val medications: List<MedicationTreatmentEntity> = emptyList(),
    val doseLogs: List<DoseLogEntity> = emptyList(),
    val appointments: List<MedicalAppointmentEntity> = emptyList(),
    val temperatures: List<TemperatureRecordEntity> = emptyList(),
    val sleepRecords: List<SleepEntity> = emptyList(),
    val mealRecords: List<MealEntity> = emptyList(),
    val activityRecords: List<ActivityEntity> = emptyList()
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
                            subtitle = file.description ?: "Base de datos completa de la familia ($modDate)",
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
     */
    suspend fun downloadDatabaseBackup(
        account: GoogleSignInAccount,
        roomDatabase: PequeCareDatabase,
        targetFileId: String? = null
    ): Result<List<ChildProfileEntity>> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)

            val fileId = targetFileId ?: findFamilyBackupFileId(driveService) ?: findLegacyBackupFileId(driveService)
                ?: return@withContext Result.failure(Exception("No se encontró ningún respaldo completo en Google Drive."))

            val tempDownloadFile = File(context.cacheDir, "pequecare_download.tmp")
            FileOutputStream(tempDownloadFile).use { outputStream ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }

            if (!tempDownloadFile.exists() || tempDownloadFile.length() == 0L) {
                tempDownloadFile.delete()
                return@withContext Result.failure(Exception("El archivo descargado de Google Drive está vacío o dañado."))
            }

            val content = tempDownloadFile.readText()
            tempDownloadFile.delete()

            val pkg = try {
                Gson().fromJson(content, FamilyBackupPackage::class.java)
            } catch (e: Exception) {
                null
            }

            if (pkg != null && (pkg.profiles.isNotEmpty() || pkg.growthRecords.isNotEmpty())) {
                roomDatabase.withTransaction {
                    if (pkg.profiles.isNotEmpty()) {
                        roomDatabase.childProfileDao().upsertAll(pkg.profiles)
                    }
                    if (pkg.growthRecords.isNotEmpty()) {
                        roomDatabase.growthRecordDao().insertAll(pkg.growthRecords)
                    }
                    if (pkg.medications.isNotEmpty()) {
                        roomDatabase.medicationTreatmentDao().insertAll(pkg.medications)
                    }
                    if (pkg.doseLogs.isNotEmpty()) {
                        roomDatabase.doseLogDao().insertAll(pkg.doseLogs)
                    }
                    if (pkg.appointments.isNotEmpty()) {
                        roomDatabase.medicalAppointmentDao().insertAll(pkg.appointments)
                    }
                    if (pkg.temperatures.isNotEmpty()) {
                        roomDatabase.temperatureRecordDao().insertAll(pkg.temperatures)
                    }
                    if (pkg.sleepRecords.isNotEmpty()) {
                        roomDatabase.sleepDao().insertAll(pkg.sleepRecords)
                    }
                    if (pkg.mealRecords.isNotEmpty()) {
                        roomDatabase.mealDao().insertAll(pkg.mealRecords)
                    }
                    if (pkg.activityRecords.isNotEmpty()) {
                        roomDatabase.activityDao().insertAll(pkg.activityRecords)
                    }
                }

                android.util.Log.d("DriveBackup", "Base familiar restaurada exitosamente vía Room: ${pkg.profiles.size} peques")
                Result.success(pkg.profiles)
            } else {
                // Posible archivo de respaldo individual
                val singlePkg = try {
                    Gson().fromJson(content, ChildBackupPackage::class.java)
                } catch (e: Exception) {
                    null
                }
                if (singlePkg != null && singlePkg.profile != null) {
                    roomDatabase.withTransaction {
                        roomDatabase.childProfileDao().insertOrUpdate(singlePkg.profile)
                        if (singlePkg.growthRecords.isNotEmpty()) roomDatabase.growthRecordDao().insertAll(singlePkg.growthRecords)
                        if (singlePkg.medications.isNotEmpty()) roomDatabase.medicationTreatmentDao().insertAll(singlePkg.medications)
                        if (singlePkg.doseLogs.isNotEmpty()) roomDatabase.doseLogDao().insertAll(singlePkg.doseLogs)
                        if (singlePkg.appointments.isNotEmpty()) roomDatabase.medicalAppointmentDao().insertAll(singlePkg.appointments)
                        if (singlePkg.temperatures.isNotEmpty()) roomDatabase.temperatureRecordDao().insertAll(singlePkg.temperatures)
                        if (singlePkg.sleepRecords.isNotEmpty()) roomDatabase.sleepDao().insertAll(singlePkg.sleepRecords)
                        if (singlePkg.mealRecords.isNotEmpty()) roomDatabase.mealDao().insertAll(singlePkg.mealRecords)
                        if (singlePkg.activityRecords.isNotEmpty()) roomDatabase.activityDao().insertAll(singlePkg.activityRecords)
                    }
                    Result.success(listOf(singlePkg.profile))
                } else {
                    Result.failure(Exception("El archivo descargado no contiene un formato de respaldo JSON válido."))
                }
            }
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

            val jsonString = tempFile.readText()
            tempFile.delete()

            val pkg = Gson().fromJson(jsonString, ChildBackupPackage::class.java)
                ?: return@withContext Result.failure(Exception("Archivo de respaldo de niño corrupto o no válido."))

            roomDatabase.withTransaction {
                roomDatabase.childProfileDao().insertOrUpdate(pkg.profile)
                if (pkg.growthRecords.isNotEmpty()) roomDatabase.growthRecordDao().insertAll(pkg.growthRecords)
                if (pkg.medications.isNotEmpty()) roomDatabase.medicationTreatmentDao().insertAll(pkg.medications)
                if (pkg.doseLogs.isNotEmpty()) roomDatabase.doseLogDao().insertAll(pkg.doseLogs)
                if (pkg.appointments.isNotEmpty()) roomDatabase.medicalAppointmentDao().insertAll(pkg.appointments)
                if (pkg.temperatures.isNotEmpty()) roomDatabase.temperatureRecordDao().insertAll(pkg.temperatures)
                if (pkg.sleepRecords.isNotEmpty()) roomDatabase.sleepDao().insertAll(pkg.sleepRecords)
                if (pkg.mealRecords.isNotEmpty()) roomDatabase.mealDao().insertAll(pkg.mealRecords)
                if (pkg.activityRecords.isNotEmpty()) roomDatabase.activityDao().insertAll(pkg.activityRecords)
            }

            android.util.Log.d("DriveBackup", "Ficha individual de ${pkg.profile.name} restaurada con éxito.")
            Result.success(pkg.profile)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error restaurando ficha individual de niño", e)
            Result.failure(e)
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
