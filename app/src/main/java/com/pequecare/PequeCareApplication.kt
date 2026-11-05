package com.pequecare

import android.app.Application
import android.util.Log

class PequeCareApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Captura global de excepciones no controladas para registrarlas en Logcat
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("PequeCareCrash", "FATAL CRASH en hilo [${thread.name}]", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Log.i("PequeCare", "=======================================================")
        Log.i("PequeCare", "PequeCare iniciado exitosamente.")
        Log.i("PequeCare", "Versión: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
        Log.i("PequeCare", "Logcat activo y listo para depuración en Android Studio.")
        Log.i("PequeCare", "Filtros sugeridos en Logcat: 'tag:PequeCare' o 'package:mine'")
        Log.i("PequeCare", "=======================================================")
    }
}
