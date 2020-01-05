package pl.simplyinc.simplyclime.network

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.SuperscriptSpan
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey
import pl.simplyinc.simplyclime.activities.server
import pl.simplyinc.simplyclime.elements.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class NewestWeatherRequest(val c:Context,private val v:View, private val position:Int) {

    private val tool = WeatherTools()
    var station = JSONObject()
    val json = Json(JsonConfiguration.Stable)
    val session = SessionPref(c)
    private val weatherold = session.getPref("weathers").split("|")[position]

    fun getNewestWeather(stat:JSONObject, lat:String = "", lon:String = "", onSuccess: (searchval: String) -> Unit) {

        station = stat
        val searchvalue = station.getString("searchvalue")

        val url = "https://$server/weather/$searchvalue?newest=true&gps=${station.getString("gps")}&lat=$lat&long=$lon"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)

                if (!response.getBoolean("error")) {
                    val w = response.getJSONArray("weather").getJSONArray(0)
                    val unixTime = (System.currentTimeMillis() / 1000L).toDouble().roundToInt()


                    val tempimg = tool.getTempImgId(w.getString(0), true)

                    val tempin = w.getString(1)
                    val tempout = w.getString(0)
                    val humiout = tool.roundto(w.getString(2))
                    val humiin = tool.roundto(w.getString(3))
                    val insolation = tool.roundto(w.getString(9))
                    val rainfall = tool.roundto(w.getString(5))
                    val pressure = tool.roundto(w.getString(4))

                    //15 - 22 temp1-8
                    //23 - 30 humidity1-8
                    val newW = WeatherData(tempout,tempin,humiout,humiin, pressure,rainfall,
                        w.getString(6),w.getString(7),w.getString(8), insolation,w.getString(10),
                        w.getInt(11),unixTime, tempimg, "", "", w.getString(15),
                        w.getString(16), w.getString(17), w.getString(18), w.getString(19),
                        w.getString(20), w.getString(21), w.getString(22),
                        tool.roundto(w.getString(23)),
                        tool.roundto(w.getString(24)),
                        tool.roundto(w.getString(25)),
                        tool.roundto(w.getString(26)),
                        tool.roundto(w.getString(27)),
                        tool.roundto(w.getString(28)),
                        tool.roundto(w.getString(29)),
                        tool.roundto(w.getString(30))
                    )


                    val newweather = json.stringify(WeatherData.serializer(), newW)
                    saveWeather(newweather)

                    setWeather(JSONObject(newweather), station, false)
                }else{

                    if(response.getString("message") == "There are no weather stations in this city") {

                        saveNewStation(response.getString("city"),response.getInt("sunset"),response.getInt("sunrise"),
                            response.getString("country"), response.getInt("timezone"), lat, lon)

                        val city = v.findViewById<TextView>(R.id.citymain)
                        city.text = station.getString("city")
                            .toLowerCase(Locale.getDefault())
                            .capitalize()

                        if(station.getBoolean("gps")) {
                            if(station.getString("city") != response.getString("city")) {
                                val newW = WeatherData()
                                val newweather = json.stringify(WeatherData.serializer(), newW)
                                saveWeather(newweather)
                            }
                            city.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_gps_fixed_white_24dp,
                                0
                            )
                        }
                    }

                    setWeather(JSONObject(weatherold), station, true)
                }
            onSuccess(searchvalue)
            },
            Response.ErrorListener {
                setWeather(JSONObject(weatherold), station, true)
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }


    fun getNewestWeatherOpenWeather(stat:JSONObject, lat:String = "", lon:String = "", onSuccess: (searchval: String) -> Unit) {

        station = stat
        val searchvalue = if(lat != "") {
            "lat=$lat&lon=$lon"
        }else {
            "id="+station.getString("searchvalue")
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
                val tempout = w.getString("temp")
                val tempimg = tool.getTempImgId(tempout, true)
                val humiout = tool.roundto(w.getString("humidity"))
                val pressure = tool.roundto(w.getString("pressure"))
                val time = response.getInt("dt") + response.getInt("timezone")

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
                        lat, lon
                    )

                setWeather(JSONObject(newweather), station, false)
            }else{
                setWeather(JSONObject(weatherold), station, true)
            }
            onSuccess(station.getString("city"))
        },
            Response.ErrorListener {
                setWeather(JSONObject(weatherold), station, true)
                onSuccess(station.getString("city"))
                Toast.makeText(c, c.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(c).addToRequestQueue(request)
    }



    private fun saveNewStation(city:String, sunset:Int, sunrise:Int, country:String, timezone:Int, lati:String, longi:String){

        if(station.getBoolean("gps") && station.getString("city") != city) {
            val searchvalue = "$country/$city"

            val newstat = StationsData(
                "city",
                city,
                timezone,
                searchvalue,
                city,
                "",
                true,
                station.getBoolean("privstation"),
                station.getString("tempunit"),
                station.getString("windunit"),
                station.getInt("refreshtime"),
                sunset,
                sunrise,
                lati.toDouble(),
                longi.toDouble()
            )


            val newstation = json.stringify(StationsData.serializer(), newstat)
            station = JSONObject(newstation)

            val allstat = session.getPref("stations").split("|")

            var allnewstat = ""
            var allnewforecast = ""
            //delete old forecast
            val activeforecasts = session.getPref("forecasts").split("|")
            val forecast = ForecastData()
            val addedforecast = json.stringify(ForecastData.serializer(), forecast) + "|"

            for (i in 0 until allstat.size - 1) {
                allnewstat += if (position == i) "$newstation|" else allstat[i] + "|"
                allnewforecast += if (position == i) "$addedforecast|" else activeforecasts[i] + "|"
            }


            session.setPref("stations", allnewstat)
            session.setPref("forecasts", allnewforecast)
        }
    }

    private fun saveWeather(newweather:String){
        var allnewweathers = ""
        val allweathers = session.getPref("weathers").split("|")
        for(i in 0 until allweathers.size-1){
            allnewweathers += if(position == i) "$newweather|" else allweathers[i]+"|"
        }
        session.setPref("weathers", allnewweathers)
    }

    fun setWeather(w:JSONObject, s:JSONObject, error:Boolean):Boolean{

        val city = v.findViewById<TextView>(R.id.citymain)
        val tempout = v.findViewById<TextView>(R.id.tempout)
        val tempin = v.findViewById<TextView>(R.id.tempin)
        val tempunit = v.findViewById<TextView>(R.id.tempoutunit)
        val humiout = v.findViewById<TextView>(R.id.humiout)
        val humiin = v.findViewById<TextView>(R.id.humiin)
        val humiall = v.findViewById<LinearLayout>(R.id.humidityinfo)
        val pressure = v.findViewById<TextView>(R.id.press)
        val pressall = v.findViewById<LinearLayout>(R.id.allpress)
        val wind = v.findViewById<TextView>(R.id.wind)
        val windall = v.findViewById<LinearLayout>(R.id.allwind)
        val ins = v.findViewById<TextView>(R.id.ins)
        val insall = v.findViewById<LinearLayout>(R.id.allins)
        val rain = v.findViewById<TextView>(R.id.rain)
        val weatherimg = v.findViewById<ImageView>(R.id.weathericon)
        val rainall = v.findViewById<LinearLayout>(R.id.allrain)
        val airtxt = v.findViewById<TextView>(R.id.poltxt)
        val air10 = v.findViewById<TextView>(R.id.pol10)
        val air25 = v.findViewById<TextView>(R.id.pol25)
        val lastdata = v.findViewById<TextView>(R.id.lastdata)
        val updated = v.findViewById<TextView>(R.id.updatedAt)
        val batterylvl = v.findViewById<TextView>(R.id.baterrystatustxt)
        val batteryimg = v.findViewById<ImageView>(R.id.batterystatus)
        val allweather = v.findViewById<ConstraintLayout>(R.id.allweather)
        val errorlayout = v.findViewById<ConstraintLayout>(R.id.errorlayout)
        val pollutions = v.findViewById<LinearLayout>(R.id.linearLayout5)
        val updateClock = v.findViewById<LinearLayout>(R.id.allUpdateClock)
        val additionalTemp = v.findViewById<HorizontalScrollView>(R.id.additionalTemp)
        val unitGrid = v.findViewById<TextView>(R.id.unitGrid)

        val title = listOf<TextView>(
            v.findViewById(R.id.title1),
            v.findViewById(R.id.title2),
            v.findViewById(R.id.title3),
            v.findViewById(R.id.title4),
            v.findViewById(R.id.title5),
            v.findViewById(R.id.title6),
            v.findViewById(R.id.title7),
            v.findViewById(R.id.title8)
        )
        val humi = listOf<TextView>(
            v.findViewById(R.id.hum1),
            v.findViewById(R.id.hum2),
            v.findViewById(R.id.hum3),
            v.findViewById(R.id.hum4),
            v.findViewById(R.id.hum5),
            v.findViewById(R.id.hum6),
            v.findViewById(R.id.hum7),
            v.findViewById(R.id.hum8)
        )
        val temp = listOf<TextView>(
            v.findViewById(R.id.temp1),
            v.findViewById(R.id.temp2),
            v.findViewById(R.id.temp3),
            v.findViewById(R.id.temp4),
            v.findViewById(R.id.temp5),
            v.findViewById(R.id.temp6),
            v.findViewById(R.id.temp7),
            v.findViewById(R.id.temp8)
            )


        city.text = s.getString("city")
            .toLowerCase(Locale.getDefault())
            .capitalize()

        if(s.getBoolean("gps"))
            city.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_gps_fixed_white_24dp,0)


        if(error) {
            if (w.getInt("time") == 0) {
                allweather.visibility = View.GONE
                updated.visibility = View.GONE
                errorlayout.visibility = View.VISIBLE
                return false
            }else{
                allweather.visibility = View.VISIBLE
                errorlayout.visibility = View.GONE
            }
        }

        //clock
        if(s.getBoolean("privstation")) {
            val unixTime = System.currentTimeMillis()
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val lastdat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
            lastdat.timeZone = TimeZone.getTimeZone("GMT")
            val update = Date(unixTime)
            val datafrom = Date(w.getLong("time") * 1000L)
            val lastDataText =
                c.getString(R.string.lastupdate) + " " + lastdat.format(datafrom)
            val updatedText = c.getString(
                R.string.updated
            ) + " " + sdf.format(update)

            lastdata.text = lastDataText
            updated.text = updatedText
            updated.visibility = View.VISIBLE
            lastdata.visibility = View.VISIBLE
            updateClock.visibility = View.VISIBLE
        }else{
            updateClock.visibility = View.INVISIBLE
        }


        //temp
            if(w.getString("tempin") != "null") {
                setanimListener(tempin)

                val converttemp = tool.kelvintoTempUnit(w.getString("tempin"), s.getString("tempunit"))
                val temptext = c.getString(R.string.`in`) + " " + converttemp + s.getString("tempunit")
                tempin.text = temptext
            }else tempin.visibility = View.INVISIBLE

            if(w.getString("tempout") != "null"){
                setanimListener(tempout)
                setanimListener(tempunit)

                tempunit.text = s.getString("tempunit")
                val converttemp = tool.kelvintoTempUnit(w.getString("tempout"), s.getString("tempunit"))
                tempout.text = converttemp
            }else {
                tempout.visibility = View.INVISIBLE
                tempunit.visibility = View.INVISIBLE
            }

        //humidity

        if(w.getString("humidityout") != "null" || w.getString("humidityin") != "null"){

            var humiouttext = ""
            if(w.getString("humidityin") != "null") {
                val humitext = c.getString(R.string.`in`) + " " + w.getString("humidityin") + "%"
                humiin.text = humitext
                humiouttext = c.getString(R.string.out)
            }else humiin.visibility = View.GONE

            if(w.getString("humidityout") != "null"){
                val humitext = humiouttext+" "+w.getString("humidityout")+"%"
                humiout.text = humitext
            } else humiout.visibility = View.GONE

        }else{
            humiall.visibility = View.GONE
        }

        //pressure
        if(w.getString("pressure") != "null") {
            val presstext = w.getString("pressure") + "hPa"
            pressure.text = presstext
        } else pressall.visibility = View.GONE

        //windspeed
        if(w.getString("windspeed") != "null") {
            val windval = tool.mstoWindUnit(w.getString("windspeed"), s.getString("windunit"))
            val windtext = windval + s.getString("windunit")
            wind.text = windtext
        }else windall.visibility = View.GONE

        //insolation
        if(w.getString("insolation") != "null") {
            ins.text = if(s.has("ecowitt") && s.getBoolean("ecowitt")){
                val pom = w.getString("insolation") + " W/m2"
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(pom)
                builder.setSpan(
                    superscriptSpan,
                    pom.length-1,
                    pom.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                builder
            }else{
                w.getString("insolation") + "%"
            }
        }else insall.visibility = View.GONE

        //rainfall
        if(w.getString("rainfall") != "null") {
            val unit = if(s.has("ecowitt") && s.getBoolean("ecowitt")){
                " mm"
            }else{
                "%"
            }
            val raintext = w.getString("rainfall") + unit
            rain.text = raintext
        }else rainall.visibility = View.GONE

        //airpollution
        var indexgorny:String

        if(w.getString("airpollution10") != "null" || w.getString("airpollution25") != "null"){
            setanimListener(null, pollutions)
            setanimListener(airtxt)

            if(w.getString("airpollution10") != "null") {

                indexgorny = "PM10  " + w.getString("airpollution10") +" "+ c.getString(R.string.pollutionuit)
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(indexgorny)
                builder.setSpan(
                    superscriptSpan,
                    indexgorny.length-1,
                    indexgorny.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                air10.text = builder

                val colorid = tool.pm10(w.getString("airpollution10"))
                air10.background = ContextCompat.getDrawable(c,colorid)
            } else air10.visibility = View.INVISIBLE

            if(w.getString("airpollution25") != "null") {

                indexgorny = "PM2.5  " + w.getString("airpollution25") +" "+ c.getString(R.string.pollutionuit)
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(indexgorny)
                builder.setSpan(
                    superscriptSpan,
                    indexgorny.length-1,
                    indexgorny.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                air25.text = builder

                val colorid = tool.pm25(w.getString("airpollution25"))
                air25.background = ContextCompat.getDrawable(c,colorid)

            }else air25.visibility = View.INVISIBLE

        }else{
            airtxt.visibility = View.INVISIBLE
            air10.visibility = View.INVISIBLE
            air25.visibility = View.INVISIBLE
        }

        //battery
        if(w.getString("batterylvl") != "null"){

            val batlvl = w.getString("batterylvl") + "%"
            batterylvl.text = batlvl
            val batteryimgid = tool.batterylvl(batlvl, true)

            batteryimg.background = ContextCompat.getDrawable(c, batteryimgid)
            setanimListener(null, null, batteryimg)
            setanimListener(batterylvl)
        }else{
            batterylvl.visibility = View.GONE
            batteryimg.visibility = View.GONE
        }

        val weatherimage = if(s.getBoolean("privstation")) {
            tool.weathericon(
                w.getString("tempout"),
                w.getString("rainfall"),
                w.getString("insolation"),
                s.getInt("sunrise"),
                s.getInt("sunset"),
                s.getInt("timezone"),
                s.getBoolean("ecowitt"),
                true)
        }else{
            tool.weatherIconOpenWeather(
                w.getString("main"),
                w.getString("description"),
                s.getInt("sunrise"),
                s.getInt("sunset"),
                s.getInt("timezone"),
                true,
                w.getString("rainfall"))
        }

        weatherimg.background = ContextCompat.getDrawable(c, weatherimage)

        var anyone = false

        for(i in 1..8){
            if((w.getString("humidity${i}") != "null" && w.getString("humidity${i}") != "")
                || (w.getString("temp${i}") != "null") && w.getString("temp${i}") != ""){

                unitGrid.text = s.getString("tempunit")
                anyone = true
                if(s.getString("title${i}") != "") {
                    title[i-1].text = s.getString("title${i}")

                }else title[i-1].text = "${c.getString(R.string.room)}${i}"

                if(w.getString("humidity${i}") != "null") {
                    humi[i-1].text = w.getString("humidity${i}")

                }else humi[i-1].visibility = View.GONE

                if(w.getString("temp${i}") != "null"){
                    temp[i-1].text = tool.kelvintoTempUnit(w.getString("temp${i}"), s.getString("tempunit"))
                }else temp[i-1].visibility = View.GONE

            }else{
                temp[i-1].visibility = View.GONE
                humi[i-1].visibility = View.GONE
                title[i-1].visibility = View.GONE
            }
        }

        if(anyone){
            additionalTemp.visibility = View.VISIBLE
        }
        val items = v.findViewById<LinearLayout>(R.id.linearLayout)
        setanimListener(null, items)
        setanimListener(null, null, weatherimg)

        return true
    }

    private fun setanimListener(textView: TextView? = null, layout: LinearLayout? = null, image:ImageView? = null){
        val anim = AlphaAnimation(0f, 1f)
        anim.interpolator = DecelerateInterpolator() //add this
        anim.duration = 1000

        anim.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                if(textView != null)
                    textView.visibility = View.VISIBLE
                else if(layout != null)
                    layout.visibility = View.VISIBLE
                else if(image != null)
                    image.visibility = View.VISIBLE
            }
        })
        if(textView != null)
            textView.startAnimation(anim)
        else if(layout != null)
            layout.startAnimation(anim)
        else if(image != null)
            image.startAnimation(anim)
    }

}