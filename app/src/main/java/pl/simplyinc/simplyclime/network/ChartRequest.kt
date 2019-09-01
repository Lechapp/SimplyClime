package pl.simplyinc.simplyclime.network

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import org.json.JSONArray
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey
import pl.simplyinc.simplyclime.activities.server
import pl.simplyinc.simplyclime.elements.OnSwipeTouchListener
import pl.simplyinc.simplyclime.elements.WeatherTools
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class ChartRequest(val c:Context, private val mChart:CombinedChart, private val progress:ProgressBar,
                   private val swipe:ImageView) {

    fun getChartData(searchvalue:String, tempunit:String, lat:String = "", lon:String = ""){
        mChart.visibility = View.GONE
        progress.visibility = View.VISIBLE
        val gps = if(lat != "") "true" else "false"
        val url = "http://$server/api/weather/$searchvalue?chart=true&gps=$gps&lat=$lat&long=$lon"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)
                if (!response.getBoolean("error")) {
                    mChart.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                    val wdata = response.getJSONArray("weather")

                    val offset = (response.getDouble("countHistoryData") * 0.80f).toFloat()

                    setChart(wdata, tempunit, offset)
                }else{
                    progress.visibility = View.GONE
                    Toast.makeText(c, c.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }

            },
            Response.ErrorListener {
                progress.visibility = View.GONE
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }

    fun getChartDataOpenWeather(city:String, tempunit:String, lat:String = "", lon:String = "", onSuccess: (data:MutableList<JSONArray>) -> Unit){
        mChart.visibility = View.GONE
        progress.visibility = View.VISIBLE
        val searchval = if(lat == "") "q=$city" else "lat=$lat&lon=$lon"

        val url = "https://api.openweathermap.org/data/2.5/forecast?$searchval&appid=$openWeatherAPIKey"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)
            progress.visibility = View.GONE
            if (response.getString("cod") == "200") {
                val list = response.getJSONArray("list")
                val timezone = response.getJSONObject("city").getInt("timezone")
                val endloop = list.length()-1
                val weatherData = JSONArray()
                val allforecast = mutableListOf<JSONArray>()

                for(i in 0 until endloop){
                    val pack = list.getJSONObject(endloop-i-1)
                    val onePack = JSONArray()
                    val forecast = JSONArray()
                    val main = pack.getJSONObject("main")
                    val rain = try {
                        val helpval = pack.getJSONObject("rain").getDouble("3h")
                        ((helpval/12)*100).roundToInt()
                    }catch (e:Exception){
                        0
                    }
                    onePack.put(main.getString("temp"))

                    forecast.put(main.getString("temp"))
                    forecast.put("null")
                    forecast.put(main.getString("humidity"))
                    forecast.put("null")
                    forecast.put(main.getString("pressure"))
                    forecast.put(rain)
                    forecast.put(pack.getJSONObject("wind").getString("speed"))
                    forecast.put("null")
                    forecast.put("null")
                    forecast.put("null")
                    forecast.put("null")
                    forecast.put(pack.getString("dt"))
                    forecast.put(main.getString("temp_min"))
                    forecast.put(main.getString("temp_max"))
                    forecast.put(pack.getJSONArray("weather").getJSONObject(0).getString("main"))
                    forecast.put(pack.getJSONArray("weather").getJSONObject(0).getString("description"))


                    onePack.put(rain)
                    val date = (pack.getInt("dt") + timezone) * 1000L

                    val dateFormat = SimpleDateFormat("HH:mm u", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("GMT")

                    onePack.put(dateFormat.format(date))
                    weatherData.put(onePack)
                    allforecast.add(forecast)
                }

                onSuccess(allforecast)
                mChart.visibility = View.VISIBLE

                setChart(weatherData, tempunit, 1.1101f)
            }else{
                Toast.makeText(c, c.getString(R.string.error), Toast.LENGTH_SHORT).show()
            }

        },
            Response.ErrorListener {
                progress.visibility = View.GONE
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setChart(weatherData:JSONArray, tempunit:String,offset:Float) {
        val xAxis = mChart.xAxis
        val templist: ArrayList<Entry> = ArrayList()
        val rainlist: ArrayList<BarEntry> = ArrayList()
        val xValsDateLabel = ArrayList<String>()
        val tool = WeatherTools()
        //set Data - 0 najnowsze, max najstarsze
        //forecast - 30 paczek
        var prevHour = 0
        var countDataPack = 0

        for (i in 0 until weatherData.length()) {
            val fromEnd = weatherData.length() - i - 1

            val onePack = weatherData.getJSONArray(fromEnd)

            //vertical day line
            //fromend max 43
            if (i > (weatherData.length() - 36)) {
                val hour = ((onePack.getString(2).split(" ")[0]).split(":")[0]).toInt()
                countDataPack++

                if (prevHour > hour) {
                    val dayOfWeek = when (onePack.getString(2).split(" ")[1]) {
                        "1" -> c.getString(R.string.fmonday)
                        "2" -> c.getString(R.string.ftuesday)
                        "3" -> c.getString(R.string.fwednesday)
                        "4" -> c.getString(R.string.fthursday)
                        "5" -> c.getString(R.string.ffriday)
                        "6" -> c.getString(R.string.fsaturday)
                        "7" -> c.getString(R.string.fsunday)
                        else -> c.getString(R.string.nextday)
                    }

                    val limlinee = LimitLine(((countDataPack * 1f) + offset +1.3f), dayOfWeek)
                    limlinee.textColor = ContextCompat.getColor(c, R.color.white)
                    limlinee.enableDashedLine(6f, 6f, 2f)
                    xAxis.addLimitLine(limlinee)
                }
                prevHour = hour
            }
            val temp = tool.kelvintoTempUnit(onePack.getString(0), tempunit).toFloat()
            templist.add(Entry(i.toFloat(), temp))
            rainlist.add(BarEntry(i.toFloat(), onePack.getString(1).toFloat()))
            xValsDateLabel.add(onePack.getString(2).split(" ")[0])
        }

        val tempDataSet = LineDataSet(templist, "Temperature")
        tempDataSet.color = ContextCompat.getColor(c, R.color.sunrise)
        tempDataSet.lineWidth = 2f
        tempDataSet.valueTextSize = 10.5f
        tempDataSet.valueTextColor = ContextCompat.getColor(c, R.color.white)
        tempDataSet.setDrawCircles(false)
        tempDataSet.valueFormatter = MyDecimalValueFormatter()
        tempDataSet.axisDependency = YAxis.AxisDependency.LEFT

        val linearTemp = LineData(tempDataSet)

        val rainDataSet = BarDataSet(rainlist, "Rainfall")
        rainDataSet.color = ContextCompat.getColor(c, R.color.rainfall)
        rainDataSet.setDrawValues(false)
        rainDataSet.axisDependency = YAxis.AxisDependency.RIGHT
        val barRainfall = BarData(rainDataSet)


        val combinedData = CombinedData()
        combinedData.setData(barRainfall)
        combinedData.setData(linearTemp)

        mChart.data = combinedData
        mChart.axisLeft.setDrawLabels(false)
        mChart.axisRight.setDrawLabels(false)
        mChart.setTouchEnabled(false)
        val description = Description()
        description.text = ""
        mChart.description = description
        mChart.legend.isEnabled = false
        mChart.axisLeft.setDrawGridLines(false)
        mChart.axisRight.setDrawGridLines(false)
        mChart.axisLeft.setDrawAxisLine(false)
        mChart.axisRight.setDrawAxisLine(false)
        mChart.axisLeft.axisMinimum = mChart.axisLeft.axisMinimum - 3
        mChart.axisLeft.axisMaximum = mChart.axisLeft.axisMaximum + 6
        mChart.axisRight.axisMaximum = 105f
        mChart.axisRight.axisMinimum = 0f

        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.mAxisMaximum = combinedData.xMax + 0.25f
        xAxis.valueFormatter = MyValueFormatter(xValsDateLabel)
        xAxis.textColor = ContextCompat.getColor(c, R.color.white)
        mChart.animateX(400)
        mChart.animateY(400)
        mChart.setVisibleXRangeMaximum(13f)

        val startline = if(offset == 1.1101f) 0f else offset+0.1f //if true then only futurecast from openweather
        val limline = LimitLine(startline, c.getString(R.string.today))
            mChart.moveViewToX(startline)

        limline.textColor = ContextCompat.getColor(c, R.color.white)
        limline.enableDashedLine(6f,6f,2f)
        xAxis.addLimitLine(limline)

        val listeer = OnSwipeTouchListener(c, mChart, swipe, offset)
        mChart.setOnTouchListener(listeer)

    }

    class MyValueFormatter(private val xValsDateLabel: ArrayList<String>) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return value.toString()
        }

        override fun getAxisLabel(value: Float, axis: AxisBase): String {
            return if (value.toInt() >= 0 && value.toInt() <= xValsDateLabel.size - 1)
                xValsDateLabel[value.toInt()]
            else
                ("").toString()

        }
    }


    class MyDecimalValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return  value.roundToInt().toString()
        }
    }
}