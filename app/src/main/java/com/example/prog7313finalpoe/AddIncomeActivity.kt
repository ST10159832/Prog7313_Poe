package com.example.prog7313finalpoe

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore

//Jony Apps. 2021. Add Firebase to Android Project | Kotlin | Android Studio [Source Code]. Available at: < https://www.youtube.com/watch?v=K7QEyNVMxSA > [Accesssed on 28 May 2025]

class AddIncomeActivity : AppCompatActivity() {

    private lateinit var etCategory: EditText
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        firestore = FirebaseFirestore.getInstance()

        etCategory = findViewById(R.id.etIncomeCategory)
        etName = findViewById(R.id.etIncomeName)
        etAmount = findViewById(R.id.etIncomeAmount)
        etDate = findViewById(R.id.etIncomeDate)

        val btnSubmit = findViewById<View>(R.id.btnSubmitIncome)

        etDate.setOnClickListener {
            showDatePicker()
        }

        btnSubmit.setOnClickListener {
            saveIncomeToFirestore()
        }


    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this,
            { _, year, month, day ->
                val date = "$day/${month + 1}/$year"
                etDate.setText(date)
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun saveIncomeToFirestore() {
        val category = etCategory.text.toString().trim()
        val name = etName.text.toString().trim()
        val amount = etAmount.text.toString().trim()
        val date = etDate.text.toString().trim()

        if (category.isEmpty() || name.isEmpty() || amount.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val income = hashMapOf(
            "category" to category,
            "name" to name,
            "amount" to amount.toDoubleOrNull(),
            "date" to date,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("income")
            .add(income)
            .addOnSuccessListener {
                Toast.makeText(this, "Income saved", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        etCategory.text.clear()
        etName.text.clear()
        etAmount.text.clear()
        etDate.text.clear()
    }
}