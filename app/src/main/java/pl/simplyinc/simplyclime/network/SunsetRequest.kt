package pl.simplyinc.simplyclime.network

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey
import pl.simplyinc.simplyclime.elements.CircularProgressBar
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class SunsetRequest {

    fun getNewestSunset(context: Context, searchval:String, pos:Int,day:CircularProgressBar, night:CircularProgressBar, sset:TextView, srise:TextView) {

        val url = "https://api.openweathermap.org/data/2.5/weather?$searchval&appid=$openWeatherAPIKey"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)
                if (response.getString("cod") == "200") {

                    try {
                        val timezone = response.getInt("timezone")
                        val data = response.getJSONObject("sys")
                        val sunrise = data.getInt("sunrise")
                        val sunset = data.getInt("sunset")

                        setDayProgress(day,night, sset, srise, sunset, sunrise, timezone)
                        saveSunset(context,sunset,sunrise,pos)

                    }catch (e:Exception){}
                }

            },
            Response.ErrorListener {
                Toast.makeText(context, context.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    private fun saveSunset(c:Context,sunset:Int, sunrise:Int, pos:Int){
        val session = SessionPref(c)
        val allstation = session.getPref("stations")

        val station = allstation.split("|")[pos]
        val statobj = JSONObject(station)

        var newstation = station.replace("\"sunset\":" + statobj.getString("sunset"), "\"sunset\":$sunset")
        newstation = newstation.replace("\"sunrise\":" + statobj.getString("sunrise"), "\"sunrise\":$sunrise")

        val allnewstation = allstation.replace(station, newstation)
        session.setPref("stations", allnewstation)
    }

    fun setDayProgress(day:CircularProgressBar, night:CircularProgressBar, sunset:TextView,
                       sunrise:TextView, settimee:Int, risetimee:Int, cityTimezone:Int){

        val risetime = risetimee + cityTimezone
        val settime = settimee + cityTimezone
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val sunsett = sdf.format(Date((settime)*1000L))
        val sunrisee = sdf.format(Date((risetime)*1000L))
        val unixTime = System.currentTimeMillis() / 1000
        val timeInCity = unixTime + cityTimezone

        sunset.text = sunsett
        sunrise.text = sunrisee

        sunset.visibility = View.VISIBLE
        sunrise.visibility = View.VISIBLE

        val dayprogres:Float
        val nightprogres:Float

        if(timeInCity > settime || timeInCity < risetime){
            dayprogres = 50.toFloat()
            val all = risetime+86400-settime
            var piece = timeInCity - settime
            if(piece < 0)
                piece = all - (risetime-timeInCity)

            nightprogres = ((piece/all.toDouble()) * 50).toFloat()
        }else{
            nightprogres = 0.toFloat()
            val all = settime - risetime
            val piece = timeInCity - risetime

            dayprogres = ((piece/all.toDouble()) * 50).toFloat()
        }


        day.setProgressWithAnimation(dayprogres)
        if(nightprogres > 0) {
            Handler().postDelayed({
                night.setProgressWithAnimation(nightprogres)
            },1100)
        }else{
            night.setProgressWithAnimation(nightprogres)
        }
    }
}