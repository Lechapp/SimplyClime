package pl.simplyinc.simplyclime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.network.VolleySingleton
import pl.simplyinc.simplyclime.adapters.StationNameAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.StringRequest
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.viewpager.widget.ViewPager
import pl.simplyinc.simplyclime.adapters.PagerAdapter
import pl.simplyinc.simplyclime.widget.NewestWeather
import kotlin.system.exitProcess


const val server = "www.api.simplyclime.pl"
//const val server = "192.168.1.11/weatherapp"
const val openWeatherAPIKey = "45dc4902d2e0ef0659fc3e32b9195973"

class MainActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: PagerAdapter
    lateinit var session:SessionPref
    lateinit var adaptername:StationNameAdapter
    private lateinit var title:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(intent.getBooleanExtra("slideanim", false))
            overridePendingTransition(R.anim.slidein_from_left_to_right, R.anim.activity_slide_out_right)

        supportActionBar?.hide()
        session = SessionPref(this)
        pagerAdapter = PagerAdapter(supportFragmentManager, this)
        weathers.adapter = pagerAdapter

        setTitleBar()


        checkLogIn()

        updateWidget()

        checkactiveposition()

        weathers.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                adaptername.setActive(position)
            }
        })
    }

    override fun onBackPressed() {
        if(!pagerAdapter.hidedaylist(weathers.currentItem))
            exitProcess(-1)
    }

    private fun setTitleBar(){
        title = arrayListOf()
        val stations = session.getPref("stations").split("|")

        for(i in 0 until stations.size-1){
            val station = JSONObject(stations[i])
            try{
                title.add(station.getString("title"))
            }catch (exception:Exception){}
        }

        title.add(getString(R.string.addcity))

        adaptername = StationNameAdapter(title,this, weathers)
        namerecycler.adapter = adaptername
        namerecycler.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

    }

    private fun tokenCheck(){
        val url = "https://$server/user/login"
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
        val weathersS = session.getPref("weathers").split("|")

        var activestation = ""
        var activeweather = ""
        for(i in 0 until stations.size-1) {
            val station = JSONObject(stations[i])
            if (station.getString("type") != "mystation") {
                activestation += (stations[i] + "|")
                activeweather += (weathersS[i] + "|")
            }
        }
        session.setPref("stations", activestation)
        session.setPref("weathers", activeweather)

        checklogin.text = getString(R.string.notlogg)

        checklogin.setOnClickListener {
            val intent = Intent(applicationContext, LogInActivity::class.java)
            startActivity(intent)
        }

        setTitleBar()
        pagerAdapter = PagerAdapter(supportFragmentManager, this)
        weathers.adapter = pagerAdapter
    }


    private fun checkactiveposition(){
        val weatherposition = intent.getIntExtra("setweather", -1)

        if(weatherposition != -1){
            adaptername.setActive(weatherposition)
            weathers.setCurrentItem(weatherposition,true)
        }
    }

    private fun checkLogIn(){

        if(session.getPref("login") != "") {
            tokenCheck()
            checklogin.text = getString(R.string.logged)

            checklogin.setOnClickListener {
                logOut()
            }
        }else{
            checklogin.setOnClickListener {
                val intent = Intent(applicationContext, LogInActivity::class.java)
                startActivity(intent)
            }
        }
    }
}