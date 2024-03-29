package pl.simplyinc.simplyclime.network

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.core.content.ContextCompat
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import android.text.Spanned
import android.text.SpannableStringBuilder
import android.text.style.SuperscriptSpan
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey
import pl.simplyinc.simplyclime.elements.*


class WidgetWeatherRequest(val c:Context, private val appWidgetManager: AppWidgetManager, private val appWidgetId: Int,
                                private var s:JSONObject, private val widgetinfo:JSONObject, private var f:JSONObject,
                           private val pos:Int) {

   private val tool = WeatherTools()
   val session = SessionPref(c)


    fun getNewestWeatherWidget(weatherOld:String, lat:String = "", lon:String = "", forecast:JSONObject? = null) {
        if(forecast!= null)
            f = forecast


        val searchvalue = s.getString("searchvalue")
        val url = "https://$server/weather/$searchvalue?newest=true&gps=${s.getString("gps")}&lat=$lat&long=$lon"


        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

            val response = JSONObject(res)
            if (!response.getBoolean("error") && !response.getJSONArray("weather").isNull(0)) {
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

                saveWeather(newweather)
                    saveNewStation(
                        response.getString("city"),
                        response.getInt("sunset"),
                        response.getInt("sunrise"),
                        response.getString("country"),
                        response.getInt("timezone"),
                        lat,
                        lon
                    )
                    setWidget(newweather, false)

            }else{
                setWidget(weatherOld, true)
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
                val tempimg = tool.getTempImgId(tempout, widgetinfo.getBoolean("blackbg"))
                val humiout = w.getString("humidity")
                val pressure = tool.roundto(w.getString("pressure"))
                val time = response.getInt("dt") + response.getInt("timezone")
                val json = Json(JsonConfiguration.Stable)

                val newW = WeatherData(tempout,"null",humiout,"null", pressure,"null",
                    wind.getString("speed"),"null","null", "null","null",
                    time,unixTime, tempimg,description, main)


                val newweather = json.stringify(WeatherData.serializer(), newW)

                saveWeather(newweather)
                    val sys = response.getJSONObject("sys")

                    saveNewStation(
                        response.getString("name"),
                        sys.getInt("sunset"),
                        sys.getInt("sunrise"),
                        sys.getString("country"),
                        response.getInt("timezone"),
                        lat,
                        lon
                    )

                    setWidget(newweather, false)

            }else{
                setWidget(weatherOld, true)
            }
        },
            Response.ErrorListener {
                setWidget(weatherOld, true)
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }

    private fun saveNewStation(city:String, sunset:Int, sunrise:Int, country:String, timezone:Int, lati:String, longi:String){

        if(s.getBoolean("gps") && s.getString("city") != city) {
            val searchvalue = "$country/$city"

            val newstat = StationsData(
                "city",
                city,
                timezone,
                searchvalue,
                city,
                "",
                true,
                s.getBoolean("privstation"),
                s.getString("tempunit"),
                s.getString("windunit"),
                s.getInt("refreshtime"),
                sunset,
                sunrise,
                lati.toDouble(),
                longi.toDouble()
            )

            val json = Json(JsonConfiguration.Stable)

            val newstation = json.stringify(StationsData.serializer(), newstat)
            s = JSONObject(newstation)

            val allstations = session.getPref("stations").split("|").toMutableList()
            allstations[pos] = newstation

            val allreadystation = allstations.joinToString("|")
            session.setPref("stations", allreadystation)
        }
    }

    private fun saveWeather(newweather:String){

        val allweathers = session.getPref("weathers").split("|").toMutableList()
        allweathers[pos] = newweather
        val all = allweathers.joinToString("|")
        session.setPref("weathers", all)
    }

    fun setWidget(weather:String, error:Boolean, newforecast:JSONObject = f):Boolean{

        val views = RemoteViews(c.packageName, R.layout.newest_weather)
        //s in argument is station data
        views.setTextViewText(R.id.title, s.getString("title"))
        val black = if(widgetinfo.getBoolean("blackbg")) "weathericon" else "weathericonblack"
        setForecast(views,newforecast, s.getString("tempunit"), black)
        val w = JSONObject(weather)
        val tempunit = s.getString("tempunit")
        val windunit = s.getString("windunit")

        if(error && w.getInt("updatedtime") == 0) {
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


        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val lastdat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        lastdat.timeZone = TimeZone.getTimeZone("GMT")
        val unixTime = System.currentTimeMillis()
        val update = Date(unixTime)
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
        if(w.getString("insolation") != "null" && widgetinfo.getBoolean("insolation")) {

            val insol = if (s.has("ecowitt") && s.getBoolean("ecowitt")) {
                val pom = w.getString("insolation") + " W/m2"
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(pom)
                builder.setSpan(
                    superscriptSpan,
                    pom.length - 1,
                    pom.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                builder
            } else {
                w.getString("insolation") + "%"
            }
            views.setTextViewText(R.id.insolation, insol)
        }else{
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
            val unit = if(s.has("ecowitt") && s.getBoolean("ecowitt")){
                " mm"
            }else{
                "%"
            }
            val raintext = w.getString("rainfall") + unit
            views.setTextViewText(R.id.rainfall, raintext)
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
                s.getBoolean("ecowitt"),
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


    private fun setForecast(v:RemoteViews, f:JSONObject, tempunit:String, blacktheme:String){

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


        var time = f.getInt("time")
        val weatherdate = SimpleDateFormat("dd", Locale(Locale.getDefault().displayLanguage))
        val weatherDay = SimpleDateFormat("EEEE", Locale(Locale.getDefault().displayLanguage))


        for(i in 0 until f.length()-1){
            val dayone: JSONObject
            try {
                dayone = JSONObject(f.getString("day$i"))
            }catch (e:Exception){
                v.setViewVisibility(R.id.widgetforecast, View.GONE)
                break
            }
            val dayofWeekText = weatherDay.format( Date(time*1000L)).substring(0,3)
            val dayofMonth = weatherdate.format( Date(time*1000L)).toInt()
            time += (24*3600)


            v.setInt(arrayimg[i], "setBackgroundResource",dayone.getInt(blacktheme))
            v.setTextViewText(arraytemp[i],tool.roundto(tool.kelvintoTempUnit(dayone.getString("tempmax"),tempunit))
                    +"/"
                    +tool.roundto(tool.kelvintoTempUnit(dayone.getString("tempmin"), tempunit)) + tempunit)
            v.setTextViewText(arraydate[i],"$dayofWeekText $dayofMonth")

        }
    }

}