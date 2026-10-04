package com.example.data.repository

import com.example.data.local.GradeBookDao
import com.example.data.model.AppSettings
import com.example.data.model.SchoolTeacherInfo
import com.example.data.model.SheetMeta
import com.example.data.model.StudentRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GradeBookRepository(private val dao: GradeBookDao) {

    fun getSchoolInfo(): Flow<SchoolTeacherInfo> {
        return dao.getSchoolInfo().map {
            it ?: SchoolTeacherInfo()
        }
    }

    suspend fun saveSchoolInfo(info: SchoolTeacherInfo) = withContext(Dispatchers.IO) {
        dao.insertSchoolInfo(info)
    }

    fun getSettings(): Flow<AppSettings> {
        return dao.getSettings().map {
            it ?: AppSettings()
        }
    }

    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        dao.insertSettings(settings)
    }

    fun getSheetMetaList(): Flow<List<SheetMeta>> {
        return dao.getAllSheetMeta()
    }

    suspend fun saveSheetMeta(meta: SheetMeta) = withContext(Dispatchers.IO) {
        dao.insertSheetMeta(meta)
    }

    fun getStudentsForSheet(sheetIndex: Int): Flow<List<StudentRecord>> {
        return dao.getStudentsForSheet(sheetIndex).map { list ->
            if (list.size < 25) {
                // Ensure all 25 rows exist
                val mapByIndex = list.associateBy { it.studentIndex }
                (1..25).map { idx ->
                    mapByIndex[idx] ?: StudentRecord(
                        id = "${sheetIndex}_${idx}",
                        sheetIndex = sheetIndex,
                        studentIndex = idx
                    )
                }
            } else {
                list
            }
        }
    }

    suspend fun saveStudent(student: StudentRecord) = withContext(Dispatchers.IO) {
        dao.insertStudent(student)
    }

    suspend fun saveStudents(students: List<StudentRecord>) = withContext(Dispatchers.IO) {
        dao.insertStudents(students)
    }

    suspend fun clearSheet(sheetIndex: Int) = withContext(Dispatchers.IO) {
        dao.resetSheet(sheetIndex)
    }

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val existingInfo = dao.getSchoolInfoSync()
        if (existingInfo == null) {
            dao.insertSchoolInfo(SchoolTeacherInfo())
            dao.insertSettings(AppSettings())
            val sheetMetas = (1..35).map { SheetMeta(it, "سجل $it") }
            dao.insertAllSheetMeta(sheetMetas)
            val initial = mutableListOf<StudentRecord>()
            for (sheet in 1..35) {
                for (student in 1..25) {
                    initial.add(
                        StudentRecord(
                            id = "${sheet}_${student}",
                            sheetIndex = sheet,
                            studentIndex = student
                        )
                    )
                }
            }
            dao.insertStudents(initial)
        }
    }

    // Export entire database to JSON String
    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "سجل درجات الأستاذ نواف")
        root.put("exportTimestamp", System.currentTimeMillis())

        // School Info
        val school = dao.getSchoolInfoSync() ?: SchoolTeacherInfo()
        val schoolObj = JSONObject()
        schoolObj.put("schoolName", school.schoolName)
        schoolObj.put("teacherName", school.teacherName)
        schoolObj.put("gradeLevel", school.gradeLevel)
        schoolObj.put("section", school.section)
        root.put("schoolInfo", schoolObj)

        // Settings
        val settings = dao.getSettingsSync() ?: AppSettings()
        val setObj = JSONObject()
        setObj.put("fontSize", settings.fontSize)
        setObj.put("fontFamily", settings.fontFamily)
        setObj.put("themeMode", settings.themeMode)
        setObj.put("voiceDictationEnabled", settings.voiceDictationEnabled)
        setObj.put("passingGrade", settings.passingGrade.toDouble())
        root.put("settings", setObj)

        // Sheet Metas
        val metas = dao.getAllSheetMetaSync()
        val metasArray = JSONArray()
        for (m in metas) {
            val obj = JSONObject()
            obj.put("sheetIndex", m.sheetIndex)
            obj.put("title", m.title)
            obj.put("customGradeLevel", m.customGradeLevel)
            obj.put("customSection", m.customSection)
            obj.put("subject", m.subject)
            metasArray.put(obj)
        }
        root.put("sheetMeta", metasArray)

        // Students
        val students = dao.getAllStudentsSync()
        val studentsArray = JSONArray()
        for (s in students) {
            // Keep all rows or only rows with data, to be safe keep all
            val obj = JSONObject()
            obj.put("sheetIndex", s.sheetIndex)
            obj.put("studentIndex", s.studentIndex)
            obj.put("studentName", s.studentName)
            if (s.firstTermScore != null) obj.put("firstTermScore", s.firstTermScore.toDouble())
            if (s.midYearScore != null) obj.put("midYearScore", s.midYearScore.toDouble())
            if (s.secondTermScore != null) obj.put("secondTermScore", s.secondTermScore.toDouble())
            if (s.finalExamScore != null) obj.put("finalExamScore", s.finalExamScore.toDouble())
            obj.put("notes", s.notes)
            studentsArray.put(obj)
        }
        root.put("students", studentsArray)

        root.toString(2)
    }

    // Import from JSON String
    suspend fun importFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("schoolInfo")) {
                val sc = root.getJSONObject("schoolInfo")
                dao.insertSchoolInfo(
                    SchoolTeacherInfo(
                        id = 1,
                        schoolName = sc.optString("schoolName", "متوسطة الرسالة للبنات"),
                        teacherName = sc.optString("teacherName", "الأستاذ نواف صالح"),
                        gradeLevel = sc.optString("gradeLevel", "الثالث متوسط"),
                        section = sc.optString("section", "أ")
                    )
                )
            }

            if (root.has("settings")) {
                val st = root.getJSONObject("settings")
                dao.insertSettings(
                    AppSettings(
                        id = 1,
                        fontSize = st.optString("fontSize", "medium"),
                        fontFamily = st.optString("fontFamily", "default"),
                        themeMode = st.optString("themeMode", "system"),
                        voiceDictationEnabled = st.optBoolean("voiceDictationEnabled", true),
                        passingGrade = st.optDouble("passingGrade", 50.0).toFloat()
                    )
                )
            }

            if (root.has("sheetMeta")) {
                val arr = root.getJSONArray("sheetMeta")
                val metas = mutableListOf<SheetMeta>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    metas.add(
                        SheetMeta(
                            sheetIndex = obj.getInt("sheetIndex"),
                            title = obj.optString("title", "سجل ${obj.getInt("sheetIndex")}"),
                            customGradeLevel = obj.optString("customGradeLevel", ""),
                            customSection = obj.optString("customSection", ""),
                            subject = obj.optString("subject", "")
                        )
                    )
                }
                if (metas.isNotEmpty()) {
                    dao.insertAllSheetMeta(metas)
                }
            }

            if (root.has("students")) {
                val arr = root.getJSONArray("students")
                val students = mutableListOf<StudentRecord>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val sheetIdx = obj.getInt("sheetIndex")
                    val studentIdx = obj.getInt("studentIndex")
                    val firstTerm = if (obj.has("firstTermScore") && !obj.isNull("firstTermScore")) obj.getDouble("firstTermScore").toFloat() else null
                    val midYear = if (obj.has("midYearScore") && !obj.isNull("midYearScore")) obj.getDouble("midYearScore").toFloat() else null
                    val secondTerm = if (obj.has("secondTermScore") && !obj.isNull("secondTermScore")) obj.getDouble("secondTermScore").toFloat() else null
                    val finalExam = if (obj.has("finalExamScore") && !obj.isNull("finalExamScore")) obj.getDouble("finalExamScore").toFloat() else null

                    students.add(
                        StudentRecord(
                            id = "${sheetIdx}_${studentIdx}",
                            sheetIndex = sheetIdx,
                            studentIndex = studentIdx,
                            studentName = obj.optString("studentName", ""),
                            firstTermScore = firstTerm,
                            midYearScore = midYear,
                            secondTermScore = secondTerm,
                            finalExamScore = finalExam,
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                if (students.isNotEmpty()) {
                    dao.insertStudents(students)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
