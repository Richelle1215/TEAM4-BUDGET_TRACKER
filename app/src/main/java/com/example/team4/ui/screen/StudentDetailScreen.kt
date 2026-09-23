package com.example.team4.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.team4.data.model.Payment
import com.example.team4.ui.theme.*
import com.example.team4.ui.viewmodel.StudentFilter
import com.example.team4.ui.viewmodel.StudentViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentDetailScreen(
    studentId: String,
    viewModel: StudentViewModel,
    onBackClick: () -> Unit
) {
    val studentWithStatus by viewModel.getStudentWithStatus(studentId).collectAsState(initial = null)
    val payments by viewModel.getPaymentsForStudent(studentId).collectAsState(initial = emptyList())
    var showPaymentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<com.example.team4.data.model.Student?>(null) }
    var editingPayment by remember { mutableStateOf<Payment?>(null) }

    val statusColor = when (studentWithStatus?.status) {
        StudentFilter.PAID -> SuccessGreenDeep
        else -> ErrorRedDeep
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPaymentDialog = true },
                containerColor = IndigoPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Payment")
            }
        },
        bottomBar = { Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dynamic Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(statusColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Back Button
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TextButton(
                                onClick = onBackClick,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                Spacer(Modifier.width(4.dp))
                                Text("Back", fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(Modifier.weight(1f))
                            
                            IconButton(onClick = { editingStudent = studentWithStatus?.student }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Student", tint = Color.White)
                            }
                            IconButton(onClick = { 
                                studentWithStatus?.student?.let { viewModel.deleteStudent(it); onBackClick() }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Student", tint = Color.White)
                            }
                        }

                        // Avatar
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = studentWithStatus?.student?.name?.split(" ")
                                    ?.filter { it.isNotEmpty() }
                                    ?.take(2)
                                    ?.joinToString("") { it.take(1).uppercase() } ?: ""
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Student Name
                        Text(
                            text = studentWithStatus?.student?.name ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(8.dp))

                        // Status Pills Row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Main Status Pill
                            Surface(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (studentWithStatus?.status == StudentFilter.PAID) "Paid in Full" else "Unpaid",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Early Bird Pill
                            if (studentWithStatus?.isEarlyBird == true) {
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFE65100)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "EARLY BIRD",
                                            color = Color(0xFFE65100),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Payment History Section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 60.dp)
                ) {
                    Text(
                        text = "Payment History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    if (payments.isEmpty()) {
                        EmptyPaymentState()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(payments.sortedByDescending { it.date }) { payment ->
                                PaymentItemTimeline(
                                    payment = payment,
                                    onDelete = { viewModel.deletePayment(payment) }
                                )
                            }
                        }
                    }
                }
            }

            // Floating Progress Card
            studentWithStatus?.let { status ->
                ProgressCard(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 210.dp)
                        .padding(horizontal = 20.dp),
                    item = status,
                    color = statusColor
                )
            }
        }
    }

    if (showPaymentDialog) {
        AddPaymentDialog(
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount ->
                viewModel.addPayment(studentId, amount, System.currentTimeMillis())
                showPaymentDialog = false
            }
        )
    }

    editingStudent?.let { student ->
        EditStudentDialog(
            student = student,
            onDismiss = { editingStudent = null },
            onConfirm = { updatedName ->
                viewModel.updateStudent(student.copy(name = updatedName))
                editingStudent = null
            }
        )
    }
}

@Composable
fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment") },
        text = {
            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) }) {
                Text("Confirm")
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
fun ProgressCard(
    modifier: Modifier = Modifier,
    item: com.example.team4.ui.viewmodel.StudentWithStatus,
    color: Color
) {
    val progress = if (item.student.targetAmount > 0) 
        (item.totalPaid / item.student.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "₱${item.totalPaid.toInt()}  /  ₱${item.student.targetAmount.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = color,
                trackColor = Color(0xFFF0F0F0)
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${(progress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
                if (item.totalPaid < item.student.targetAmount) {
                    Text(
                        text = "₱${(item.student.targetAmount - item.totalPaid).toInt()} remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentItemTimeline(
    payment: Payment,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d,  2024", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot and Line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color(0xFF5C6BC0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("₱", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE8EAF6),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5CAE9))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₱${payment.amount.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = dateFormat.format(Date(payment.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmptyPaymentState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFF3E0),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("📋", fontSize = 32.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No payments recorded yet.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Use the + button to add a payment.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
