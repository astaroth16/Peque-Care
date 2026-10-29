package com.pequecare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.pequecare.ui.PequeCareViewModel
import com.pequecare.ui.navigation.PequeCareApp

class MainActivity : ComponentActivity() {
    private val viewModel: PequeCareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PequeCareApp(viewModel = viewModel)
        }
    }
}
