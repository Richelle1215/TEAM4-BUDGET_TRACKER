package com.example.team4.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.team4.data.model.ClothingOrder
import com.example.team4.data.model.Student
import com.example.team4.ui.theme.*
import com.example.team4.ui.viewmodel.DashboardViewModel
import com.example.team4.ui.viewmodel.ExpenseViewModel
import com.example.team4.ui.viewmodel.StudentViewModel
import com.example.team4.util.ExportService

@Composable
fun ImportExportScreen(
    studentViewModel: StudentViewModel,
    expenseViewModel: ExpenseViewModel,
    dashboardViewModel: DashboardViewModel,
    exportService: ExportService
) {
    val summary by dashboardViewModel.summary.collectAsState()
    val statusCounts by studentViewModel.statusCounts.collectAsState()
    val students by studentViewModel.students.collectAsState()
    val payments by studentViewModel.payments.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()
    val clothingOrders by studentViewModel.clothingOrders.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }
    var showTshirtListDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Blue Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Reports & Sync",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Export data and manage sync",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sync Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = SuccessGreen.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sync Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Last synced: Just now",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { /* Trigger Sync */ },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F2F8), contentColor = IndigoPrimary)
                    ) {
                        Text("Sync Now", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Financial Summary
            Text("Financial Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SummaryRow("Total Collected (Budget)", summary.totalCollected, SuccessGreenDeep)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    SummaryRow("Total Order for Tshirt/Jersey", summary.targetedCollection, Color(0xFF7B1FA2))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    SummaryRow("Total Expenses", summary.totalExpenses, ErrorRedDeep)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    SummaryRow(
                        "Balance", 
                        summary.balance, 
                        if (summary.balance >= 0) SuccessGreenDeep else ErrorRedDeep,
                        isLast = true
                    )
                }
            }

            // Tshirt / Jersey Orders Roster Action
            Text("Tshirt/Jersey Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
            ExportActionRow(
                title = "View Tshirt/Jersey Orders List",
                subtitle = "${clothingOrders.size} students availed (Total: ₱${clothingOrders.sumOf { it.price }.toInt()})",
                icon = Icons.Default.CheckCircle,
                onClick = { showTshirtListDialog = true }
            )

            // Collection Status
            Text("Collection Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusMetricCard("Fully Paid", statusCounts.paid, SuccessGreen.copy(alpha = 0.1f), SuccessGreenDeep, Modifier.weight(1f))
                StatusMetricCard("Partial", statusCounts.partial, WarningAmber.copy(alpha = 0.1f), WarningAmberDeep, Modifier.weight(1f))
                StatusMetricCard("Unpaid", statusCounts.unpaid, Color(0xFFFFEBEE), ErrorRedDeep, Modifier.weight(1f))
            }

            // Export Data
            Text("Export Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
            ExportActionRow(
                title = "Export Report (PDF / Excel)",
                subtitle = "Choose category (Students, Shirts, Expenses) and format",
                icon = Icons.Default.PictureAsPdf,
                onClick = { showExportDialog = true }
            )
        }
    }

    if (showExportDialog) {
        ExportOptionsDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { reportType, format ->
                exportService.exportReport(reportType, format, students, payments, expenses, clothingOrders)
                showExportDialog = false
            }
        )
    }

    if (showTshirtListDialog) {
        TshirtOrdersListDialog(
            clothingOrders = clothingOrders,
            students = students,
            onDismiss = { showTshirtListDialog = false },
            onDeleteOrder = { order -> studentViewModel.deleteClothingOrder(order) }
        )
    }
}

@Composable
fun TshirtOrdersListDialog(
    clothingOrders: List<ClothingOrder>,
    students: List<Student>,
    onDismiss: () -> Unit,
    onDeleteOrder: (ClothingOrder) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Tshirt/Jersey Orders List", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${clothingOrders.size} Availed • Total: ₱${clothingOrders.sumOf { it.price }.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        text = {
            if (clothingOrders.isEmpty()) {
                Text("No t-shirt or jersey orders recorded yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clothingOrders.sortedByDescending { it.date }) { order ->
                        val studentName = students.find { it.id == order.studentId }?.name ?: order.customName
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4A148C))
                                    Text(
                                        "${order.itemType} • Name: ${order.customName} #${order.customNumber} • Size: ${order.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                                Text("₱${order.price.toInt()}", fontWeight = FontWeight.Black, color = Color(0xFF4A148C))
                                IconButton(onClick = { onDeleteOrder(order) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SummaryRow(label: String, amount: Double, color: Color, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isLast) color.copy(alpha = 0.05f) else Color.Transparent)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Text(
            text = "₱${String.format("%,d", amount.toInt())}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun StatusMetricCard(label: String, count: Int, bgColor: Color, textColor: Color, modifier: Modifier) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = textColor)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ExportActionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = IndigoPrimary.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = IndigoPrimary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.FileDownload, contentDescription = null, tint = IndigoPrimary.copy(alpha = 0.5f))
        }
    }
}
