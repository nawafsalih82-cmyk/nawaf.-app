package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.GradeBookRepository
import com.example.ui.components.RtlProvider
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.GradeBookScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GradeBookViewModel
import com.example.ui.viewmodel.GradeBookViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: GradeBookViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GradeBookRepository(database.gradeBookDao())
        GradeBookViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GradeBookApp(viewModel = viewModel)
        }
    }
}

@Composable
fun GradeBookApp(viewModel: GradeBookViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val schoolInfo by viewModel.schoolInfo.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentSheetIndex by viewModel.currentSheetIndex.collectAsStateWithLifecycle()
    val students by viewModel.currentSheetStudents.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    val fontScale = when (settings.fontSize) {
        "small" -> 0.9f
        "large" -> 1.15f
        else -> 1.0f
    }

    val fontFamily = when (settings.fontFamily) {
        "naskh" -> FontFamily.Serif
        "kufi" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }

    MyApplicationTheme(
        darkTheme = isDark,
        fontFamily = fontFamily,
        fontScaleFactor = fontScale
    ) {
        RtlProvider {
            Surface(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    is AppScreen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            schoolInfo = schoolInfo,
                            voiceEnabled = settings.voiceDictationEnabled
                        )
                    }
                    is AppScreen.GradeBook -> {
                        GradeBookScreen(
                            viewModel = viewModel,
                            currentSheetIndex = currentSheetIndex,
                            students = students,
                            schoolInfo = schoolInfo,
                            statistics = statistics,
                            voiceEnabled = settings.voiceDictationEnabled
                        )
                    }
                    is AppScreen.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            settings = settings
                        )
                    }
                    is AppScreen.About -> {
                        AboutScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
