package pl.simplyinc.simplyclime.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.serialization.json.Json
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.network.VolleySingleton
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonConfiguration
import pl.simplyinc.simplyclime.elements.ForecastData
import pl.simplyinc.simplyclime.elements.StationsData
import pl.simplyinc.simplyclime.elements.WeatherData

class LogInActivity : AppCompatActivity() {

    lateinit var session:SessionPref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        session = SessionPref(this)

        register.setOnClickListener {
            val reg = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(reg)
        }


        login.setOnClickListener {
            error.text = ""
            val username = username.text.trim().toString()
            val password = password.text.trim().toString()

            if(username.length < 4 || password.length < 4){
                error.text = getString(R.string.emptylogin)
            }else{
                logInRequest(username,password)
            }
        }
    }

    private fun logInRequest(username:String, password:String){
        login.visibility = View.INVISIBLE
        login.isEnabled = false
        progressLogin.visibility = View.VISIBLE

        val url = "http://$server/api/user/login"

        val request = object: StringRequest(Method.POST, url, Response.Listener{ res ->
                val response = JSONObject(res)

            if(!response.getBoolean("error")){

                    val intentt:Intent = when(intent.getBooleanExtra("fromadd", false)){
                        true -> Intent(applicationContext, AddStationActivity::class.java)
                        false ->Intent(applicationContext, MainActivity::class.java)
                    }

                    session.setPref("login", response.getString("login"))
                    session.setPref("id", response.getString("id"))
                    session.setPref("token", response.getString("token"))
                    val st = StringBuilder()
                    val wh = StringBuilder()
                    val fc = StringBuilder()
                    val json = Json(JsonConfiguration.Stable)

                    val weatherstations = response.getJSONObject("weatherstations")
                    val ids = weatherstations.getJSONArray("ids")
                    val names = weatherstations.getJSONArray("names")
                    val cities = weatherstations.getJSONArray("cities")
                    val timezone = weatherstations.getJSONArray("timezone")
                    val apikeys = weatherstations.getJSONArray("apikey")


                    for(i in 0 until ids.length()) {
                        val station = StationsData("mystation", cities.getString(i), timezone.getInt(i), ids.getString(i),
                            names.getString(i), apikeys.getString(i))
                        st.append(json.stringify(StationsData.serializer(), station)).append("|")

                        val weather = WeatherData()
                        wh.append(json.stringify(WeatherData.serializer(), weather)).append("|")

                        val forecast = ForecastData()
                        fc.append(json.stringify(ForecastData.serializer(), forecast)).append("|")

                    }

                    val activestation = session.getPref("stations")
                    val activeweather = session.getPref("weathers")
                    val activeforecast = session.getPref("forecasts")


                    session.setPref("stations",activestation+st)
                    session.setPref("weathers",activeweather+wh)
                    session.setPref("forecasts",activeforecast+fc)

                    startActivity(intentt)
                    finish()
                }else{
                    error.text = response.getString("message")
                }

                progressLogin.visibility = View.GONE
                login.visibility = View.VISIBLE
                login.isEnabled = true
            },
            Response.ErrorListener {
                error.text = getString(R.string.error)

                progressLogin.visibility = View.GONE
                login.visibility = View.VISIBLE
                login.isEnabled = true
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["login"] = username
                params["password"] = password.md5()
                return params
            }

        }

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun String.md5():String{
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32,'0').toUpperCase()
    }
}
