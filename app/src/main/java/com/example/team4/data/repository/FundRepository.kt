package com.example.team4.data.repository

import com.example.team4.data.local.FundDao
import com.example.team4.data.model.ClothingOrder
import com.example.team4.data.model.Expense
import com.example.team4.data.model.Payment
import com.example.team4.data.model.Student
import com.example.team4.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
            "Pangilinan Richard Martin", "Pesebre Aeron Jay", "Pimentel, Mikaela Joy", "Quiñones James Kenneth Balon",
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
                if (FirebaseAuth.getInstance().currentUser == null) {
                    FirebaseAuth.getInstance().signInAnonymously().await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        repositoryScope.launch {
            try {
                // 1. Immediate Seeding & Local Deduplication
                val count = fundDao.getStudentCount().first()
                if (count == 0) {
                    seedStudents()
                } else {
                    val allStudents = fundDao.getAllStudents().first()
                    deduplicateStudents(allStudents)
                    
                    // Ensure all default students exist
                    DEFAULT_STUDENT_LIST.forEach { name ->
                        val studentId = UUID.nameUUIDFromBytes(name.toByteArray()).toString()
                        val existing = fundDao.getStudentById(studentId)
                        if (existing == null) {
                            val student = Student(id = studentId, name = name)
                            fundDao.insertStudent(student)
                            try { firestoreService.saveStudent(student) } catch (e: Exception) {}
                        }
                    }
                }

                // 2. Real-time Cloud Sync with Deduplication
                firestoreService.getStudents().collect { cloudStudents ->
                    if (cloudStudents.isNotEmpty()) {
                        cloudStudents.forEach { fundDao.insertStudent(it) }
                        deduplicateStudents(cloudStudents)
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
        repositoryScope.launch {
            firestoreService.getClothingOrders().collect { orders ->
                orders.forEach { fundDao.insertClothingOrder(it) }
            }
        }
    }

    private suspend fun deduplicateStudents(students: List<Student>) {
        val grouped = students.groupBy { it.name.trim().lowercase() }
        val allPayments = fundDao.getAllPayments().first()
        val allOrders = fundDao.getAllClothingOrders().first()

        grouped.forEach { (_, studentGroup) ->
            if (studentGroup.size > 1) {
                // Find canonical student ID (prefer deterministic UUID if in DEFAULT_STUDENT_LIST)
                val canonicalStudent = studentGroup.firstOrNull { s ->
                    val expectedId = UUID.nameUUIDFromBytes(s.name.toByteArray()).toString()
                    s.id == expectedId
                } ?: studentGroup.first()

                val duplicates = studentGroup.filter { it.id != canonicalStudent.id }

                duplicates.forEach { dup ->
                    // Re-link payments to canonical student
                    allPayments.filter { it.studentId == dup.id }.forEach { p ->
                        val updated = p.copy(studentId = canonicalStudent.id)
                        fundDao.updatePayment(updated)
                        try { firestoreService.savePayment(updated) } catch (e: Exception) {}
                    }

                    // Re-link clothing orders to canonical student
                    allOrders.filter { it.studentId == dup.id }.forEach { o ->
                        val updated = o.copy(studentId = canonicalStudent.id)
                        fundDao.updateClothingOrder(updated)
                        try { firestoreService.saveClothingOrder(updated) } catch (e: Exception) {}
                    }

                    // Delete duplicate student from local Room and cloud Firestore
                    fundDao.deleteStudent(dup)
                    try {
                        firestoreService.deleteStudent(dup.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private suspend fun seedStudents() {
        DEFAULT_STUDENT_LIST.forEach { name ->
            val studentId = UUID.nameUUIDFromBytes(name.toByteArray()).toString()
            val student = Student(
                id = studentId,
                name = name
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
    fun getClothingOrders(): Flow<List<ClothingOrder>> = fundDao.getAllClothingOrders()

    suspend fun addClothingOrder(studentId: String, itemType: String, description: String, customName: String, customNumber: String, size: String, price: Double) {
        val order = ClothingOrder(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            itemType = itemType,
            description = description,
            customName = customName,
            customNumber = customNumber,
            size = size,
            price = price,
            date = System.currentTimeMillis()
        )
        fundDao.insertClothingOrder(order)
        try {
            firestoreService.saveClothingOrder(order)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateClothingOrder(order: ClothingOrder) {
        fundDao.updateClothingOrder(order)
        try {
            firestoreService.saveClothingOrder(order)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteClothingOrder(order: ClothingOrder) {
        fundDao.deleteClothingOrder(order)
        try {
            firestoreService.deleteClothingOrder(order.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addStudent(name: String) {
        val student = Student(
            id = UUID.randomUUID().toString(),
            name = name
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
            getClothingOrders()
        ) { payments, expenses, orders ->
            val totalCollected = payments.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.amount }
            val balance = totalCollected - totalExpenses
            val totalOrdersPrice = orders.sumOf { it.price }
            
            FinancialSummary(
                totalCollected = totalCollected,
                totalExpenses = totalExpenses,
                balance = balance,
                targetedCollection = totalOrdersPrice
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
