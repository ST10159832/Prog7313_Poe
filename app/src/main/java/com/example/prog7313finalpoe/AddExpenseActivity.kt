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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

//Jony Apps. 2021. Add Firebase to Android Project | Kotlin | Android Studio [Source Code]. Available at: < https://www.youtube.com/watch?v=K7QEyNVMxSA > [Accesssed on 28 May 2025]

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etCategory: EditText
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize input fields
        etCategory = findViewById(R.id.etExpenseCategory)
        etName = findViewById(R.id.etExpenseName)
        etAmount = findViewById(R.id.etExpenseAmount)
        etDate = findViewById(R.id.etExpenseDate)

        val btnSubmit = findViewById<View>(R.id.btnSubmitExpense)

        // Date picker
        etDate.setOnClickListener {
            showDatePicker()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            saveExpenseToFirestore()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                etDate.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveExpenseToFirestore() {
        val category = etCategory.text.toString().trim()
        val name = etName.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val date = etDate.text.toString().trim()

        if (category.isEmpty() || name.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = hashMapOf(
            "category" to category,
            "name" to name,
            "amount" to amount,
            "date" to date,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
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