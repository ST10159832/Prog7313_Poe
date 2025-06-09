package com.example.prog7313finalpoe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GoalsActivity : AppCompatActivity() {
//Jony Apps. 2021. Add Firebase to Android Project | Kotlin | Android Studio [Source Code]. Available at: < https://www.youtube.com/watch?v=K7QEyNVMxSA > [Accesssed on 28 May 2025]
    private lateinit var etMinGoalAmount: EditText
    private lateinit var etMinGoalDate: EditText
    private lateinit var etMaxGoalAmount: EditText
    private lateinit var etMaxGoalDate: EditText
    private lateinit var btnSaveGoals: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // Initialize views
        etMinGoalAmount = findViewById(R.id.etMinGoalAmount)
        etMinGoalDate = findViewById(R.id.etMinGoalDate)
        etMaxGoalAmount = findViewById(R.id.etMaxGoalAmount)
        etMaxGoalDate = findViewById(R.id.etMaxGoalDate)
        btnSaveGoals = findViewById(R.id.btnSaveGoals)

        // Set click listener
        btnSaveGoals.setOnClickListener { saveGoalsToFirestore() }
    }

    private fun saveGoalsToFirestore() {
        val minAmount = etMinGoalAmount.text.toString().trim()
        val minDate = etMinGoalDate.text.toString().trim()
        val maxAmount = etMaxGoalAmount.text.toString().trim()
        val maxDate = etMaxGoalDate.text.toString().trim()

        if (minAmount.isEmpty() || minDate.isEmpty() || maxAmount.isEmpty() || maxDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val goals = hashMapOf(
            "minAmount" to minAmount,
            "minDate" to minDate,
            "maxAmount" to maxAmount,
            "maxDate" to maxDate,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("monthly_goals")
            .add(goals)
            .addOnSuccessListener {
                Toast.makeText(this, "Goals saved!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        etMinGoalAmount.text.clear()
        etMinGoalDate.text.clear()
        etMaxGoalAmount.text.clear()
        etMaxGoalDate.text.clear()
    }
}