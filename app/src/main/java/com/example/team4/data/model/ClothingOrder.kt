package com.example.team4.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "clothing_orders")
data class ClothingOrder(
    @PrimaryKey val id: String = "",
    val studentId: String = "",
    val itemType: String = "", // e.g. "Jersey", "Shirt", "Polo"
    val description: String = "", // e.g. "Dry-fit material, black/white"
    val customName: String = "", // Name on jersey/shirt
    val customNumber: String = "", // Number on jersey/shirt
    val size: String = "", // S, M, L, XL, XXL
    val price: Double = 0.0,
    val date: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "", "", 0.0, 0L)
}
