package pl.simplyinc.simplyclime.network

import android.app.Activity
import android.location.Location
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.SearchWeatherActivity
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey

class GetCityFromGPSOpenWeather {

    fun getCity(act: Activity, loc: Location) {

        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${loc.latitude}&lon=${loc.longitude}&appid=$openWeatherAPIKey"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)
                if (response.getString("cod") == "200") {

                    val activity = act as SearchWeatherActivity
                    val sys = response.getJSONObject("sys")
                    activity.setCity(response.getString("name"), sys.getString("country"),
                        response.getInt("timezone"), response.getString("id"))

                }else{
                    Toast.makeText(act, act.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(act, act.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)
    }

}