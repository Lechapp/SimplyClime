package pl.simplyinc.simplyclime.network

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.SearchWeatherActivity
import pl.simplyinc.simplyclime.activities.openWeatherAPIKey
import pl.simplyinc.simplyclime.activities.server
import pl.simplyinc.simplyclime.adapters.SearchWeatherAdapter
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception
import java.util.*

@Suppress("IMPLICIT_CAST_TO_ANY")
class SearchCity(private val act:SearchWeatherActivity, private val searcherror:TextView, private val progressBarSearch:ProgressBar,
                 private val searchrecycler: RecyclerView, private val tutorial:TextView) {

    fun searchCity(city:String):Boolean{

        if(city.length < 3) {
            searcherror.text = act.getString(R.string.searchempty)
            return false
        } else {
            searcherror.text = ""
            //only last call is execute
            if(act.countcallback != act.countcallfun)
                return false
        }
        progressBarSearch.visibility = View.VISIBLE
        val url = "https://$server/search/$city?lang="+ Locale.getDefault().language

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener{ res ->

                val response = JSONObject(res)

                val cities = mutableListOf<String>()
                val coords = mutableListOf<String>()
                val countries = mutableListOf<String>()
                val numberstation = mutableListOf<String>()
                val timezone = mutableListOf<Int>()
                val ecowitt = mutableListOf<Boolean>()

                if(!response.getBoolean("error")){

                    val listweather = response.getJSONArray("locations")
                    val liststations = response.getJSONObject("stations")
                    for(i in 0 until listweather.length()){
                        val arr = listweather.getJSONArray(i)
                        countries.add(arr.getString(0))
                        cities.add(arr.getString(1))
                        numberstation.add(arr.getString(2))
                        timezone.add(arr.getInt(3))
                        coords.add(arr.getString(4))
                        ecowitt.add(arr.getBoolean(5))
                    }

                    val adaptersearch = SearchWeatherAdapter(cities, countries, numberstation, timezone, act,
                        liststations, coords, ecowitt)

                    searchrecycler.adapter = adaptersearch
                    showtutorial()

                }else {
                    searcherror.text = response.getString("message")
                }
                progressBarSearch.visibility = View.GONE
            },
            Response.ErrorListener {
                progressBarSearch.visibility = View.GONE
                searcherror.text = act.getString(R.string.error)
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)

        return true
    }

    fun searchFromOpenWeather(city:String):Boolean{

        if(city.length < 3) {
            searcherror.text = act.getString(R.string.searchempty)
            return false
        } else {
            searcherror.text = ""
            //only last call is execute
            if(act.countcallback != act.countcallfun)
                return false
        }
        progressBarSearch.visibility = View.VISIBLE
        val url = "https://api.openweathermap.org/data/2.5/find?q=$city&appid=$openWeatherAPIKey"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener{ res ->

                val response = JSONObject(res)
                var ids = ""
                val names = mutableListOf<String>()
                if(response.getString("cod") == "200"){
                    if(response.getString("count") == "0") {
                        searcherror.text = act.getString(R.string.wrongcity)
                        progressBarSearch.visibility = View.GONE
                    }else{
                        val list = response.getJSONArray("list")
                        for(i in 0 until list.length()){
                            val data = list.getJSONObject(i)
                            ids += (if(i>0) "," else "") + data.getString("id")
                            names.add(data.getString("name"))
                        }
                        setCitiesFromWeatherApi(ids,names)

                    }

                    showtutorial()
                }else {
                    searcherror.text = response.getString("message")
                    progressBarSearch.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                progressBarSearch.visibility = View.GONE
                searcherror.text = act.getString(R.string.error)
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)

        return true
    }
    private fun showtutorial(){
        if(SessionPref(act).getPref("mapstutorial") != "true" && tutorial.visibility == View.GONE) {
            val anim = AnimationUtils.loadAnimation(act, R.anim.slidein_frombottom)
            anim.duration = 1000
            tutorial.visibility = View.VISIBLE
            tutorial.startAnimation(anim)
        }
    }


    private fun setCitiesFromWeatherApi(ids:String, names:MutableList<String>){

        val url = "https://api.openweathermap.org/data/2.5/group?id=$ids&appid=$openWeatherAPIKey"

        val request = StringRequest(
            Request.Method.GET, url, Response.Listener{ res ->
                progressBarSearch.visibility = View.GONE

                var cod = ""
                val response = JSONObject(res)
                try{
                    cod = response.getString("cod")
                }catch (e: Exception){}

                if(cod == ""){
                    val list = response.getJSONArray("list")

                    val cities = mutableListOf<String>()
                    val countries = mutableListOf<String>()
                    val coords = mutableListOf<String>()
                    val numberstation = mutableListOf<String>()
                    val timezone = mutableListOf<Int>()
                    val ecowitt = mutableListOf<Boolean>()

                    for(i in 0 until list.length()){
                        val data = list.getJSONObject(i)
                        //val preparestring  = names[i].toLowerCase(Locale.getDefault()).normalize()

                            val sys = data.getJSONObject("sys")
                            timezone.add(sys.getInt("timezone"))
                            countries.add(sys.getString("country"))
                            cities.add(data.getString("name"))
                            numberstation.add(data.getString("id"))
                            ecowitt.add(false)

                            val onecoord = data.getJSONObject("coord")
                            coords.add(onecoord.getString("lat") + "," + onecoord.getString("lon"))

                    }
                    val adaptersearch = SearchWeatherAdapter(cities, countries, numberstation, timezone, act,
                        null, coords, ecowitt)
                    searchrecycler.adapter = adaptersearch

                }else {
                    searcherror.text = act.getString(R.string.error)
                }
            },
            Response.ErrorListener {
                progressBarSearch.visibility = View.GONE
                searcherror.text = act.getString(R.string.error)
            })

        VolleySingleton.getInstance(act).addToRequestQueue(request)

    }
    private fun String.normalize(): String {

        val original = arrayOf("Ą", "ą", "Ć", "ć", "Ę", "ę", "Ł", "ł", "Ń", "ń", "Ó", "ó", "Ś", "ś", "Ź", "ź", "Ż", "ż")

        val normalized = arrayOf("A", "a", "C", "c", "E", "e", "L", "l", "N", "n", "O", "o", "S", "s", "Z", "z", "Z", "z")



        return this.map { char ->

            val index = original.indexOf(char.toString())

            if (index >= 0) normalized[index] else char

        }.joinToString("")

    }
}