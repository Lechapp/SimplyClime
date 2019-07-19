package pl.simplyinc.simplyclime.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_weatherinfo.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.CircularProgressBar
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.network.GetRefreshStationTime
import pl.simplyinc.simplyclime.network.NewestWeatherRequest
import pl.simplyinc.simplyclime.network.SunsetRequest
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
    private lateinit var background:ScrollView
    private var position:Int = -1
    private lateinit var session:SessionPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootview =  inflater.inflate(R.layout.fragment_weatherinfo, container, false)
        dayProgress = rootview.findViewById(R.id.day_ProgressBar)
        dayProgress.setType("day")
        nightProgress = rootview.findViewById(R.id.night_ProgressBar)
        nightProgress.setType("night")
        sunriset = rootview.findViewById(R.id.sunrisetime)
        sunsett = rootview.findViewById(R.id.sunsettime)
        background = rootview.findViewById(R.id.bgweather)

        session = SessionPref(activity!!.applicationContext)
        val weatherall = session.getPref("weathers").split("|")
        val weather = weatherall[position]
        val station = session.getPref("stations").split("|")[position]
        val update = NewestWeatherRequest()
        val unixTime = System.currentTimeMillis() / 1000L

        if(weatherall.size-1 > position){
            val stationdata = JSONObject(station)
            if(weather.length > 5 && station.length > 5) {
                val weatherdata = JSONObject(weather)

                if(stationdata.getInt("refreshtime") + weatherdata.getInt("updatedtime") <= unixTime)
                    update.getNewestWeather(activity!!.applicationContext, weather, stationdata, rootview)
                else
                    update.setWeather(rootview, weatherdata, stationdata, false)

                setdayprogress(stationdata)
                setBackground()

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


    private fun setdayprogress(stationdata:JSONObject){
        val sunset = SunsetRequest()
        val sdf = SimpleDateFormat("dd",Locale(Locale.getDefault().displayLanguage))
        val currentday = sdf.format(Date()).toInt()
        val saveday = sdf.format(Date(stationdata.getLong("sunset")*1000L)).toInt()
        if(saveday != currentday){
           sunset.getNewestSunset(activity!!.applicationContext, stationdata.getString("city"),position,
               dayProgress,nightProgress, sunsett, sunriset)

            setRefreshTime()

        }else{            setRefreshTime()

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

    private fun setBackground(){
        background.background = ContextCompat.getDrawable(context!!, R.drawable.moon_0)
    }
}
