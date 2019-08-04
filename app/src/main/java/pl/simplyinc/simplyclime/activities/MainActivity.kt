package pl.simplyinc.simplyclime.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.adapters.PagerAdapter
import pl.simplyinc.simplyclime.network.VolleySingleton
import android.support.v4.view.ViewPager.OnPageChangeListener
import pl.simplyinc.simplyclime.adapters.StationNameAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.volley.toolbox.StringRequest
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception

const val server = "192.168.1.8/weatherapp"

class MainActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: PagerAdapter
    lateinit var session:SessionPref
    lateinit var adaptername:StationNameAdapter
    private lateinit var title:ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        session = SessionPref(this)
        setTitleBar()
        pagerAdapter = PagerAdapter(supportFragmentManager, this)
        weathers.adapter = pagerAdapter

        checkactiveposition()

        checkLogIn()


        weathers.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                adaptername.setActive(position)
            }
        })

    }

    override fun onBackPressed() {
        if(!pagerAdapter.hidedaylist(weathers.currentItem))
            super.onBackPressed()
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

        pagerAdapter.changeData()
        setTitleBar()
    }


    private fun checkactiveposition(){
        if(intent.getBooleanExtra("station", false)){
            adaptername.setActive(title.size-2)
            weathers.setCurrentItem(title.size-2,true)
        }
        val widgetposition = intent.getIntExtra("fromwidget", -1)
        if(widgetposition != -1){
            adaptername.setActive(widgetposition)
            weathers.setCurrentItem(widgetposition,true)
        }
        val weatherposition = intent.getIntExtra("fromsettings", -1)
        if(weatherposition != -1){
            adaptername.setActive(weatherposition)
            weathers.setCurrentItem(weatherposition,false)
        }
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