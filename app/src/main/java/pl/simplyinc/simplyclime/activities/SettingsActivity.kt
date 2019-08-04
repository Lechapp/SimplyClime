package pl.simplyinc.simplyclime.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_settings.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.elements.WeatherTools
import java.lang.Exception
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import pl.simplyinc.simplyclime.network.DeleteWeaterStation


class SettingsActivity : AppCompatActivity() {

    var position:Int = -1
    lateinit var allstations:String
    lateinit var station:String
    lateinit var session:SessionPref
    lateinit var objStation:JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        position = intent.getIntExtra("position",-1)
        session = SessionPref(this)
        allstations = session.getPref("stations")
        station = allstations.split("|")[position]
        objStation = JSONObject(station)
        settingscity.text = objStation.getString("city")

        setTitle()

        setTempunit()

        setWindunit()

        apiKey()

        delete.setOnClickListener {
            deleteWeather()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exit()
    }

    private fun exit(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("fromsettings", position)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home){
            exit()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setTitle(){

        snamee.text = objStation.getString("title")
        edittextsname.setText(objStation.getString("title"))
        editsname.setOnClickListener {
            snamee.visibility = View.GONE
            edittextsname.visibility = View.VISIBLE
            editsname.visibility = View.GONE
            applysname.visibility = View.VISIBLE
        }

        applysname.setOnClickListener {
            val newtitle = edittextsname.text

            val newstation = station.replace("\"title\":\""+ objStation.getString("title") +"\"",
                "\"title\":\"$newtitle\"")

            val allnewstation = allstations.replace(station, newstation)
            session.setPref("stations", allnewstation)

            station = newstation
            objStation = JSONObject(station)
            snamee.text = newtitle
            snamee.visibility = View.VISIBLE
            edittextsname.visibility = View.GONE
            editsname.visibility = View.VISIBLE
            applysname.visibility = View.GONE
        }
    }

    private fun setTempunit(){
        temptextunit.text = objStation.getString("tempunit")

        when(objStation.getString("tempunit")){
            "°C" -> sC.isChecked = true
            "°F" -> sF.isChecked = true
            else -> sK.isChecked = true
        }

        edittunit.setOnClickListener {
            edittunit.visibility = View.GONE
            temptextunit.visibility = View.GONE
            applytunit.visibility = View.VISIBLE
            stempunit.visibility = View.VISIBLE
        }


        applytunit.setOnClickListener {

            val selectedUnit = findViewById<RadioButton>(stempunit.checkedRadioButtonId).text.toString()
            val newstation = station.replace("\"tempunit\":\""+ objStation.getString("tempunit") +"\"",
                "\"tempunit\":\"$selectedUnit\"")


            val allweatherdata = session.getPref("weathers")
            val weatherdata = allweatherdata.split("|")[position]
            val objWeather = JSONObject(weatherdata)
            val newWeathertempout = tempToSelectedUnit(objStation.getString("tempunit"), selectedUnit, objWeather.getString("tempout"))
            val newWeathertempin = tempToSelectedUnit(objStation.getString("tempunit"), selectedUnit, objWeather.getString("tempin"))

            var newweatherData = weatherdata.replace("\"tempout\":\""+ objWeather.getString("tempout") +"\"",
                "\"tempout\":\"$newWeathertempout\"")

            newweatherData = newweatherData.replace("\"tempin\":\""+ objWeather.getString("tempin") +"\"",
                "\"tempin\":\"$newWeathertempin\"")

            val allnewWeather = allweatherdata.replace(weatherdata, newweatherData)
            val allnewstation = allstations.replace(station, newstation)

            session.setPref("weathers", allnewWeather)
            session.setPref("stations", allnewstation)
            station = newstation
            objStation = JSONObject(station)

            temptextunit.text = selectedUnit
            edittunit.visibility = View.VISIBLE
            temptextunit.visibility = View.VISIBLE
            applytunit.visibility = View.GONE
            stempunit.visibility = View.GONE
        }

    }

    private fun setWindunit(){
        windtextunit.text = objStation.getString("windunit")

        when(objStation.getString("tempunit")) {
            "km/h" -> skmh.isChecked = true
            "mph" -> smph.isChecked = true
            else -> sms.isChecked = true
        }

            editwunit.setOnClickListener {
            editwunit.visibility = View.GONE
            windtextunit.visibility = View.GONE
            applywunit.visibility = View.VISIBLE
            wtempunit.visibility = View.VISIBLE
        }


        applywunit.setOnClickListener {

            val selectedUnit = findViewById<RadioButton>(wtempunit.checkedRadioButtonId).text.toString()
            val newstation = station.replace("\"windunit\":\""+ objStation.getString("windunit") +"\"",
                "\"windunit\":\"$selectedUnit\"")

            val allweatherdata = session.getPref("weathers")
            val weatherdata = allweatherdata.split("|")[position]
            val objWeather = JSONObject(weatherdata)
            val newWeatherWind = windToSelectedUnit(objStation.getString("windunit"), selectedUnit, objWeather.getString("windspeed"))

            val newweatherData = weatherdata.replace("\"windspeed\":\""+ objWeather.getString("windspeed") +"\"",
                "\"windspeed\":\"$newWeatherWind\"")
            val allnewWeather = allweatherdata.replace(weatherdata, newweatherData)

            val allnewstation = allstations.replace(station, newstation)
            session.setPref("weathers", allnewWeather)
            session.setPref("stations", allnewstation)
            station = newstation
            objStation = JSONObject(station)

            windtextunit.text = selectedUnit
            editwunit.visibility = View.VISIBLE
            windtextunit.visibility = View.VISIBLE
            applywunit.visibility = View.GONE
            wtempunit.visibility = View.GONE
        }
    }

    private fun tempToSelectedUnit(fromUnit:String, toUnit:String,valu:String):String{

        val kelvinValue:Double
        val value:Double
        val finishValue:String
        try{
            value = valu.toDouble()
        }catch (e:Exception){
            return "null"
        }

        kelvinValue = when(fromUnit) {
            "°C" -> value + 275.15
            "°F" -> ((5/9) * (value - 32)) + 273.15
            else -> value
        }


        finishValue = WeatherTools().kelvintoTempUnit(kelvinValue.toString(),toUnit)

        return finishValue
    }

    private fun windToSelectedUnit(fromUnit:String, toUnit:String,valu:String):String{

        val msValue:Double
        val value:Double
        val finishValue:String

        try{
            value = valu.toDouble()
        }catch (e:Exception){
            return "null"
        }

        msValue = when(fromUnit) {
            "km/h" -> value/3.6
            "mph" -> (1.609344*value)/3.6
            else -> value
        }


        finishValue = WeatherTools().mstoWindUnit(msValue.toString(),toUnit)


        return finishValue
    }

    private fun apiKey(){

        if(objStation.getString("type") == "mystation"){
            allapi.visibility = View.VISIBLE
            apiKey.text = objStation.getString("apikey")

            copyapi.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied text", objStation.getString("apikey"))
                clipboard.primaryClip = clip
                Toast.makeText(applicationContext, getString(R.string.textcopied), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun deleteWeather(){

        if(objStation.getString("type") == "mystation"){
            val stationid = objStation.getString("searchvalue")
            DeleteWeaterStation().deleteStation(applicationContext, stationid,position, progressDelete, delete)
        }else{

            val allnewstation = allstations.replace("$station|", "")
            val allweatherdata = session.getPref("weathers")
            val weatherdata = allweatherdata.split("|")[position]

            val allnewweather = allweatherdata.replace("$weatherdata|", "")
            session.setPref("stations", allnewstation)
            session.setPref("weathers", allnewweather)
            val intentt = Intent(applicationContext, MainActivity::class.java)
            startActivity(intentt)
            finish()
        }

    }
}
