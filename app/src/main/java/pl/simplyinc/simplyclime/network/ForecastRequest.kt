package pl.simplyinc.simplyclime.network

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey
import pl.simplyinc.simplyclime.adapters.ForecastAdapter
import pl.simplyinc.simplyclime.elements.ForecastData
import pl.simplyinc.simplyclime.elements.OneDayForecast
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class ForecastRequest(val context: Context, private val pos:Int, val tempunit:String = "", val widget:Boolean = false) {

    val session = SessionPref(context)

    fun getNewestForecast(city:String, forecastRecycler: RecyclerView? = null, onSuccess : (forecast:JSONObject) -> Unit) {
        val alloldforecasts = session.getPref("forecasts").split("|")[pos]
        val oldforecast = JSONObject(alloldforecasts)

        val url = "https://api.openweathermap.org/data/2.5/forecast?$city&appid=$openWeatherAPIKey"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)
            if (response.getString("cod") == "200") {

                val timezone = response.getJSONObject("city").getInt("timezone")

                val weathers = response.getJSONArray("list")

                var firsttime = 0
                val nextdays = arrayListOf<String>()
                val weatherdate = SimpleDateFormat("dd", Locale(Locale.getDefault().displayLanguage))
                var oneday = "0"
                var tempmin = 999
                var tempmax = 0
                var iconid:List<Int>
                var minweather:Int
                var maxweather:Int
                var icon:String
                var time:Int
                var day:String
                var licznik = 0
                var licznikfirstday = 0
                var arrayiconOpenWeather = arrayListOf(0,0,0,0,0,0,0,0,0)
                val json = Json(JsonConfiguration.Stable)

                for(i in 0 until weathers.length()){
                    licznik++
                    try {
                        val obj = weathers.getJSONObject(i)
                        time = obj.getInt("dt")
                        day = weatherdate.format(Date(time * 1000L))
                        if(oneday == "0")
                            oneday = day

                        val weatherdata = obj.getJSONObject("main")
                        icon = obj.getJSONArray("weather").getJSONObject(0).getString("icon")
                        minweather = weatherdata.getDouble("temp_min").roundToInt()
                        maxweather = weatherdata.getDouble("temp_max").roundToInt()
                    }catch (e:Exception){
                        continue
                    }

                    if(tempmin > minweather)
                        tempmin = minweather

                    if(tempmax < maxweather)
                        tempmax = maxweather

                    when("${icon[0]}${icon[1]}"){
                        "01"-> arrayiconOpenWeather[0]++
                        "02"-> arrayiconOpenWeather[1]++
                        "03"-> arrayiconOpenWeather[3]++
                        "04"-> arrayiconOpenWeather[4]++
                        "09"-> arrayiconOpenWeather[7]++
                        "10"-> arrayiconOpenWeather[6]++
                        "13"-> arrayiconOpenWeather[8]++
                        else -> arrayiconOpenWeather[3]++
                    }

                    if(firsttime == 0)
                        firsttime = time

                    if(oneday != day) {
                        oneday = day
                            arrayiconOpenWeather[5] = arrayiconOpenWeather[1] + arrayiconOpenWeather[3] + arrayiconOpenWeather[4]
                            arrayiconOpenWeather[2] = arrayiconOpenWeather[0] + arrayiconOpenWeather[1]

                            iconid = getIcon(arrayiconOpenWeather)
                            arrayiconOpenWeather = arrayListOf(0,0,0,0,0,0,0,0,0)

                            val timeforecast = (System.currentTimeMillis()/1000).toInt()
                            val dayObject = OneDayForecast(tempmin, tempmax, iconid[0], iconid[1], timeforecast)

                            val addday = json.stringify(OneDayForecast.serializer(), dayObject)
                            tempmin = 999
                            tempmax = 0

                            nextdays.add(addday)


                            if(licznikfirstday == 0)
                                licznikfirstday = licznik

                            licznik = 0

                    }

                }
                val checkTime = (System.currentTimeMillis()/1000).toInt()

                arrayiconOpenWeather[5] = arrayiconOpenWeather[1] + arrayiconOpenWeather[3] + arrayiconOpenWeather[4]
                arrayiconOpenWeather[2] = arrayiconOpenWeather[0] + arrayiconOpenWeather[1]
                iconid = getIcon(arrayiconOpenWeather)
                //the last one
                val dayObject = OneDayForecast(tempmin, tempmax, iconid[0], iconid[1])
                val addday = json.stringify(OneDayForecast.serializer(), dayObject)
                nextdays.add(addday)

                if(licznik < 6) {
                    nextdays.dropLast(1)
                }
                if(licznikfirstday < 5 && oldforecast.getString("day0").isNotEmpty()) {
                    val day0 = JSONObject(oldforecast.getString("day0"))
                    val timeDay0 = day0.getInt("time")
                    if (timeDay0 != 0 && checkTime - timeDay0 < (10 * 3600)) {
                        nextdays[0] = oldforecast.getString("day0")
                    }
                }


                for(i in nextdays.size .. 5)
                    nextdays.add("")

                oldforecast.getString("day0")

                val forecast = ForecastData(firsttime.plus(timezone), nextdays[0], nextdays[1], nextdays[2], nextdays[3], nextdays[4])
                val newforecast = json.stringify(ForecastData.serializer(), forecast)

                saveNewForecast(newforecast, forecastRecycler)

                onSuccess(JSONObject(newforecast))
            }

        },
            Response.ErrorListener {
                Toast.makeText(context, context.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    private fun saveNewForecast(newforecast:String, forecastRecycler: androidx.recyclerview.widget.RecyclerView?){
        val active = session.getPref("forecasts").split("|").toMutableList()
        active[pos] = newforecast
        val allnew = active.joinToString("|") + "|"
        session.setPref("forecasts", allnew)

        if(!widget)
            forecastRecycler?.adapter = ForecastAdapter(context, JSONObject(newforecast), tempunit)
    }

    private fun getIcon(arrayIcon:ArrayList<Int>):List<Int>{
        //0 - sun, 1 - little_cloud_sun, 2 - cloud_sun, 3 - cloud, 4 - cloud_rain, 5 - cloud_little_rain, 6 - cloud_snow

        //5 suma wszystkich chmur
        //2 suma slonca i slonca z chmurką małą

        val maxValue = arrayIcon.max()
        val allmaxs = ArrayList<Int>()
        while (arrayIcon.indexOf(maxValue) != -1 && arrayIcon[arrayIcon.indexOf(maxValue)] != 0){
            allmaxs.add(arrayIcon.indexOf(maxValue))
            arrayIcon[arrayIcon.indexOf(maxValue)] = 0
        }

        //for sure
        if(allmaxs.size < 2)
            allmaxs.add(0)

        val icon = if(allmaxs.size > 1) ((allmaxs[0] + allmaxs[1] - 0.01)/2.0).roundToInt() else allmaxs[0]

        return when(icon){
            0 -> listOf(R.drawable.sun_w, R.drawable.sun_b)
            1,2 -> listOf(R.drawable.little_cloud_sun_w, R.drawable.little_cloud_sun_b)
            3 -> listOf(R.drawable.cloud_sun_w, R.drawable.cloud_sun_b)
            4,5 -> listOf(R.drawable.cloud_w, R.drawable.cloud_b)
            6 -> listOf(R.drawable.cloud_little_rain_w, R.drawable.cloud_little_rain_b)
            7 -> listOf(R.drawable.cloud_rain_w, R.drawable.cloud_rain_b)
            8 -> listOf(R.drawable.cloud_snow_w, R.drawable.cloud_snow_b)
            else -> listOf(R.drawable.cloud_w, R.drawable.cloud_b)
        }

    }
}