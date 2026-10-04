package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sheet_meta")
data class SheetMeta(
    @PrimaryKey
    val sheetIndex: Int, // 1 to 35
    val title: String = "سجل $sheetIndex",
    val customGradeLevel: String = "",
    val customSection: String = "",
    val subject: String = ""
)
