package pl.simplyinc.simplyclime.network

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.server
import pl.simplyinc.simplyclime.adapters.DayByDayAdapter

class DayByDayRequest {

    private val cacheWeatherData = mutableListOf<JSONArray>()
    private lateinit var adapt:DayByDayAdapter
    var busy = false

    fun getWeather(context: Context, station:String, recycler:RecyclerView, forecast:RecyclerView, prog:ProgressBar, offset:String = "null", dateto:String = ""):Boolean {

        var detail = false
        val stationData = JSONObject(station)

        if(offset == "null") {
            forecast.adapter

            if (cacheWeatherData.size > 0) {
                recycler.adapter = DayByDayAdapter(context, cacheWeatherData, station, recycler, forecast, prog, false)
                return true
            }
        }

        if(busy)
            return false
        else busy = !busy

        prog.visibility = View.VISIBLE
        recycler.visibility = View.GONE

        val offs = when(offset) {
            "null" -> "period=day"
            else -> {
                detail = true
                "offset=$offset&dateTo=$dateto&dateFrom=02-06-2019"
            }
        }

        if(!::adapt.isInitialized)
            adapt = DayByDayAdapter(context, cacheWeatherData, station, recycler, forecast, prog, detail)

        val url = "http://$server/api/weather/"+ stationData.getString("searchvalue")+"?$offs"

        val request = StringRequest(Request.Method.GET, url, Response.Listener { res ->

                prog.visibility = View.GONE
                recycler.visibility = View.VISIBLE

                val r = JSONObject(res)
                if (!r.getBoolean("error")) {

                    val weathers = r.getJSONArray("weather")

                    for (i in 0 until weathers.length()) {
                        cacheWeatherData.add(weathers.getJSONArray(i))
                    }

                    if (weathers.length() == 15) {
                        busy = false
                    }

                        if (offset == "null" || offset == "0") {
                            recycler.adapter = adapt
                        } else adapt.add(weathers.length())



                    if(offset != "null") {
                        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                                val visibleItemCount = recycler.layoutManager!!.childCount
                                val totalItemCount = recycler.layoutManager!!.itemCount
                                val pastVisibleItems = (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                                if (pastVisibleItems + visibleItemCount >= totalItemCount && !busy) {

                                    val newOffset = (offset.toInt()+15).toString()
                                    getWeather(context, station, recycler, forecast, prog, newOffset, dateto)
                                }
                            }
                        })
                    }
                }

            },
            Response.ErrorListener {
                prog.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                busy = false
                Toast.makeText(context, context.getString(R.string.checkconnection), Toast.LENGTH_SHORT).show()
            })

        VolleySingleton.getInstance(context).addToRequestQueue(request)
        return false
    }

}