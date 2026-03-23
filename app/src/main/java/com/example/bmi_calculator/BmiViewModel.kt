package com.example.bmi_calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.pow

enum class UnitSystem {
    METRIC, IMPERIAL
}

class BmiViewModel : ViewModel() {

    var weightInput by mutableStateOf("")
    var heightInput by mutableStateOf("")
    var unitSystem by mutableStateOf(UnitSystem.METRIC)

    private var _bmiState by mutableStateOf<BmiState>(BmiState.Idle)
    val bmiState: BmiState get() = _bmiState

    sealed class BmiState {
        object Idle : BmiState()
        data class Success(
            val bmi: Double,
            val categoryResId: Int,
            val colorResId: Int,
            val percent: Float // 0.0 to 1.0 for the gauge
        ) : BmiState()
        object InvalidInput : BmiState()
    }

    fun onWeightChange(value: String) {
        weightInput = value
    }

    fun onHeightChange(value: String) {
        heightInput = value
    }

    fun toggleUnitSystem(system: UnitSystem) {
        unitSystem = system
        _bmiState = BmiState.Idle
        // Optional: Convert values? Usually better to clear for accuracy.
        weightInput = ""
        heightInput = ""
    }

    fun calculateBmi() {
        val weight = weightInput.toFloatOrNull()
        val height = heightInput.toFloatOrNull()

        if (weight != null && height != null && weight > 0 && height > 0) {
            val bmi = if (unitSystem == UnitSystem.METRIC) {
                weight / (height / 100).pow(2)
            } else {
                703 * (weight / height.pow(2))
            }

            val (categoryResId, colorResId, percent) = when {
                bmi < 18.5 -> Triple(R.string.category_underweight, R.color.bmi_underweight, (bmi / 18.5f).coerceIn(0f, 1f) * 0.25f)
                bmi < 24.9 -> Triple(R.string.category_normal, R.color.bmi_normal, 0.25f + ((bmi - 18.5f) / (24.9f - 18.5f)) * 0.25f)
                bmi < 29.9 -> Triple(R.string.category_overweight, R.color.bmi_overweight, 0.5f + ((bmi - 24.9f) / (29.9f - 24.9f)) * 0.25f)
                else -> Triple(R.string.category_obesity, R.color.bmi_obesity, 0.75f + ((bmi - 29.9f) / 10f).coerceIn(0f, 1f) * 0.25f)
            }
            _bmiState = BmiState.Success(bmi.toDouble(), categoryResId, colorResId, percent.toFloat())
        } else {
            _bmiState = BmiState.InvalidInput
        }
    }
}