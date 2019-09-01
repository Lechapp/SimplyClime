package pl.simplyinc.simplyclime.network

import android.app.Activity
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.SearchWeatherActivity
import pl.simplyinc.simplyclime.activities.server

class GetCityFromGPSRequest {

        fun getCity(act: Activity, loc: Location) {

            val url = "http://$server/api/station/search/${loc.latitude}/${loc.longitude}"
            val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                    val response = JSONObject(res)
                    if (!response.getBoolean("error")) {

                            val activity = act as SearchWeatherActivity

                            activity.setCity(response.getString("city"), response.getString("country"),
                                response.getInt("timezone"))


                    }else{
                        Toast.makeText(act, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(act, act.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
                })

            VolleySingleton.getInstance(act).addToRequestQueue(request)
        }
}
