package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val fontSize: String = "medium", // "small", "medium", "large"
    val fontFamily: String = "default", // "default", "naskh", "kufi"
    val themeMode: String = "system", // "system", "light", "dark"
    val voiceDictationEnabled: Boolean = true,
    val passingGrade: Float = 50f
)
