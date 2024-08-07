package com.umer.beehiveclient.activities

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.umer.beehiveclient.R
import com.umer.beehiveclient.databinding.ActivityHiveDetailsBinding
import com.umer.beehiveclient.models.DataPoint
import com.umer.beehiveclient.models.DataWrapper
import java.text.SimpleDateFormat
import java.util.*

class HiveDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHiveDetailsBinding
    private val dataWrapper = getData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHiveDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.backButton.setOnClickListener {
            finish()
        }

        // Set default chart data to "day"
        setupCharts("day")

        // Set listeners for tab items to switch data range
        binding.day.setOnClickListener { setupCharts("day") }
        binding.week.setOnClickListener { setupCharts("week") }
        binding.month.setOnClickListener { setupCharts("month") }
    }

    private fun setupCharts(range: String) {
        val currentTime = System.currentTimeMillis()

        // Filter data based on the selected range
        val filteredData = when (range) {
            "day" -> dataWrapper.data.filter { isSameDay(it.timestamp, currentTime) }
            "week" -> dataWrapper.data.filter { isWithinWeek(it.timestamp, currentTime) }
            "month" -> dataWrapper.data.filter { isWithinMonth(it.timestamp, currentTime) }
            else -> dataWrapper.data
        }

        // Handle no data scenario
        if (filteredData.isEmpty()) {
            binding.noDataLayoutTemperature.visibility = VISIBLE
            binding.noDataLayoutHumidity.visibility = VISIBLE
            binding.temperatureChart.clear()
            binding.humidityChart.clear()
            binding.temperatureChart.invalidate()
            binding.humidityChart.invalidate()
            binding.temperatureChart.visibility = GONE
            binding.humidityChart.visibility = GONE
            return
        } else {
            binding.noDataLayoutTemperature.visibility = GONE
            binding.noDataLayoutHumidity.visibility = GONE
            binding.temperatureChart.visibility = VISIBLE
            binding.humidityChart.visibility = VISIBLE
        }

        // Prepare data for temperature chart
        val temperatureEntries = filteredData.mapIndexed { index, dataPoint ->
            val timestampMillis = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                Locale.getDefault()
            ).parse(dataPoint.timestamp)?.time?.toFloat() ?: 0f
            Entry(timestampMillis, dataPoint.temperature.toFloat())
        }

        val temperatureDataSet = LineDataSet(temperatureEntries, "Temperature")
        temperatureDataSet.color = getColor(R.color.primaryColor)
        temperatureDataSet.valueTextColor = getColor(R.color.primaryDarkColor)
        temperatureDataSet.valueTextSize = 8f
        temperatureDataSet.valueFormatter = object : ValueFormatter() {
            override fun getPointLabel(entry: Entry?): String {
                return "${entry?.y?.toInt()}°C"
            }
        }

        val temperatureLineData = LineData(temperatureDataSet)
        binding.temperatureChart.data = temperatureLineData

        // Set up X-axis to display time values
        binding.temperatureChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (range) {
                    "day" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(value.toLong())
                    "week" -> SimpleDateFormat("EEE", Locale.getDefault()).format(value.toLong())
                    "month" -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(value.toLong())
                    else -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(value.toLong())
                }
            }
        }
        binding.temperatureChart.xAxis.labelRotationAngle = 45f
        binding.temperatureChart.xAxis.position =
            com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM

        binding.temperatureChart.invalidate() // Refresh the chart

        // Prepare data for humidity chart
        val humidityEntries = filteredData.mapIndexed { index, dataPoint ->
            val timestampMillis = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                Locale.getDefault()
            ).parse(dataPoint.timestamp)?.time?.toFloat() ?: 0f
            Entry(timestampMillis, dataPoint.humidity.toFloat())
        }

        val humidityDataSet = LineDataSet(humidityEntries, "Humidity")
        humidityDataSet.color = getColor(R.color.primaryColor)
        humidityDataSet.valueTextColor = getColor(R.color.primaryDarkColor)
        humidityDataSet.valueTextSize = 8f

        val humidityLineData = LineData(humidityDataSet)
        binding.humidityChart.data = humidityLineData

        // Set up X-axis to display time values
        binding.humidityChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (range) {
                    "day" -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(value.toLong())
                    "week" -> SimpleDateFormat("EEE", Locale.getDefault()).format(value.toLong())
                    "month" -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(value.toLong())
                    else -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(value.toLong())
                }
            }
        }
        binding.humidityChart.xAxis.labelRotationAngle = 45f
        binding.humidityChart.xAxis.position =
            com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM

        binding.humidityChart.invalidate() // Refresh the chart
    }

    private fun isSameDay(timestamp: String, currentTime: Long): Boolean {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(timestamp)
        return dateFormatter.format(parsedDate) == dateFormatter.format(currentTime)
    }

    private fun isWithinWeek(timestamp: String, currentTime: Long): Boolean {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(timestamp)
        val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000
        return currentTime - parsedDate.time <= oneWeekInMillis
    }

    private fun isWithinMonth(timestamp: String, currentTime: Long): Boolean {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(timestamp)
        val oneMonthInMillis = 30 * 24 * 60 * 60 * 1000
        return currentTime - parsedDate.time <= oneMonthInMillis
    }

    private fun generateDummyData(): List<DataPoint> {
        val dataPoints = mutableListOf<DataPoint>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        // Initialize base values
        val baseTemperature = 20.0
        val baseHumidity = 60
        val temperatureVariation = 10.0
        val humidityVariation = 20

        // Generate data for today and the rest of the month
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in today..lastDayOfMonth) {
            for (hour in 0..23) {
                // Calculate the timestamp
                val timestamp = dateFormat.format(calendar.time)

                // Create smooth temperature variation
                val tempVariation = Math.sin(hour / 24.0 * Math.PI * 2) * temperatureVariation
                val temperature = baseTemperature + tempVariation

                // Create smooth humidity variation
                val humidityVariationFactor = Math.cos(hour / 24.0 * Math.PI * 2) * humidityVariation
                val humidity = (baseHumidity + humidityVariationFactor).toInt().coerceIn(40, 100)

                // Add the data point to the list
                dataPoints.add(DataPoint(timestamp, temperature, humidity))

                // Increment by one hour
                calendar.add(Calendar.HOUR_OF_DAY, 1)
            }
        }

        return dataPoints
    }

    private fun getData(): DataWrapper {
        val newData = generateDummyData()

        val dataWrapper = DataWrapper(newData)
        val gson = Gson()

        val jsonString = gson.toJson(dataWrapper)

        println(jsonString)
        val type = object : TypeToken<DataWrapper>() {}.type
        val parsedDataWrapper: DataWrapper = gson.fromJson(jsonString, type)

        return parsedDataWrapper
    }
}
