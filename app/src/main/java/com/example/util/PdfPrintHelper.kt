package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.SchoolTeacherInfo
import com.example.data.model.StudentRecord
import java.io.File
import java.io.FileOutputStream

object PdfPrintHelper {

    /**
     * إنشاء كود HTML متكامل مصمم للطباعة A4 Landscape بدقة متناهية
     */
    fun generateHtmlGradeBook(
        schoolInfo: SchoolTeacherInfo,
        sheetIndex: Int,
        students: List<StudentRecord>
    ): String {
        val teacherName = schoolInfo.teacherName.ifBlank { "الأستاذ نواف صالح" }
        val schoolName = schoolInfo.schoolName.ifBlank { "متوسطة الرسالة للبنات" }
        val gradeLevel = schoolInfo.gradeLevel.ifBlank { "............" }
        val section = schoolInfo.section.ifBlank { "............" }

        val rowsHtml = StringBuilder()
        for (i in 1..25) {
            val student = students.find { it.studentIndex == i }
            val name = student?.studentName?.trim().orEmpty()
            val term1 = student?.firstTermScore?.let { StudentRecord.formatScore(it) }.orEmpty()
            val mid = student?.midYearScore?.let { StudentRecord.formatScore(it) }.orEmpty()
            val term2 = student?.secondTermScore?.let { StudentRecord.formatScore(it) }.orEmpty()
            val strive = student?.annualStriveScore?.let { StudentRecord.formatScore(it) }.orEmpty()
            val finalExam = student?.finalExamScore?.let { StudentRecord.formatScore(it) }.orEmpty()
            val finalGrade = student?.finalGradeScore?.let { StudentRecord.formatScore(it) }.orEmpty()
            val notes = student?.notes?.trim().orEmpty()

            val bgStyle = if (i % 2 == 0) "background-color: #fbfbfb;" else "background-color: #ffffff;"

            rowsHtml.append(
                """
                <tr style="$bgStyle">
                    <td style="font-weight: bold; color: #1e3a8a;">$i</td>
                    <td style="text-align: right; padding-right: 8px; font-weight: 500;">$name</td>
                    <td>$term1</td>
                    <td>$mid</td>
                    <td>$term2</td>
                    <td style="font-weight: bold; background-color: #fef9c3; color: #854d0e;">$strive</td>
                    <td>$finalExam</td>
                    <td style="font-weight: bold; background-color: #dcfce7; color: #166534;">$finalGrade</td>
                    <td style="text-align: right; padding-right: 6px; font-size: 8.5pt;">$notes</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>سجل درجات - السجل $sheetIndex من 35</title>
            <style>
                @page {
                    size: A4 landscape;
                    margin: 8mm 10mm;
                }
                * {
                    box-sizing: border-box;
                    -webkit-print-color-adjust: exact !important;
                    print-color-adjust: exact !important;
                }
                body {
                    font-family: 'Segoe UI', Tahoma, 'Cairo', 'Arial', sans-serif;
                    direction: rtl;
                    text-align: right;
                    margin: 0;
                    padding: 0;
                    color: #111;
                    font-size: 9.5pt;
                    background-color: #fff;
                }
                .header-container {
                    display: table;
                    width: 100%;
                    margin-bottom: 8px;
                    border-bottom: 2px solid #1e3a8a;
                    padding-bottom: 6px;
                }
                .header-col {
                    display: table-cell;
                    vertical-align: middle;
                }
                .header-right {
                    width: 32%;
                    text-align: right;
                    line-height: 1.45;
                }
                .header-center {
                    width: 36%;
                    text-align: center;
                }
                .header-left {
                    width: 32%;
                    text-align: left;
                    line-height: 1.45;
                }
                .school-name {
                    font-size: 13pt;
                    font-weight: bold;
                    color: #1e3a8a;
                }
                .title-main {
                    font-size: 16pt;
                    font-weight: bold;
                    color: #0f172a;
                    letter-spacing: 0.5px;
                }
                .sheet-badge {
                    display: inline-block;
                    background-color: #1e3a8a;
                    color: #ffffff;
                    padding: 3px 12px;
                    border-radius: 6px;
                    font-size: 11pt;
                    font-weight: bold;
                    margin-top: 4px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    text-align: center;
                    margin-top: 4px;
                }
                th, td {
                    border: 1px solid #334155;
                    padding: 3.5px 4px;
                    height: 17px;
                    vertical-align: middle;
                }
                th {
                    background-color: #1e3a8a;
                    color: #ffffff;
                    font-weight: bold;
                    font-size: 9.5pt;
                }
                th.th-strive {
                    background-color: #b45309;
                }
                th.th-final {
                    background-color: #047857;
                }
                .footer-container {
                    margin-top: 10px;
                    display: table;
                    width: 100%;
                }
                .footer-col {
                    display: table-cell;
                    vertical-align: middle;
                }
                .signature-box {
                    text-align: left;
                    font-size: 10.5pt;
                    font-weight: bold;
                }
                .rules-box {
                    font-size: 8pt;
                    color: #475569;
                    line-height: 1.4;
                }
            </style>
        </head>
        <body>
            <div class="header-container">
                <div class="header-col header-right">
                    <div style="font-weight: bold; font-size: 10pt;">جمهورية العراق • وزارة التربية</div>
                    <div class="school-name">$schoolName</div>
                    <div style="font-size: 9.5pt; color: #334155;">العام الدراسي: 2025 - 2026</div>
                </div>
                <div class="header-col header-center">
                    <div class="title-main">سجل درجات المدرس</div>
                    <div class="sheet-badge">السجل $sheetIndex من 35</div>
                </div>
                <div class="header-col header-left">
                    <div style="font-weight: bold; font-size: 10.5pt; color: #1e3a8a;">اسم المدرس: $teacherName</div>
                    <div style="font-size: 9.5pt;">الصف: <span style="font-weight: bold;">$gradeLevel</span> • الشعبة: <span style="font-weight: bold;">$section</span></div>
                    <div style="font-size: 8.5pt; color: #64748b;">تاريخ الطباعة: ${java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.ENGLISH).format(java.util.Date())}</div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 4%;">ت</th>
                        <th style="width: 25%;">اسم الطالب</th>
                        <th style="width: 10%;">معدل الفصل الأول</th>
                        <th style="width: 9%;">نصف السنة</th>
                        <th style="width: 10%;">معدل الفصل الثاني</th>
                        <th style="width: 11%;" class="th-strive">السعي السنوي</th>
                        <th style="width: 10%;">الامتحان النهائي</th>
                        <th style="width: 10%;" class="th-final">الدرجة النهائية</th>
                        <th style="width: 11%;">الملاحظات</th>
                    </tr>
                </thead>
                <tbody>
                    $rowsHtml
                </tbody>
            </table>

            <div class="footer-container">
                <div class="footer-col" style="width: 60%;">
                    <div class="rules-box">
                        • السعي السنوي = (معدل الفصل الأول + نصف السنة + معدل الفصل الثاني) ÷ 3<br>
                        • الدرجة النهائية = السعي السنوي + الامتحان النهائي
                    </div>
                </div>
                <div class="footer-col signature-box" style="width: 40%;">
                    <div>اسم المدرس: $teacherName</div>
                    <div style="margin-top: 8px;">التوقيع: ....................................................</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * طباعة السجل مباشرة عبر مدير الطباعة في أندرويد A4 Landscape
     */
    fun printGradeBook(
        context: Context,
        schoolInfo: SchoolTeacherInfo,
        sheetIndex: Int,
        students: List<StudentRecord>
    ) {
        val htmlContent = generateHtmlGradeBook(schoolInfo, sheetIndex, students)
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager == null) {
                    Toast.makeText(context, "خدمة الطباعة غير متوفرة على هذا الجهاز", Toast.LENGTH_SHORT).show()
                    return
                }
                val jobName = "سجل_الدرجات_سجل_${sheetIndex}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=utf-8", "UTF-8", null)
    }

    /**
     * تصدير ملف PDF حقيقي عالي الدقة A4 Landscape وحفظه ومشاركته
     */
    fun exportGradeBookPdf(
        context: Context,
        schoolInfo: SchoolTeacherInfo,
        sheetIndex: Int,
        students: List<StudentRecord>,
        onPdfCreated: (File) -> Unit = {}
    ) {
        // We use Android's native PdfDocument with ISO A4 Landscape dimensions:
        // A4 Landscape: 842 points width, 595 points height (at 72 DPI)
        val pdfDocument = PdfDocument()
        val pageWidth = 842
        val pageHeight = 595
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        drawPdfContent(canvas, schoolInfo, sheetIndex, students, pageWidth, pageHeight)

        pdfDocument.finishPage(page)

        try {
            val fileName = "سجل_الدرجات_سجل_${sheetIndex}.pdf"
            val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val pdfFile = File(outputDir, fileName)

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            onPdfCreated(pdfFile)
            sharePdfFile(context, pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            Toast.makeText(context, "حدث خطأ أثناء تصدير PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdfFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "تصدير وطباعة ملف PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تم حفظ الملف بنجاح في: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawPdfContent(
        canvas: Canvas,
        schoolInfo: SchoolTeacherInfo,
        sheetIndex: Int,
        students: List<StudentRecord>,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val teacherName = schoolInfo.teacherName.ifBlank { "الأستاذ نواف صالح" }
        val schoolName = schoolInfo.schoolName.ifBlank { "متوسطة الرسالة للبنات" }
        val gradeLevel = schoolInfo.gradeLevel.ifBlank { "............" }
        val section = schoolInfo.section.ifBlank { "............" }

        val marginLeft = 28f
        val marginRight = 28f
        val marginTop = 24f
        val contentWidth = pageWidth - marginLeft - marginRight // 786f
        val tableRight = pageWidth - marginRight // 814f
        val tableLeft = marginLeft // 28f

        // Draw Outer Frame
        paint.color = Color.rgb(30, 58, 138) // Deep Blue
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(tableLeft, marginTop, tableRight, pageHeight - 20f, paint)

        // Header Texts
        paint.style = Paint.Style.FILL

        // Center: Title & Sheet Badge
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("سجل درجات المدرس", pageWidth / 2f, marginTop + 22f, paint)

        // Badge Box
        val badgeRect = RectF(pageWidth / 2f - 75f, marginTop + 28f, pageWidth / 2f + 75f, marginTop + 48f)
        paint.color = Color.rgb(30, 58, 138)
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("السجل $sheetIndex من 35", pageWidth / 2f, marginTop + 42f, paint)

        // Right side: Ministry & School
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.rgb(30, 58, 138)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(schoolName, tableRight - 10f, marginTop + 22f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("جمهورية العراق • وزارة التربية", tableRight - 10f, marginTop + 35f, paint)
        canvas.drawText("العام الدراسي: 2025 - 2026", tableRight - 10f, marginTop + 47f, paint)

        // Left side: Teacher, Grade & Section
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(30, 58, 138)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("اسم المدرس: $teacherName", tableLeft + 10f, marginTop + 22f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("الصف: $gradeLevel  |  الشعبة: $section", tableLeft + 10f, marginTop + 36f, paint)

        // Column widths definition (from right to left)
        // Total = 786 pt
        val colWidths = floatArrayOf(
            34f,  // ت (index 0)
            192f, // اسم الطالب (index 1)
            75f,  // معدل الفصل الأول (index 2)
            68f,  // نصف السنة (index 3)
            75f,  // معدل الفصل الثاني (index 4)
            80f,  // السعي السنوي (index 5)
            76f,  // الامتحان النهائي (index 6)
            80f,  // الدرجة النهائية (index 7)
            106f  // الملاحظات (index 8)
        )
        val colHeaders = arrayOf(
            "ت",
            "اسم الطالب",
            "معدل ف1",
            "نصف السنة",
            "معدل ف2",
            "السعي السنوي",
            "الامتحان النهائي",
            "الدرجة النهائية",
            "الملاحظات"
        )

        val tableTop = marginTop + 56f
        val headerHeight = 22f
        val rowHeight = 15.5f

        // Draw Table Header Background
        paint.color = Color.rgb(30, 58, 138)
        paint.style = Paint.Style.FILL
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headerHeight, paint)

        // Draw Table Header Texts & Vertical borders
        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        var curX = tableRight
        for (c in colWidths.indices) {
            val width = colWidths[c]
            val cellCenter = curX - (width / 2f)

            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.WHITE
            canvas.drawText(colHeaders[c], cellCenter, tableTop + 14f, paint)

            // Header separator line
            paint.color = Color.rgb(71, 85, 105)
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(curX - width, tableTop, curX - width, tableTop + headerHeight, paint)
            paint.style = Paint.Style.FILL

            curX -= width
        }

        // Draw 25 Student Rows
        for (i in 1..25) {
            val rowY = tableTop + headerHeight + (i - 1) * rowHeight
            val student = students.find { it.studentIndex == i }

            // Row background alternating
            paint.style = Paint.Style.FILL
            paint.color = if (i % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
            canvas.drawRect(tableLeft, rowY, tableRight, rowY + rowHeight, paint)

            // Cell highlights for Strive and Final Grade
            var highlightX = tableRight
            for (c in 0 until 5) highlightX -= colWidths[c]
            // Strive column
            paint.color = Color.rgb(254, 249, 195)
            canvas.drawRect(highlightX - colWidths[5], rowY, highlightX, rowY + rowHeight, paint)
            // Final Grade column
            highlightX -= (colWidths[5] + colWidths[6])
            paint.color = Color.rgb(220, 252, 231)
            canvas.drawRect(highlightX - colWidths[7], rowY, highlightX, rowY + rowHeight, paint)

            // Horizontal line under row
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 0.8f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(tableLeft, rowY + rowHeight, tableRight, rowY + rowHeight, paint)

            // Draw Values
            curX = tableRight
            for (c in colWidths.indices) {
                val width = colWidths[c]
                paint.style = Paint.Style.FILL
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8f
                paint.color = Color.rgb(15, 23, 42)

                when (c) {
                    0 -> { // Index
                        paint.textAlign = Paint.Align.CENTER
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.color = Color.rgb(30, 58, 138)
                        canvas.drawText("$i", curX - width / 2f, rowY + 11f, paint)
                    }
                    1 -> { // Student Name
                        val name = student?.studentName?.trim().orEmpty()
                        paint.textAlign = Paint.Align.RIGHT
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText(name, curX - 6f, rowY + 11f, paint)
                    }
                    2 -> { // Term 1
                        paint.textAlign = Paint.Align.CENTER
                        val score = student?.firstTermScore?.let { StudentRecord.formatScore(it) }.orEmpty()
                        canvas.drawText(score, curX - width / 2f, rowY + 11f, paint)
                    }
                    3 -> { // Mid Year
                        paint.textAlign = Paint.Align.CENTER
                        val score = student?.midYearScore?.let { StudentRecord.formatScore(it) }.orEmpty()
                        canvas.drawText(score, curX - width / 2f, rowY + 11f, paint)
                    }
                    4 -> { // Term 2
                        paint.textAlign = Paint.Align.CENTER
                        val score = student?.secondTermScore?.let { StudentRecord.formatScore(it) }.orEmpty()
                        canvas.drawText(score, curX - width / 2f, rowY + 11f, paint)
                    }
                    5 -> { // Annual Strive (Calculated)
                        paint.textAlign = Paint.Align.CENTER
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.color = Color.rgb(180, 83, 9)
                        val score = student?.annualStriveScore?.let { StudentRecord.formatScore(it) }.orEmpty()
                        canvas.drawText(score, curX - width / 2f, rowY + 11f, paint)
                    }
                    6 -> { // Final Exam
                        paint.textAlign = Paint.Align.CENTER
                        val score = student?.finalExamScore?.let { StudentRecord.formatScore(it) }.orEmpty()
                        canvas.drawText(score, curX - width / 2f, rowY + 11f, paint)
                    }
                    7 -> { // Final Grade (Calculated)
                        paint.textAlign = Paint.Align.CENTER
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.color = Color.rgb(22, 101, 52)
                        val score = student?.finalGradeScore?.let { StudentRecord.formatScore(it) }.orEmpty()
                        canvas.drawText(score, curX - width / 2f, rowY + 11f, paint)
                    }
                    8 -> { // Notes
                        paint.textAlign = Paint.Align.RIGHT
                        paint.textSize = 7.5f
                        val notes = student?.notes?.trim().orEmpty()
                        canvas.drawText(notes, curX - 4f, rowY + 11f, paint)
                    }
                }

                // Vertical column separator
                paint.color = Color.rgb(203, 213, 225)
                paint.strokeWidth = 0.8f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(curX - width, rowY, curX - width, rowY + rowHeight, paint)

                curX -= width
            }
        }

        // Draw Table Outer Left & Right border lines
        paint.color = Color.rgb(30, 58, 138)
        paint.strokeWidth = 1.5f
        paint.style = Paint.Style.STROKE
        val tableBottom = tableTop + headerHeight + 25 * rowHeight
        canvas.drawRect(tableLeft, tableTop, tableRight, tableBottom, paint)

        // Footer Section
        val footerY = tableBottom + 18f
        paint.style = Paint.Style.FILL

        // Left side: Signature of Teacher
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("اسم المدرس: $teacherName", tableLeft + 10f, footerY, paint)
        canvas.drawText("التوقيع: ....................................................", tableLeft + 10f, footerY + 14f, paint)

        // Right side: Equation & Notes
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.rgb(71, 85, 105)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawText("• السعي السنوي = (معدل الفصل الأول + نصف السنة + معدل الفصل الثاني) ÷ 3", tableRight - 10f, footerY, paint)
        canvas.drawText("• الدرجة النهائية = السعي السنوي + الامتحان النهائي", tableRight - 10f, footerY + 12f, paint)
    }
}
