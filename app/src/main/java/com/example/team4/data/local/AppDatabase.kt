package com.example.team4.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.team4.data.model.Expense
import com.example.team4.data.model.Payment
import com.example.team4.data.model.Student

@Database(entities = [Student::class, Payment::class, Expense::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fundDao(): FundDao
}
