package pl.simplyinc.simplyclime.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_weatherinfo.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.activities.SettingsActivity
import pl.simplyinc.simplyclime.adapters.DayByDayAdapter
import pl.simplyinc.simplyclime.adapters.ForecastAdapter
import pl.simplyinc.simplyclime.elements.CircularProgressBar
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.elements.WeatherTools
import pl.simplyinc.simplyclime.network.*
import pl.simplyinc.simplyclime.widget.NewestWeather
import java.lang.Exception
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "position"

class Weatherinfo : Fragment() {

    private lateinit var dayProgress:CircularProgressBar
    private lateinit var nightProgress:CircularProgressBar
    private lateinit var sunriset:TextView
    private lateinit var sunsett:TextView
    lateinit var station:String
    lateinit var dayrecycler: RecyclerView
    private lateinit var background:ScrollView
    private lateinit var daybyday:TextView
    private var position:Int = -1
    private lateinit var act:MainActivity
    private lateinit var forecastRecycler:RecyclerView
    private lateinit var session:SessionPref
    private lateinit var forecastObj:JSONObject
    lateinit var dayByDayRequest: DayByDayRequest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootview =  inflater.inflate(R.layout.fragment_weatherinfo, container, false)
        val settings = rootview.findViewById<ImageView>(R.id.settings)
        dayByDayRequest = DayByDayRequest()
        dayProgress = rootview.findViewById(R.id.day_ProgressBar)
        dayProgress.setType("day")
        forecastRecycler = rootview.findViewById(R.id.forecastrecycler)
        nightProgress = rootview.findViewById(R.id.night_ProgressBar)
        daybyday = rootview.findViewById(R.id.daybyday)
        nightProgress.setType("night")
        act = activity as MainActivity
        dayrecycler = rootview.findViewById(R.id.daybydayrec)
        sunriset = rootview.findViewById(R.id.sunrisetime)
        sunsett = rootview.findViewById(R.id.sunsettime)
        background = rootview.findViewById(R.id.bgweather)
        session = SessionPref(activity!!.applicationContext)
        dayrecycler.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        val weatherall = session.getPref("weathers").split("|")

        val weather = weatherall[position]
        station = session.getPref("stations").split("|")[position]
        val update = NewestWeatherRequest()
        val unixTime = System.currentTimeMillis() / 1000L

        if(weatherall.size-1 > position){

            val stationdata = JSONObject(station)
            if(weather.length > 5 && station.length > 5) {
                forecastRecycler.layoutManager = LinearLayoutManager(activity!!.applicationContext, LinearLayoutManager.HORIZONTAL, false)
                val weatherdata = JSONObject(weather)
                val forecastsall = session.getPref("forecasts").split("|")
                forecastObj = JSONObject(forecastsall[position])
                if(stationdata.getInt("refreshtime") + weatherdata.getInt("updatedtime") <= unixTime)
                    update.getNewestWeather(activity!!.applicationContext, position, stationdata, rootview)
                else
                  update.setWeather(rootview, weatherdata, stationdata, false, activity!!.applicationContext)

                setdayprogress(stationdata)
                setBackground(weatherdata, stationdata)
                setClickDayByDay()

            }

            settings.setOnClickListener{
                val intent = Intent(activity!!.applicationContext, SettingsActivity::class.java)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        }
        return rootview
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_PARAM1)
        }

    }


    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            Weatherinfo().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }

    private fun setClickDayByDay(){

        daybyday.setOnClickListener {

            dayByDayRequest.getWeather(activity!!.applicationContext, station, dayrecycler, forecastrecycler, progressWeatherDay)

            val stat = JSONObject(station)
            val now = System.currentTimeMillis()/1000L
            if(forecastObj.getInt("time") < (now-12800)) {
                val forecast = ForecastRequest(activity!!.applicationContext, position, stat.getString("tempunit"))
                forecast.getNewestForecast(stat.getString("city"), forecastRecycler)
            }else{
                forecastrecycler.adapter =
                    ForecastAdapter(activity!!.applicationContext, forecastObj, stat.getString("tempunit"))
            }
            allmain.visibility = View.GONE
            dayfragment.visibility = View.VISIBLE
            dayfragment.animation = AnimationUtils.loadAnimation(activity!!.applicationContext, R.anim.slidein_from_right_to_left)
        }
    }


    fun hideDayByDay():Boolean{
        return if(dayfragment.visibility == View.VISIBLE) {
            dayfragment.visibility = View.GONE
            allmain.visibility = View.VISIBLE
            allmain.animation = AnimationUtils.loadAnimation(activity!!.applicationContext, R.anim.slidein_from_left_to_right)
            true
        }else false
    }


    private fun setdayprogress(stationdata:JSONObject){
        val sunset = SunsetRequest()
        val sdf = SimpleDateFormat("dd",Locale(Locale.getDefault().displayLanguage))
        val currentday = sdf.format(Date()).toInt()
        val saveday = sdf.format(Date(stationdata.getLong("sunset")*1000L)).toInt()
        if(saveday != currentday){
           sunset.getNewestSunset(activity!!.applicationContext, stationdata.getString("city"),position,
               dayProgress,nightProgress, sunsett, sunriset)

            setRefreshTime()

        }else{
            setRefreshTime()

            sunset.setDayProgress(dayProgress,nightProgress, sunsett, sunriset, stationdata.getInt("sunset"),
                stationdata.getInt("sunrise"), stationdata.getInt("timezone"))
        }
    }


    private fun setRefreshTime(){

        val allstation = session.getPref("stations")
        val station = allstation.split("|")[position]
        val stationdata = JSONObject(station)
        val refreshtime = GetRefreshStationTime(session, station)

        if(stationdata.getString("type") != "city"){
            refreshtime.getNewestSunset(activity!!.applicationContext, stationdata.getString("searchvalue"))
        }else refreshtime.setRefreshTime()

    }

    private fun setBackground(w:JSONObject, s:JSONObject){

        val img = WeatherTools().setBackground(w.getString("tempout"),w.getString("rainfall"), w.getString("insolation"),
            s.getInt("sunrise"), s.getInt("sunset"),s.getInt("timezone"),s.getString("tempunit"))
        background.background = ContextCompat.getDrawable(context!!, img)
    }
}
