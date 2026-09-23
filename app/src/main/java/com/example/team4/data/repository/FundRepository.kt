package com.example.team4.data.repository

import com.example.team4.data.local.FundDao
import com.example.team4.data.model.Expense
import com.example.team4.data.model.Payment
import com.example.team4.data.model.Student
import com.example.team4.data.remote.FirestoreService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundRepository @Inject constructor(
    private val fundDao: FundDao,
    private val firestoreService: FirestoreService
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val DEFAULT_STUDENT_LIST = listOf(
            "Abanes Prince Jester Clete", "Avanceña Zeus Aimon", "Azures Rainel Dave",
            "Balderama Judy Ann", "Balunsay Emil Valencia", "Bayani John Mark Sanchez",
            "Benitez James Frad Paming", "Bermas Jane Roth", "Bermas John Harold",
            "Bongbonga Khrysler Clark Raro", "Buela Rose Ann Mesia", "Cabangon Alberth Joseph Yanto",
            "Cajan Nelberto Asis", "Calano Jerwin", "Capaclete Haljein Severino",
            "Casiano Levy Jr. Zabala", "Danas Al Vincent Adie", "Dasco Arbie Millama",
            "Dating Martin Gabriel Vedad", "David Precious Wena Cajan", "De Guzman Mack Ehween",
            "De Leon Miggy Angelo Aquino", "Dela Rama Kyle Raven Aquino", "Esperanza Gilonhlel Lavaple",
            "Esplana Hazley Basallote", "Estacion John Lyod Asis", "Estrella Kenneth Adrian",
            "Evangelista Justhine Sena", "Factor Glen Santiago", "Fillo Mark Clarence Pepino",
            "Fulgencio Nash", "Gadil Ethan Joshua Bartolay", "Galvan Laurence",
            "Garrido Nomarie", "Grava Jaymhel Sanchez", "Hatori Dayanne Yuki",
            "Hombre Aldrei Jazh Makilig", "Jueves Nimrod Troy Nava", "Laudes John Justin Gomez",
            "Lerum Andrie Sabas", "Linguete Vaun Whelder Deuna", "Lumenario Ivan Tacalan",
            "Manarang Rhon Ruszell Marabe", "Marca Arvie", "Mendez Richelle Aguilar",
            "Molina Jade Cedrick Barcoma", "Nano Dennis Kyle Marzan", "Nava Mhyco Allen Kien",
            "Obligacion Reynalen Malabunga", "Oda CJ Coronel", "Paderes Bea Blanca Macandog",
            "Pangilinan Richard Martin", "Pesebre Aeron Jay", "Quiñones James Kenneth Balon",
            "Rafa Razel Ken Hernandez", "Rajas John Nicco Villeno", "Rogacion Rlezza Mae Omaga",
            "San Antonio Mary Ann", "Sanchez Leslie Faye", "Sarical Ella Bardon",
            "Simon Justine Michael Acula", "Suyat Rhoy Jayson", "Tacanay Neil Gaibbriel Tagala",
            "Triñanes John Zidney Hywel", "Vega Riz Lorenz Astrovalo", "Villaluz Jasper Phillip",
            "Villania Crystal Lee Rafer", "Yaneza John Carlo Barbosa", "Yape Adrian",
            "Yebra Erwin Jr. Balane"
        )
    }

    init {
        repositoryScope.launch {
            try {
                // 1. Immediate Seeding (Local-First)
                val count = fundDao.getStudentCount().first()
                if (count == 0) {
                    seedStudents()
                } else {
                    // Cleanup student "40" if exists
                    fundDao.getAllStudents().first().find { it.name == "40" }?.let { badStudent ->
                        deleteStudent(badStudent)
                    }
                }

                // 2. Real-time Cloud Sync (Two-way)
                // Firestore sync starting...
                firestoreService.getStudents().collect { cloudStudents ->
                    if (cloudStudents.isNotEmpty()) {
                        cloudStudents.forEach { fundDao.insertStudent(it) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        repositoryScope.launch {
            firestoreService.getPayments().collect { payments ->
                payments.forEach { fundDao.insertPayment(it) }
            }
        }
        repositoryScope.launch {
            firestoreService.getExpenses().collect { expenses ->
                expenses.forEach { fundDao.insertExpense(it) }
            }
        }
    }

    private suspend fun seedStudents() {
        DEFAULT_STUDENT_LIST.forEach { name ->
            val student = Student(
                id = UUID.randomUUID().toString(),
                name = name,
                targetAmount = 0.0
            )
            // Insert to local database immediately so UI updates
            fundDao.insertStudent(student)
            
            // Fire-and-forget sync to Firestore (don't wait for server)
            repositoryScope.launch {
                try {
                    firestoreService.saveStudent(student)
                } catch (e: Exception) {
                    // Ignore seeding errors for Firestore, we'll sync later
                }
            }
        }
    }

    fun getStudents(): Flow<List<Student>> = fundDao.getAllStudents()
    fun getPayments(): Flow<List<Payment>> = fundDao.getAllPayments()
    fun getExpenses(): Flow<List<Expense>> = fundDao.getAllExpenses()

    suspend fun addStudent(name: String) {
        val student = Student(
            id = UUID.randomUUID().toString(),
            name = name,
            targetAmount = 0.0 // To be assigned on first payment
        )
        // Insert to local Room immediately
        fundDao.insertStudent(student)
        // Try to save to Firestore in background
        try {
            firestoreService.saveStudent(student)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateStudent(student: Student) {
        fundDao.insertStudent(student)
        try {
            firestoreService.saveStudent(student)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteStudent(student: Student) {
        fundDao.deleteStudent(student)
        try {
            firestoreService.deleteStudent(student.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteAllStudents() {
        fundDao.deleteAllStudents()
    }

    suspend fun resetStudents() {
        fundDao.deleteAllStudents()
        seedStudents()
    }

    suspend fun addPayment(studentId: String, amount: Double, date: Long) {
        val student = fundDao.getStudentById(studentId) ?: return
        
        // Check if targetAmount is already set
        if (student.targetAmount == 0.0) {
            val paidStudentsCount = fundDao.getAllStudents().first().count { it.targetAmount > 0.0 }
            val target = if (paidStudentsCount < 50) 600.0 else 650.0
            updateStudent(student.copy(targetAmount = target))
        }

        val payment = Payment(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            amount = amount,
            date = date
        )
        insertPayment(payment)
    }

    suspend fun insertPayment(payment: Payment) {
        fundDao.insertPayment(payment)
        try {
            firestoreService.savePayment(payment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updatePayment(payment: Payment) {
        fundDao.updatePayment(payment)
        try {
            firestoreService.savePayment(payment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deletePayment(payment: Payment) {
        fundDao.deletePayment(payment)
        try {
            firestoreService.deletePayment(payment.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addExpense(description: String, amount: Double, date: Long) {
        val expense = Expense(
            id = UUID.randomUUID().toString(),
            description = description,
            amount = amount,
            date = date
        )
        insertExpense(expense)
    }

    suspend fun insertExpense(expense: Expense) {
        fundDao.insertExpense(expense)
        try {
            firestoreService.saveExpense(expense)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        fundDao.deleteExpense(expense)
        try {
            firestoreService.deleteExpense(expense.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateExpense(expense: Expense) {
        fundDao.updateExpense(expense)
        try {
            firestoreService.saveExpense(expense)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(
            getPayments(),
            getExpenses(),
            getStudents()
        ) { payments, expenses, students ->
            val totalCollected = payments.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.amount }
            val balance = totalCollected - totalExpenses
            val targetedCollection = students.sumOf { it.targetAmount }
            
            FinancialSummary(
                totalCollected = totalCollected,
                totalExpenses = totalExpenses,
                balance = balance,
                targetedCollection = targetedCollection
            )
        }
    }
}

data class FinancialSummary(
    val totalCollected: Double,
    val totalExpenses: Double,
    val balance: Double,
    val targetedCollection: Double
)
