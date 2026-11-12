package com.pequecare.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.pequecare.MainActivity
import com.pequecare.R
import com.pequecare.data.local.db.PequeCareDatabase
import com.pequecare.data.local.entity.SleepEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class ActiveSleepState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val childId: String = "",
    val childName: String = "",
    val startDate: String = "",
    val startTimeStr: String = "",
    val type: String = "siesta",
    val quality: String = "tranquilo",
    val notes: String = "",
    val startEpochMs: Long = 0L,
    val accumulatedSeconds: Long = 0L
) {
    /**
     * Calcula los segundos totales transcurridos usando reloj monotónico/epoch,
     * sin necesidad de correr un bucle de CPU en segundo plano.
     */
    fun currentElapsedSeconds(nowMs: Long = System.currentTimeMillis()): Long {
        if (!isRunning) return 0L
        return if (isPaused) {
            accumulatedSeconds
        } else {
            val delta = ((nowMs - startEpochMs) / 1000L).coerceAtLeast(0L)
            accumulatedSeconds + delta
        }
    }
}

class SleepTrackerService : Service() {

    companion object {
        const val CHANNEL_ID = "pequecare_sleep_tracker_v2"
        const val NOTIFICATION_ID = 9901

        const val ACTION_START = "com.pequecare.sleep.ACTION_START"
        const val ACTION_PAUSE = "com.pequecare.sleep.ACTION_PAUSE"
        const val ACTION_RESUME = "com.pequecare.sleep.ACTION_RESUME"
        const val ACTION_STOP = "com.pequecare.sleep.ACTION_STOP"
        const val ACTION_STOP_AND_SAVE = "com.pequecare.sleep.ACTION_STOP_AND_SAVE"
        const val ACTION_RESTORE = "com.pequecare.sleep.ACTION_RESTORE"

        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private const val PREFS_NAME = "pequecare_prefs"
        private const val KEY_IS_RUNNING = "sleep_timer_is_running"
        private const val KEY_IS_PAUSED = "sleep_timer_is_paused"
        private const val KEY_CHILD_ID = "sleep_timer_child_id"
        private const val KEY_CHILD_NAME = "sleep_timer_child_name"
        private const val KEY_START_DATE = "sleep_timer_start_date"
        private const val KEY_START_TIME = "sleep_timer_start_time"
        private const val KEY_TYPE = "sleep_timer_type"
        private const val KEY_QUALITY = "sleep_timer_quality"
        private const val KEY_NOTES = "sleep_timer_notes"
        private const val KEY_START_EPOCH_MS = "sleep_timer_start_epoch_ms"
        private const val KEY_ACCUMULATED_SEC = "sleep_timer_accumulated_sec"

        private val _sleepState = MutableStateFlow(ActiveSleepState())
        val sleepState: StateFlow<ActiveSleepState> = _sleepState.asStateFlow()

        fun loadStateFromPrefs(context: Context): ActiveSleepState {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
            if (!isRunning) {
                val empty = ActiveSleepState()
                _sleepState.value = empty
                return empty
            }
            val state = ActiveSleepState(
                isRunning = true,
                isPaused = prefs.getBoolean(KEY_IS_PAUSED, false),
                childId = prefs.getString(KEY_CHILD_ID, "") ?: "",
                childName = prefs.getString(KEY_CHILD_NAME, "") ?: "",
                startDate = prefs.getString(KEY_START_DATE, LocalDate.now().toString()) ?: LocalDate.now().toString(),
                startTimeStr = prefs.getString(KEY_START_TIME, "00:00") ?: "00:00",
                type = prefs.getString(KEY_TYPE, "siesta") ?: "siesta",
                quality = prefs.getString(KEY_QUALITY, "tranquilo") ?: "tranquilo",
                notes = prefs.getString(KEY_NOTES, "") ?: "",
                startEpochMs = prefs.getLong(KEY_START_EPOCH_MS, System.currentTimeMillis()),
                accumulatedSeconds = prefs.getLong(KEY_ACCUMULATED_SEC, 0L)
            )
            _sleepState.value = state
            return state
        }

        private fun saveStateToPrefs(context: Context, state: ActiveSleepState) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                if (!state.isRunning) {
                    putBoolean(KEY_IS_RUNNING, false)
                    remove(KEY_IS_PAUSED)
                    remove(KEY_CHILD_ID)
                    remove(KEY_CHILD_NAME)
                    remove(KEY_START_DATE)
                    remove(KEY_START_TIME)
                    remove(KEY_TYPE)
                    remove(KEY_QUALITY)
                    remove(KEY_NOTES)
                    remove(KEY_START_EPOCH_MS)
                    remove(KEY_ACCUMULATED_SEC)
                } else {
                    putBoolean(KEY_IS_RUNNING, true)
                    putBoolean(KEY_IS_PAUSED, state.isPaused)
                    putString(KEY_CHILD_ID, state.childId)
                    putString(KEY_CHILD_NAME, state.childName)
                    putString(KEY_START_DATE, state.startDate)
                    putString(KEY_START_TIME, state.startTimeStr)
                    putString(KEY_TYPE, state.type)
                    putString(KEY_QUALITY, state.quality)
                    putString(KEY_NOTES, state.notes)
                    putLong(KEY_START_EPOCH_MS, state.startEpochMs)
                    putLong(KEY_ACCUMULATED_SEC, state.accumulatedSeconds)
                }
                apply()
            }
            _sleepState.value = state
        }

        fun updateMetadata(context: Context, type: String, quality: String, notes: String) {
            val current = _sleepState.value
            if (!current.isRunning) return
            val updated = current.copy(type = type, quality = quality, notes = notes)
            saveStateToPrefs(context, updated)
        }

        fun startTracking(
            context: Context,
            childId: String,
            childName: String,
            type: String,
            quality: String,
            notes: String
        ) {
            val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val today = LocalDate.now().toString()
            val newState = ActiveSleepState(
                isRunning = true,
                isPaused = false,
                childId = childId,
                childName = childName,
                startDate = today,
                startTimeStr = nowTime,
                type = type,
                quality = quality,
                notes = notes,
                startEpochMs = System.currentTimeMillis(),
                accumulatedSeconds = 0L
            )
            saveStateToPrefs(context, newState)
            val intent = Intent(context, SleepTrackerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseTracking(context: Context) {
            val intent = Intent(context, SleepTrackerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeTracking(context: Context) {
            val intent = Intent(context, SleepTrackerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopAndClearTracking(context: Context): ActiveSleepState {
            val finalState = _sleepState.value
            saveStateToPrefs(context, ActiveSleepState(isRunning = false))
            val intent = Intent(context, SleepTrackerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
            return finalState
        }

        fun ensureServiceRunningIfActive(context: Context) {
            val state = loadStateFromPrefs(context)
            if (state.isRunning) {
                val intent = Intent(context, SleepTrackerService::class.java).apply {
                    action = ACTION_RESTORE
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (_: Exception) {
                    // En caso de restricción de inicio en background
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val currentState = loadStateFromPrefs(this)

        when (intent?.action) {
            ACTION_START, ACTION_RESTORE, null -> {
                if (currentState.isRunning) {
                    promoteToForeground(currentState)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
            ACTION_PAUSE -> {
                if (currentState.isRunning && !currentState.isPaused) {
                    val now = System.currentTimeMillis()
                    val delta = ((now - currentState.startEpochMs) / 1000L).coerceAtLeast(0L)
                    val pausedState = currentState.copy(
                        isPaused = true,
                        accumulatedSeconds = currentState.accumulatedSeconds + delta,
                        startEpochMs = now
                    )
                    saveStateToPrefs(this, pausedState)
                    promoteToForeground(pausedState)
                }
            }
            ACTION_RESUME -> {
                if (currentState.isRunning && currentState.isPaused) {
                    val resumedState = currentState.copy(
                        isPaused = false,
                        startEpochMs = System.currentTimeMillis()
                    )
                    saveStateToPrefs(this, resumedState)
                    promoteToForeground(resumedState)
                }
            }
            ACTION_STOP -> {
                saveStateToPrefs(this, ActiveSleepState(isRunning = false))
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_STOP_AND_SAVE -> {
                if (currentState.isRunning) {
                    val elapsedSec = currentState.currentElapsedSeconds()
                    saveStateToPrefs(this, ActiveSleepState(isRunning = false))
                    stopForeground(STOP_FOREGROUND_REMOVE)

                    val appCtx = applicationContext
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = PequeCareDatabase.getDatabase(appCtx)
                            val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            val targetChildId = currentState.childId.ifBlank {
                                prefs.getString("selected_child_id", null)
                                    ?: db.childProfileDao().getAllChildProfilesOnce().firstOrNull()?.id
                                    ?: ""
                            }
                            if (targetChildId.isNotBlank()) {
                                val endTimeStr = LocalTime.now().format(TIME_FORMATTER)
                                val startTimeStr = currentState.startTimeStr.ifBlank { endTimeStr }
                                val durationMin = if (elapsedSec >= 60L) {
                                    (elapsedSec / 60L).toInt()
                                } else {
                                    try {
                                        val t1 = LocalTime.parse(startTimeStr)
                                        val t2 = LocalTime.parse(endTimeStr)
                                        var min = Duration.between(t1, t2).toMinutes().toInt()
                                        if (min < 0) min += 24 * 60
                                        min.coerceAtLeast(1)
                                    } catch (_: Exception) {
                                        1
                                    }
                                }

                                val entity = SleepEntity(
                                    id = UUID.randomUUID().toString(),
                                    childId = targetChildId,
                                    date = currentState.startDate.ifBlank { LocalDate.now().toString() },
                                    startTime = startTimeStr,
                                    endTime = endTimeStr,
                                    durationMinutes = durationMin,
                                    type = currentState.type.ifBlank { "siesta" },
                                    quality = currentState.quality.ifBlank { "tranquilo" },
                                    notes = currentState.notes.ifBlank { null }
                                )
                                db.sleepDao().insert(entity)

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        appCtx,
                                        "😴 Sueño guardado (${durationMin} min)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PequeCare", "Error guardando sueño desde notificación", e)
                        } finally {
                            stopSelf()
                        }
                    }
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        // START_STICKY garantiza que si el sistema mata el proceso por falta de RAM,
        // Android recreará el servicio automáticamente y restaurará el contador desde SharedPreferences.
        return START_STICKY
    }

    private fun promoteToForeground(state: ActiveSleepState) {
        createNotificationChannel()
        val notification = buildNotification(state)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            // Forzar reposteo en SystemUI por si el usuario deslizó/cerró la notificación en Android 14+
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            android.util.Log.e("PequeCare", "Error iniciando foreground service de sueño", e)
        }
    }

    /**
     * Construye la notificación persistente usando el Chronometer nativo de Android (setUsesChronometer).
     * Incluye botones de acción directos para Pausar/Reanudar y Detener (guardando el sueño),
     * además de un deleteIntent que la vuelve a mostrar automáticamente si se desliza por error.
     */
    private fun buildNotification(state: ActiveSleepState): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            9902,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Si el usuario desliza/cierra la notificación en Android 14+, volver a publicarla inmediatamente
        val restoreIntent = Intent(this, SleepTrackerService::class.java).apply {
            action = ACTION_RESTORE
        }
        val deletePendingIntent = PendingIntent.getService(
            this,
            9906,
            restoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val childLabel = if (state.childName.isNotBlank()) state.childName else "Tu peque"
        val sleepTypeLabel = if (state.type == "noche") "Sueño nocturno" else "Siesta"

        val title = if (state.isPaused) {
            "⏸️ Contador de sueño en pausa ($childLabel)"
        } else {
            "😴 $childLabel está durmiendo ($sleepTypeLabel)"
        }

        val subtitle = if (state.isPaused) {
            val mins = state.accumulatedSeconds / 60
            "Pausado • Acumulado: ${mins} min (Inició ${state.startTimeStr} hs)"
        } else {
            "Inició a las ${state.startTimeStr} hs • Toca para controlar en PequeCare"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(deletePendingIntent)

        // 1. Botón Pausar / Reanudar
        if (!state.isPaused) {
            val effectiveStartMs = System.currentTimeMillis() - (state.currentElapsedSeconds() * 1000L)
            builder.setWhen(effectiveStartMs)
            builder.setShowWhen(true)
            builder.setUsesChronometer(true)

            val pauseActionIntent = Intent(this, SleepTrackerService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePending = PendingIntent.getService(
                this,
                9904,
                pauseActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_pause, "Pausar", pausePending)
        } else {
            val resumeActionIntent = Intent(this, SleepTrackerService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePending = PendingIntent.getService(
                this,
                9903,
                resumeActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_media_play, "Reanudar", resumePending)
        }

        // 2. Botón Detener y Guardar directamente desde la notificación
        val stopAndSaveIntent = Intent(this, SleepTrackerService::class.java).apply {
            action = ACTION_STOP_AND_SAVE
        }
        val stopAndSavePending = PendingIntent.getService(
            this,
            9905,
            stopAndSaveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(android.R.drawable.ic_delete, "Detener y Guardar", stopAndSavePending)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Contador de Sueño Persistente",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Mantiene activo el cronómetro de sueño en la barra de estado y pantalla de bloqueo"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }
}
