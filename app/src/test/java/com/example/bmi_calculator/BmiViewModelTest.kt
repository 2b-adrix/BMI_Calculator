package com.example.bmi_calculator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BmiViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: BmiViewModel

    @Before
    fun setup() {
        viewModel = BmiViewModel()
    }

    @Test
    fun `calculateBmi with valid inputs returns Success`() {
        // Given
        val weight = "70"
        val height = "170"

        // When
        viewModel.calculateBmi(weight, height)

        // Then
        val state = viewModel.bmiState.value
        assertTrue(state is BmiViewModel.BmiState.Success)
        val successState = state as BmiViewModel.BmiState.Success
        // BMI = 70 / (1.7^2) = 24.22
        assertEquals(24.22, successState.bmi, 0.01)
        assertEquals(R.string.category_normal, successState.categoryResId)
    }

    @Test
    fun `calculateBmi with invalid weight returns InvalidInput`() {
        // When
        viewModel.calculateBmi("abc", "170")

        // Then
        assertTrue(viewModel.bmiState.value is BmiViewModel.BmiState.Idle || viewModel.bmiState.value is BmiViewModel.BmiState.InvalidInput)
        assertEquals(BmiViewModel.BmiState.InvalidInput, viewModel.bmiState.value)
    }

    @Test
    fun `calculateBmi with zero height returns InvalidInput`() {
        // When
        viewModel.calculateBmi("70", "0")

        // Then
        assertEquals(BmiViewModel.BmiState.InvalidInput, viewModel.bmiState.value)
    }
}