package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.ui.platform.LocalContext
import com.example.util.PdfPrintHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolTeacherInfo
import com.example.data.model.StudentRecord
import com.example.ui.components.PercentageCard
import com.example.ui.components.ScoreInputField
import com.example.ui.components.VoiceDictationButton
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoSky
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GradeBookViewModel
import com.example.ui.viewmodel.SheetStatistics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeBookScreen(
    viewModel: GradeBookViewModel,
    currentSheetIndex: Int,
    students: List<StudentRecord>,
    schoolInfo: SchoolTeacherInfo,
    statistics: SheetStatistics?,
    voiceEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.Home)
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var isTableViewMode by remember { mutableStateOf(true) }
    var showSheetDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سجل الدرجات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "السجل $currentSheetIndex من 35 • ${schoolInfo.schoolName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoldAccent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Home) },
                        modifier = Modifier.testTag("back_to_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "العودة للرئيسية",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // Print Button in TopBar
                    IconButton(
                        onClick = {
                            PdfPrintHelper.printGradeBook(context, schoolInfo, currentSheetIndex, students)
                        },
                        modifier = Modifier.testTag("top_bar_print_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "طباعة السجل",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // PDF Button in TopBar
                    IconButton(
                        onClick = {
                            PdfPrintHelper.exportGradeBookPdf(context, schoolInfo, currentSheetIndex, students)
                        },
                        modifier = Modifier.testTag("top_bar_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "تصدير PDF",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Toggle View Mode (Table / Cards)
                    IconButton(
                        onClick = { isTableViewMode = !isTableViewMode },
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isTableViewMode) Icons.Default.ViewAgenda else Icons.Default.TableChart,
                            contentDescription = if (isTableViewMode) "عرض البطاقات" else "عرض الجدول",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Save Button
                    IconButton(
                        onClick = { viewModel.saveAllStudents() },
                        modifier = Modifier.testTag("save_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "حفظ البيانات",
                            tint = GoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Navigation Header
            item {
                SheetPaginationCard(
                    currentSheetIndex = currentSheetIndex,
                    onPrev = { viewModel.prevSheet() },
                    onNext = { viewModel.nextSheet() },
                    onSelectSheet = { viewModel.selectSheet(it) },
                    onBackToHome = { viewModel.navigateTo(AppScreen.Home) },
                    onClearSheet = { showClearConfirmDialog = true },
                    onSaveSheet = { viewModel.saveAllStudents() },
                    onPrint = {
                        PdfPrintHelper.printGradeBook(context, schoolInfo, currentSheetIndex, students)
                    },
                    onExportPdf = {
                        PdfPrintHelper.exportGradeBookPdf(context, schoolInfo, currentSheetIndex, students)
                    }
                )
            }

            // Students Content: Table or Cards
            if (isTableViewMode) {
                item {
                    GradeTableView(
                        students = students,
                        voiceEnabled = voiceEnabled,
                        onStudentChange = { updated ->
                            viewModel.updateStudent(updated)
                        }
                    )
                }
            } else {
                itemsIndexed(
                    items = students,
                    key = { _, item -> item.id }
                ) { index, student ->
                    StudentCardItem(
                        student = student,
                        voiceEnabled = voiceEnabled,
                        onStudentChange = { updated ->
                            viewModel.updateStudent(updated)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Percentage & Statistics Section
            item {
                PercentageCard(
                    statistics = statistics,
                    onCalculate = { viewModel.calculatePercentage() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Confirmation Dialog for Clearing Sheet Data
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "مسح بيانات السجل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف بيانات هذا السجل؟ (السجل $currentSheetIndex)\nسيتم تفريغ أسماء ودرجات جميع الطلاب في هذا السجل.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearCurrentSheet()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun SheetPaginationCard(
    currentSheetIndex: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelectSheet: (Int) -> Unit,
    onBackToHome: () -> Unit,
    onClearSheet: () -> Unit,
    onSaveSheet: () -> Unit,
    onPrint: () -> Unit,
    onExportPdf: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("sheet_pagination_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Pagination Navigation (السجل السابق / السجل التالي / اختر السجل)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                Button(
                    onClick = onPrev,
                    enabled = currentSheetIndex > 1,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("prev_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("السجل السابق", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Current Sheet Dropdown Trigger
                Box {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("select_sheet_dropdown_button")
                    ) {
                        Text(
                            text = "السجل $currentSheetIndex من 35",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.height(300.dp)
                    ) {
                        (1..35).forEach { idx ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "سجل $idx",
                                        fontWeight = if (idx == currentSheetIndex) FontWeight.Bold else FontWeight.Normal,
                                        color = if (idx == currentSheetIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onSelectSheet(idx)
                                }
                            )
                        }
                    }
                }

                // Next Button
                Button(
                    onClick = onNext,
                    enabled = currentSheetIndex < 35,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("next_sheet_button")
                ) {
                    Text("السجل التالي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Row 2: Print & Export PDF (MANDATORY PDF & PRINT FEATURE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🖨️ طباعة السجل
                Button(
                    onClick = onPrint,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("print_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🖨️ طباعة السجل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // 📄 تصدير PDF
                Button(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryEmerald
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📄 تصدير PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Row 3: Secondary Controls (العودة للرئيسية / حفظ البيانات / مسح بيانات السجل)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back to home
                OutlinedButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("return_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("العودة للرئيسية", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Save Sheet
                Button(
                    onClick = onSaveSheet,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ البيانات", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Clear Sheet Data
                Button(
                    onClick = onClearSheet,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clear_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح السجل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * عرض الجدول الكامل مع التمرير الأفقي للأعمدة الـ9:
 * 1. ت
 * 2. اسم الطالب
 * 3. معدل الفصل الأول
 * 4. نصف السنة
 * 5. معدل الفصل الثاني
 * 6. السعي السنوي
 * 7. الامتحان النهائي
 * 8. الدرجة النهائية
 * 9. الملاحظات
 */
@Composable
private fun GradeTableView(
    students: List<StudentRecord>,
    voiceEnabled: Boolean,
    onStudentChange: (StudentRecord) -> Unit
) {
    val horizontalScroll = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("grade_table_container"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("ت", 50.dp)
                TableHeaderCell("اسم الطالب", 230.dp)
                TableHeaderCell("معدل الفصل الأول", 125.dp)
                TableHeaderCell("نصف السنة", 115.dp)
                TableHeaderCell("معدل الفصل الثاني", 125.dp)
                TableHeaderCell("السعي السنوي", 120.dp, GoldAccent)
                TableHeaderCell("الامتحان النهائي", 125.dp)
                TableHeaderCell("الدرجة النهائية", 120.dp, SecondaryEmerald)
                TableHeaderCell("الملاحظات", 200.dp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Student Rows (25 rows)
            students.forEachIndexed { index, student ->
                val isEven = index % 2 == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isEven) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(vertical = 5.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. ت (Auto-generated 1..25)
                    Box(
                        modifier = Modifier.width(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${student.studentIndex}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 2. اسم الطالب (text field + mic)
                    Box(modifier = Modifier.width(230.dp).padding(horizontal = 4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp)
                        ) {
                            BasicTextField(
                                value = student.studentName,
                                onValueChange = { newName ->
                                    onStudentChange(student.copy(studentName = newName))
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("student_name_${student.studentIndex}")
                            )
                            VoiceDictationButton(
                                onResult = { spoken ->
                                    onStudentChange(student.copy(studentName = spoken))
                                },
                                enabled = voiceEnabled,
                                modifier = Modifier.size(32.dp),
                                testTag = "mic_student_${student.studentIndex}"
                            )
                        }
                    }

                    // 3. معدل الفصل الأول
                    Box(modifier = Modifier.width(125.dp).padding(horizontal = 4.dp)) {
                        ScoreInputField(
                            value = student.firstTermScore,
                            onValueChange = { newScore ->
                                onStudentChange(student.copy(firstTermScore = newScore))
                            },
                            testTag = "first_term_${student.studentIndex}"
                        )
                    }

                    // 4. نصف السنة
                    Box(modifier = Modifier.width(115.dp).padding(horizontal = 4.dp)) {
                        ScoreInputField(
                            value = student.midYearScore,
                            onValueChange = { newScore ->
                                onStudentChange(student.copy(midYearScore = newScore))
                            },
                            testTag = "mid_year_${student.studentIndex}"
                        )
                    }

                    // 5. معدل الفصل الثاني
                    Box(modifier = Modifier.width(125.dp).padding(horizontal = 4.dp)) {
                        ScoreInputField(
                            value = student.secondTermScore,
                            onValueChange = { newScore ->
                                onStudentChange(student.copy(secondTermScore = newScore))
                            },
                            testTag = "second_term_${student.studentIndex}"
                        )
                    }

                    // 6. السعي السنوي (محسوب تلقائياً = (فصل 1 + نصف سنة + فصل 2) / 3)
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(44.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = GoldAccent.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = GoldAccent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StudentRecord.formatScore(student.annualStriveScore),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (student.annualStriveScore != null) GoldAccent else MaterialTheme.colorScheme.outline
                        )
                    }

                    // 7. الامتحان النهائي
                    Box(modifier = Modifier.width(125.dp).padding(horizontal = 4.dp)) {
                        ScoreInputField(
                            value = student.finalExamScore,
                            onValueChange = { newScore ->
                                onStudentChange(student.copy(finalExamScore = newScore))
                            },
                            testTag = "final_exam_${student.studentIndex}"
                        )
                    }

                    // 8. الدرجة النهائية (محسوبة تلقائياً = السعي + النهائي)
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(44.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = SecondaryEmerald.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = SecondaryEmerald.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StudentRecord.formatScore(student.finalGradeScore),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (student.finalGradeScore != null) SecondaryEmerald else MaterialTheme.colorScheme.outline
                        )
                    }

                    // 9. الملاحظات
                    Box(modifier = Modifier.width(200.dp).padding(horizontal = 4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp)
                        ) {
                            BasicTextField(
                                value = student.notes,
                                onValueChange = { newNotes ->
                                    onStudentChange(student.copy(notes = newNotes))
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("student_notes_${student.studentIndex}")
                            )
                            VoiceDictationButton(
                                onResult = { spoken ->
                                    onStudentChange(student.copy(notes = spoken))
                                },
                                enabled = voiceEnabled,
                                modifier = Modifier.size(32.dp),
                                testTag = "mic_notes_${student.studentIndex}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    textColor: Color = Color.White
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * عرض مخصص للهاتف بالبطاقات لكل طالب (Student Card Item)
 */
@Composable
private fun StudentCardItem(
    student: StudentRecord,
    voiceEnabled: Boolean,
    onStudentChange: (StudentRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_card_${student.studentIndex}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Student Number & Name Field + Mic
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.studentIndex}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = student.studentName,
                        onValueChange = { onStudentChange(student.copy(studentName = it)) },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (student.studentName.isEmpty()) {
                                Text(
                                    text = "اسم الطالب...",
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 13.sp
                                )
                            }
                            inner()
                        }
                    )
                    VoiceDictationButton(
                        onResult = { onStudentChange(student.copy(studentName = it)) },
                        enabled = voiceEnabled,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Scores Grid: 3 Terms Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "معدل ف1", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    ScoreInputField(
                        value = student.firstTermScore,
                        onValueChange = { onStudentChange(student.copy(firstTermScore = it)) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "نصف السنة", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    ScoreInputField(
                        value = student.midYearScore,
                        onValueChange = { onStudentChange(student.copy(midYearScore = it)) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "معدل ف2", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    ScoreInputField(
                        value = student.secondTermScore,
                        onValueChange = { onStudentChange(student.copy(secondTermScore = it)) }
                    )
                }
            }

            // Calculated Annual Strive & Final Exam & Total Grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto Strive
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "السعي السنوي (تلقائي)", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StudentRecord.formatScore(student.annualStriveScore),
                            fontWeight = FontWeight.Bold,
                            color = if (student.annualStriveScore != null) GoldAccent else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Final Exam
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "الامتحان النهائي", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    ScoreInputField(
                        value = student.finalExamScore,
                        onValueChange = { onStudentChange(student.copy(finalExamScore = it)) }
                    )
                }

                // Auto Final Grade
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "الدرجة النهائية", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = SecondaryEmerald, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(SecondaryEmerald.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, SecondaryEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StudentRecord.formatScore(student.finalGradeScore),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (student.finalGradeScore != null) SecondaryEmerald else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Notes Row + Mic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = student.notes,
                    onValueChange = { onStudentChange(student.copy(notes = it)) },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (student.notes.isEmpty()) {
                            Text(
                                text = "ملاحظات إضافية...",
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        inner()
                    }
                )
                VoiceDictationButton(
                    onResult = { onStudentChange(student.copy(notes = it)) },
                    enabled = voiceEnabled,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
