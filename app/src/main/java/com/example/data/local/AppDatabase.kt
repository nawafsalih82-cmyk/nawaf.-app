package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSettings
import com.example.data.model.SchoolTeacherInfo
import com.example.data.model.SheetMeta
import com.example.data.model.StudentRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentRecord::class,
        SchoolTeacherInfo::class,
        SheetMeta::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gradeBookDao(): GradeBookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teacher_gradebook_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.gradeBookDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: GradeBookDao) {
                // Initialize School & Teacher Info
                dao.insertSchoolInfo(
                    SchoolTeacherInfo(
                        id = 1,
                        schoolName = "متوسطة الرسالة للبنات",
                        teacherName = "الأستاذ نواف صالح",
                        gradeLevel = "الثالث متوسط",
                        section = "أ"
                    )
                )

                // Initialize App Settings
                dao.insertSettings(AppSettings())

                // Initialize 35 sheets metadata
                val sheetMetas = (1..35).map { index ->
                    SheetMeta(
                        sheetIndex = index,
                        title = "سجل $index"
                    )
                }
                dao.insertAllSheetMeta(sheetMetas)

                // Initialize 25 student records for each of the 35 sheets
                val initialStudents = mutableListOf<StudentRecord>()
                for (sheet in 1..35) {
                    for (student in 1..25) {
                        initialStudents.add(
                            StudentRecord(
                                id = "${sheet}_${student}",
                                sheetIndex = sheet,
                                studentIndex = student,
                                studentName = "",
                                notes = ""
                            )
                        )
                    }
                }
                dao.insertStudents(initialStudents)
            }
        }
    }
}
