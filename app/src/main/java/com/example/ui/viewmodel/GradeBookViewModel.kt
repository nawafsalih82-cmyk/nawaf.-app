package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.SchoolTeacherInfo
import com.example.data.model.SheetMeta
import com.example.data.model.StudentRecord
import com.example.data.repository.GradeBookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed class AppScreen {
    object Home : AppScreen()
    object GradeBook : AppScreen()
    object Settings : AppScreen()
    object About : AppScreen()
}

data class SheetStatistics(
    val sheetIndex: Int,
    val totalStudents: Int = 25,
    val enteredCount: Int = 0,
    val completedCount: Int = 0,
    val passingCount: Int = 0,
    val failingCount: Int = 0,
    val passPercentage: Float = 0f,
    val averageAnnualStrive: Float = 0f,
    val averageFinalGrade: Float = 0f,
    val highestGrade: Float = 0f,
    val lowestGrade: Float = 0f,
    val maxPossibleStrive: Float = 100f,
    val strivePercentageOfMax: Float = 0f
)

@OptIn(ExperimentalCoroutinesApi::class)
class GradeBookViewModel(
    private val repository: GradeBookRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentSheetIndex = MutableStateFlow(1)
    val currentSheetIndex: StateFlow<Int> = _currentSheetIndex.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _statistics = MutableStateFlow<SheetStatistics?>(null)
    val statistics: StateFlow<SheetStatistics?> = _statistics.asStateFlow()

    val schoolInfo: StateFlow<SchoolTeacherInfo> = repository.getSchoolInfo()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SchoolTeacherInfo()
        )

    val settings: StateFlow<AppSettings> = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val sheetMetas: StateFlow<List<SheetMeta>> = repository.getSheetMetaList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSheetStudents = MutableStateFlow<List<StudentRecord>>(
        (1..25).map { idx -> StudentRecord(id = "1_${idx}", sheetIndex = 1, studentIndex = idx) }
    )
    val currentSheetStudents: StateFlow<List<StudentRecord>> = _currentSheetStudents.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            // Observe repository flow for current sheet
            _currentSheetIndex.collect { sheetIdx ->
                repository.getStudentsForSheet(sheetIdx).collect { list ->
                    _currentSheetStudents.value = list
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.GradeBook) {
            _statistics.value = null
        }
    }

    fun selectSheet(index: Int) {
        if (index in 1..35) {
            _currentSheetIndex.value = index
            _statistics.value = null
        }
    }

    fun nextSheet() {
        if (_currentSheetIndex.value < 35) {
            selectSheet(_currentSheetIndex.value + 1)
        }
    }

    fun prevSheet() {
        if (_currentSheetIndex.value > 1) {
            selectSheet(_currentSheetIndex.value - 1)
        }
    }

    fun saveSchoolInfo(info: SchoolTeacherInfo) {
        viewModelScope.launch {
            repository.saveSchoolInfo(info)
            _userMessage.emit("تم حفظ البيانات بنجاح")
        }
    }

    fun updateStudent(student: StudentRecord) {
        // 1. Instant update in memory for real-time recalculation of strive and final grade
        val currentList = _currentSheetStudents.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentIndex == student.studentIndex }
        if (index != -1) {
            currentList[index] = student
            _currentSheetStudents.value = currentList
        }
        // 2. Asynchronous local database persistence
        viewModelScope.launch {
            repository.saveStudent(student)
        }
    }

    fun saveAllStudents(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val current = currentSheetStudents.value
            repository.saveStudents(current)
            _userMessage.emit("تم حفظ البيانات بنجاح")
            onSaved()
        }
    }

    fun clearCurrentSheet(onCleared: () -> Unit = {}) {
        viewModelScope.launch {
            val sheet = _currentSheetIndex.value
            repository.clearSheet(sheet)
            _statistics.value = null
            _userMessage.emit("تم مسح بيانات السجل")
            onCleared()
        }
    }

    fun calculatePercentage() {
        val students = currentSheetStudents.value
        val passingThreshold = settings.value.passingGrade

        val enteredList = students.filter {
            it.studentName.isNotBlank() ||
                    it.firstTermScore != null ||
                    it.midYearScore != null ||
                    it.secondTermScore != null ||
                    it.finalExamScore != null
        }

        val completedWithFinalGrade = students.mapNotNull { it.finalGradeScore }
        val completedWithStrive = students.mapNotNull { it.annualStriveScore }

        val passingCount = if (completedWithFinalGrade.isNotEmpty()) {
            // If final exam is out of 100 and strive is out of 100, max total is 200, passing threshold standard is 100 (or configured)
            val effectiveThreshold = if (passingThreshold <= 50f) 100f else passingThreshold
            completedWithFinalGrade.count { it >= effectiveThreshold }
        } else if (completedWithStrive.isNotEmpty()) {
            completedWithStrive.count { it >= passingThreshold }
        } else {
            0
        }

        val activeCount = if (completedWithFinalGrade.isNotEmpty()) {
            completedWithFinalGrade.size
        } else if (completedWithStrive.isNotEmpty()) {
            completedWithStrive.size
        } else {
            enteredList.size
        }

        val failingCount = if (activeCount > passingCount) activeCount - passingCount else 0

        val passRate = if (activeCount > 0) {
            ((passingCount.toFloat() / activeCount.toFloat()) * 100f * 10f).roundToInt() / 10f
        } else 0f

        val avgStrive = if (completedWithStrive.isNotEmpty()) {
            val sum = completedWithStrive.sum()
            (sum / completedWithStrive.size * 10f).roundToInt() / 10f
        } else 0f

        val avgFinal = if (completedWithFinalGrade.isNotEmpty()) {
            val sum = completedWithFinalGrade.sum()
            (sum / completedWithFinalGrade.size * 10f).roundToInt() / 10f
        } else 0f

        val highest = if (completedWithFinalGrade.isNotEmpty()) {
            completedWithFinalGrade.maxOrNull() ?: 0f
        } else if (completedWithStrive.isNotEmpty()) {
            completedWithStrive.maxOrNull() ?: 0f
        } else 0f

        val lowest = if (completedWithFinalGrade.isNotEmpty()) {
            completedWithFinalGrade.minOrNull() ?: 0f
        } else if (completedWithStrive.isNotEmpty()) {
            completedWithStrive.minOrNull() ?: 0f
        } else 0f

        val strivePercentOfMax = if (avgStrive > 0f) {
            (avgStrive / 100f * 100f * 10f).roundToInt() / 10f
        } else 0f

        _statistics.value = SheetStatistics(
            sheetIndex = _currentSheetIndex.value,
            totalStudents = 25,
            enteredCount = enteredList.size,
            completedCount = activeCount,
            passingCount = passingCount,
            failingCount = failingCount,
            passPercentage = passRate,
            averageAnnualStrive = avgStrive,
            averageFinalGrade = avgFinal,
            highestGrade = highest,
            lowestGrade = lowest,
            maxPossibleStrive = 100f,
            strivePercentageOfMax = strivePercentOfMax
        )

        viewModelScope.launch {
            _userMessage.emit("تم حساب النسبة المئوية بنجاح")
        }
    }

    fun updateSettings(
        fontSize: String = settings.value.fontSize,
        fontFamily: String = settings.value.fontFamily,
        themeMode: String = settings.value.themeMode,
        voiceDictationEnabled: Boolean = settings.value.voiceDictationEnabled,
        passingGrade: Float = settings.value.passingGrade
    ) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                fontSize = fontSize,
                fontFamily = fontFamily,
                themeMode = themeMode,
                voiceDictationEnabled = voiceDictationEnabled,
                passingGrade = passingGrade
            )
            repository.saveSettings(updated)
            _userMessage.emit("تم حفظ الإعدادات")
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            val defaultSettings = AppSettings(
                id = 1,
                fontSize = "medium",
                fontFamily = "default",
                themeMode = "system",
                voiceDictationEnabled = true,
                passingGrade = 50f
            )
            repository.saveSettings(defaultSettings)
            _userMessage.emit("تمت استعادة الإعدادات الافتراضية بنجاح")
        }
    }

    suspend fun exportJson(): String {
        val json = repository.exportToJson()
        _userMessage.emit("تم تصدير البيانات بنجاح")
        return json
    }

    suspend fun importJson(jsonString: String): Boolean {
        val success = repository.importFromJson(jsonString)
        if (success) {
            _userMessage.emit("تم استيراد البيانات بنجاح")
            // reset statistics
            _statistics.value = null
        } else {
            _userMessage.emit("فشل في استيراد البيانات، يرجى التأكد من صحة الملف")
        }
        return success
    }
}

class GradeBookViewModelFactory(
    private val repository: GradeBookRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GradeBookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GradeBookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
