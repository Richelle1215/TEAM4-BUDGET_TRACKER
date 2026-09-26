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
import com.example.team4.data.model.ClothingOrder
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
    val clothingOrders by viewModel.getOrdersForStudent(studentId).collectAsState(initial = emptyList())
    
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showClothingDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<com.example.team4.data.model.Student?>(null) }
    var editingPayment by remember { mutableStateOf<Payment?>(null) }
    var editingClothingOrder by remember { mutableStateOf<ClothingOrder?>(null) }

    val statusColor = when (studentWithStatus?.status) {
        StudentFilter.PAID -> SuccessGreenDeep
        else -> ErrorRedDeep
    }

    Scaffold(
        bottomBar = { Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dynamic Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
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
                            modifier = Modifier.size(72.dp),
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

                        Spacer(Modifier.height(8.dp))

                        // Student Name
                        Text(
                            text = studentWithStatus?.student?.name ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(6.dp))

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
                                        text = if (studentWithStatus?.status == StudentFilter.PAID) "Paid" else "Unpaid",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Scrollable Content Section
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(top = 35.dp, bottom = 100.dp)
                ) {
                    // Payment History Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment History",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E)
                            )
                            Button(
                                onClick = { showPaymentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("Add Payment", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            }
                        }
                    }

                    if (payments.isEmpty()) {
                        item { EmptyPaymentState() }
                    } else {
                        items(payments.sortedByDescending { it.date }) { payment ->
                            PaymentItemTimeline(
                                payment = payment,
                                onEdit = { editingPayment = payment },
                                onDelete = { viewModel.deletePayment(payment) }
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                    }

                    // Clothing Orders Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Clothing Orders",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A148C)
                                )
                                Text(
                                    text = "Jerseys, Shirts, Names & Numbers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Button(
                                onClick = { showClothingDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2), contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("Add Order", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            }
                        }
                    }

                    if (clothingOrders.isEmpty()) {
                        item { EmptyClothingOrderState() }
                    } else {
                        items(clothingOrders.sortedByDescending { it.date }) { order ->
                            ClothingOrderItemCard(
                                order = order,
                                onEdit = { editingClothingOrder = order },
                                onDelete = { viewModel.deleteClothingOrder(order) }
                            )
                        }
                    }
                }
            }

            // Floating Progress Card
            studentWithStatus?.let { status ->
                ProgressCard(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 205.dp)
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

    if (showClothingDialog) {
        AddClothingOrderDialog(
            onDismiss = { showClothingDialog = false },
            onConfirm = { itemType, desc, name, number, size, price ->
                viewModel.addClothingOrder(studentId, itemType, desc, name, number, size, price)
                showClothingDialog = false
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

    editingPayment?.let { payment ->
        EditPaymentDialog(
            payment = payment,
            onDismiss = { editingPayment = null },
            onConfirm = { updatedAmount ->
                viewModel.updatePayment(payment.copy(amount = updatedAmount))
                editingPayment = null
            }
        )
    }

    editingClothingOrder?.let { order ->
        EditClothingOrderDialog(
            order = order,
            onDismiss = { editingClothingOrder = null },
            onConfirm = { itemType, desc, name, number, size, price ->
                viewModel.updateClothingOrder(order.copy(
                    itemType = itemType,
                    description = desc,
                    customName = name,
                    customNumber = number,
                    size = size,
                    price = price
                ))
                editingClothingOrder = null
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
            Button(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary, contentColor = Color.White)
            ) {
                Text("Confirm", color = Color.White)
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
                    text = "Total Payments Made",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "₱${item.totalPaid.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
        }
    }
}

@Composable
fun PaymentItemTimeline(
    payment: Payment,
    onEdit: () -> Unit,
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Payment", tint = Color.Gray)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ClothingOrderItemCard(
    order: ClothingOrder,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color(0xFF7B1FA2)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👕", fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = order.itemType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                        if (order.description.isNotBlank()) {
                            Text(
                                text = order.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                Text(
                    text = "₱${order.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4A148C)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFCE93D8).copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (order.customName.isNotBlank()) {
                        Text(
                            text = "Name: ${order.customName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    if (order.customNumber.isNotBlank()) {
                        Text(
                            text = "#${order.customNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "Size: ${order.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Order", tint = Color(0xFF7B1FA2), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Order", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPaymentState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFF3E0),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("📋", fontSize = 24.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No payments recorded yet.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyClothingOrderState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF3E5F5),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("👕", fontSize = 24.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No clothing orders yet.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add jerseys, shirts, names, numbers & sizes.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
