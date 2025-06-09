package com.example.prog7313finalpoe

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class SpendingOverviewActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spending_overview)

        lineChart = findViewById(R.id.lineChart)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid ?: return

        val expensesRef = db.collection("users").document(userId).collection("expenses")
        val incomeRef = db.collection("users").document(userId).collection("income")

        val expensesList = mutableListOf<FinancialEntry>()
        val incomeList = mutableListOf<FinancialEntry>()

        // Fetch expenses
        expensesRef.get().addOnSuccessListener { expenseDocs ->
            for (doc in expenseDocs) {
                val amount = doc.getDouble("amount") ?: 0.0
                val date = doc.getTimestamp("date")?.toDate() ?: Date()
                expensesList.add(FinancialEntry(amount, date))
            }

            // Fetch income
            incomeRef.get().addOnSuccessListener { incomeDocs ->
                for (doc in incomeDocs) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    val date = doc.getTimestamp("date")?.toDate() ?: Date()
                    incomeList.add(FinancialEntry(amount, date))
                }

                plotGraph(expensesList, incomeList)
            }
        }
    }

    private fun plotGraph(expenses: List<FinancialEntry>, income: List<FinancialEntry>) {
        val expenseEntries = expenses.sortedBy { it.date }.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.amount.toFloat())
        }

        val incomeEntries = income.sortedBy { it.date }.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.amount.toFloat())
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Expenses").apply {
            color = Color.RED
            valueTextColor = Color.WHITE
            lineWidth = 2f
            circleRadius = 4f
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = Color.GREEN
            valueTextColor = Color.WHITE
            lineWidth = 2f
            circleRadius = 4f
        }

        val data = LineData(expenseDataSet, incomeDataSet)

        lineChart.data = data
        lineChart.setBackgroundColor(Color.parseColor("#1E4D8B"))
        lineChart.setNoDataText("No financial data available")
        lineChart.animateX(1000)
        lineChart.invalidate()
    }
}