package com.pequecare.data.remote.google

import android.content.Context
import androidx.room.RoomDatabase
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
        private const val BACKUP_FILE_NAME = "pequecare_backup.db"
        private const val DB_NAME = "pequecare_database"
        private const val MIME_TYPE_SQLITE = "application/x-sqlite3"
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
                if (name == BACKUP_FILE_NAME || name.startsWith("pequecare_backup_full")) {
                    items.add(
                        DriveBackupItem(
                            fileId = file.id,
                            title = "📦 Copia Completa Familiar",
                            subtitle = file.description ?: "Base de datos completa con todos los niños ($modDate)",
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
     * Sube el archivo de la base de datos Room a la carpeta oculta de la app en Google Drive (AppFolder).
     */
    suspend fun uploadDatabaseBackup(
        account: GoogleSignInAccount,
        roomDatabase: RoomDatabase
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Ejecutar WAL Checkpoint con TRUNCATE para asegurar que todos los datos se consoliden en el archivo .db
            checkpointDatabase(roomDatabase)

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists() || dbFile.length() == 0L) {
                return@withContext Result.failure(Exception("El archivo local de base de datos no existe o está vacío."))
            }

            // 2. Copiar a un archivo temporal para subir de forma segura sin bloqueos de concurrencia
            val tempBackupFile = File(context.cacheDir, "pequecare_backup_upload.db")
            dbFile.copyTo(tempBackupFile, overwrite = true)

            val driveService = getDriveService(account)
            val existingFileId = findBackupFileId(driveService)
            val mediaContent = FileContent(MIME_TYPE_SQLITE, tempBackupFile)
            val nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            val desc = "Copia familiar completa • $nowStr"

            val resultId = if (existingFileId != null) {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    description = desc
                }
                val updatedFile = driveService.files().update(existingFileId, fileMetadata, mediaContent).execute()
                updatedFile.id
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = BACKUP_FILE_NAME
                    description = desc
                    parents = Collections.singletonList(APP_DATA_FOLDER)
                }
                val createdFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                createdFile.id
            }

            tempBackupFile.delete()
            android.util.Log.d("DriveBackup", "Respaldo completo subido exitosamente. File ID: $resultId")
            Result.success(resultId)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error al subir respaldo a Google Drive", e)
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
     * Descarga la última copia de seguridad completa o una específica por ID y sobreescribe el archivo SQLite local.
     */
    suspend fun downloadDatabaseBackup(
        account: GoogleSignInAccount,
        targetFileId: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)

            val fileId = targetFileId ?: findBackupFileId(driveService)
                ?: return@withContext Result.failure(Exception("No se encontró ningún respaldo completo en Google Drive."))

            val tempDownloadFile = File(context.cacheDir, "pequecare_download.db")
            FileOutputStream(tempDownloadFile).use { outputStream ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }

            if (!tempDownloadFile.exists() || tempDownloadFile.length() == 0L) {
                tempDownloadFile.delete()
                return@withContext Result.failure(Exception("El archivo descargado de Google Drive está vacío o dañado."))
            }

            PequeCareDatabase.closeDatabase()

            val targetDbFile = context.getDatabasePath(DB_NAME)
            tempDownloadFile.copyTo(targetDbFile, overwrite = true)
            tempDownloadFile.delete()

            context.getDatabasePath("$DB_NAME-wal").delete()
            context.getDatabasePath("$DB_NAME-shm").delete()

            val restoredDb = PequeCareDatabase.getDatabase(context)
            val checkCursor = restoredDb.openHelper.readableDatabase.query("SELECT count(*) FROM child_profiles")
            val count = if (checkCursor.moveToFirst()) checkCursor.getInt(0) else 0
            checkCursor.close()
            android.util.Log.d("DriveBackup", "Base de datos restaurada. Niños encontrados: $count")

            Result.success(Unit)
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

            // Insertar o actualizar todos los registros de este niño en Room
            roomDatabase.childProfileDao().insertOrUpdate(pkg.profile)
            if (pkg.growthRecords.isNotEmpty()) roomDatabase.growthRecordDao().insertAll(pkg.growthRecords)
            if (pkg.medications.isNotEmpty()) roomDatabase.medicationTreatmentDao().insertAll(pkg.medications)
            if (pkg.doseLogs.isNotEmpty()) roomDatabase.doseLogDao().insertAll(pkg.doseLogs)
            if (pkg.appointments.isNotEmpty()) roomDatabase.medicalAppointmentDao().insertAll(pkg.appointments)
            if (pkg.temperatures.isNotEmpty()) roomDatabase.temperatureRecordDao().insertAll(pkg.temperatures)
            if (pkg.sleepRecords.isNotEmpty()) roomDatabase.sleepDao().insertAll(pkg.sleepRecords)
            if (pkg.mealRecords.isNotEmpty()) roomDatabase.mealDao().insertAll(pkg.mealRecords)
            if (pkg.activityRecords.isNotEmpty()) roomDatabase.activityDao().insertAll(pkg.activityRecords)

            android.util.Log.d("DriveBackup", "Ficha individual de ${pkg.profile.name} restaurada con éxito.")
            Result.success(pkg.profile)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error restaurando ficha individual de niño", e)
            Result.failure(e)
        }
    }

    private fun findBackupFileId(driveService: Drive): String? {
        return findFileIdByName(driveService, BACKUP_FILE_NAME)
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

    private fun checkpointDatabase(roomDatabase: RoomDatabase) {
        try {
            val db = roomDatabase.openHelper.writableDatabase
            val cursor = db.query("PRAGMA wal_checkpoint(TRUNCATE)")
            if (cursor.moveToFirst()) {
                val busy = cursor.getInt(0)
                val log = cursor.getInt(1)
                val checkpointed = cursor.getInt(2)
                android.util.Log.d("DriveBackup", "wal_checkpoint TRUNCATE: busy=$busy, log=$log, checkpointed=$checkpointed")
            }
            cursor.close()
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Error ejecutando wal_checkpoint", e)
        }
    }
}
