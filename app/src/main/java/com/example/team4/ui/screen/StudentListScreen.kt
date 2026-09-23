package com.example.team4.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.team4.data.model.Student
import com.example.team4.ui.viewmodel.StudentFilter
import com.example.team4.ui.viewmodel.StudentViewModel
import com.example.team4.ui.viewmodel.StudentWithStatus

@Composable
fun StudentListScreen(
    viewModel: StudentViewModel,
    onStudentClick: (String) -> Unit
) {
    val filteredStudents by viewModel.filteredStudents.collectAsState()
    val statusCounts by viewModel.statusCounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            // Blue Header Section
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Students",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        
                        var showHeaderMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showHeaderMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showHeaderMenu,
                                onDismissRequest = { showHeaderMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Reload Default List") },
                                    onClick = {
                                        showHeaderMenu = false
                                        viewModel.resetStudents()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete All Students", color = Color.Red) },
                                    onClick = {
                                        showHeaderMenu = false
                                        viewModel.deleteAllStudents()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Integrated Search Bar
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Search ${statusCounts.all} students...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            // Filter Chips Section
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatusFilterChip(
                        label = "All",
                        count = statusCounts.all,
                        isSelected = selectedFilter == StudentFilter.ALL,
                        onClick = { viewModel.updateFilter(StudentFilter.ALL) }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Paid",
                        count = statusCounts.paid,
                        isSelected = selectedFilter == StudentFilter.PAID,
                        onClick = { viewModel.updateFilter(StudentFilter.PAID) }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Unpaid",
                        count = statusCounts.unpaid,
                        isSelected = selectedFilter == StudentFilter.UNPAID,
                        onClick = { viewModel.updateFilter(StudentFilter.UNPAID) }
                    )
                }
            }

            // List of Students
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStudents) { item ->
                    StudentItemMatch(
                        item = item,
                        onClick = { onStudentClick(item.student.id) },
                        onEdit = { editingStudent = item.student },
                        onDelete = { viewModel.deleteStudent(item.student) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addStudent(name)
                showAddDialog = false
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
fun StatusFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label)
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (isSelected) Color.White.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.4f),
                    shape = CircleShape
                ) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = Color.LightGray.copy(alpha = 0.5f),
            enabled = true,
            selected = isSelected
        )
    )
}

@Composable
fun StudentItemMatch(
    item: StudentWithStatus,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (item.student.targetAmount > 0) 
        (item.totalPaid / item.student.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    val statusColor = when (item.status) {
        StudentFilter.PAID -> Color(0xFF4CAF50)
        else -> Color(0xFFE53935)
    }
    
    var showMenu by remember { mutableStateOf(false) }
    
    val initials = item.student.name.split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (item.isEarlyBird) Color(0xFFE68A00) else Color.LightGray.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // Early Bird Corner Badge
            if (item.isEarlyBird) {
                Surface(
                    color = Color(0xFFE68A00),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, topEnd = 20.dp))
                ) {
                    Text(
                        text = "EARLY BIRD",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.student.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Text(
                            text = "Target: ₱${String.format("%.0f", item.student.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
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
                                text = { Text("Edit Student") },
                                onClick = { showMenu = false; onEdit() },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Student", color = Color.Red) },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Pill
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (item.status == StudentFilter.PAID) "Paid in Full" else "Unpaid",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (progress >= 1f) Color(0xFF1B5E20) else statusColor,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val remaining = maxOf(0.0, item.student.targetAmount - item.totalPaid)
                Text(
                    text = buildString {
                        append("₱${String.format("%.0f", item.totalPaid)} paid")
                        if (remaining > 0) append(" • ₱${String.format("%.0f", remaining)} remaining")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
