package pl.simplyinc.simplyclime.elements

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import android.view.View
import org.json.JSONObject
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.activities.SearchWeatherActivity
import pl.simplyinc.simplyclime.fragments.Weatherinfo
import pl.simplyinc.simplyclime.network.ChartRequest
import pl.simplyinc.simplyclime.network.ForecastRequest
import pl.simplyinc.simplyclime.network.NewestWeatherRequest
import pl.simplyinc.simplyclime.network.WidgetWeatherRequest


class GpsLocation(private val act: Activity? = null, private val c:Context, private val first:Boolean, val position: Int = 0,
                  private val stationsData: JSONObject? = null, private val rootview: View? = null, val widget:Boolean = false,
                  val request:WidgetWeatherRequest? = null, private val weatherOld:String = "", private val checkChangeLoc:Boolean = false,
                  private val chartRequest: ChartRequest? = null, private val frag:Weatherinfo? = null, private val forecastreq:ForecastRequest? = null) {

    private lateinit var locationManager : LocationManager
    private var hasGps = false
    private var gpslocation:Location? = null
    private var netlocation:Location? = null
    private var hasNetwork = false
    private var countLoc = 0
    private var counter = 0


    inner class GpsLocationListener(private var gpslocation:Location?, private val netlocation:Location?) :LocationListener{
        override fun onLocationChanged(location: Location?) {
            if (location != null)
            {
                gpslocation = location
                updatelocation(gpslocation,netlocation)
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }

   inner class NetLocationListener(private  var netlocation:Location?,private val gpslocation: Location?) :LocationListener{
        override fun onLocationChanged(location: Location?) {
            if (location != null)
            {
                netlocation = location
                updatelocation(gpslocation, netlocation)
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }
    private val gpsloclis = GpsLocationListener(gpslocation, netlocation)
    private val netloclis = NetLocationListener(netlocation, gpslocation)


    @SuppressLint("MissingPermission")
    fun getLocation(){
        counter = 0
        countLoc = 0
        locationManager = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(hasGps || hasNetwork){
            if(hasGps) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
                    0F, gpsloclis)

                val localgps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localgps != null)
                    gpslocation = localgps
            }
            if(hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0F,
                    netloclis)
                val localnet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localnet != null)
                    netlocation = localnet
            }
            updatelocation(gpslocation,netlocation)
        }else{
            startActivity(c,Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), null)
        }
    }
    private fun stopListener(){
        locationManager.removeUpdates(netloclis)
        locationManager.removeUpdates(gpsloclis)
    }

    private fun updatelocation(gloc:Location?,nloc:Location?){
        //longitude latitude accuarycy
        if(gloc != null) {
            checkLoc(gloc)
        }
        if(nloc != null) {
            checkLoc(nloc)
        }
    }

    private fun checkLoc(loc:Location){
        counter++

        if(loc.accuracy < (1000 + (counter*150))) {
            countLoc++
        }
        if(countLoc == 2){
            stopListener()

            if(widget){

                if(forecastreq != null){
                    val searchstring = "lat=${loc.latitude}&lon=${loc.longitude}"
                    forecastreq.getNewestForecast(searchstring){

                        if(stationsData!!.getBoolean("privstation"))
                            request?.getNewestWeatherWidget(weatherOld, loc.latitude.toString(), loc.longitude.toString(), it)
                        else request?.getNewestWeatherWidgetOpenWeather(weatherOld, loc.latitude.toString(), loc.longitude.toString(), it)
                    }
                }else{
                    if(stationsData!!.getBoolean("privstation"))
                        request?.getNewestWeatherWidget(weatherOld, loc.latitude.toString(), loc.longitude.toString())
                    else request?.getNewestWeatherWidgetOpenWeather(weatherOld, loc.latitude.toString(), loc.longitude.toString())
                }

            }else{
                if(first){
                    val activity = act as SearchWeatherActivity
                    activity.setLocation(loc)
                }else{
                    val update =  NewestWeatherRequest(c, rootview!!,position)

                    if(checkChangeLoc){

                        val oldLocation = Location("")
                        oldLocation.latitude = stationsData!!.getDouble("lat")
                        oldLocation.longitude = stationsData.getDouble("lon")

                        val distance = loc.distanceTo(oldLocation)

                        if(distance > 7000){
                                allRequestGPS(loc, stationsData.getBoolean("privstation"))
                        }else{
                            update.setWeather(JSONObject(weatherOld), stationsData, false)

                            if(stationsData.getBoolean("privstation")) {
                                val ecowitt = if(stationsData.has("ecowitt")){
                                    stationsData.getBoolean("ecowitt")
                                }else {
                                    false
                                }
                                chartRequest?.getChartData(
                                    stationsData.getString("searchvalue"),
                                    stationsData.getString("tempunit"),
                                    loc.latitude.toString(),
                                    loc.longitude.toString(),
                                    ecowitt
                                )
                            }else{
                                    chartRequest?.getChartDataOpenWeather(
                                        stationsData.getString("city"),
                                        stationsData.getString("tempunit"),
                                        loc.latitude.toString(),
                                        loc.longitude.toString()
                                    ){data ->
                                        frag?.setForecastData(data)
                                    }

                            }
                        }

                    }else {
                        allRequestGPS(loc, stationsData!!.getBoolean("privstation"))
                    }
                }
            }
        }
    }

    private fun allRequestGPS(loc:Location, privatestation:Boolean){
        val getWeather = NewestWeatherRequest(
            c,
            rootview!!,
            position
        )
        //val main = act as MainActivity

        if(privatestation){
            val ecowitt = if(stationsData!!.has("ecowitt")){
                stationsData.getBoolean("ecowitt")
            }else {
                false
            }
            getWeather.getNewestWeather(
                stationsData,
                loc.latitude.toString(),
                loc.longitude.toString()
            ) {
                chartRequest?.getChartData(
                    it,
                    stationsData.getString("tempunit"),
                    loc.latitude.toString(),
                    loc.longitude.toString(),
                    ecowitt
                )
                //main.setTitleBar()
            }

        }else {

            getWeather.getNewestWeatherOpenWeather(
                stationsData!!,
                loc.latitude.toString(),
                loc.longitude.toString()
            ) {
                chartRequest?.getChartDataOpenWeather(
                    it,
                    stationsData.getString("tempunit"),
                    loc.latitude.toString(),
                    loc.longitude.toString()
                ) { data ->
                    frag?.setForecastData(data)
                }

                //main.setTitleBar()
            }
        }
    }
}

