package com.pequecare.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pequecare.util.NotificationHelper

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
        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "PequeCare"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Recordatorio"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        val channelId = if (type == TYPE_MEDICATION) {
            NotificationHelper.CHANNEL_MEDICATIONS
        } else {
            NotificationHelper.CHANNEL_APPOINTMENTS
        }

        NotificationHelper.showNotification(
            context = context,
            channelId = channelId,
            title = title,
            message = message,
            notificationId = notificationId
        )

        // Si es una alarma de medicación periódica activa, reprogramar la siguiente dosis
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
    }
}
