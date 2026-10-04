package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.AppSettings
import com.example.data.model.SchoolTeacherInfo
import com.example.data.model.SheetMeta
import com.example.data.model.StudentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeBookDao {

    @Query("SELECT * FROM students WHERE sheetIndex = :sheetIndex ORDER BY studentIndex ASC")
    fun getStudentsForSheet(sheetIndex: Int): Flow<List<StudentRecord>>

    @Query("SELECT * FROM students WHERE sheetIndex = :sheetIndex ORDER BY studentIndex ASC")
    suspend fun getStudentsForSheetSync(sheetIndex: Int): List<StudentRecord>

    @Query("SELECT * FROM students ORDER BY sheetIndex ASC, studentIndex ASC")
    fun getAllStudents(): Flow<List<StudentRecord>>

    @Query("SELECT * FROM students ORDER BY sheetIndex ASC, studentIndex ASC")
    suspend fun getAllStudentsSync(): List<StudentRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentRecord>)

    @Query("DELETE FROM students WHERE sheetIndex = :sheetIndex")
    suspend fun deleteStudentsForSheet(sheetIndex: Int)

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    // School & Teacher Info
    @Query("SELECT * FROM school_info WHERE id = 1")
    fun getSchoolInfo(): Flow<SchoolTeacherInfo?>

    @Query("SELECT * FROM school_info WHERE id = 1")
    suspend fun getSchoolInfoSync(): SchoolTeacherInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolInfo(info: SchoolTeacherInfo)

    // App Settings
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsSync(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)

    // Sheet Metadata
    @Query("SELECT * FROM sheet_meta ORDER BY sheetIndex ASC")
    fun getAllSheetMeta(): Flow<List<SheetMeta>>

    @Query("SELECT * FROM sheet_meta ORDER BY sheetIndex ASC")
    suspend fun getAllSheetMetaSync(): List<SheetMeta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheetMeta(meta: SheetMeta)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSheetMeta(metas: List<SheetMeta>)

    @Transaction
    suspend fun resetSheet(sheetIndex: Int) {
        deleteStudentsForSheet(sheetIndex)
        val defaultRows = (1..25).map { index ->
            StudentRecord(
                id = "${sheetIndex}_${index}",
                sheetIndex = sheetIndex,
                studentIndex = index
            )
        }
        insertStudents(defaultRows)
    }
}
