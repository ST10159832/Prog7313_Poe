package com.example.prog7313finalpoe

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryOverviewActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var categoryList: LinearLayout
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_overview)

        pieChart = findViewById(R.id.pieChart)
        categoryList = findViewById(R.id.categoryList)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchAndDisplayData()
    }

    private fun fetchAndDisplayData() {
        val userId = auth.currentUser?.uid ?: return

        var totalIncome = 0.0
        var totalExpenses = 0.0
        val entries = mutableListOf<String>()

        firestore.collection("income")
            .get()
            .addOnSuccessListener { incomeDocs ->
                for (doc in incomeDocs) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    totalIncome += amount
                    entries.add("Income - ${doc.getString("category")}: R$amount")
                }

                firestore.collection("expenses")
                    .get()
                    .addOnSuccessListener { expenseDocs ->
                        for (doc in expenseDocs) {
                            val amount = doc.getDouble("amount") ?: 0.0
                            totalExpenses += amount
                            entries.add("Expense - ${doc.getString("category")}: R$amount")
                        }

                        updatePieChart(totalIncome, totalExpenses)
                        updateEntryList(entries)
                    }
            }
    }

    private fun updatePieChart(income: Double, expenses: Double) {
        val pieEntries = listOf(
            PieEntry(income.toFloat(), "Income"),
            PieEntry(expenses.toFloat(), "Expenses")
        )

        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = listOf(Color.GREEN, Color.RED)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Income vs Expenses"
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setUsePercentValues(true)
        pieChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        pieChart.invalidate()
    }

    private fun updateEntryList(entries: List<String>) {
        categoryList.removeAllViews()
        for (entry in entries) {
            val textView = TextView(this).apply {
                text = entry
                setTextColor(Color.WHITE)
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            categoryList.addView(textView)
        }
    }
}