package com.example.team4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team4.data.model.Student
import com.example.team4.data.model.Payment
import com.example.team4.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StudentFilter {
    ALL, PAID, UNPAID
}

data class StudentWithStatus(
    val student: Student,
    val totalPaid: Double,
    val status: StudentFilter,
    val isEarlyBird: Boolean
)

data class StatusCounts(
    val all: Int = 0,
    val paid: Int = 0,
    val partial: Int = 0,
    val unpaid: Int = 0
)

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(StudentFilter.ALL)
    val selectedFilter: StateFlow<StudentFilter> = _selectedFilter.asStateFlow()

    private val allStudentsWithStatus = combine(
        repository.getStudents(),
        repository.getPayments()
    ) { students, payments ->
        students.map { student ->
            val totalPaid = payments.filter { it.studentId == student.id }.sumOf { it.amount }
            val status = when {
                student.targetAmount == 0.0 -> StudentFilter.UNPAID
                totalPaid >= student.targetAmount -> StudentFilter.PAID
                else -> StudentFilter.UNPAID
            }
            StudentWithStatus(
                student = student,
                totalPaid = totalPaid,
                status = status,
                isEarlyBird = student.targetAmount == 600.0
            )
        }
    }

    val statusCounts: StateFlow<StatusCounts> = allStudentsWithStatus.map { list ->
        StatusCounts(
            all = list.size,
            paid = list.count { it.status == StudentFilter.PAID },
            unpaid = list.count { it.status == StudentFilter.UNPAID }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatusCounts())

    val filteredStudents: StateFlow<List<StudentWithStatus>> = combine(
        allStudentsWithStatus,
        searchQuery,
        selectedFilter
    ) { list, query, filter ->
        list.filter { item ->
            val matchesQuery = item.student.name.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                StudentFilter.ALL -> true
                else -> item.status == filter
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Legacy support for other screens if needed
    val students: StateFlow<List<Student>> = repository.getStudents()
        .combine(searchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.getPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: StudentFilter) {
        _selectedFilter.value = filter
    }

    fun addStudent(name: String) {
        viewModelScope.launch {
            repository.addStudent(name)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun updatePayment(payment: Payment) {
        viewModelScope.launch {
            repository.updatePayment(payment)
        }
    }

    fun deleteAllStudents() {
        viewModelScope.launch {
            repository.deleteAllStudents()
        }
    }

    fun resetStudents() {
        viewModelScope.launch {
            repository.resetStudents()
        }
    }

    fun getStudentWithStatus(studentId: String): Flow<StudentWithStatus?> {
        return allStudentsWithStatus.map { list -> list.find { it.student.id == studentId } }
    }

    fun addPayment(studentId: String, amount: Double, date: Long) {
        viewModelScope.launch {
            repository.addPayment(studentId, amount, date)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }
    
    fun getPaymentsForStudent(studentId: String): Flow<List<Payment>> {
        return payments.map { list -> list.filter { it.studentId == studentId } }
    }
}
