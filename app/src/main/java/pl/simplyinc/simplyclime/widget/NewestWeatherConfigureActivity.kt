package pl.simplyinc.simplyclime.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.newest_weather_configure.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.adapters.WidgetChooseAdapter
import pl.simplyinc.simplyclime.elements.SessionPref
import java.lang.Exception
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import pl.simplyinc.simplyclime.elements.WidgetData
import android.view.ViewGroup
import kotlinx.android.synthetic.main.newest_weather.view.*


class NewestWeatherConfigureActivity : Activity() {
    var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    lateinit var session:SessionPref
    private lateinit var stations:List<String>
    private lateinit var title:ArrayList<String>
    var choosedPlace = -1
    var transparency = 70
    var darktheme = false
    val visibilityelement = HashMap<String,Boolean>()
    private lateinit var widgetlayout: View

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.newest_weather_configure)
        initVisibilityArray()

        val extras = intent.extras
        if (extras != null)
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)


        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        session = SessionPref(this)
        getWeatherPlaces()
        //preview
        val container = preview as ViewGroup
        val inflater = LayoutInflater.from(this)
        widgetlayout = inflater.inflate(R.layout.newest_weather, container)


        mylocations.layoutManager = LinearLayoutManager(this)
        mylocations.adapter = WidgetChooseAdapter(this,title)
        if(title.size == 0){
            addwidget.text = getString(R.string.addcity)
            addwidget.setOnClickListener {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }else{
            addwidget.setOnClickListener {

                if(choosedPlace != -1) {

                    transparency *= (255/100)
                    val bg = when(darktheme){
                        true -> "000000"
                        false -> "FFFFFF"
                    }
                    val color = "#"+Integer.toHexString(transparency)+bg
                    val widget = WidgetData(choosedPlace,Color.parseColor(color), darktheme, visibilityelement["tempout"]!!,
                        visibilityelement["tempin"]!!, visibilityelement["humidityout"]!!, visibilityelement["humidityin"]!!,
                        visibilityelement["pressure"]!!, visibilityelement["rainfall"]!!, visibilityelement["windspeed"]!!,
                        visibilityelement["airpollution10"]!!, visibilityelement["airpollution25"]!!, visibilityelement["insolation"]!!)

                    val json = Json(JsonConfiguration.Stable)
                    val newwidget = json.stringify(WidgetData.serializer(), widget)

                    session.setPref("widget$mAppWidgetId", newwidget)

                    val appWidgetManager = AppWidgetManager.getInstance(this)
                    NewestWeather.updateAppWidget(this,appWidgetManager,mAppWidgetId, choosedPlace)

                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                    setResult(RESULT_OK,resultValue)
                    finish()

                }else{
                    Toast.makeText(this,getString(R.string.chooseplace), Toast.LENGTH_SHORT).show()
                }
            }
        }


        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
                transparency = progresValue
                procent.text = "$progresValue %"
                setPreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        blackbg.setOnCheckedChangeListener { _, isChecked ->
            darktheme = isChecked
            setPreview()
        }


    }

    private fun getWeatherPlaces(){
        title = arrayListOf()
        stations = session.getPref("stations").split("|")

        for(i in 0 until stations.size-1){
            val station = JSONObject(stations[i])
            try{
                title.add(station.getString("title"))
            }catch (exception: Exception){}
        }
    }

    private var previewTheme = false
    private fun setPreview(){

        val trans = ((transparency * 255) / 100)
        val bg = when(darktheme){
            true -> "000000"
            false -> "FFFFFF"
        }
        var trans0 = Integer.toHexString(trans)

        if(trans0.length == 1)
            trans0 = "0$trans0"

        val color = "#$trans0$bg"
        widgetlayout.widget.setBackgroundColor(Color.parseColor(color))

        if(previewTheme != darktheme){
            previewTheme = darktheme
            setTheme()
        }
    }

    private fun setTheme(){
        if(darktheme){
            widgetlayout.tempimage.setBackgroundResource(R.drawable.temp2_w)
            widgetlayout.humidityimg.setBackgroundResource(R.drawable.humidity_w)
            widgetlayout.insolationimg.setBackgroundResource(R.drawable.sun_w)
            widgetlayout.windspeedimg.setBackgroundResource(R.drawable.wind_w)
            widgetlayout.rainfallimg.setBackgroundResource(R.drawable.umbrella_w)
            widgetlayout.pressureimg.setBackgroundResource(R.drawable.pressure_w)
            widgetlayout.weathericon.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.batteryimg.setBackgroundResource(R.drawable.ic_battery_50_white_24dp)
            val white = ContextCompat.getColor(this,R.color.white)
            widgetlayout.title.setTextColor(white)
            widgetlayout.battery.setTextColor(white)
            widgetlayout.updatedat.setTextColor(white)
            widgetlayout.tempin.setTextColor(white)
            widgetlayout.tempout.setTextColor(white)
            widgetlayout.humidityin.setTextColor(white)
            widgetlayout.humidityout.setTextColor(white)
            widgetlayout.pressure.setTextColor(white)
            widgetlayout.rainfall.setTextColor(white)
            widgetlayout.windspeed.setTextColor(white)
            widgetlayout.insolation.setTextColor(white)
            widgetlayout.airpollution.setTextColor(white)
            widgetlayout.forecastimg1.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp1.setTextColor(white)
            widgetlayout.widgetday1.setTextColor(white)
            widgetlayout.forecastimg2.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp2.setTextColor(white)
            widgetlayout.widgetday2.setTextColor(white)
            widgetlayout.forecastimg3.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp3.setTextColor(white)
            widgetlayout.widgetday3.setTextColor(white)
            widgetlayout.forecastimg4.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp4.setTextColor(white)
            widgetlayout.widgetday4.setTextColor(white)
            widgetlayout.forecastimg5.setBackgroundResource(R.drawable.cloud_sun_w)
            widgetlayout.forecasttemp5.setTextColor(white)
            widgetlayout.widgetday5.setTextColor(white)
        }else{
            widgetlayout.tempimage.setBackgroundResource(R.drawable.temp2_b)
            widgetlayout.humidityimg.setBackgroundResource(R.drawable.humidity_b)
            widgetlayout.insolationimg.setBackgroundResource(R.drawable.sun_b)
            widgetlayout.windspeedimg.setBackgroundResource(R.drawable.wind_b)
            widgetlayout.rainfallimg.setBackgroundResource(R.drawable.umbrella_b)
            widgetlayout.pressureimg.setBackgroundResource(R.drawable.pressure_b)
            widgetlayout.weathericon.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.batteryimg.setBackgroundResource(R.drawable.ic_battery_50_black_24dp)
            val white = ContextCompat.getColor(this,R.color.blackfont)
            widgetlayout.title.setTextColor(white)
            widgetlayout.battery.setTextColor(white)
            widgetlayout.updatedat.setTextColor(white)
            widgetlayout.tempin.setTextColor(white)
            widgetlayout.tempout.setTextColor(white)
            widgetlayout.humidityin.setTextColor(white)
            widgetlayout.humidityout.setTextColor(white)
            widgetlayout.pressure.setTextColor(white)
            widgetlayout.rainfall.setTextColor(white)
            widgetlayout.windspeed.setTextColor(white)
            widgetlayout.insolation.setTextColor(white)
            widgetlayout.airpollution.setTextColor(white)
            widgetlayout.forecastimg1.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp1.setTextColor(white)
            widgetlayout.widgetday1.setTextColor(white)
            widgetlayout.forecastimg2.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp2.setTextColor(white)
            widgetlayout.widgetday2.setTextColor(white)
            widgetlayout.forecastimg3.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp3.setTextColor(white)
            widgetlayout.widgetday3.setTextColor(white)
            widgetlayout.forecastimg4.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp4.setTextColor(white)
            widgetlayout.widgetday4.setTextColor(white)
            widgetlayout.forecastimg5.setBackgroundResource(R.drawable.cloud_sun_b)
            widgetlayout.forecasttemp5.setTextColor(white)
            widgetlayout.widgetday5.setTextColor(white)
        }
    }

    private fun initVisibilityArray(){
        visibilityelement["tempout"] = true
        visibilityelement["tempin"] = true
        visibilityelement["humidityout"] = true
        visibilityelement["humidityin"] = true
        visibilityelement["insolation"] = true
        visibilityelement["rainfall"] = true
        visibilityelement["windspeed"] = true
        visibilityelement["pressure"] = true
        visibilityelement["airpollution10"] = true
        visibilityelement["airpollution25"] = true

        tempin.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["tempin"] = isChecked
            visibleofitems("temp")
        }
        tempout.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["tempout"] = isChecked
            visibleofitems("temp")
        }
        humidityin.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["humidityin"] = isChecked
            visibleofitems("humidity")
        }
        humidityout.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["humidityout"] = isChecked
            visibleofitems("humidity")
        }
        airpol10.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["airpollution10"] = isChecked
            visibleofitems("airpollution")
        }
        airpol25.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["airpollution25"] = isChecked
            visibleofitems("airpollution")
        }

        insolation.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["insolation"] = isChecked
            if(isChecked){
                widgetlayout.insolation.visibility = View.VISIBLE
                widgetlayout.insolationimg.visibility = View.VISIBLE
            }else{
                widgetlayout.insolation.visibility = View.GONE
                widgetlayout.insolationimg.visibility = View.GONE
            }
        }
        rainfall.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["rainfall"] = isChecked
            if(isChecked){
                widgetlayout.rainfall.visibility = View.VISIBLE
                widgetlayout.rainfallimg.visibility = View.VISIBLE
            }else{
                widgetlayout.rainfall.visibility = View.GONE
                widgetlayout.rainfallimg.visibility = View.GONE
            }        }
        windspeed.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["windspeed"] = isChecked
            if(isChecked){
                widgetlayout.windspeed.visibility = View.VISIBLE
                widgetlayout.windspeedimg.visibility = View.VISIBLE
            }else{
                widgetlayout.windspeed.visibility = View.GONE
                widgetlayout.windspeedimg.visibility = View.GONE
            }        }
        pressure.setOnCheckedChangeListener { _, isChecked ->
            visibilityelement["pressure"] = isChecked
            if(isChecked){
                widgetlayout.pressure.visibility = View.VISIBLE
                widgetlayout.pressureimg.visibility = View.VISIBLE
            }else{
                widgetlayout.pressure.visibility = View.GONE
                widgetlayout.pressureimg.visibility = View.GONE
            }
        }
    }

    private fun visibleofitems(item:String){
        when(item){
            "temp"->{
                if(visibilityelement["tempout"]!! || visibilityelement["tempin"]!!){
                    widgetlayout.alltemp.visibility = View.VISIBLE

                    if(visibilityelement["tempout"]!!)
                        widgetlayout.tempout.visibility = View.VISIBLE
                    else widgetlayout.tempout.visibility = View.GONE

                    if(visibilityelement["tempin"]!!)
                        widgetlayout.tempin.visibility = View.VISIBLE
                    else widgetlayout.tempin.visibility = View.GONE

                }else widgetlayout.alltemp.visibility = View.GONE
            }
            "humidity"->{
                if(visibilityelement["humidityout"]!! || visibilityelement["humidityin"]!!){
                    widgetlayout.allhumidity.visibility = View.VISIBLE

                    if(visibilityelement["humidityout"]!!)
                        widgetlayout.humidityout.visibility = View.VISIBLE
                    else widgetlayout.humidityout.visibility = View.GONE

                    if(visibilityelement["humidityin"]!!)
                        widgetlayout.humidityin.visibility = View.VISIBLE
                    else widgetlayout.humidityin.visibility = View.GONE

                }else widgetlayout.allhumidity.visibility = View.GONE
            }
            "airpollution"->{
                if(visibilityelement["airpollution10"]!! || visibilityelement["airpollution25"]!!){
                    widgetlayout.allpolition.visibility = View.VISIBLE

                    if(visibilityelement["airpollution10"]!!)
                        widgetlayout.airpol10.visibility = View.VISIBLE
                    else widgetlayout.airpol10.visibility = View.GONE

                    if(visibilityelement["airpollution25"]!!)
                        widgetlayout.airpol25.visibility = View.VISIBLE
                    else widgetlayout.airpol25.visibility = View.GONE

                }else widgetlayout.allpolition.visibility = View.GONE
            }
        }
    }
}

