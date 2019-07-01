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
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_register.*
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception

val server = "192.168.1.8/weatherapp"

class MainActivity : AppCompatActivity() {

    lateinit var pagerAdapter: PagerAdapter
    lateinit var session:SessionPref
    lateinit var adaptername:StationNameAdapter
    private lateinit var title:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        session = SessionPref(this)
        setTitleBar()
        pagerAdapter = PagerAdapter(supportFragmentManager, this)
        weathers.adapter = pagerAdapter

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

        weathers.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                adaptername.setActive(position)
            }
        })

    }

    private fun setTitleBar(){
        title = arrayListOf()
        val cities = session.getPref("cities").split(",")
        val streets = session.getPref("stations").split(",")
        val stations = session.getPref("mystations").split(",")

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

        for(i in 0 until stations.size-1){
            val station = stations[i].split("/")
            try{
                title.add(station[0])
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
        session.setPref("mystations","")
        loginname.visibility = View.GONE
        checklogin.text = getString(R.string.notlogg)

        checklogin.setOnClickListener {
            val intent = Intent(applicationContext, LogInActivity::class.java)
            startActivity(intent)
        }

        pagerAdapter.changeData()
        setTitleBar()
    }

}