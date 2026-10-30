package com.pequecare.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object CalendarHelper {
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        location: String,
        dateStr: String, // YYYY-MM-DD
        timeStr: String  // HH:mm
    ) {
        val date = try { LocalDate.parse(dateStr) } catch (_: Exception) { LocalDate.now() }
        val time = try { LocalTime.parse(timeStr) } catch (_: Exception) { LocalTime.of(10, 0) }
        val dateTime = LocalDateTime.of(date, time)
        val startMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = startMillis + (60 * 60 * 1000L) // 1 hora de consulta

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Reminders.MINUTES, 1440) // Recordatorio 24 horas antes
            putExtra(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Manejar si no hay app de calendario instalada
        }
    }
}
