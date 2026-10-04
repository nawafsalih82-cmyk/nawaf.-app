package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_info")
data class SchoolTeacherInfo(
    @PrimaryKey
    val id: Int = 1,
    val schoolName: String = "متوسطة الرسالة للبنات",
    val teacherName: String = "الأستاذ نواف صالح",
    val gradeLevel: String = "الثالث متوسط",
    val section: String = "أ"
)
