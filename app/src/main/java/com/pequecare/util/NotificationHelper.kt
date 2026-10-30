package com.pequecare.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pequecare.MainActivity
import com.pequecare.R
import com.pequecare.receiver.PequeCareAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationHelper {
    const val CHANNEL_APPOINTMENTS = "pequecare_appointments"
    const val CHANNEL_MEDICATIONS = "pequecare_medications"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val apptChannel = NotificationChannel(
                CHANNEL_APPOINTMENTS,
                "Turnos Médicos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de citas y turnos pediátricos"
                enableVibration(true)
            }

            val medChannel = NotificationChannel(
                CHANNEL_MEDICATIONS,
                "Medicamentos y Tratamientos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de horarios de administración de medicamentos"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(apptChannel)
            notificationManager.createNotificationChannel(medChannel)
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleAppointment24hNotification(
        context: Context,
        appointmentId: String,
        doctorName: String,
        specialty: String,
        clinic: String,
        dateStr: String,
        timeStr: String
    ) {
        val date = try { LocalDate.parse(dateStr) } catch (_: Exception) { LocalDate.now() }
        val time = try { LocalTime.parse(timeStr) } catch (_: Exception) { LocalTime.of(10, 0) }
        val appointmentDateTime = LocalDateTime.of(date, time)
        val appointmentEpochMillis =
            appointmentDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 24 horas antes:
        val triggerMillis = appointmentEpochMillis - (24 * 60 * 60 * 1000L)
        val now = System.currentTimeMillis()

        // Si faltan menos de 24 horas pero aún está en el futuro, avisar 2 minutos después para prueba/alerta
        val finalTriggerMillis = when {
            triggerMillis > now -> triggerMillis
            appointmentEpochMillis > now + (30 * 60 * 1000L) -> now + (60 * 1000L)
            else -> return // El turno ya pasó
        }

        val intent = Intent(context, PequeCareAlarmReceiver::class.java).apply {
            putExtra(PequeCareAlarmReceiver.EXTRA_TYPE, PequeCareAlarmReceiver.TYPE_APPOINTMENT)
            putExtra(PequeCareAlarmReceiver.EXTRA_TITLE, "🩺 Recordatorio de Turno Médico")
            putExtra(
                PequeCareAlarmReceiver.EXTRA_MESSAGE,
                "Recordatorio de turno con $doctorName ($specialty) a las $timeStr hs en $clinic"
            )
            putExtra(
                PequeCareAlarmReceiver.EXTRA_NOTIFICATION_ID,
                kotlin.math.abs(appointmentId.hashCode())
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            kotlin.math.abs(appointmentId.hashCode()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(context, finalTriggerMillis, pendingIntent)
    }

    fun scheduleMedicationNotification(
        context: Context,
        medicationId: String,
        medName: String,
        dosage: String,
        startEpochMillis: Long,
        intervalMillis: Long
    ) {
        val now = System.currentTimeMillis()
        val triggerMillis = if (startEpochMillis >= now) startEpochMillis else now + 5000L

        val intent = Intent(context, PequeCareAlarmReceiver::class.java).apply {
            putExtra(PequeCareAlarmReceiver.EXTRA_TYPE, PequeCareAlarmReceiver.TYPE_MEDICATION)
            putExtra(PequeCareAlarmReceiver.EXTRA_TITLE, "💊 Hora de Medicación")
            putExtra(
                PequeCareAlarmReceiver.EXTRA_MESSAGE,
                "Es momento de suministrar $medName ($dosage)"
            )
            putExtra(
                PequeCareAlarmReceiver.EXTRA_NOTIFICATION_ID,
                kotlin.math.abs(medicationId.hashCode())
            )
            putExtra(PequeCareAlarmReceiver.EXTRA_MED_ID, medicationId)
            putExtra(PequeCareAlarmReceiver.EXTRA_INTERVAL_MILLIS, intervalMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            kotlin.math.abs(medicationId.hashCode()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(context, triggerMillis, pendingIntent)
    }

    fun cancelReminder(context: Context, id: String) {
        val intent = Intent(context, PequeCareAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            kotlin.math.abs(id.hashCode()),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun scheduleAlarm(context: Context, triggerMillis: Long, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }
}
