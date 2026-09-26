package com.example.team4.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "students")
data class Student(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    // Default constructor for Firebase
    constructor() : this("", "", 0L)
}
