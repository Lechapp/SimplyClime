package pl.simplyinc.simplyclime.fragments

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import kotlinx.android.synthetic.main.fragment_weatherinfo.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONArray
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.SettingsActivity
import pl.simplyinc.simplyclime.adapters.DayByDayAdapter
import pl.simplyinc.simplyclime.adapters.ForecastAdapter
import pl.simplyinc.simplyclime.elements.*
import pl.simplyinc.simplyclime.network.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM1 = "position"

class Weatherinfo : Fragment() {

    lateinit var station:String
    private lateinit var rootview:View
    private var position:Int = -1
    var wantToOpenDayByDay = false
    private lateinit var session:SessionPref
    private lateinit var forecastObj:JSONObject
    private var dayByDayRequest = DayByDayRequest()
    private var forecastWeatherData = mutableListOf<JSONArray>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootview = inflater.inflate(R.layout.fragment_weatherinfo, container, false)
        return rootview
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setVal()

        val weatherall = session.getPref("weathers").split("|")
        val weather = weatherall[position]

        val update = NewestWeatherRequest(activity!!, rootview, position)
        val unixTime = System.currentTimeMillis() / 1000L

        if(weatherall.size-1 > position){
            val stationdata = JSONObject(station)
            val chart = ChartRequest(activity!!.applicationContext, weatherchart, chartProgress, swipetutorial, charttitle)
            var isChartSet = false

            forecastrecycler.layoutManager = LinearLayoutManager(
                    activity!!.applicationContext,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            val weatherdata = JSONObject(weather)

            if(stationdata.getInt("refreshtime") + weatherdata.getInt("updatedtime") <= unixTime) {
                if(stationdata.getBoolean("gps")){
                    update.setWeather(weatherdata, stationdata, false) //tymczasowo

                    val gps = GpsLocation(activity!!,
                        activity!!.applicationContext,
                        false,
                        position,
                        stationdata,
                        rootview,
                        false,
                        null,
                        weather,
                        false,
                        chart,
                        this)
                    getLocation(gps)
                    isChartSet = true
                }else {
                    if(stationdata.getBoolean("privstation"))
                        update.getNewestWeather(stationdata) {}
                    else update.getNewestWeatherOpenWeather(stationdata){}
                }
            }else{
                if(stationdata.getBoolean("gps")){
                    update.setWeather(weatherdata, stationdata, false) //tymczasowo

                    val gps = GpsLocation(activity!!,
                        activity!!.applicationContext,
                        false,
                        position,
                        stationdata,
                        rootview,
                        false,
                        null,
                        weather,
                        true,
                        chart,
                        this)
                    getLocation(gps)
                    isChartSet = true
                }else
                    update.setWeather(weatherdata, stationdata, false)
            }

            setdayprogress(stationdata)

            setBackground(weatherdata, stationdata)

            setClickDayByDay()


            if(!isChartSet) {
                if(stationdata.getBoolean("privstation")) {
                    val ecowitt = if(stationdata.has("ecowitt")){
                        stationdata.getBoolean("ecowitt")
                    }else {
                        false
                    }
                    chart.getChartData(
                        stationdata.getString("searchvalue"),
                        stationdata.getString("tempunit"),
                        "","",
                        ecowitt
                    )
                }else{
                    chart.getChartDataOpenWeather(
                        stationdata.getString("city"),
                        stationdata.getString("tempunit")
                    ){
                        forecastWeatherData = it
                        if(wantToOpenDayByDay)
                            setDayByDayForecastOpenWeather()
                    }
                }
            }
            settings.setOnClickListener{
                val intent = Intent(activity!!.applicationContext, SettingsActivity::class.java)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        }
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
            charttitle.visibility = View.GONE
            val stat = JSONObject(station)
            val now = System.currentTimeMillis()/1000L
            val forecastsall = session.getPref("forecasts").split("|")

            try {
                forecastObj = JSONObject(forecastsall[position])
            }catch (e:Exception){
                val forecast = ForecastData()
                val json = Json(JsonConfiguration.Stable)
                val addforecast = json.stringify(ForecastData.serializer(), forecast)
                val save = forecastsall.toMutableList()
                save[position] = "$addforecast|"
                val allnew = save.joinToString("|") + "|"
                session.setPref("forecasts", allnew)
                forecastObj = JSONObject(addforecast)
            }

            if(forecastObj.getInt("time") < (now - stat.getInt("refreshtime"))) {
                station = session.getPref("stations").split("|")[position]
                dayByDayRequest = DayByDayRequest()
                val forecast = ForecastRequest(activity!!.applicationContext, position, stat.getString("tempunit"))
                forecast.getNewestForecast("q="+stat.getString("city"), forecastrecycler){}
            }else{
                forecastrecycler.adapter =
                    ForecastAdapter(activity!!.applicationContext, forecastObj, stat.getString("tempunit"))
            }
            forecastrecycler.visibility = View.VISIBLE
            if(stat.getBoolean("privstation")) {
                dayByDayRequest.getWeather(
                    activity!!.applicationContext,
                    station,
                    daybydayrec,
                    forecastrecycler,
                    progressWeatherDay
                )
            }else{
                setDayByDayForecastOpenWeather(stat.getInt("sunset"), stat.getInt("sunrise"), stat.getInt("timezone"))
            }
            allmain.visibility = View.GONE
            weathercontainer.visibility = View.GONE
            dayfragment.visibility = View.VISIBLE
            dayfragment.animation = AnimationUtils.loadAnimation(activity!!.applicationContext, R.anim.slidein_from_right_to_left)
        }
    }

    fun setForecastData(foracast:MutableList<JSONArray>){
        forecastWeatherData = foracast
        if(wantToOpenDayByDay)
            setDayByDayForecastOpenWeather()
    }

    private fun setDayByDayForecastOpenWeather(sunset:Int = 0, sunrise:Int = 0, timezone:Int = 0) {
        if (forecastWeatherData.size > 10) {
            val adapt = DayByDayAdapter(
                context!!,
                forecastWeatherData,
                station,
                daybydayrec,
                forecastrecycler,
                progressWeatherDay,
                true,
                true,
                sunset, sunrise, timezone
            )
            daybydayrec.adapter = adapt
            daybydayrec.visibility = View.VISIBLE
        }else wantToOpenDayByDay = true
    }

    fun hideDayByDay():Boolean{
        return if(dayfragment != null && dayfragment.visibility == View.VISIBLE) {
            charttitle.visibility = View.VISIBLE
            dayfragment.visibility = View.GONE
            allmain.visibility = View.VISIBLE
            weathercontainer.visibility = View.VISIBLE
            allmain.animation = AnimationUtils.loadAnimation(activity!!.applicationContext, R.anim.slidein_from_left_to_right)
            true
        }else false
    }


    private fun setdayprogress(stationdata:JSONObject){
        val sunset = SunsetRequest()
        val sdf = SimpleDateFormat("dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val currentday = sdf.format(Date()).toInt()

        val saveday = sdf.format(Date(stationdata.getLong("sunset")*1000L)).toInt()

        val searchval = if(stationdata.getBoolean("gps")){
            "lat=${stationdata.getString("lat")}&lon=${stationdata.getString("lon")}"
        }else "q=${stationdata.getString("city")}"

        if(saveday != currentday || stationdata.getInt("sunset") == 0){
           sunset.getNewestSunset(activity!!.applicationContext, searchval, position,
               day_ProgressBar,night_ProgressBar, sunsettime, sunrisetime)

            setRefreshTime()
        }else{
            sunset.setDayProgress(day_ProgressBar, night_ProgressBar, sunsettime, sunrisetime, stationdata.getInt("sunset"),
                stationdata.getInt("sunrise"), stationdata.getInt("timezone"))
        }
    }


    private fun setRefreshTime(){

        val allstation = session.getPref("stations")
        val station = allstation.split("|")[position]
        val stationdata = JSONObject(station)
        val refreshtime = GetRefreshStationTime(session, station)

        if(stationdata.getString("type") != "city"){
            refreshtime.getNewestRefreshTime(activity!!.applicationContext, stationdata.getString("searchvalue"))
        }else refreshtime.setRefreshTime()

    }

    private fun setVal(){
        day_ProgressBar.setType("day")
        night_ProgressBar.setType("night")
        session = SessionPref(activity!!.applicationContext)
        daybydayrec.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        val allstat =  session.getPref("stations").split("|")
        station = allstat[position]

    }

    private fun setBackground(w:JSONObject, s:JSONObject) {

        val img = if (s.getBoolean("privstation")) {
            WeatherTools().setBackground(
                w.getString("tempout"),
                w.getString("rainfall"),
                w.getString("insolation"),
                s.getInt("sunrise"),
                s.getInt("sunset"),
                s.getInt("timezone"),
                s.getString("tempunit")
            )
        }else{
            WeatherTools().setBackgroundOpenWeather(
                w.getString("main"),
                w.getString("description"),
                s.getInt("timezone"),
                s.getInt("sunrise"),
                s.getInt("sunset"),
                w.getString("rainfall"))
        }

       bgweather.background = ContextCompat.getDrawable(context!!, img)

    }

    private var permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val PERMISION_REQUEST = 10

    private fun getLocation(gps:GpsLocation){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission(permission)){
                gps.getLocation()
            }else{
                requestPermissions(permission, PERMISION_REQUEST)
            }
        }else{
            gps.getLocation()
        }
    }

    private fun checkPermission(perarray:Array<String>):Boolean{
        var success = true

        for (i in perarray.indices){
            if(checkCallingOrSelfPermission(context!!,perarray[i]) == PermissionChecker.PERMISSION_DENIED)
                success = false
        }
        return success
    }

}
