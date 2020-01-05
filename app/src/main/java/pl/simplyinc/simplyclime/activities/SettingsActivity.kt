package pl.simplyinc.simplyclime.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

        setSaveUnits()

        setWindunit()

        apiKey()

        setTitleEdit()

        delete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.deleteTitle))
            val content = "${getString(R.string.deleteContent)} ${objStation.getString("title")}?"
            builder.setMessage(content)
            builder.setPositiveButton(getString(R.string.yes)){_, _ ->
                deleteWeather()
            }

            builder.setNegativeButton(getString(R.string.no)){_,_->
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }


        apply.setOnClickListener {

            val selectedwindUnit = findViewById<RadioButton>(wtempunit.checkedRadioButtonId).text.toString()
            val selectedtempUnit = findViewById<RadioButton>(stempunit.checkedRadioButtonId).text.toString()
            val newtitle = edittextsname.text.trim().toString()
            val titlelist = listOf<TextView>(room1, room2, room3, room4, room5, room6, room7, room8)
            val titlelistT = listOf<TextView>(roomT1, roomT2, roomT3, roomT4, roomT5, roomT6, roomT7, roomT8)
            for (i in 1..8) {
                titlelistT[i - 1].text = titlelist[i - 1].text
            }

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
                objStation.getDouble("lon"),
                objStation.getString("tempunitsave"),
                objStation.getString("windunitsave"),
                objStation.getBoolean("ecowitt"),
                room1.text.toString(),
                room2.text.toString(),
                room3.text.toString(),
                room4.text.toString(),
                room5.text.toString(),
                room6.text.toString(),
                room7.text.toString(),
                room8.text.toString()
                )
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

            roomedit1.visibility = View.GONE
            roomedit2.visibility = View.GONE
            roomedit3.visibility = View.VISIBLE
            roomedit4.visibility = View.VISIBLE

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


    private fun setSaveUnits(){
        if(objStation.getString("type") == "mystation"){

            saveTemp.text = objStation.getString("tempunitsave")
            saveWind.text = objStation.getString("windunitsave")

        }else{
            showas.visibility = View.GONE
            saveas.visibility = View.GONE
            settingsunits.visibility = View.GONE
        }
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

        if(objStation.getBoolean("privstation")) {
            val titlelist = listOf<TextView>(room1, room2, room3, room4, room5, room6, room7, room8)
            val titlelistT = listOf<TextView>(roomT1, roomT2, roomT3, roomT4, roomT5, roomT6, roomT7, roomT8)
            for (i in 1..8) {
                if (objStation.getString("title${i}") != "") {
                    titlelist[i - 1].text = objStation.getString("title${i}")
                    titlelistT[i - 1].text = objStation.getString("title${i}")
                } else {
                    titlelist[i - 1].text = "${getString(R.string.room)}${i}"
                    titlelistT[i - 1].text = "${getString(R.string.room)}${i}"
                }
            }
        }else{
            changeroomname.visibility = View.GONE
        }
    }

    private fun setTitleEdit(){

        editroom.setOnClickListener {
            roomedit1.visibility = View.VISIBLE
            roomedit2.visibility = View.VISIBLE
            roomedit3.visibility = View.GONE
            roomedit4.visibility = View.GONE
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
            val apikey = objStation.getString("apikey")
            DeleteWeaterStation().deleteStation(applicationContext, stationid,position, progressDelete, delete, apikey)
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
