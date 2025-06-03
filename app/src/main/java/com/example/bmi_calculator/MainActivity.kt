package com.example.bmi_calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmi_calculator.databinding.ActivityMainBinding
import java.lang.Math.pow
import kotlin.math.pow

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calculateBtn.setOnClickListener {
            calculateBMI()

        }

    }

      private fun calculateBMI() {
          val weight = binding.weightEdit.text.toString().toFloatOrNull()
          val height = binding.heightEdit.text.toString().toFloatOrNull()

          if (weight != null && height != null) {
              val bmi = weight / (height / 100).pow(2)
             val bmiResult = String.format("%.2f", bmi)

              val bmiCategory = when {
                  bmi < 18.5 -> "Underweight"
                  bmi < 24.9 -> "Normal Weight"
                  bmi < 29.9 -> "Overweight"
                  else -> "Obesity"

              }

              binding.resultText.text="BMI: $bmiResult\nCategory: $bmiCategory"
          }
          else{
               binding.resultText.text="Invalid Input"
          }
    }
}