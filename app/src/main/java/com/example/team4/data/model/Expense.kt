package com.example.team4.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", 0.0, 0L)
}
