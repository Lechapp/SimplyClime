package pl.simplyinc.simplyclime.network

import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.server
import pl.simplyinc.simplyclime.elements.SessionPref

class GetRefreshStationTime(val session:SessionPref, val station:String) {

    fun getNewestSunset(context: Context, stationid:String) {

        val url = "http://$server/api/station/refreshtime/$stationid"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)
                if (!response.getBoolean("error")) {
                    setRefreshTime(response.getString("refresh"))
                }else setRefreshTime()

            },
            Response.ErrorListener {
                setRefreshTime()
                Toast.makeText(context, context.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    fun setRefreshTime(refTime:String = "1800"){

        val allstation = session.getPref("stations")
        val currentreftime = JSONObject(station).getString("refreshtime")
        val newstation = station.replace("\"refreshtime\":$currentreftime", "\"refreshtime\":$refTime")

        val allnewstation = allstation.replace(station, newstation)
        session.setPref("stations", allnewstation)
    }
}