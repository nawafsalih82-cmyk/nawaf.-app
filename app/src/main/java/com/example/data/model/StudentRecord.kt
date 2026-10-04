package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale
import kotlin.math.roundToInt

@Entity(tableName = "students")
data class StudentRecord(
    @PrimaryKey
    val id: String = "", // e.g. "1_1" for sheet 1, student 1
    val sheetIndex: Int = 1, // 1 to 35
    val studentIndex: Int = 1, // 1 to 25
    val studentName: String = "",
    val firstTermScore: Float? = null,
    val midYearScore: Float? = null,
    val secondTermScore: Float? = null,
    val finalExamScore: Float? = null,
    val notes: String = ""
) {
    /**
     * معادلة السعي السنوي:
     * (معدل الفصل الأول + نصف السنة + معدل الفصل الثاني) ÷ 3
     * تظهر فقط عند توفر جميع الحقول المطلوبة الثلاثة
     */
    val annualStriveScore: Float?
        get() {
            return if (firstTermScore != null && midYearScore != null && secondTermScore != null) {
                val sum = firstTermScore + midYearScore + secondTermScore
                (sum / 3f * 10f).roundToInt() / 10f
            } else {
                null
            }
        }

    /**
     * الدرجة النهائية:
     * السعي السنوي + الامتحان النهائي
     */
    val finalGradeScore: Float?
        get() {
            val strive = annualStriveScore
            return if (strive != null && finalExamScore != null) {
                val total = strive + finalExamScore
                (total * 10f).roundToInt() / 10f
            } else {
                null
            }
        }

    companion object {
        fun formatScore(score: Float?): String {
            if (score == null) return "-"
            return if (score % 1.0f == 0.0f) {
                score.toInt().toString()
            } else {
                String.format(Locale.US, "%.1f", score)
            }
        }
    }
}
