package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SchoolTeacherInfo
import com.example.data.model.StudentRecord
import com.example.util.PdfPrintHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("سجل درجات الأستاذ نواف", appName)
    }

    @Test
    fun `test annual strive and final grade calculations from prompt`() {
        val student = StudentRecord(
            id = "1_1",
            sheetIndex = 1,
            studentIndex = 1,
            studentName = "فاطمة أحمد",
            firstTermScore = 80f,
            midYearScore = 90f,
            secondTermScore = 85f,
            finalExamScore = 90f
        )

        assertEquals(85f, student.annualStriveScore)
        assertEquals(175f, student.finalGradeScore)
    }

    @Test
    fun `test strive present but final exam missing`() {
        val student = StudentRecord(
            id = "1_2",
            sheetIndex = 1,
            studentIndex = 2,
            studentName = "مريم حسين",
            firstTermScore = 90f,
            midYearScore = 90f,
            secondTermScore = 90f,
            finalExamScore = null
        )

        assertEquals(90f, student.annualStriveScore)
        assertNull(student.finalGradeScore)
    }

    @Test
    fun `test missing term field returns null for strive and final`() {
        val student = StudentRecord(
            id = "1_3",
            sheetIndex = 1,
            studentIndex = 3,
            studentName = "زينب علي",
            firstTermScore = 80f,
            midYearScore = null,
            secondTermScore = 85f,
            finalExamScore = 90f
        )

        assertNull(student.annualStriveScore)
        assertNull(student.finalGradeScore)
    }

    @Test
    fun `test sheets independence`() {
        val studentSheet1 = StudentRecord(
            id = "1_5",
            sheetIndex = 1,
            studentIndex = 5,
            studentName = "طالبة في السجل الأول",
            firstTermScore = 95f
        )
        val studentSheet35 = StudentRecord(
            id = "35_5",
            sheetIndex = 35,
            studentIndex = 5,
            studentName = "طالبة في السجل الخامس والثلاثين",
            firstTermScore = 70f
        )

        assertNotEquals(studentSheet1.id, studentSheet35.id)
        assertEquals("1_5", studentSheet1.id)
        assertEquals("35_5", studentSheet35.id)
        assertNotEquals(studentSheet1.studentName, studentSheet35.studentName)
    }

    @Test
    fun `test PDF and Print HTML generation meets all official specifications`() {
        val school = SchoolTeacherInfo(
            schoolName = "متوسطة الرسالة للبنات",
            teacherName = "الأستاذ نواف صالح",
            gradeLevel = "الثالث متوسط",
            section = "أ"
        )
        val students = (1..25).map { idx ->
            if (idx == 1) {
                StudentRecord(
                    id = "1_1",
                    sheetIndex = 1,
                    studentIndex = 1,
                    studentName = "نور الهدى كريم",
                    firstTermScore = 80f,
                    midYearScore = 90f,
                    secondTermScore = 85f,
                    finalExamScore = 90f
                )
            } else {
                StudentRecord(id = "1_$idx", sheetIndex = 1, studentIndex = idx)
            }
        }

        val html = PdfPrintHelper.generateHtmlGradeBook(school, 1, students)

        // 1. Must specify A4 landscape
        assertTrue("Must have A4 landscape page setup", html.contains("size: A4 landscape"))
        // 2. RTL direction
        assertTrue("Must be RTL", html.contains("dir=\"rtl\""))
        // 3. School and Teacher info
        assertTrue("Must contain school name", html.contains("متوسطة الرسالة للبنات"))
        assertTrue("Must contain teacher name", html.contains("الأستاذ نواف صالح"))
        assertTrue("Must contain sheet indicator", html.contains("السجل 1 من 35"))
        // 4. Calculations
        assertTrue("Must contain student name", html.contains("نور الهدى كريم"))
        assertTrue("Must contain calculated strive 85", html.contains("85"))
        assertTrue("Must contain calculated final 175", html.contains("175"))
        // 5. Must not contain null or undefined
        assertFalse("Must not contain literal 'null'", html.contains(">null<"))
        assertFalse("Must not contain literal 'undefined'", html.contains("undefined"))
        // 6. Contains teacher signature line
        assertTrue("Must contain teacher signature line", html.contains("التوقيع:"))
    }
}
