package com.example.team4.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.team4.data.model.ClothingOrder
import com.example.team4.data.model.Expense
import com.example.team4.data.model.Payment
import com.example.team4.data.model.Student
import com.example.team4.ui.screen.ExportFormat
import com.example.team4.ui.screen.ReportType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportReport(
        reportType: ReportType,
        format: ExportFormat,
        students: List<Student>,
        payments: List<Payment>,
        expenses: List<Expense>,
        clothingOrders: List<ClothingOrder>
    ) {
        when (format) {
            ExportFormat.EXCEL -> exportExcel(reportType, students, payments, expenses, clothingOrders)
            ExportFormat.PDF -> exportPdf(reportType, students, payments, expenses, clothingOrders)
        }
    }

    private fun exportExcel(
        reportType: ReportType,
        students: List<Student>,
        payments: List<Payment>,
        expenses: List<Expense>,
        clothingOrders: List<ClothingOrder>
    ) {
        val fileName = "report_${reportType.name.lowercase()}_${System.currentTimeMillis()}.xls"
        val file = File(context.cacheDir, fileName)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val htmlBuilder = StringBuilder()
        htmlBuilder.append("""
            <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
            <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
            <style>
              body { font-family: Arial, sans-serif; font-size: 11pt; }
              .title { font-size: 16pt; font-weight: bold; color: #1F4E78; margin-bottom: 10px; }
              .subtitle { font-size: 10pt; color: #595959; margin-bottom: 15px; }
              .section-header { font-size: 12pt; font-weight: bold; color: #1F4E78; margin-top: 20px; margin-bottom: 8px; }
              table { border-collapse: collapse; margin-bottom: 20px; }
              th { background-color: #1F4E78; color: #FFFFFF; font-weight: bold; border: 1px solid #0D233A; padding: 10px 20px; text-align: left; white-space: nowrap; }
              td { border: 1px solid #D9D9D9; padding: 8px 20px; vertical-align: middle; white-space: nowrap; }
              .col-name { min-width: 300px; }
              .col-payment { min-width: 140px; text-align: right; }
              tr.paid { background-color: #D4EDDA; font-weight: bold; color: #155724; }
              tr.unpaid { background-color: #FFFFFF; }
              tr.borrow-funds { background-color: #FFE0B2; font-weight: bold; color: #E65100; }
              tr.footer-success { background-color: #D4EDDA; font-weight: bold; color: #155724; }
              tr.footer-danger { background-color: #FFEBEE; font-weight: bold; color: #C62828; }
              tr.footer-purple { background-color: #F3E5F5; font-weight: bold; color: #4A148C; }
              .number { text-align: right; }
              .bold { font-weight: bold; }
            </style>
            </head>
            <body>
            <div class="title">TEAM 4</div>
            <div class="subtitle">Report Category: ${reportType.title} | Generated: ${dateFormat.format(Date())}</div>
        """.trimIndent())

        val regularStudents = students.filterNot { it.name.contains("Borrow Funds", ignoreCase = true) }.sortedBy { it.name }
        val borrowFundsStudent = students.find { it.name.contains("Borrow Funds", ignoreCase = true) }
        val sortedStudents = if (borrowFundsStudent != null) regularStudents + borrowFundsStudent else regularStudents
        val sortedExpenses = expenses.sortedBy { it.date }

        val totalColl = payments.sumOf { it.amount }
        val totalClothing = clothingOrders.sumOf { it.price }
        val totalExp = expenses.sumOf { it.amount }
        val balance = totalColl - totalExp
        val unpaidStudentsCount = regularStudents.count { s -> payments.none { it.studentId == s.id && it.amount > 0.0 } }

        when (reportType) {
            ReportType.STUDENTS_PAYMENTS -> {
                htmlBuilder.append("""
                    <div class="section-header">STUDENTS & PAYMENTS</div>
                    <table>
                      <thead>
                        <tr>
                          <th class="col-name">Full Name</th>
                          <th class="col-payment">PAYMENT</th>
                        </tr>
                      </thead>
                      <tbody>
                """.trimIndent())

                sortedStudents.forEach { s ->
                    val isBorrow = s.name.contains("Borrow Funds", ignoreCase = true)
                    val paid = payments.filter { it.studentId == s.id }.sumOf { it.amount }
                    val rowClass = when {
                        isBorrow -> "borrow-funds"
                        paid > 0.0 -> "paid"
                        else -> "unpaid"
                    }
                    val paymentText = if (paid > 0.0) "${paid.toInt()}" else "Unpaid"
                    htmlBuilder.append("""
                        <tr class="$rowClass">
                          <td>${s.name}</td>
                          <td class="number">$paymentText</td>
                        </tr>
                    """.trimIndent())
                }
                htmlBuilder.append("""
                      </tbody>
                      <tfoot>
                        <tr class="footer-success">
                          <td>TOTAL COLLECTED</td>
                          <td class="number">₱${totalColl.toInt()}</td>
                        </tr>
                        <tr class="footer-danger">
                          <td>UNPAID STUDENTS (Hindi pa nagbabayad)</td>
                          <td class="number">${unpaidStudentsCount} student(s)</td>
                        </tr>
                      </tfoot>
                    </table>
                """.trimIndent())
            }

            ReportType.CLOTHING_ORDERS -> {
                htmlBuilder.append("""
                    <div class="section-header">CLOTHING ORDERS</div>
                    <table>
                      <thead>
                        <tr>
                          <th class="col-name">Student Name</th>
                          <th>type</th>
                          <th>name to print</th>
                          <th>number to print</th>
                          <th>size</th>
                          <th class="number">payment</th>
                        </tr>
                      </thead>
                      <tbody>
                """.trimIndent())

                clothingOrders.forEach { o ->
                    val studentName = students.find { it.id == o.studentId }?.name ?: o.customName
                    htmlBuilder.append("""
                        <tr>
                          <td>$studentName</td>
                          <td>${o.itemType}</td>
                          <td>${o.customName}</td>
                          <td>${o.customNumber}</td>
                          <td>${o.size}</td>
                          <td class="number">₱${o.price.toInt()}</td>
                        </tr>
                    """.trimIndent())
                }
                htmlBuilder.append("""
                      </tbody>
                      <tfoot>
                        <tr class="footer-purple">
                          <td colspan="5">TOTAL CLOTHING ORDERS</td>
                          <td class="number">₱${totalClothing.toInt()}</td>
                        </tr>
                      </tfoot>
                    </table>
                """.trimIndent())
            }

            ReportType.EXPENSES_SUMMARY -> {
                htmlBuilder.append("""
                    <div class="section-header">EXPENSES</div>
                    <table>
                      <thead>
                        <tr>
                          <th>DATE</th>
                          <th>DESCRIPTION/CATEGORY</th>
                          <th class="number">AMOUNT</th>
                        </tr>
                      </thead>
                      <tbody>
                """.trimIndent())

                sortedExpenses.forEach { e ->
                    htmlBuilder.append("""
                        <tr>
                          <td>${dateFormat.format(Date(e.date))}</td>
                          <td>${e.description}</td>
                          <td class="number">₱${e.amount.toInt()}</td>
                        </tr>
                    """.trimIndent())
                }
                htmlBuilder.append("""
                      </tbody>
                      <tfoot>
                        <tr class="footer-danger">
                          <td colspan="2">TOTAL EXPENSES</td>
                          <td class="number">₱${totalExp.toInt()}</td>
                        </tr>
                      </tfoot>
                    </table>
                    <div class="section-header">NET FINANCIAL SUMMARY</div>
                    <table>
                      <thead>
                        <tr>
                          <th>FINANCIAL CATEGORY</th>
                          <th class="number">AMOUNT</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>Total Collected from Students</td>
                          <td class="number bold" style="color: #155724;">₱${totalColl.toInt()}</td>
                        </tr>
                        <tr>
                          <td>Total Expenses</td>
                          <td class="number bold" style="color: #C62828;">₱${totalExp.toInt()}</td>
                        </tr>
                        <tr class="footer-success">
                          <td>NET REMAINING BALANCE (Total Collected - Expenses)</td>
                          <td class="number bold">₱${balance.toInt()}</td>
                        </tr>
                      </tbody>
                    </table>
                """.trimIndent())
            }

            ReportType.FULL_REPORT -> {
                // 1. Students & Payments Section
                htmlBuilder.append("""
                    <div class="section-header">STUDENTS & PAYMENTS</div>
                    <table>
                      <thead>
                        <tr>
                          <th class="col-name">Full Name</th>
                          <th class="col-payment">PAYMENT</th>
                        </tr>
                      </thead>
                      <tbody>
                """.trimIndent())

                sortedStudents.forEach { s ->
                    val isBorrow = s.name.contains("Borrow Funds", ignoreCase = true)
                    val paid = payments.filter { it.studentId == s.id }.sumOf { it.amount }
                    val rowClass = when {
                        isBorrow -> "borrow-funds"
                        paid > 0.0 -> "paid"
                        else -> "unpaid"
                    }
                    val paymentText = if (paid > 0.0) "${paid.toInt()}" else "Unpaid"
                    htmlBuilder.append("""
                        <tr class="$rowClass">
                          <td>${s.name}</td>
                          <td class="number">$paymentText</td>
                        </tr>
                    """.trimIndent())
                }
                htmlBuilder.append("""
                      </tbody>
                      <tfoot>
                        <tr class="footer-success">
                          <td>TOTAL COLLECTED</td>
                          <td class="number">₱${totalColl.toInt()}</td>
                        </tr>
                        <tr class="footer-danger">
                          <td>UNPAID STUDENTS (Hindi pa nagbabayad)</td>
                          <td class="number">${unpaidStudentsCount} student(s)</td>
                        </tr>
                      </tfoot>
                    </table>
                """.trimIndent())

                // 2. Clothing Orders Section
                htmlBuilder.append("""
                    <div class="section-header">CLOTHING ORDERS</div>
                    <table>
                      <thead>
                        <tr>
                          <th class="col-name">Student Name</th>
                          <th>type</th>
                          <th>name to print</th>
                          <th>number to print</th>
                          <th>size</th>
                          <th class="number">payment</th>
                        </tr>
                      </thead>
                      <tbody>
                """.trimIndent())

                clothingOrders.forEach { o ->
                    val studentName = students.find { it.id == o.studentId }?.name ?: o.customName
                    htmlBuilder.append("""
                        <tr>
                          <td>$studentName</td>
                          <td>${o.itemType}</td>
                          <td>${o.customName}</td>
                          <td>${o.customNumber}</td>
                          <td>${o.size}</td>
                          <td class="number">₱${o.price.toInt()}</td>
                        </tr>
                    """.trimIndent())
                }
                htmlBuilder.append("""
                      </tbody>
                      <tfoot>
                        <tr class="footer-purple">
                          <td colspan="5">TOTAL CLOTHING ORDERS</td>
                          <td class="number">₱${totalClothing.toInt()}</td>
                        </tr>
                      </tfoot>
                    </table>
                """.trimIndent())

                // 3. Expenses Section
                htmlBuilder.append("""
                    <div class="section-header">EXPENSES</div>
                    <table>
                      <thead>
                        <tr>
                          <th>DATE</th>
                          <th>DESCRIPTION/CATEGORY</th>
                          <th class="number">AMOUNT</th>
                        </tr>
                      </thead>
                      <tbody>
                """.trimIndent())

                sortedExpenses.forEach { e ->
                    htmlBuilder.append("""
                        <tr>
                          <td>${dateFormat.format(Date(e.date))}</td>
                          <td>${e.description}</td>
                          <td class="number">₱${e.amount.toInt()}</td>
                        </tr>
                    """.trimIndent())
                }
                htmlBuilder.append("""
                      </tbody>
                      <tfoot>
                        <tr class="footer-danger">
                          <td colspan="2">TOTAL EXPENSES</td>
                          <td class="number">₱${totalExp.toInt()}</td>
                        </tr>
                      </tfoot>
                    </table>
                    <div class="section-header">NET FINANCIAL SUMMARY</div>
                    <table>
                      <thead>
                        <tr>
                          <th>FINANCIAL CATEGORY</th>
                          <th class="number">AMOUNT</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>Total Collected from Students</td>
                          <td class="number bold" style="color: #155724;">₱${totalColl.toInt()}</td>
                        </tr>
                        <tr>
                          <td>Total Order for Tshirt/Jersey</td>
                          <td class="number bold" style="color: #4A148C;">₱${totalClothing.toInt()}</td>
                        </tr>
                        <tr>
                          <td>Total Expenses</td>
                          <td class="number bold" style="color: #C62828;">₱${totalExp.toInt()}</td>
                        </tr>
                        <tr class="footer-success">
                          <td>NET REMAINING BALANCE (Total Collected - Expenses)</td>
                          <td class="number bold">₱${balance.toInt()}</td>
                        </tr>
                      </tbody>
                    </table>
                """.trimIndent())
            }
        }

        htmlBuilder.append("</body></html>")

        file.writeText(htmlBuilder.toString(), Charsets.UTF_8)
        shareFile(file, "application/vnd.ms-excel")
    }

    private fun exportPdf(
        reportType: ReportType,
        students: List<Student>,
        payments: List<Payment>,
        expenses: List<Expense>,
        clothingOrders: List<ClothingOrder>
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val textPaint = Paint().apply { isAntiAlias = true }
        val fillPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        val strokePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.parseColor("#CCCCCC") }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var y = 40f

        fun newPage() {
            document.finishPage(page)
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }

        fun checkSpace(heightNeeded: Float) {
            if (y + heightNeeded > 790f) {
                newPage()
            }
        }

        // Title Header Banner
        fillPaint.color = Color.parseColor("#1F4E78")
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 60f, fillPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 20f
        textPaint.isFakeBoldText = true
        canvas.drawText("TEAM 4", 40f, 38f, textPaint)

        textPaint.textSize = 10f
        textPaint.isFakeBoldText = false
        canvas.drawText("${reportType.title} | ${dateFormat.format(Date())}", 350f, 38f, textPaint)

        y = 80f

        val regularStudents = students.filterNot { it.name.contains("Borrow Funds", ignoreCase = true) }.sortedBy { it.name }
        val borrowFundsStudent = students.find { it.name.contains("Borrow Funds", ignoreCase = true) }
        val sortedStudents = if (borrowFundsStudent != null) regularStudents + borrowFundsStudent else regularStudents
        val sortedExpenses = expenses.sortedBy { it.date }

        val totalColl = payments.sumOf { it.amount }
        val totalClothing = clothingOrders.sumOf { it.price }
        val totalExp = expenses.sumOf { it.amount }
        val balance = totalColl - totalExp
        val unpaidStudentsCount = regularStudents.count { s -> payments.none { it.studentId == s.id && it.amount > 0.0 } }

        fun drawTableHeader(headers: List<String>, colWidths: List<Float>, startX: Float = 40f) {
            checkSpace(30f)
            fillPaint.color = Color.parseColor("#1F4E78")
            var currentX = startX
            val totalWidth = colWidths.sum()
            canvas.drawRect(startX, y, startX + totalWidth, y + 25f, fillPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 10f
            textPaint.isFakeBoldText = true

            headers.forEachIndexed { idx, h ->
                val width = colWidths[idx]
                canvas.drawText(h, currentX + 8f, y + 17f, textPaint)
                currentX += width
            }

            // Outer border for header
            strokePaint.color = Color.parseColor("#0D233A")
            canvas.drawRect(startX, y, startX + totalWidth, y + 25f, strokePaint)

            y += 25f
        }

        fun drawTableRow(
            values: List<String>,
            colWidths: List<Float>,
            isPaid: Boolean = false,
            bgColor: Int? = null,
            textColor: Int? = null,
            isBold: Boolean = false,
            startX: Float = 40f
        ) {
            checkSpace(24f)
            val totalWidth = colWidths.sum()

            val actualBgColor = bgColor ?: if (isPaid) Color.parseColor("#D4EDDA") else Color.WHITE
            val actualTextColor = textColor ?: if (isPaid) Color.parseColor("#155724") else Color.BLACK

            fillPaint.color = actualBgColor
            canvas.drawRect(startX, y, startX + totalWidth, y + 22f, fillPaint)

            textPaint.color = actualTextColor
            textPaint.textSize = 9.5f
            textPaint.isFakeBoldText = isBold || isPaid

            var currentX = startX
            values.forEachIndexed { idx, value ->
                val width = colWidths[idx]
                
                // Draw vertical grid line
                canvas.drawRect(currentX, y, currentX + width, y + 22f, strokePaint)
                
                val maxChars = (width / 6f).toInt().coerceAtLeast(5)
                val displayText = if (value.length > maxChars) value.take(maxChars - 2) + ".." else value

                canvas.drawText(displayText, currentX + 8f, y + 15f, textPaint)
                currentX += width
            }

            y += 22f
        }

        fun drawNetSummaryTable(startX: Float = 40f) {
            checkSpace(110f)
            drawTableHeader(listOf("NET FINANCIAL SUMMARY", "AMOUNT"), listOf(380f, 135f), startX)
            drawTableRow(
                listOf("Total Collected from Students", "₱${totalColl.toInt()}"),
                listOf(380f, 135f),
                textColor = Color.parseColor("#155724"),
                startX = startX
            )
            drawTableRow(
                listOf("Total Order for Tshirt/Jersey", "₱${totalClothing.toInt()}"),
                listOf(380f, 135f),
                textColor = Color.parseColor("#4A148C"),
                startX = startX
            )
            drawTableRow(
                listOf("Total Expenses", "₱${totalExp.toInt()}"),
                listOf(380f, 135f),
                textColor = Color.parseColor("#C62828"),
                startX = startX
            )
            drawTableRow(
                listOf("NET REMAINING BALANCE (Collected - Expenses)", "₱${balance.toInt()}"),
                listOf(380f, 135f),
                bgColor = Color.parseColor("#E8F5E9"),
                textColor = Color.parseColor("#2E7D32"),
                isBold = true,
                startX = startX
            )
        }

        when (reportType) {
            ReportType.STUDENTS_PAYMENTS -> {
                drawTableHeader(listOf("Full Name", "PAYMENT"), listOf(380f, 135f))
                sortedStudents.forEach { s ->
                    val isBorrow = s.name.contains("Borrow Funds", ignoreCase = true)
                    val paid = payments.filter { it.studentId == s.id }.sumOf { it.amount }
                    val isPaid = paid > 0.0
                    val paymentText = if (isPaid) "₱${paid.toInt()}" else "Unpaid"

                    if (isBorrow) {
                        drawTableRow(
                            listOf(s.name, paymentText),
                            listOf(380f, 135f),
                            bgColor = Color.parseColor("#FFE0B2"),
                            textColor = Color.parseColor("#E65100"),
                            isBold = true
                        )
                    } else {
                        drawTableRow(listOf(s.name, paymentText), listOf(380f, 135f), isPaid = isPaid)
                    }
                }
                drawTableRow(
                    listOf("TOTAL COLLECTED", "₱${totalColl.toInt()}"),
                    listOf(380f, 135f),
                    bgColor = Color.parseColor("#D4EDDA"),
                    textColor = Color.parseColor("#155724"),
                    isBold = true
                )
                drawTableRow(
                    listOf("UNPAID STUDENTS (Hindi pa nagbabayad)", "${unpaidStudentsCount} student(s)"),
                    listOf(380f, 135f),
                    bgColor = Color.parseColor("#FFEBEE"),
                    textColor = Color.parseColor("#C62828"),
                    isBold = true
                )
            }

            ReportType.CLOTHING_ORDERS -> {
                drawTableHeader(
                    listOf("Student Name", "type", "name to print", "number to print", "size", "payment"),
                    listOf(130f, 65f, 110f, 85f, 50f, 75f)
                )
                clothingOrders.forEach { o ->
                    val studentName = students.find { it.id == o.studentId }?.name ?: o.customName
                    drawTableRow(
                        listOf(studentName, o.itemType, o.customName, o.customNumber, o.size, "₱${o.price.toInt()}"),
                        listOf(130f, 65f, 110f, 85f, 50f, 75f)
                    )
                }
                drawTableRow(
                    listOf("TOTAL CLOTHING ORDERS", "", "", "", "", "₱${totalClothing.toInt()}"),
                    listOf(130f, 65f, 110f, 85f, 50f, 75f),
                    bgColor = Color.parseColor("#F3E5F5"),
                    textColor = Color.parseColor("#4A148C"),
                    isBold = true
                )
            }

            ReportType.EXPENSES_SUMMARY -> {
                drawTableHeader(listOf("DATE", "DESCRIPTION/CATEGORY", "AMOUNT"), listOf(110f, 285f, 120f))
                sortedExpenses.forEach { e ->
                    drawTableRow(
                        listOf(dateFormat.format(Date(e.date)), e.description, "₱${e.amount.toInt()}"),
                        listOf(110f, 285f, 120f)
                    )
                }
                drawTableRow(
                    listOf("TOTAL EXPENSES", "", "₱${totalExp.toInt()}"),
                    listOf(110f, 285f, 120f),
                    bgColor = Color.parseColor("#FFEBEE"),
                    textColor = Color.parseColor("#C62828"),
                    isBold = true
                )
                y += 20f
                drawNetSummaryTable()
            }

            ReportType.FULL_REPORT -> {
                // 1. Students & Payments Section
                drawTableHeader(listOf("Full Name", "PAYMENT"), listOf(380f, 135f))
                sortedStudents.forEach { s ->
                    val isBorrow = s.name.contains("Borrow Funds", ignoreCase = true)
                    val paid = payments.filter { it.studentId == s.id }.sumOf { it.amount }
                    val isPaid = paid > 0.0
                    val paymentText = if (isPaid) "₱${paid.toInt()}" else "Unpaid"

                    if (isBorrow) {
                        drawTableRow(
                            listOf(s.name, paymentText),
                            listOf(380f, 135f),
                            bgColor = Color.parseColor("#FFE0B2"),
                            textColor = Color.parseColor("#E65100"),
                            isBold = true
                        )
                    } else {
                        drawTableRow(listOf(s.name, paymentText), listOf(380f, 135f), isPaid = isPaid)
                    }
                }
                drawTableRow(
                    listOf("TOTAL COLLECTED", "₱${totalColl.toInt()}"),
                    listOf(380f, 135f),
                    bgColor = Color.parseColor("#D4EDDA"),
                    textColor = Color.parseColor("#155724"),
                    isBold = true
                )
                drawTableRow(
                    listOf("UNPAID STUDENTS (Hindi pa nagbabayad)", "${unpaidStudentsCount} student(s)"),
                    listOf(380f, 135f),
                    bgColor = Color.parseColor("#FFEBEE"),
                    textColor = Color.parseColor("#C62828"),
                    isBold = true
                )

                y += 20f
                checkSpace(60f)

                // 2. Clothing Orders Section
                drawTableHeader(
                    listOf("Student Name", "type", "name to print", "number to print", "size", "payment"),
                    listOf(130f, 65f, 110f, 85f, 50f, 75f)
                )
                clothingOrders.forEach { o ->
                    val studentName = students.find { it.id == o.studentId }?.name ?: o.customName
                    drawTableRow(
                        listOf(studentName, o.itemType, o.customName, o.customNumber, o.size, "₱${o.price.toInt()}"),
                        listOf(130f, 65f, 110f, 85f, 50f, 75f)
                    )
                }
                drawTableRow(
                    listOf("TOTAL CLOTHING ORDERS", "", "", "", "", "₱${totalClothing.toInt()}"),
                    listOf(130f, 65f, 110f, 85f, 50f, 75f),
                    bgColor = Color.parseColor("#F3E5F5"),
                    textColor = Color.parseColor("#4A148C"),
                    isBold = true
                )

                y += 20f
                checkSpace(60f)

                // 3. Expenses Section
                drawTableHeader(listOf("DATE", "DESCRIPTION/CATEGORY", "AMOUNT"), listOf(110f, 285f, 120f))
                sortedExpenses.forEach { e ->
                    drawTableRow(
                        listOf(dateFormat.format(Date(e.date)), e.description, "₱${e.amount.toInt()}"),
                        listOf(110f, 285f, 120f)
                    )
                }
                drawTableRow(
                    listOf("TOTAL EXPENSES", "", "₱${totalExp.toInt()}"),
                    listOf(110f, 285f, 120f),
                    bgColor = Color.parseColor("#FFEBEE"),
                    textColor = Color.parseColor("#C62828"),
                    isBold = true
                )

                y += 20f
                drawNetSummaryTable()
            }
        }

        document.finishPage(page)
        val file = File(context.cacheDir, "report_${reportType.name.lowercase()}.pdf")
        val fos = FileOutputStream(file)
        try {
            document.writeTo(fos)
        } finally {
            fos.close()
        }
        document.close()
        shareFile(file, "application/pdf")
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
