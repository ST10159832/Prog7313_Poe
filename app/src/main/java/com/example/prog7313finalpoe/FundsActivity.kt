package com.example.prog7313finalpoe

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FundsActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var entryList: LinearLayout
    private lateinit var tvCurrentBudget: TextView

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    private val dailyNet = mutableMapOf<String, Double>()
    private val allDates = mutableSetOf<String>()
    private var totalIncome = 0.0
    private var totalExpenses = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_funds)

        lineChart = findViewById(R.id.lineChart) // Ensure this is LineChart in XML
        entryList = findViewById(R.id.entryList)
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget)

        setupChart()
        loadEntries()
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setNoDataText("No financial data available")
            setNoDataTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1E4D8B"))

            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
        }
    }

    private fun loadEntries() {
        dailyNet.clear()
        allDates.clear()
        totalIncome = 0.0
        totalExpenses = 0.0

        val combinedEntries = mutableListOf<Pair<String, Double>>() // For list display

        // Fetch income
        db.collection("income").get().addOnSuccessListener { incomeDocs ->
            for (doc in incomeDocs) {
                val amount = doc.getDouble("amount") ?: 0.0
                val timestamp = doc.getLong("timestamp") ?: continue
                val dateKey = dateFormat.format(Date(timestamp))

                dailyNet[dateKey] = dailyNet.getOrDefault(dateKey, 0.0) + amount
                totalIncome += amount
                allDates.add(dateKey)

                combinedEntries.add(Pair("Income on $dateKey", amount))
            }

            // Fetch expenses
            db.collection("expenses").get().addOnSuccessListener { expenseDocs ->
                for (doc in expenseDocs) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    val timestamp = doc.getLong("timestamp") ?: continue
                    val dateKey = dateFormat.format(Date(timestamp))

                    dailyNet[dateKey] = dailyNet.getOrDefault(dateKey, 0.0) - amount
                    totalExpenses += amount
                    allDates.add(dateKey)

                    combinedEntries.add(Pair("Expense on $dateKey", amount))
                }

                updateChart()
                updateList(combinedEntries)
                tvCurrentBudget.text = "R %.2f".format(totalIncome - totalExpenses)
            }
        }
    }

    private fun updateChart() {
        val sortedDates = allDates.sortedBy { dateFormat.parse(it) }

        val entries = sortedDates.mapIndexed { index, date ->
            Entry(index.toFloat(), dailyNet[date]?.toFloat() ?: 0f)
        }

        val lineDataSet = LineDataSet(entries, "Net Balance").apply {
            color = Color.RED
            setCircleColor(Color.RED)
            valueTextColor = Color.WHITE
            lineWidth = 5.5f
            circleRadius = 7f
            valueTextSize = 9f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return sortedDates.getOrNull(value.toInt()) ?: ""
            }
        }

        lineChart.data = LineData(lineDataSet)
        lineChart.invalidate()
    }

    private fun updateList(entries: List<Pair<String, Double>>) {
        entryList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for ((label, amount) in entries) {
            val itemView = inflater.inflate(android.R.layout.simple_list_item_2, entryList, false)
            val title = itemView.findViewById<TextView>(android.R.id.text1)
            val value = itemView.findViewById<TextView>(android.R.id.text2)

            title.text = label
            value.text = "R %.2f".format(amount)

            title.setTextColor(Color.WHITE)
            value.setTextColor(Color.LTGRAY)

            entryList.addView(itemView)
        }
    }
}