package pl.simplyinc.simplyclime.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_add_station.*
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.street_row.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.network.VolleySingleton
import android.location.Geocoder
import android.view.View
import android.widget.Toast
import pl.simplyinc.simplyclime.R
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap


class AddStationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_station)
        progressaddStation.visibility = View.GONE

        addbutton.setOnClickListener {

            if(checkData()){
               addStation()
            }

        }
    }

    private fun checkData():Boolean{
        var ok = false
        val stationname = addstationname.text.trim().toString()
        val city = addcity.text.trim().toString()
        val street = addstreet.text.trim().toString()
        val number = flatnumber.text.trim().toString()

        if(stationname.length < 3){
            addstationname.error = getString(R.string.requiered)
        }else if(city.length < 3){
            addcity.error = getString(R.string.requiered)
        }else if (street.length < 3){
            addstreet.error = getString(R.string.requiered)
        }else if(number.isEmpty()){
            flatnumber.error = getString(R.string.requiered)
        } else ok = true

        return ok
    }

    private fun addStation(){
        progressaddStation.visibility = View.VISIBLE
        addbutton.visibility = View.GONE

        val session = SessionPref(this)
        val stationname = addstationname.text.trim().toString()
        val city = addcity.text.trim().toString()
        val street = addstreet.text.trim().toString()
        val number = flatnumber.text.trim().toString()
        val temp = findViewById<RadioButton>(tempunit.checkedRadioButtonId).text.toString()
        val wind = findViewById<RadioButton>(windunit.checkedRadioButtonId).text.toString()
        var latitude = 0.0
        var longitude = 0.0

       try {
           val geocoder = Geocoder(this, Locale.getDefault())
           val addresses = geocoder.getFromLocationName("$city $street $number", 1)

           if (addresses.size > 0) {
               latitude = addresses[0].latitude
               longitude = addresses[0].longitude
           }
       }catch (e:Exception){

       }
        val url = "http://$server/api/weather/station/add"

        val request = object: StringRequest(Method.POST, url, Response.Listener{ res ->

                val response = JSONObject(res)

                if(!response.getBoolean("error")){
                    val station = response.getString("name")+"/"+response.getString("stationID")+","
                    val activestation = session.getPref("mystations")
                    session.setPref("mystations",activestation+station)

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("mystations", true)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,response.getString("message"), Toast.LENGTH_SHORT).show()
                }
                progressaddStation.visibility = View.GONE
                addbutton.visibility = View.VISIBLE
            },
            Response.ErrorListener {
                progressaddStation.visibility = View.GONE
                addbutton.visibility = View.VISIBLE
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()

            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["name"] = stationname
                params["userID"] = session.getPref("id")
                params["city"] = city
                params["street"] = street
                params["flatnumber"] = number
                params["tempunit"] = temp
                params["windunit"] = wind
                params["longitude"] = longitude.toString()
                params["latitude"] = latitude.toString()
                //params["lang"] = Locale.getDefault().language
                return params
            }

        }

        VolleySingleton.getInstance(this).addToRequestQueue(request)


    }
}
