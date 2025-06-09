package com.example.prog7313finalpoe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Use your correct layout file here (e.g., activity_home.xml)

        val addExpenseButton = findViewById<Button>(R.id.btn_AddExpense)
        val addIncomeButton = findViewById<Button>(R.id.btn_AddIncome)
        val spendingOverviewButton = findViewById<Button>(R.id.btn_SpendingOverview)
        val budgetOverviewButton = findViewById<Button>(R.id.btn_BudgetOverview)
        val fundsButton = findViewById<Button>(R.id.btn_Funds)
        val goalsButton = findViewById<Button>(R.id.btn_Goals)
        val historyOverviewButton = findViewById<Button>(R.id.btn_HistoryOverview)

        addExpenseButton.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        addIncomeButton.setOnClickListener {
            val intent = Intent(this, AddIncomeActivity::class.java)
            startActivity(intent)
        }

        spendingOverviewButton.setOnClickListener {
            startActivity(Intent(this, SpendingOverviewActivity::class.java))
        }

        budgetOverviewButton.setOnClickListener {
            startActivity(Intent(this, BudgetOverviewActivity::class.java))
        }

        fundsButton.setOnClickListener {
            startActivity(Intent(this, FundsActivity::class.java))
        }

        goalsButton.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        historyOverviewButton.setOnClickListener {
            startActivity(Intent(this, HistoryOverviewActivity::class.java))
        }
    }
}