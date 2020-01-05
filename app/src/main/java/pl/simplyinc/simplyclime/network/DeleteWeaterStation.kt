package pl.simplyinc.simplyclime.network

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.activities.server
import pl.simplyinc.simplyclime.elements.SessionPref

class DeleteWeaterStation {
    fun deleteStation(context:Context, stationid:String, position:Int, progressDelete:ProgressBar, delete:ImageView, apikey:String){

        val url = "https://$server/station/delete/$stationid/$apikey"
        progressDelete.visibility = View.VISIBLE
        delete.visibility = View.GONE

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                val response = JSONObject(res)
                if (!response.getBoolean("error")) {

                    val session = SessionPref(context)
                    val allstations = session.getPref("stations").split("|")
                    val allweatherdata = session.getPref("weathers").split("|")

                    var allnewstations = ""
                    var allnewweathers = ""
                    for(i in 0 until allstations.size-1){
                        if(position != i){
                            allnewstations += allstations[i]+"|"
                            allnewweathers += allweatherdata[i]+"|"
                        }
                    }

                    session.setPref("stations", allnewstations)
                    session.setPref("weathers", allnewweathers)

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }else{
                    progressDelete.visibility = View.GONE
                    delete.visibility = View.VISIBLE
                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show()
                }

            },
            Response.ErrorListener {
                progressDelete.visibility = View.GONE
                delete.visibility = View.VISIBLE
                Toast.makeText(context, context.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }
}