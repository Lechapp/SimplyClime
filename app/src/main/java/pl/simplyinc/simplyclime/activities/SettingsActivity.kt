package pl.simplyinc.simplyclime.activities

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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import pl.simplyinc.simplyclime.elements.StationsData
import pl.simplyinc.simplyclime.network.DeleteWeaterStation


class SettingsActivity : AppCompatActivity() {

    var position:Int = -1
    private lateinit var allstations:String
    lateinit var station:List<String>
    lateinit var session:SessionPref
    private lateinit var objStation:JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out)
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        position = intent.getIntExtra("position",-1)
        session = SessionPref(this)
        allstations = session.getPref("stations")
        station = allstations.split("|")
        objStation = JSONObject(station[position])

        settingscity.text = objStation.getString("city")

        setTitle()

        setTempunit()

        setWindunit()

        apiKey()

        delete.setOnClickListener {
            deleteWeather()
        }


        apply.setOnClickListener {

            val selectedwindUnit = findViewById<RadioButton>(wtempunit.checkedRadioButtonId).text.toString()
            val selectedtempUnit = findViewById<RadioButton>(stempunit.checkedRadioButtonId).text.toString()
            val newtitle = edittextsname.text.trim().toString()


            var newstations = ""
            val newstat = StationsData(
                objStation.getString("type"),
                objStation.getString("city"),
                objStation.getInt("timezone"),
                objStation.getString("searchvalue"),
                newtitle,
                objStation.getString("apikey"),
                objStation.getBoolean("gps"),
                objStation.getBoolean("privstation"),
                selectedtempUnit,
                selectedwindUnit,
                objStation.getInt("refreshtime"),
                objStation.getInt("sunset"),
                objStation.getInt("sunrise"),
                objStation.getDouble("lat"),
                objStation.getDouble("lon"))
            val json = Json(JsonConfiguration.Stable)

            val newstatstring = json.stringify(StationsData.serializer(), newstat)

            for (i in 0 until station.size-1){
                newstations += if(i == position) "$newstatstring|" else "${station[i]}|"
            }

            session.setPref("stations", newstations)
            station = newstations.split("|")
            objStation = JSONObject(station[position])

            windtextunit.text = selectedwindUnit
            editwunit.visibility = View.VISIBLE
            windtextunit.visibility = View.VISIBLE
            wtempunit.visibility = View.GONE

            snamee.text = newtitle
            snamee.visibility = View.VISIBLE
            edittextsname.visibility = View.GONE
            editsname.visibility = View.VISIBLE

            temptextunit.text = selectedtempUnit
            edittunit.visibility = View.VISIBLE
            temptextunit.visibility = View.VISIBLE
            stempunit.visibility = View.GONE

            apply.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        exit()
    }

    private fun exit(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("setweather", position)
        intent.putExtra("slideanim", true)
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
            apply.visibility = View.VISIBLE
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
            apply.visibility = View.VISIBLE
            stempunit.visibility = View.VISIBLE
        }

    }

    private fun setWindunit(){
        windtextunit.text = objStation.getString("windunit")
        when(objStation.getString("windunit")) {
            "km/h" -> skmh.isChecked = true
            "mph" -> smph.isChecked = true
            else -> sms.isChecked = true
        }

            editwunit.setOnClickListener {
            editwunit.visibility = View.GONE
            windtextunit.visibility = View.GONE
            apply.visibility = View.VISIBLE
            wtempunit.visibility = View.VISIBLE
        }
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

            var newstations = ""
            var newforecasts = ""
            var newweathers = ""

            val allforecasts = session.getPref("forecasts").split("|")
            val allweatherdata = session.getPref("weathers").split("|")
            val allstat = allstations.split("|")

            for(i in 0 until allweatherdata.size-1){
                if(i != position){
                    newstations += "${allstat[i]}|"
                    newweathers += "${allweatherdata[i]}|"
                    newforecasts += "${allforecasts[i]}|"
                }
            }
            session.setPref("stations", newstations)
            session.setPref("weathers", newweathers)
            session.setPref("forecasts", newforecasts)
            val intentt = Intent(applicationContext, MainActivity::class.java)
            intentt.putExtra("slideanim", true)
            startActivity(intentt)
            finish()
        }

    }
}
