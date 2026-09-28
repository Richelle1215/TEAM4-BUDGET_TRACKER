package com.example.team4.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.team4.data.model.Expense
import com.example.team4.ui.theme.ErrorRedDeep
import com.example.team4.ui.theme.IndigoPrimary
import com.example.team4.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel
) {
    val expenses by viewModel.expenses.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF5C6BC0),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        bottomBar = { Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Crimson Header
            Surface(
                color = ErrorRedDeep,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Expense Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "TOTAL EXPENSES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "₱${String.format("%,d", totalExpenses.toInt())}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "${expenses.size} transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Expense List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenses.sortedByDescending { it.date }) { expense ->
                    ExpenseItemMatch(
                        expense = expense,
                        onEdit = { editingExpense = expense },
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialogSheet(
            onDismiss = { showAddDialog = false },
            onConfirm = { desc, amount, date ->
                viewModel.addExpense(desc, amount, date)
                showAddDialog = false
            }
        )
    }

    editingExpense?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            onDismiss = { editingExpense = null },
            onConfirm = { updatedDesc, updatedAmount ->
                viewModel.updateExpense(expense.copy(description = updatedDesc, amount = updatedAmount))
                editingExpense = null
            }
        )
    }
}

@Composable
fun ExpenseItemMatch(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd/2024", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFFF5F5F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "Transaction",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Action Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Expense") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Expense", color = Color.Red) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₱${String.format("%,d", expense.amount.toInt())}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    text = dateFormat.format(Date(expense.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialogSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Long) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What was purchased?") },
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Amount (₱)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.00") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Date", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = SimpleDateFormat("MM/dd/2026", Locale.getDefault()).format(Date()),
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = { onConfirm(desc, amount.toDoubleOrNull() ?: 0.0, System.currentTimeMillis()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Add Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}
