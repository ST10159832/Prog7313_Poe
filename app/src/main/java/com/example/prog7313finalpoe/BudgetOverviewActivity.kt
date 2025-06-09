package com.example.prog7313finalpoe

import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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

// Handle activity lifecycle for proper resource management
// Source: Android Developers (2024)

class BudgetOverviewActivity : AppCompatActivity() {
    private lateinit var currentBudgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var incomeText: TextView
    private lateinit var spentProgressBar: ProgressBar
    private lateinit var incomeProgressBar: ProgressBar
    private lateinit var minGoalText: TextView
    private lateinit var maxGoalText: TextView
    private lateinit var spentMinText: TextView
    private lateinit var spentMaxText: TextView
    private lateinit var incomeMinText: TextView
    private lateinit var incomeMaxText: TextView
    private lateinit var lineChart: LineChart

    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    private var totalSpent = 0.0
    private var totalIncome = 0.0
    private var minGoal = 0.0
    private var maxGoal = 0.0

    private val dailyExpenses = mutableMapOf<String, Double>()
    private val dailyIncome = mutableMapOf<String, Double>()
    private val allDates = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_overview)

        initViews()
        setupChart()
        loadGoals()
        loadTransactions()
    }

    private fun initViews() {
        currentBudgetText = findViewById(R.id.currentBudgetText)
        spentText = findViewById(R.id.spentTextView)
        incomeText = findViewById(R.id.incomeTextView)
        spentProgressBar = findViewById(R.id.spentProgressBar)
        incomeProgressBar = findViewById(R.id.incomeProgressBar)
        minGoalText = findViewById(R.id.minGoalTextView)
        maxGoalText = findViewById(R.id.maxGoalTextView)
        spentMinText = findViewById(R.id.spentMinText)
        spentMaxText = findViewById(R.id.spentMaxText)
        incomeMinText = findViewById(R.id.incomeMinText)
        incomeMaxText = findViewById(R.id.incomeMaxText)
        lineChart = findViewById(R.id.lineChart)
    }
//Admin Grabs Media. 2024. Android Display Bar Graph using Kotlin - Android Studio 2023 - Part 1 [Source Code]. Available at: < https://www.youtube.com/watch?v=-TGUV_LbcmE&list=PLlvGFqCblJgQ23xB6_aMvrQToXmDos2uM > [ Accessed on 03 Jube 2025]
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

    private fun loadGoals() {
        firestore.collection("monthly_goals")
            .orderBy("timestamp")
            .limitToLast(1)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    minGoal = doc.getDouble("minAmount") ?: 0.0
                    maxGoal = doc.getDouble("maxAmount") ?: 0.0
                }

                minGoalText.text = "Min Goal: R %.2f".format(minGoal)
                maxGoalText.text = "Max Goal: R %.2f".format(maxGoal)

                spentMinText.text = "Min: R %.2f".format(minGoal)
                spentMaxText.text = "Max: R %.2f".format(maxGoal)
                incomeMinText.text = "Min: R %.2f".format(minGoal)
                incomeMaxText.text = "Max: R %.2f".format(maxGoal)

                updateProgressBars()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load goals", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTransactions() {
        totalSpent = 0.0
        totalIncome = 0.0
        dailyExpenses.clear()
        dailyIncome.clear()
        allDates.clear()

        firestore.collection("expenses")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    val category = doc.getString("category") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val dateKey = dateFormat.format(Date(timestamp))

                    if (category.lowercase().contains("income")) {
                        totalIncome += amount
                        dailyIncome[dateKey] = dailyIncome.getOrDefault(dateKey, 0.0) + amount
                    } else {
                        totalSpent += amount
                        dailyExpenses[dateKey] = dailyExpenses.getOrDefault(dateKey, 0.0) + amount
                    }
                    allDates.add(dateKey)
                }

                spentText.text = "SPENT · R %.2f".format(totalSpent)
                incomeText.text = "INCOME · R %.2f".format(totalIncome)
                currentBudgetText.text = "R %.2f".format(totalIncome - totalSpent)

                updateProgressBars()
                updateChart()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgressBars() {
        val safeMaxGoal = if (maxGoal == 0.0) 1.0 else maxGoal

        spentProgressBar.max = 100
        spentProgressBar.progress = ((totalSpent / safeMaxGoal) * 100).toInt().coerceIn(0, 100)

        incomeProgressBar.max = 100
        incomeProgressBar.progress = ((totalIncome / safeMaxGoal) * 100).toInt().coerceIn(0, 100)
    }

    private fun updateChart() {
        val sortedDates = allDates.sortedBy { dateFormat.parse(it) }

        val expenseEntries = ArrayList<Entry>()
        val incomeEntries = ArrayList<Entry>()

        sortedDates.forEachIndexed { index, date ->
            expenseEntries.add(Entry(index.toFloat(), dailyExpenses[date]?.toFloat() ?: 0f))
            incomeEntries.add(Entry(index.toFloat(), dailyIncome[date]?.toFloat() ?: 0f))
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Expenses").apply {
            color = Color.parseColor("#FF6B6B")
            valueTextColor = Color.WHITE
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(Color.parseColor("#FF6B6B"))
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = Color.parseColor("#4CAF50")
            valueTextColor = Color.WHITE
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(Color.parseColor("#4CAF50"))
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in sortedDates.indices) sortedDates[value.toInt()] else ""
            }
        }

        lineChart.data = LineData(expenseDataSet, incomeDataSet)
        lineChart.invalidate()
    }
}
