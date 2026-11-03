package com.pequecare.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.core.content.edit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.pequecare.data.local.db.PequeCareDatabase
import com.pequecare.data.remote.google.GoogleDriveBackupRepository
import java.util.concurrent.TimeUnit

class BackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "pequecare_auto_backup"

        fun schedulePeriodicBackup(context: Context, frequency: String) {
            val workManager = WorkManager.getInstance(context)

            if (frequency.equals("none", ignoreCase = true) || frequency.isBlank()) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }

            val (repeatInterval, timeUnit) = when (frequency.lowercase()) {
                "daily", "diaria", "diario" -> Pair(1L, TimeUnit.DAYS)
                "weekly", "semanal" -> Pair(7L, TimeUnit.DAYS)
                "monthly", "mensual" -> Pair(30L, TimeUnit.DAYS)
                else -> Pair(1L, TimeUnit.DAYS)
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(repeatInterval, timeUnit)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancelPeriodicBackup(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                return Result.failure()
            }

            val hasScope = GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))
            if (!hasScope) {
                return Result.failure()
            }

            val repo = GoogleDriveBackupRepository(context)
            val db = PequeCareDatabase.getDatabase(context)
            val result = repo.uploadDatabaseBackup(account, db)

            if (result.isSuccess) {
                val prefs = context.getSharedPreferences("pequecare_prefs", Context.MODE_PRIVATE)
                prefs.edit { putString("last_drive_backup_date", java.time.LocalDateTime.now().toString()) }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
