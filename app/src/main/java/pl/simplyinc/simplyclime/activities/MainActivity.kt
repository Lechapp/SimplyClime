package pl.simplyinc.simplyclime.activities
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.adapters.PagerAdapter
import pl.simplyinc.simplyclime.network.VolleySingleton
import java.net.URLEncoder
import android.support.v4.view.ViewPager.OnPageChangeListener
import pl.simplyinc.simplyclime.adapters.StationNameAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception


val openWeatherKeyAPI = "45dc4902d2e0ef0659fc3e32b9195973"
val server = "192.168.1.8/weatherapp"

class MainActivity : AppCompatActivity() {

    lateinit var pagerAdapter: PagerAdapter
    lateinit var session:SessionPref
    lateinit var adaptername:StationNameAdapter
    private val title = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        session = SessionPref(this)
        adaptername = StationNameAdapter(title,this, weathers)
        setTitleBar()

        if(session.getPref("login") != "") {
            loginname.text = session.getPref("login")
            loginname.visibility = View.VISIBLE
            checklogin.text = getString(R.string.logged)

            checklogin.setOnClickListener {
                session.setPref("token","")
                session.setPref("login","")
                session.setPref("mystations","")
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
        }else{
            loginname.visibility = View.GONE
            checklogin.setOnClickListener {
                val intent = Intent(applicationContext, LogInActivity::class.java)
                startActivity(intent)
            }
        }


        pagerAdapter = PagerAdapter(supportFragmentManager, this)
        weathers.adapter = pagerAdapter
        weathers.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                adaptername.setActive(position)
            }
        })

/*
        //przyklad openweather
        //        var encoded = URLEncoder.encode("Kołobrzeg dzikiąęł", "UTF-8")
        //https://api.openweathermap.org/data/2.5/weather?q=London&appid=$openWeatherKeyAPI

        val url = "http://$server/weatherapp/api/weather/1?newest=true"
        val params = JSONObject()
        params.put("newest", true)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                Log.d("testt","Response: %s".format(response.toString()))
            },
            Response.ErrorListener { error ->
                Log.d("testt", error.toString())
            }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
        */
    }

    private fun setTitleBar(){
        val cities = session.getPref("cities").split(",")
        val streets = session.getPref("stations").split(",")

        for(i in 0 until cities.size-1){
            val city = cities[i].split("/")
            try{
                title.add(city[1])
            }catch (exception:Exception){}
        }
        for(i in 0 until streets.size-1){
            val street = streets[i].split("/")
            try{
                title.add(street[0] + " " + street[1])
            }catch (exception:Exception){}
        }
        title.add(getString(R.string.addcity))

        namerecycler.adapter = adaptername
        adaptername.notifyDataSetChanged()
        namerecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

    }


}