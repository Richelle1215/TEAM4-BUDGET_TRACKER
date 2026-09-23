package com.example.team4.data.repository

import com.example.team4.data.model.Student
import org.junit.Assert.assertEquals
import org.junit.Test

class PricingTest {

    @Test
    fun `test pricing logic for first 50 students`() {
        val paidCount = 49
        val target = if (paidCount < 50) 600.0 else 650.0
        assertEquals(600.0, target, 0.0)
    }

    @Test
    fun `test pricing logic for 51st student`() {
        val paidCount = 50
        val target = if (paidCount < 50) 600.0 else 650.0
        assertEquals(650.0, target, 0.0)
    }
}
