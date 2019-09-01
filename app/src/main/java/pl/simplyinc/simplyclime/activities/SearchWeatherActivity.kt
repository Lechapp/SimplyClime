package pl.simplyinc.simplyclime.activities

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_search_weather.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.adapters.SearchWeatherAdapter
import pl.simplyinc.simplyclime.elements.*
import pl.simplyinc.simplyclime.network.GetCityFromGPSOpenWeather
import pl.simplyinc.simplyclime.network.GetCityFromGPSRequest
import pl.simplyinc.simplyclime.network.SearchCity


class SearchWeatherActivity : AppCompatActivity() {

    private var permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val PERMISION_REQUEST = 10
    private val gpsManager = GpsLocation(this, this,true)
    var countcallback = 0
    var countcallfun = 0
    private var searchedCity = ""
    private var searchedcountry = ""
    private var searchedid = ""
    private var searchedtimezone = 0
    private var loc:Location? = null
    var prostationChecked = true
    lateinit var session:SessionPref
    lateinit var searchCity:SearchCity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.addcity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_search_weather)
        session = SessionPref(this)

        searchCity = SearchCity(this,searcherror, progressBarSearch, searchrecycler, tutorial)

        searchrecycler.layoutManager = LinearLayoutManager(this)
        progressBarSearch.visibility = View.GONE

        search.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->

            val city = search.text.trim().toString()

            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                countcallfun = 0
                countcallback = 0
                if(prostationChecked)
                    searchCity.searchFromOpenWeather(city)
                else
                    searchCity.searchCity(city)

                val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)

                return@OnKeyListener true
            }else false
        })

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val city = search.text.trim().toString()

                if(city.length > 3) {
                    countcallback++
                    Handler().postDelayed({
                        countcallfun++
                        if(prostationChecked)
                            searchCity.searchFromOpenWeather(city)
                        else
                            searchCity.searchCity(city)
                    }, 1300)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        val transitionstations = fromprostations.background as TransitionDrawable
        transitionstations.startTransition(0)

        fromprostations.setOnClickListener {
            actionOnChangeSourceWeather(false)
        }

        fromusers.setOnClickListener {
            actionOnChangeSourceWeather(true)
        }

        gpsicon.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this,R.anim.fadein)
            anim.duration = 450
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkPermission(permission)){
                    gpsManager.getLocation()
                    confirm.visibility = View.VISIBLE
                    confirm.startAnimation(anim)
                }else{
                    requestPermissions(permission, PERMISION_REQUEST)
                }
            }else{
                gpsManager.getLocation()
                confirm.visibility = View.VISIBLE
                confirm.startAnimation(anim)
            }
        }

        buttonokay.setOnClickListener {
            if(loc != null) {
                if (searchedCity != "")
                    createPlace()
                else
                    GetCityFromGPSRequest().getCity(this, loc!!)

                Toast.makeText(this, getString(R.string.waitfor), Toast.LENGTH_SHORT).show()
            }
        }

        buttonexit.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this,R.anim.fadeout)
            anim.duration = 450
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(arg0: Animation) {}
                override fun onAnimationRepeat(arg0: Animation) {}
                override fun onAnimationEnd(arg0: Animation) {
                    confirm.visibility = View.GONE
                }
            })
            confirm.startAnimation(anim)
        }
    }

    override fun onResume() {
        super.onResume()
        if(session.getPref("mapstutorial") == "true")
            tutorial.visibility = View.GONE
    }

    private fun actionOnChangeSourceWeather(fromUsers:Boolean){
        if(fromUsers == prostationChecked) {
            prostationChecked = !prostationChecked
            changeTextviewColor(fromprostations, !fromUsers)
            changeTextviewColor(fromusers, fromUsers)

            val city = search.text.trim().toString()
            if (city.length>2) {
                if (prostationChecked)
                    searchCity.searchFromOpenWeather(city)
                else
                    searchCity.searchCity(city)
            }
        }
    }

    private fun changeTextviewColor(Textvieww:TextView, check:Boolean){
        val colorwhite = ContextCompat.getColor(this, R.color.white)
        val colorblack = ContextCompat.getColor(this, R.color.blackfont)
        val colorAnim:ObjectAnimator
        val transition = Textvieww.background as TransitionDrawable

        if(check){
            transition.startTransition(150)
            colorAnim = ObjectAnimator.ofInt(Textvieww, "textColor", colorblack, colorwhite)
        }else{
            transition.reverseTransition(150)
            colorAnim = ObjectAnimator.ofInt(Textvieww, "textColor", colorwhite, colorblack)
        }

        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.start()
    }
    fun setLocation(l:Location){
        loc = l
        if(prostationChecked)
            GetCityFromGPSOpenWeather().getCity(this,l)
        else
            GetCityFromGPSRequest().getCity(this, l)
    }

    fun setCity(newCity:String, country:String, timezone:Int, id:String=""){
        searchedCity = newCity
        searchedcountry = country
        searchedtimezone = timezone
        searchedid = id
        foundcity.text = newCity
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(checkPermission(permission)) {
            gpsManager.getLocation()
            val anim = AnimationUtils.loadAnimation(this,R.anim.fadein)
            anim.duration = 450
            confirm.visibility = View.VISIBLE
            confirm.startAnimation(anim)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }




    private fun checkPermission(perarray:Array<String>):Boolean{
        var success = true

        for (i in perarray.indices){
            if(checkCallingOrSelfPermission(perarray[i]) == PackageManager.PERMISSION_DENIED)
                success = false
        }
        return success
    }

    private fun createPlace(){
        val activecities = session.getPref("stations")
        val activeweather = session.getPref("weathers")
        val activeforecasts = session.getPref("forecasts")
        val json = Json(JsonConfiguration.Stable)
        val searchvalue = if(prostationChecked) "lat=${loc?.latitude}&lon=${loc?.longitude}" else "$searchedcountry/$searchedCity"
        val station = StationsData("city", searchedCity,searchedtimezone, searchvalue, searchedCity, "", true, !prostationChecked
        ,"°C","km/h", 0,0,0,loc!!.latitude,loc!!.longitude)

        val selectedcity = json.stringify(StationsData.serializer(), station) + "|"

        val weather = WeatherData()
        val addweather = json.stringify(WeatherData.serializer(), weather) + "|"

        val forecast = ForecastData()
        val addedforecast = json.stringify(ForecastData.serializer(), forecast) + "|"

        if(activecities.indexOf("\"gps\":true,\"privstation\":\"$prostationChecked\"") != -1) {
            Toast.makeText(this, getString(R.string.alreadyadd), Toast.LENGTH_SHORT).show()
        }else{
            session.setPref("stations", activecities + selectedcity)
            session.setPref("weathers", activeweather + addweather)
            session.setPref("forecasts", activeforecasts + addedforecast)

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("station", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
