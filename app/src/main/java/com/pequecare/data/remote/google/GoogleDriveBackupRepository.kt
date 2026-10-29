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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

class GoogleDriveBackupRepository(private val context: Context) {

    companion object {
        private const val BACKUP_FILE_NAME = "pequecare_backup.db"
        private const val DB_NAME = "pequecare_database"
        private const val MIME_TYPE_SQLITE = "application/x-sqlite3"
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
     * Sube el archivo de la base de datos Room a la carpeta oculta de la app en Google Drive (AppFolder).
     *
     * @param account Cuenta autenticada de Google Sign-In.
     * @param roomDatabase Instancia de Room para ejecutar el checkpoint WAL antes de subir.
     */
    suspend fun uploadDatabaseBackup(
        account: GoogleSignInAccount,
        roomDatabase: RoomDatabase
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Ejecutar WAL Checkpoint para asegurar que todos los datos en WAL se escriban en el archivo principal .db
            checkpointDatabase(roomDatabase)

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("El archivo local de base de datos no existe."))
            }

            val driveService = getDriveService(account)

            // 2. Buscar si ya existe una copia previa en AppFolder
            val existingFileId = findBackupFileId(driveService)

            val mediaContent = FileContent(MIME_TYPE_SQLITE, dbFile)

            return@withContext if (existingFileId != null) {
                // Actualizar copia existente
                val fileMetadata = com.google.api.services.drive.model.File()
                val updatedFile = driveService.files().update(existingFileId, fileMetadata, mediaContent).execute()
                Result.success(updatedFile.id)
            } else {
                // Crear nueva copia
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = BACKUP_FILE_NAME
                    parents = Collections.singletonList(APP_DATA_FOLDER)
                }
                val createdFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                Result.success(createdFile.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Descarga la última copia de seguridad de la base de datos desde Google Drive y sobreescribe el archivo SQLite local.
     *
     * @param account Cuenta autenticada de Google Sign-In.
     * @param roomDatabase Instancia de Room para cerrar conexiones antes de sobreescribir.
     */
    suspend fun downloadDatabaseBackup(
        account: GoogleSignInAccount,
        roomDatabase: RoomDatabase
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)

            // 1. Localizar el archivo en Google Drive AppFolder
            val fileId = findBackupFileId(driveService)
                ?: return@withContext Result.failure(Exception("No se encontró ningún respaldo en Google Drive."))

            // 2. Cerrar la base de datos Room para liberar los bloqueos sobre el archivo local
            if (roomDatabase.isOpen) {
                roomDatabase.close()
            }

            val targetDbFile = context.getDatabasePath(DB_NAME)

            // 3. Descargar el archivo desde Drive y sobreescribir el archivo local
            FileOutputStream(targetDbFile).use { outputStream ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }

            // Eliminar archivos WAL y SHM residuales para evitar inconsistencias al reabrir Room
            context.getDatabasePath("$DB_NAME-wal").delete()
            context.getDatabasePath("$DB_NAME-shm").delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca el ID del archivo de respaldo en la carpeta reservada de la app (appDataFolder).
     */
    private fun findBackupFileId(driveService: Drive): String? {
        val query = "name = '$BACKUP_FILE_NAME' and '$APP_DATA_FOLDER' in parents and trashed = false"
        val result = driveService.files().list()
            .setSpaces(APP_DATA_FOLDER)
            .setQ(query)
            .setFields("files(id, name)")
            .execute()

        val files = result.files
        return if (!files.isNullOrEmpty()) files[0].id else null
    }

    /**
     * Realiza un Checkpoint SQLite en el archivo WAL para mover todas las transacciones pendientes al archivo principal .db
     */
    private fun checkpointDatabase(roomDatabase: RoomDatabase) {
        val db = roomDatabase.openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL)").close()
    }
}
