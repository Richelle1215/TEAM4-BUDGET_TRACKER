package com.example.team4.data.remote

import com.example.team4.data.model.ClothingOrder
import com.example.team4.data.model.Expense
import com.example.team4.data.model.Payment
import com.example.team4.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getStudents(): Flow<List<Student>> = db.collection("students")
        .snapshots()
        .map { it.toObjects<Student>() }

    suspend fun saveStudent(student: Student) {
        db.collection("students").document(student.id).set(student).await()
    }

    suspend fun deleteStudent(studentId: String) {
        db.collection("students").document(studentId).delete().await()
    }

    fun getPayments(): Flow<List<Payment>> = db.collection("payments")
        .snapshots()
        .map { it.toObjects<Payment>() }

    suspend fun savePayment(payment: Payment) {
        db.collection("payments").document(payment.id).set(payment).await()
    }

    suspend fun deletePayment(paymentId: String) {
        db.collection("payments").document(paymentId).delete().await()
    }

    fun getExpenses(): Flow<List<Expense>> = db.collection("expenses")
        .snapshots()
        .map { it.toObjects<Expense>() }

    suspend fun saveExpense(expense: Expense) {
        db.collection("expenses").document(expense.id).set(expense).await()
    }

    suspend fun deleteExpense(expenseId: String) {
        db.collection("expenses").document(expenseId).delete().await()
    }

    fun getClothingOrders(): Flow<List<ClothingOrder>> = db.collection("clothing_orders")
        .snapshots()
        .map { it.toObjects<ClothingOrder>() }

    suspend fun saveClothingOrder(order: ClothingOrder) {
        db.collection("clothing_orders").document(order.id).set(order).await()
    }

    suspend fun deleteClothingOrder(orderId: String) {
        db.collection("clothing_orders").document(orderId).delete().await()
    }
}
