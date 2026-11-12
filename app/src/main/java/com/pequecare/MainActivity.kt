package com.pequecare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.pequecare.service.SleepTrackerService
import com.pequecare.ui.PequeCareViewModel
import com.pequecare.ui.navigation.PequeCareApp
import com.pequecare.util.BatteryOptimizationHelper
import com.pequecare.util.NotificationHelper

class MainActivity : ComponentActivity() {
    private val viewModel: PequeCareViewModel by viewModels()

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permiso concedido o denegado
            requestBatteryExemptionOnceIfNeeded()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannels(this)
        SleepTrackerService.ensureServiceRunningIfActive(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestBatteryExemptionOnceIfNeeded()
            }
        } else {
            requestBatteryExemptionOnceIfNeeded()
        }

        setContent {
            PequeCareApp(viewModel = viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        // Al volver a abrir la app, restaurar el estado del contador y volver a mostrar la notificación si estaba activo
        SleepTrackerService.ensureServiceRunningIfActive(this)
    }

    private fun requestBatteryExemptionOnceIfNeeded() {
        val prefs = getSharedPreferences("pequecare_prefs", MODE_PRIVATE)
        val alreadyPrompted = prefs.getBoolean("battery_opt_prompted", false)
        if (!alreadyPrompted && !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)) {
            prefs.edit().putBoolean("battery_opt_prompted", true).apply()
            BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
        }
    }
}
