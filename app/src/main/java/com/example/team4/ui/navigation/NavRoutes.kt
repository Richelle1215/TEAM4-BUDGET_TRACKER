package com.example.team4.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    object Dashboard : Screen
    
    @Serializable
    object Students : Screen
    
    @Serializable
    data class StudentDetail(val studentId: String) : Screen
    
    @Serializable
    object Expenses : Screen
    
    @Serializable
    object ImportExport : Screen
}
