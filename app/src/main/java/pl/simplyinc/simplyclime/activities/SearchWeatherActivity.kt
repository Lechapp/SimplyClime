package pl.simplyinc.simplyclime.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_search_weather.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.adapters.SearchWeatherAdapter
import pl.simplyinc.simplyclime.network.VolleySingleton
import java.util.*


class SearchWeatherActivity : AppCompatActivity() {

    private lateinit var adaptersearch:SearchWeatherAdapter
    private var cities = mutableListOf<String>()
    private var countries = mutableListOf<String>()
    private var numberstation = mutableListOf<String>()
    private var countcallback = 0
    private var countcallfun = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_weather)

        searchrecycler.layoutManager = LinearLayoutManager(this)
        progressBarSearch.visibility = View.GONE

        search.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->

            val city = search.text.trim().toString()

            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                countcallfun = 0
                countcallback = 0
                searchCity(city)

                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)

                return@OnKeyListener true
            }else false
        })

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val city = search.text.trim().toString()

                if(city.length > 3) {
                    countcallback++
                    Handler().postDelayed({
                        countcallfun++
                        searchCity(city)
                    }, 1300)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

    }

    private fun searchCity(city:String):Boolean{

        if(city.length < 3) {
            searcherror.text = getString(R.string.searchempty)
            return false
        } else {
            searcherror.text = ""
            //only last call is execute
            if(countcallback != countcallfun)
                return false
        }
        progressBarSearch.visibility = View.VISIBLE
        val url = "http://$server/api/search/$city?lang="+Locale.getDefault().language

        val request = StringRequest(Request.Method.GET, url, Response.Listener{ res ->

                val response = JSONObject(res)

                cities = mutableListOf()
                countries = mutableListOf()
                numberstation = mutableListOf()

                if(!response.getBoolean("error")){

                    val listweather = response.getJSONArray("locations")
                    val liststations = response.getJSONObject("stations")

                    for(i in 0 until listweather.length()){
                        val arr = listweather.getJSONArray(i)
                        countries.add(arr.getString(0))
                        cities.add(arr.getString(1))
                        numberstation.add(arr.getString(2))
                    }

                    adaptersearch = SearchWeatherAdapter(cities, countries, numberstation, applicationContext, liststations)
                    searchrecycler.adapter = adaptersearch

                }else {
                    searcherror.text = response.getString("message")
                }
             progressBarSearch.visibility = View.GONE
            },
            Response.ErrorListener {
                progressBarSearch.visibility = View.GONE
                searcherror.text = getString(R.string.error)
            })

        VolleySingleton.getInstance(this).addToRequestQueue(request)

        return true
    }
}
