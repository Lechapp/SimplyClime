package pl.simplyinc.simplyclime.network

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.SuperscriptSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

class NewestWeatherRequest {

    private val tool = WeatherTools()

    fun getNewestWeather(context:Context, weatherOld:String, station:JSONObject, v:View) {

        val tempunit = station.getString("tempunit")
        val windunit = station.getString("windunit")
        val searchvalue = station.getString("searchvalue")
        val url = "http://$server/api/weather/$searchvalue?newest=true"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)
                if (!response.getBoolean("error")) {
                    val w = response.getJSONArray("weather").getJSONArray(0)
                    val unixTime = (System.currentTimeMillis() / 1000L).toDouble().roundToInt()
                    val tempimg = tool.getTempImgId(w.getString(0), true)
                    val tempin = tool.kelvintoTempUnit(w.getString(1), tempunit)
                    val tempout = tool.kelvintoTempUnit(w.getString(0), tempunit)
                    val wind = tool.mstoWindUnit(w.getString(6), windunit)
                    val humiout = tool.roundto(w.getString(2))
                    val humiin = tool.roundto(w.getString(3))
                    val insolation = tool.roundto(w.getString(9))
                    val rainfall = tool.roundto(w.getString(5))

                    val newW = WeatherData(tempout,tempin,humiout,humiin, w.getString(4),rainfall,
                        wind,w.getString(7),w.getString(8), insolation,w.getString(10),
                        w.getInt(11),unixTime, tempimg)

                    val json = Json(JsonConfiguration.Stable)
                    val newweather = json.stringify(WeatherData.serializer(), newW)
                    val session = SessionPref(context)
                    val allnewweathers = session.getPref("weathers").replace(weatherOld,newweather)
                    session.setPref("weathers", allnewweathers)

                    setWeather(v,JSONObject(newweather), station, false)
                }else{
                    setWeather(v,JSONObject(), station, true)
                }

            },
            Response.ErrorListener {

            })

        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    fun setWeather(v:View, w:JSONObject, s:JSONObject, error:Boolean):Boolean{
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
        val updated = v.findViewById<TextView>(R.id.lastdata)
        val batterylvl = v.findViewById<TextView>(R.id.baterrystatustxt)
        val batteryimg = v.findViewById<ImageView>(R.id.batterystatus)
        val allweather = v.findViewById<ConstraintLayout>(R.id.allweather)
        val errorlayout = v.findViewById<ConstraintLayout>(R.id.errorlayout)

        val c = tempin.context

        if(error){
            allweather.visibility = View.GONE
            city.visibility = View.GONE
            updated.visibility = View.GONE
            errorlayout.visibility = View.VISIBLE
            return false
        }else{
            allweather.visibility = View.VISIBLE
            errorlayout.visibility = View.GONE
        }

        city.text = s.getString("city")

        //temp
            if(w.getString("tempin") != "null") {
                val temptext = c.getString(R.string.`in`) + " " + w.getString("tempin") + s.getString("tempunit")
                tempin.text = temptext

            }else tempin.visibility = View.INVISIBLE

            if(w.getString("tempout") != "null"){

                tempunit.text = s.getString("tempunit")
                tempout.text = w.getString("tempout")
            }else {
                tempout.visibility = View.INVISIBLE
                tempunit.visibility = View.INVISIBLE
            }

        //humidity

        if(w.getString("humidityout") != "null" || w.getString("humidityin") != "null"){

            if(w.getString("humidityin") != "null") {
                val humitext = c.getString(R.string.`in`) + " " + w.getString("humidityin") + "%"
                humiin.text = humitext
            }else humiin.visibility = View.GONE

            if(w.getString("humidityout") != "null"){
                val humitext = c.getString(R.string.out)+" "+w.getString("humidityout")+"%"
                humiout.text = humitext
            } else humiout.visibility = View.GONE

        }else{
            humiall.visibility = View.GONE
        }

        //pressure
        if(w.getString("pressure") != "null") {
            val presstext = w.getString("pressure") + "HPa"
            pressure.text = presstext
        } else pressall.visibility = View.GONE

        //windspeed
        if(w.getString("windspeed") != "null") {
            val windtext = w.getString("windspeed") + s.getString("windunit")
            wind.text = windtext
        }else windall.visibility = View.GONE

        //insolation
        if(w.getString("insolation") != "null") {
            val insoltext = w.getString("insolation") + "%"
            ins.text = insoltext
        }else insall.visibility = View.GONE

        //rainfall
        if(w.getString("rainfall") != "null") {
            val raintext = w.getString("rainfall") + "%"
            rain.text = raintext
        }else rainall.visibility = View.GONE

        //airpollution
        var indexgorny:String

        if(w.getString("airpollution10") != "null" || w.getString("airpollution25") != "null"){

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

                val colorid = tool.PM10(w.getString("airpollution10"))
                air10.background = ContextCompat.getDrawable(c,colorid)
            } else air10.visibility = View.GONE

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

                val colorid = tool.PM25(w.getString("airpollution25"))
                air25.background = ContextCompat.getDrawable(c,colorid)

            }else air25.visibility = View.GONE

        }else{
            airtxt.visibility = View.GONE
            air10.visibility = View.GONE
            air25.visibility = View.GONE
        }

        //battery
        if(w.getString("batterylvl") != "null"){

            val batlvl = w.getString("batterylvl")
            batterylvl.text = "$batlvl%"
            val batteryimgid = tool.batterylvl(batlvl, true)

            batteryimg.background = ContextCompat.getDrawable(c, batteryimgid)

        }else{
            batterylvl.visibility = View.GONE
            batteryimg.visibility = View.GONE
        }

        val weatherimage = tool.weathericon(w.getString("tempout"), w.getString("rainfall"), w.getString("insolation"),
            s.getInt("sunrise"), s.getInt("sunset"), w.getInt("time"), true)

        weatherimg.background = ContextCompat.getDrawable(c, weatherimage)

        val sdf = SimpleDateFormat("HH:mm", Locale(Locale.getDefault().displayLanguage))
        val lastdat = SimpleDateFormat("dd.MM HH:mm", Locale(Locale.getDefault().displayLanguage))
        val update = Date(w.getLong("updatedtime")*1000L)
        val datafrom = Date(w.getLong("time")*1000L)
        val text = c.getString(R.string.lastupdate) + " " + lastdat.format(datafrom) + ". " + c.getString(R.string.updated) +" "+ sdf.format(update)
        updated.text = text
        return true
    }


}