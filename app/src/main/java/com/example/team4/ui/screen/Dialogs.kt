package com.example.team4.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.team4.data.model.ClothingOrder
import com.example.team4.data.model.Expense
import com.example.team4.data.model.Payment
import com.example.team4.data.model.Student

@Composable
fun EditStudentDialog(
    student: Student,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Update name for ${student.name}", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var desc by remember { mutableStateOf(expense.description) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₱)") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (desc.isNotBlank()) onConfirm(desc, amt)
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPaymentDialog(
    payment: Payment,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(payment.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₱)") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onConfirm(amt)
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddClothingOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (itemType: String, description: String, customName: String, customNumber: String, size: String, price: Double) -> Unit
) {
    var itemType by remember { mutableStateOf("Jersey") }
    var customName by remember { mutableStateOf("") }
    var customNumber by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("L") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Clothing Order") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = itemType,
                    onValueChange = { itemType = it },
                    label = { Text("Item Type (e.g. Jersey, Shirt)") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Name to Print") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = customNumber,
                    onValueChange = { customNumber = it },
                    label = { Text("Number to Print") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size (S, M, L, XL, etc.)") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price / Payment (₱)") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (itemType.isNotBlank()) {
                        onConfirm(itemType, "", customName, customNumber, size, p)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2), contentColor = Color.White)
            ) {
                Text("Add Order", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditClothingOrderDialog(
    order: ClothingOrder,
    onDismiss: () -> Unit,
    onConfirm: (itemType: String, description: String, customName: String, customNumber: String, size: String, price: Double) -> Unit
) {
    var itemType by remember { mutableStateOf(order.itemType) }
    var description by remember { mutableStateOf(order.description) }
    var customName by remember { mutableStateOf(order.customName) }
    var customNumber by remember { mutableStateOf(order.customNumber) }
    var size by remember { mutableStateOf(order.size) }
    var price by remember { mutableStateOf(order.price.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Clothing Order") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = itemType,
                    onValueChange = { itemType = it },
                    label = { Text("Item Type") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Name to Print") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = customNumber,
                    onValueChange = { customNumber = it },
                    label = { Text("Number to Print") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₱)") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: 0.0
                if (itemType.isNotBlank()) {
                    onConfirm(itemType, description, customName, customNumber, size, p)
                }
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class ReportType(val title: String) {
    STUDENTS_PAYMENTS("Students with Payments"),
    CLOTHING_ORDERS("Clothing Orders (Shirts & Jerseys)"),
    EXPENSES_SUMMARY("Expenses & Balance Summary"),
    FULL_REPORT("Full Comprehensive Report")
}

enum class ExportFormat(val title: String) {
    PDF("PDF Document"),
    EXCEL("Excel Spreadsheet (.xls)")
}

@Composable
fun ExportOptionsDialog(
    onDismiss: () -> Unit,
    onConfirm: (ReportType, ExportFormat) -> Unit
) {
    var selectedReport by remember { mutableStateOf(ReportType.FULL_REPORT) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Report Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select Report Category:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ReportType.entries.forEach { report ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReport = report }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReport == report,
                            onClick = { selectedReport = report }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = report.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                HorizontalDivider(color = Color.LightGray)

                Text("Select File Format:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ExportFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFormat = format }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = format.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedReport, selectedFormat) }) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
