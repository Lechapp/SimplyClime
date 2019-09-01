package pl.simplyinc.simplyclime.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.network.VolleySingleton
import pl.simplyinc.simplyclime.adapters.StationNameAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.volley.toolbox.StringRequest
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.support.v4.app.Fragment
import pl.simplyinc.simplyclime.fragments.AddWeather
import pl.simplyinc.simplyclime.fragments.Weatherinfo
import pl.simplyinc.simplyclime.widget.NewestWeather
import kotlin.system.exitProcess


const val server = "192.168.1.8/weatherapp"
const val openWeatherAPIKey = "45dc4902d2e0ef0659fc3e32b9195973"

class MainActivity : AppCompatActivity() {

    lateinit var session:SessionPref
    lateinit var adaptername:StationNameAdapter
    lateinit var frag:Fragment
    private lateinit var title:ArrayList<String>
    private val fm = supportFragmentManager
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(intent.getBooleanExtra("slideanim", false))
            overridePendingTransition(R.anim.slidein_from_left_to_right, R.anim.activity_slide_out_right)

        supportActionBar?.hide()
        session = SessionPref(this)
        setTitleBar()

        checkLogIn()

        updateWidget()

        checkactiveposition()
    }

    override fun onBackPressed() {
        if(position < title.size-1 && ::frag.isInitialized){
            val weinf = frag as Weatherinfo
            if(!weinf.hideDayByDay())
                exitProcess(-1)
        }else exitProcess(-1)
    }
    fun setWeather(position:Int){

            if (::frag.isInitialized)
                fm.beginTransaction().remove(frag).commit()

            frag = if (position >= title.size - 1) {
                adaptername.setActive(position)
                AddWeather()
            }else
                Weatherinfo.newInstance(position)


            fm.beginTransaction().add(R.id.container, frag).commit()

    }

    fun setTitleBar(){
        title = arrayListOf()
        val stations = session.getPref("stations").split("|")

        for(i in 0 until stations.size-1){
            val station = JSONObject(stations[i])
            try{
                title.add(station.getString("title"))
            }catch (exception:Exception){}
        }

        title.add(getString(R.string.addcity))

        adaptername = StationNameAdapter(title,this)
        namerecycler.adapter = adaptername
        namerecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

    }

    private fun tokenCheck(){
        val url = "http://$server/api/user/login"
        val request = object: StringRequest(Method.POST, url, Response.Listener{ res ->
                val response = JSONObject(res)
                if(!response.getBoolean("error")){
                    if(!response.getBoolean("tokenCheck")){
                        logOut()
                    }
                }
            },
            Response.ErrorListener {

            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                    params["token"] = session.getPref("token")
                    params["id"] = session.getPref("id")
                return params
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun updateWidget(){
        val intent = Intent(this, NewestWeather::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, NewestWeather::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    private fun logOut(){
        session.setPref("token","")
        session.setPref("login","")
        session.setPref("id","")
        val stations = session.getPref("stations").split("|")
        val weathers = session.getPref("weathers").split("|")

        var activestation = ""
        var activeweather = ""
        for(i in 0 until stations.size-1) {
            val station = JSONObject(stations[i])
            if (station.getString("type") != "mystation") {
                activestation += (stations[i] + "|")
                activeweather += (weathers[i] + "|")
            }
        }
        session.setPref("stations", activestation)
        session.setPref("weathers", activeweather)

        loginname.visibility = View.GONE
        checklogin.text = getString(R.string.notlogg)

        checklogin.setOnClickListener {
            val intent = Intent(applicationContext, LogInActivity::class.java)
            startActivity(intent)
        }

        setTitleBar()
    }


    private fun checkactiveposition(){
        val weatherposition = intent.getIntExtra("setweather", -1)
        if(weatherposition != -1){
            setWeather(weatherposition)
            position = weatherposition
        }else setWeather(position)
    }

    private fun checkLogIn(){
        if(session.getPref("login") != "") {
            tokenCheck()
            loginname.text = session.getPref("login")
            loginname.visibility = View.VISIBLE
            checklogin.text = getString(R.string.logged)

            checklogin.setOnClickListener {
                logOut()
            }
        }else{
            loginname.visibility = View.GONE
            checklogin.setOnClickListener {
                val intent = Intent(applicationContext, LogInActivity::class.java)
                startActivity(intent)
            }
        }
    }
}