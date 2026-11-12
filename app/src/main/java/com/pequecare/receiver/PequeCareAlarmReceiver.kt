package com.pequecare.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pequecare.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PequeCareAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_MED_ID = "extra_med_id"
        const val EXTRA_INTERVAL_MILLIS = "extra_interval_millis"

        const val TYPE_APPOINTMENT = "TYPE_APPOINTMENT"
        const val TYPE_MEDICATION = "TYPE_MEDICATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            com.pequecare.service.SleepTrackerService.ensureServiceRunningIfActive(context)
            return
        }

        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "PequeCare"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Recordatorio"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        val channelId = if (type == TYPE_MEDICATION) {
            NotificationHelper.CHANNEL_MEDICATIONS
        } else {
            NotificationHelper.CHANNEL_APPOINTMENTS
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = com.pequecare.data.local.db.PequeCareDatabase.getDatabase(context)

                // 1. Validar que la entidad siga existiendo en la base de datos antes de mostrar la notificación
                val shouldShowNotification = when (type) {
                    TYPE_MEDICATION -> {
                        val medId = intent.getStringExtra(EXTRA_MED_ID)
                        if (!medId.isNullOrBlank()) {
                            val med = db.medicationTreatmentDao().getMedicationById(medId)
                            med != null && med.isActive && med.status.equals("ACTIVO", ignoreCase = true)
                        } else false
                    }
                    TYPE_APPOINTMENT -> {
                        val apptId = intent.getStringExtra("extra_appointment_id")
                        if (!apptId.isNullOrBlank()) {
                            val appt = db.medicalAppointmentDao().getAppointmentById(apptId)
                            appt != null && !appt.status.equals("pasado", ignoreCase = true)
                        } else false
                    }
                    else -> false
                }

                if (shouldShowNotification) {
                    NotificationHelper.showNotification(
                        context = context,
                        channelId = channelId,
                        title = title,
                        message = message,
                        notificationId = notificationId
                    )

                    // 2. Si es una alarma de medicación periódica activa, reprogramar la siguiente dosis
                    if (type == TYPE_MEDICATION) {
                        val intervalMillis = intent.getLongExtra(EXTRA_INTERVAL_MILLIS, 0L)
                        val medId = intent.getStringExtra(EXTRA_MED_ID)
                        if (intervalMillis > 0 && !medId.isNullOrBlank()) {
                            val nextTrigger = System.currentTimeMillis() + intervalMillis
                            val dosage = message.substringAfter("(", "").substringBefore(")", "dosis")
                            val medName = message.substringAfter("suministrar ", "").substringBefore(" (", "Medicación")
                            NotificationHelper.scheduleMedicationNotification(
                                context = context,
                                medicationId = medId,
                                medName = medName,
                                dosage = dosage,
                                startEpochMillis = nextTrigger,
                                intervalMillis = intervalMillis
                            )
                        }
                    }
                } else {
                    // Si ya no existe o no está activo, cancelar cualquier residuo de alarma o notificación
                    val idToCancel = intent.getStringExtra(EXTRA_MED_ID) ?: intent.getStringExtra("extra_appointment_id")
                    if (!idToCancel.isNullOrBlank()) {
                        NotificationHelper.cancelReminder(context, idToCancel)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
