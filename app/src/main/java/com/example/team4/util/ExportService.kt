package com.example.team4.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.team4.data.model.Expense
import com.example.team4.data.model.Payment
import com.example.team4.data.model.Student
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportToCsv(students: List<Student>, payments: List<Payment>, expenses: List<Expense>) {
        val file = File(context.cacheDir, "fund_report.csv")
        csvWriter().open(file) {
            writeRow(listOf("Student Report"))
            writeRow(listOf("ID", "Name", "Target", "Status"))
            students.forEach { s ->
                val paid = payments.filter { it.studentId == s.id }.sumOf { it.amount }
                writeRow(listOf(s.id, s.name, s.targetAmount, if (paid >= s.targetAmount) "Paid" else "Partial/Unpaid"))
            }
            writeRow(listOf(""))
            writeRow(listOf("Expense Report"))
            writeRow(listOf("Description", "Amount", "Date"))
            expenses.forEach { e ->
                writeRow(listOf(e.description, e.amount, e.date))
            }
        }
        shareFile(file, "text/csv")
    }

    fun exportToPdf(students: List<Student>, expenses: List<Expense>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        paint.color = Color.BLACK
        paint.textSize = 18f
        canvas.drawText("Class Fund Report", 50f, 50f, paint)

        paint.textSize = 12f
        var y = 80f
        canvas.drawText("Students:", 50f, y, paint)
        y += 20f
        students.forEach {
            canvas.drawText("${it.name} - Target: ${it.targetAmount}", 70f, y, paint)
            y += 15f
        }

        y += 20f
        canvas.drawText("Expenses:", 50f, y, paint)
        y += 20f
        expenses.forEach {
            canvas.drawText("${it.description}: ${it.amount}", 70f, y, paint)
            y += 15f
        }

        document.finishPage(page)
        val file = File(context.cacheDir, "fund_report.pdf")
        document.writeTo(FileOutputStream(file))
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
