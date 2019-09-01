package pl.simplyinc.simplyclime.network

import android.appwidget.AppWidgetManager
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.RemoteViews
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.server
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.elements.WeatherData
import pl.simplyinc.simplyclime.elements.WeatherTools
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import android.text.Spanned
import android.text.SpannableStringBuilder
import android.text.style.SuperscriptSpan
import android.util.Log
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey


class WidgetWeatherRequest(val c:Context, private val appWidgetManager: AppWidgetManager, private val appWidgetId: Int,
                                private val s:JSONObject, private val widgetinfo:JSONObject, private var f:JSONObject) {

   private val tool = WeatherTools()
    var tempunit = "°C"
    var windunit = "km/h"

    fun getNewestWeatherWidget(weatherOld:String, lat:String = "", lon:String = "", forecast:JSONObject? = null) {
        if(forecast!= null)
            f = forecast

        tempunit = s.getString("tempunit")
        windunit = s.getString("windunit")
        val searchvalue = s.getString("searchvalue")
        val url = "http://$server/api/weather/$searchvalue?newest=true&gps=${s.getString("gps")}&lat=$lat&long=$lon"


        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)
            if (!response.getBoolean("error")) {
                val w = response.getJSONArray("weather").getJSONArray(0)
                val unixTime = (System.currentTimeMillis() / 1000L).toDouble().roundToInt()
                val tempimg = tool.getTempImgId(w.getString(0), widgetinfo.getBoolean("blackbg"))
                val humiout = tool.roundto(w.getString(2))
                val humiin = tool.roundto(w.getString(3))
                val insolation = tool.roundto(w.getString(9))
                val rainfall = tool.roundto(w.getString(5))

                val newW = WeatherData(w.getString(0),w.getString(1),humiout,humiin, tool.roundto(w.getString(4)),rainfall,
                    w.getString(6),w.getString(7),w.getString(8), insolation,w.getString(10),
                    w.getInt(11),unixTime, tempimg)

                val json = Json(JsonConfiguration.Stable)
                val newweather = json.stringify(WeatherData.serializer(), newW)

                saveWeather(weatherOld,newweather)

                setWidget(newweather, false)
            }else{
                setWidget(weatherOld, false)
            }

        },
            Response.ErrorListener {
                setWidget(weatherOld, true)
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }

    fun getNewestWeatherWidgetOpenWeather(weatherOld: String, lat:String = "", lon:String = "", forecast: JSONObject? = null) {
        if(forecast!= null)
            f = forecast

        val searchvalue = if(lat != "") {
            "lat=$lat&lon=$lon"
        }else {
            "id="+s.getString("searchvalue")
        }
        val url = "https://api.openweathermap.org/data/2.5/weather?$searchvalue&appid=$openWeatherAPIKey"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)
            if (response.getString("cod") == "200") {
                val w = response.getJSONObject("main")
                val wind = response.getJSONObject("wind")
                val unixTime = (System.currentTimeMillis() / 1000L).toDouble().roundToInt()

                val main = response.getJSONArray("weather").getJSONObject(0).getString("main")
                val description = response.getJSONArray("weather").getJSONObject(0).getString("description")
                val tempout = tool.roundto(w.getString("temp"))
                val tempimg = tool.getTempImgId(tempout, true)
                val humiout = w.getString("humidity")
                val pressure = tool.roundto(w.getString("pressure"))
                val time = response.getInt("dt") + response.getInt("timezone")
                val json = Json(JsonConfiguration.Stable)

                val newW = WeatherData(tempout,"null",humiout,"null", pressure,"null",
                    wind.getString("speed"),"null","null", "null","null",
                    time,unixTime, tempimg,description, main)


                val newweather = json.stringify(WeatherData.serializer(), newW)

                saveWeather(weatherOld, newweather)


                setWidget(newweather, false)
            }else{
                setWidget(weatherOld, false)
            }
        },
            Response.ErrorListener {
                setWidget(weatherOld, true)
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }
    private fun saveWeather(weatherOld: String, newweather:String){
        val session = SessionPref(c)
        val allnewweathers = session.getPref("weathers").replace(weatherOld,newweather)
        session.setPref("weathers", allnewweathers)
    }

    fun setWidget(weather:String, error:Boolean, newforecast:JSONObject = f):Boolean{

        val views = RemoteViews(c.packageName, R.layout.newest_weather)
        //s in argument is station data
        views.setTextViewText(R.id.title, s.getString("title"))
        val black = if(widgetinfo.getBoolean("blackbg")) "weathericon" else "weathericonblack"
        setForecast(views,newforecast,c, s.getString("tempunit"), black)

        if(error) {
            views.setViewVisibility(R.id.battery, View.GONE)
            views.setViewVisibility(R.id.batteryimg, View.GONE)
            views.setViewVisibility(R.id.datalayout, View.GONE)
            views.setViewVisibility(R.id.allhumidity,View.GONE)
            views.setViewVisibility(R.id.alltemp,View.GONE)
            views.setViewVisibility(R.id.pressure, View.GONE)
            views.setViewVisibility(R.id.rainfall, View.GONE)
            views.setViewVisibility(R.id.allpolition,View.GONE)
            views.setViewVisibility(R.id.weathericon,View.GONE)
            views.setViewVisibility(R.id.rainfallimg, View.GONE)
            views.setViewVisibility(R.id.pressureimg, View.GONE)
            views.setViewVisibility(R.id.windspeed, View.GONE)
            views.setViewVisibility(R.id.windspeedimg, View.GONE)
            views.setViewVisibility(R.id.insolation, View.GONE)
            views.setViewVisibility(R.id.insolationimg, View.GONE)
            views.setViewVisibility(R.id.updatelayout, View.GONE)
            views.setViewVisibility(R.id.errortext, View.VISIBLE)

            views.setInt(R.id.widget, "setBackgroundColor", widgetinfo.getInt("background"))
            if(widgetinfo.getBoolean("blackbg")) {
                val white = ContextCompat.getColor(c, R.color.white)
                views.setTextColor(R.id.title, white)
                views.setTextColor(R.id.errortext, white)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            return false

        }else views.setViewVisibility(R.id.errortext, View.GONE)


        val w = JSONObject(weather)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val lastdat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        lastdat.timeZone = TimeZone.getTimeZone("GMT")
        val update = Date(w.getLong("updatedtime")*1000L)
        val datafrom = Date(w.getLong("time")*1000L)
        val text = c.getString(R.string.lastupdate) + " " + lastdat.format(datafrom) + ". " + c.getString(R.string.updated) +" "+ sdf.format(update)
        views.setTextViewText(R.id.updatedat, text)

        //temp
        var out = ""
        if((w.getString("tempout") != "null" && widgetinfo.getBoolean("tempout"))
            || (w.getString("tempin") != "null" && widgetinfo.getBoolean("tempin"))){

            if(w.getString("tempin") != "null" && widgetinfo.getBoolean("tempin")) {
                val temp = tool.kelvintoTempUnit(w.getString("tempin"), tempunit)
                views.setTextViewText(R.id.tempin,
                    c.getString(R.string.`in`) + " " + temp + s.getString("tempunit"))
                out = c.getString(R.string.out)
            }else views.setViewVisibility(R.id.tempin, View.GONE)

            if(w.getString("tempout") != "null" && widgetinfo.getBoolean("tempout")){
                val temp = tool.kelvintoTempUnit(w.getString("tempout"), tempunit)
                views.setTextViewText(R.id.tempout,
                    out+" "+ temp +s.getString("tempunit"))

                views.setInt(R.id.tempimage, "setBackgroundResource", w.getInt("tempimg"))
            }else views.setViewVisibility(R.id.tempout, View.GONE)

        }else{
            views.setViewVisibility(R.id.alltemp,View.GONE)
        }

        //humidity

        if((w.getString("humidityout") != "null" && widgetinfo.getBoolean("humidityout"))
            || (w.getString("humidityin") != "null" && widgetinfo.getBoolean("humidityin"))){
            out = ""
            if(w.getString("humidityin") != "null" && widgetinfo.getBoolean("humidityin")) {
                views.setTextViewText(R.id.humidityin,
                    c.getString(R.string.`in`) + " " + w.getString("humidityin") + "%")
                out = c.getString(R.string.out)
            }else views.setViewVisibility(R.id.humidityin, View.GONE)

            if(w.getString("humidityout") != "null" && widgetinfo.getBoolean("humidityout"))
                views.setTextViewText(R.id.humidityout,
                    out+" "+w.getString("humidityout")+"%")
            else views.setViewVisibility(R.id.humidityout, View.GONE)

        }else{
            views.setViewVisibility(R.id.allhumidity,View.GONE)
        }

        //battery
        if(w.getString("batterylvl") != "null"){

            val batlvl = w.getString("batterylvl")
            views.setTextViewText(R.id.battery, "$batlvl%")
            val batteryimgid = tool.batterylvl(batlvl, widgetinfo.getBoolean("blackbg"))

            views.setInt(R.id.batteryimg, "setBackgroundResource", batteryimgid)

        }else{
            views.setViewVisibility(R.id.battery,View.GONE)
            views.setViewVisibility(R.id.batteryimg, View.GONE)
        }

        //pressure
        if(w.getString("pressure") != "null" && widgetinfo.getBoolean("pressure"))
            views.setTextViewText(R.id.pressure, w.getString("pressure") + "HPa")
        else {
            views.setViewVisibility(R.id.pressure, View.GONE)
            views.setViewVisibility(R.id.pressureimg, View.GONE)
        }
        //insolation
        if(w.getString("insolation") != "null" && widgetinfo.getBoolean("insolation"))
            views.setTextViewText(R.id.insolation, w.getString("insolation") + "%")
        else {
            views.setViewVisibility(R.id.insolation, View.GONE)
            views.setViewVisibility(R.id.insolationimg, View.GONE)
        }

        //windspeed
        if(w.getString("windspeed") != "null" && widgetinfo.getBoolean("windspeed")) {
            val wind = tool.mstoWindUnit(w.getString("windspeed"), windunit)
            views.setTextViewText(
                R.id.windspeed,
                wind + s.getString("windunit")
            )
        }else {
            views.setViewVisibility(R.id.windspeed, View.GONE)
            views.setViewVisibility(R.id.windspeedimg, View.GONE)
        }

        //rainfall
        if(w.getString("rainfall") != "null" && widgetinfo.getBoolean("rainfall")){
            views.setTextViewText(R.id.rainfall, w.getString("rainfall") + "%")
        }else{
            views.setViewVisibility(R.id.rainfall, View.GONE)
            views.setViewVisibility(R.id.rainfallimg, View.GONE)
        }

        //airpollution

        var indexgorny:String


        if((w.getString("airpollution10") != "null" && widgetinfo.getBoolean("airpollution10"))
            || (w.getString("airpollution25") != "null" && widgetinfo.getBoolean("airpollution25"))){

            if(w.getString("airpollution10") != "null" && widgetinfo.getBoolean("airpollution10")) {

                indexgorny = "pm10  " + w.getString("airpollution10") +" "+ c.getString(R.string.pollutionuit)
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(indexgorny)
                builder.setSpan(
                    superscriptSpan,
                    indexgorny.length-1,
                    indexgorny.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                views.setTextViewText(R.id.airpol10, builder)

                val colorid = tool.pm10(w.getString("airpollution10"))
                views.setInt(R.id.airpol10, "setBackgroundResource",colorid)
            } else views.setViewVisibility(R.id.airpol10, View.GONE)

            if(w.getString("airpollution25") != "null" && widgetinfo.getBoolean("airpollution25")) {

                indexgorny = "PM2.5  " + w.getString("airpollution25") +" "+ c.getString(R.string.pollutionuit)
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(indexgorny)
                builder.setSpan(
                    superscriptSpan,
                    indexgorny.length-1,
                    indexgorny.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                views.setTextViewText(R.id.airpol25, builder)

                val colorid = tool.pm25(w.getString("airpollution25"))
                views.setInt(R.id.airpol25, "setBackgroundResource",colorid)
            }
            else views.setViewVisibility(R.id.airpol25, View.GONE)
        }else{
            views.setViewVisibility(R.id.allpolition,View.GONE)
        }

        //weatherimg
        if(widgetinfo.getBoolean("icon")) {
            val weatherimg = tool.weathericon(
                w.getString("tempout"),
                w.getString("rainfall"),
                w.getString("insolation"),
                s.getInt("sunrise"),
                s.getInt("sunset"),
                s.getInt("timezone"),
                widgetinfo.getBoolean("blackbg")
            )
            views.setInt(R.id.weathericon, "setBackgroundResource", weatherimg)
        }else views.setViewVisibility(R.id.weathericon, View.GONE)

        //setTheme
        views.setInt(R.id.widget, "setBackgroundColor", widgetinfo.getInt("background"))
        if(widgetinfo.getBoolean("blackbg")) {
            views.setInt(R.id.tempimage, "setBackgroundResource", R.drawable.temp2_w)
            views.setInt(R.id.humidityimg, "setBackgroundResource", R.drawable.humidity_w)
            views.setInt(R.id.insolationimg, "setBackgroundResource", R.drawable.sun_w)
            views.setInt(R.id.rainfallimg, "setBackgroundResource", R.drawable.umbrella_w)
            views.setInt(R.id.pressureimg, "setBackgroundResource", R.drawable.pressure_w)
            views.setInt(R.id.windspeedimg, "setBackgroundResource", R.drawable.wind_w)

            val white = ContextCompat.getColor(c, R.color.white)
            views.setTextColor(R.id.title, white)
            views.setTextColor(R.id.battery, ContextCompat.getColor(c, R.color.whitesmoke))
            views.setTextColor(R.id.tempin, white)
            views.setTextColor(R.id.tempout, white)
            views.setTextColor(R.id.humidityin,white)
            views.setTextColor(R.id.humidityout,white)
            views.setTextColor(R.id.insolation,white)
            views.setTextColor(R.id.rainfall,white)
            views.setTextColor(R.id.pressure,white)
            views.setTextColor(R.id.windspeed,white)
            views.setTextColor(R.id.airpollution,white)
            views.setTextColor(R.id.widgetday1,white)
            views.setTextColor(R.id.widgetday2,white)
            views.setTextColor(R.id.widgetday3,white)
            views.setTextColor(R.id.widgetday4,white)
            views.setTextColor(R.id.widgetday5,white)
            views.setTextColor(R.id.forecasttemp1,white)
            views.setTextColor(R.id.forecasttemp2,white)
            views.setTextColor(R.id.forecasttemp3,white)
            views.setTextColor(R.id.forecasttemp4,white)
            views.setTextColor(R.id.forecasttemp5,white)
            views.setTextColor(R.id.updatedat,ContextCompat.getColor(c, R.color.whitesmoke))
        }else views.setInt(R.id.tempimage, "setBackgroundResource", R.drawable.temp2_b)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        return true

    }


    private fun setForecast(v:RemoteViews, f:JSONObject, c:Context, tempunit:String, blacktheme:String){

        val arrayimg = listOf(
            R.id.forecastimg1,
            R.id.forecastimg2,
            R.id.forecastimg3,
            R.id.forecastimg4,
            R.id.forecastimg5)

        val arraytemp = listOf(
            R.id.forecasttemp1,
            R.id.forecasttemp2,
            R.id.forecasttemp3,
            R.id.forecasttemp4,
            R.id.forecasttemp5)

        val arraydate = listOf(
            R.id.widgetday1,
            R.id.widgetday2,
            R.id.widgetday3,
            R.id.widgetday4,
            R.id.widgetday5)


        val time = f.getInt("time")
        val weatherdate = SimpleDateFormat("u|dd", Locale(Locale.getDefault().displayLanguage))
        val day = weatherdate.format(Date(time*1000L)).split("|")
        var dayofWeek = (day[0]).toInt()
        var dayofMonth = (day[1]).toInt()
        val lastday = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)


        for(i in 0 until f.length()-1){
            val dayone: JSONObject
            try {
                dayone = JSONObject(f.getString("day$i"))
            }catch (e:Exception){
                v.setViewVisibility(R.id.widgetforecast, View.GONE)
                break
            }
            val dayofWeekText:String = when(dayofWeek){
                1 -> c.getString(R.string.monday)
                2 -> c.getString(R.string.tuesday)
                3 -> c.getString(R.string.wednesday)
                4 -> c.getString(R.string.thursday)
                5 -> c.getString(R.string.friday)
                6 -> c.getString(R.string.saturday)
                7 -> c.getString(R.string.sunday)
                else -> ""
            }

            v.setInt(arrayimg[i], "setBackgroundResource",dayone.getInt(blacktheme))
            v.setTextViewText(arraytemp[i],tool.kelvintoTempUnit(dayone.getString("tempmax"),tempunit)
                    +"/"
                    +tool.kelvintoTempUnit(dayone.getString("tempmin"), tempunit) + tempunit)
            v.setTextViewText(arraydate[i],"$dayofWeekText $dayofMonth")

            dayofWeek++
            dayofMonth++

            if(dayofMonth == (lastday+1))
                dayofMonth = 1

            if(dayofWeek == 8)
                dayofWeek = 1
        }
    }

}