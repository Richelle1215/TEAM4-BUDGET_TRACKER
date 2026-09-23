package com.example.team4.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "students")
data class Student(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val addedAt: Long = System.currentTimeMillis()
) {
    // Default constructor for Firebase
    constructor() : this("", "", 0.0, 0.0.toLong())
}
