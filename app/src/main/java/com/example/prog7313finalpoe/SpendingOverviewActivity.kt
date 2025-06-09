package com.example.prog7313finalpoe

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SpendingOverviewActivity : AppCompatActivity() {
//Admin Grabs Media. 2024. Android Display Bar Graph using Kotlin - Android Studio 2023 - Part 1 [Source Code]. Available at: < https://www.youtube.com/watch?v=-TGUV_LbcmE&list=PLlvGFqCblJgQ23xB6_aMvrQToXmDos2uM > [ Accessed on 03 Jube 2025]
    private lateinit var lineChart: LineChart
    private lateinit var db: FirebaseFirestore
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    // Data structures for aggregation
    private val dailyExpenses = mutableMapOf<String, Double>()
    private val dailyIncome = mutableMapOf<String, Double>()
    private val allDates = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spending_overview)

        initializeViews()
        setupChart()
        loadFinancialData()
    }

    private fun initializeViews() {
        lineChart = findViewById(R.id.lineChart)
        db = FirebaseFirestore.getInstance()
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            legend.isEnabled = true
            legend.textColor = Color.WHITE
            animateY(1000)
            setNoDataText("No financial data available")
            setNoDataTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1E4D8B"))

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
            }

            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.parseColor("#3A6EA5")
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
        }
    }

    private fun loadFinancialData() {
        // Clear previous data
        dailyExpenses.clear()
        dailyIncome.clear()
        allDates.clear()

        // Fetch expenses directly from the expenses collection
        db.collection("expenses")
            .get()
            .addOnSuccessListener { expenseDocs ->
                expenseDocs.forEach { doc ->
                    val amount = doc.getDouble("amount") ?: 0.0
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val dateKey = dateFormat.format(Date(timestamp))

                    dailyExpenses[dateKey] = dailyExpenses.getOrDefault(dateKey, 0.0) + amount
                    allDates.add(dateKey)
                }

                // Fetch income directly from the income collection
                db.collection("income")
                    .get()
                    .addOnSuccessListener { incomeDocs ->
                        incomeDocs.forEach { doc ->
                            val amount = doc.getDouble("amount") ?: 0.0
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val dateKey = dateFormat.format(Date(timestamp))

                            dailyIncome[dateKey] = dailyIncome.getOrDefault(dateKey, 0.0) + amount
                            allDates.add(dateKey)
                        }

                        updateChart()
                        updateActivityMessages()
                    }
            }
    }

    private fun updateChart() {
        // Sort dates chronologically
        val sortedDates = allDates.sortedBy { dateFormat.parse(it) }

        // Create entries
        val expenseEntries = ArrayList<Entry>()
        val incomeEntries = ArrayList<Entry>()

        sortedDates.forEachIndexed { index, date ->
            expenseEntries.add(Entry(
                index.toFloat(),
                dailyExpenses[date]?.toFloat() ?: 0f
            ))
            incomeEntries.add(Entry(
                index.toFloat(),
                dailyIncome[date]?.toFloat() ?: 0f
            ))
        }

        // Create datasets
        val expenseDataSet = LineDataSet(expenseEntries, "Expenses").apply {
            color = Color.parseColor("#FF6B6B") // Soft red
            valueTextColor = Color.WHITE
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(Color.parseColor("#FF6B6B"))
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = Color.parseColor("#4CAF50") // Soft green
            valueTextColor = Color.WHITE
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(Color.parseColor("#4CAF50"))
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Set X-axis labels
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in sortedDates.indices) {
                    sortedDates[value.toInt()]
                } else ""
            }
        }

        // Set data and refresh
        lineChart.data = LineData(expenseDataSet, incomeDataSet)
        lineChart.invalidate()
    }

    private fun updateActivityMessages() {
        // Calculate totals for insights
        val totalIncome = dailyIncome.values.sum()
        val totalExpenses = dailyExpenses.values.sum()
        val netSavings = totalIncome - totalExpenses

        // Update "Your Activity" message
        val activityMessage = when {
            totalIncome == 0.0 && totalExpenses == 0.0 -> "No financial activity recorded yet."
            netSavings > 0 -> "Great job! You're saving ${String.format(Locale.getDefault(), "%.2f", netSavings)}"
            netSavings < 0 -> "Watch out! You're overspending by ${String.format(Locale.getDefault(), "%.2f", -netSavings)}"
            else -> "Your spending matches your income exactly."
        }

        findViewById<TextView>(R.id.tvYourActivityMessage).text = activityMessage

        // Update "Recommended Spending" message
        val recommendationMessage = when {
            totalIncome == 0.0 -> "Start by recording your income to get recommendations"
            totalExpenses == 0.0 -> "Consider setting up a budget for your expenses"
            (totalExpenses / totalIncome) > 0.7 -> "Try to keep expenses below 70% of your income"
            (totalExpenses / totalIncome) > 0.5 -> "Good balance! Consider saving more"
            else -> "Excellent financial control! Keep it up"
        }

        findViewById<TextView>(R.id.tvRecommendedSpendingMessage).text = recommendationMessage
    }
}