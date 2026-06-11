package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.PlannerDatabase
import com.example.data.PlannerRepository
import com.example.ui.PlannerMainScreen
import com.example.ui.PlannerViewModel
import com.example.ui.theme.SmartPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Layer Architecture
        val database = PlannerDatabase.getDatabase(this)
        val repository = PlannerRepository(database.plannerDao)
        val viewModelFactory = PlannerViewModel.provideFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[PlannerViewModel::class.java]

        setContent {
            SmartPlannerTheme(themePreset = viewModel.themePreset) {
                PlannerMainScreen(viewModel)
            }
        }
    }
}
