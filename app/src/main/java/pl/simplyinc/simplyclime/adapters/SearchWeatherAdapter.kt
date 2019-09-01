package pl.simplyinc.simplyclime.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.search_row.view.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.elements.ForecastData
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.elements.StationsData
import pl.simplyinc.simplyclime.elements.WeatherData
import java.util.*
import android.net.Uri




class SearchWeatherAdapter(private val cities: MutableList<String>, private val countries: MutableList<String>, val stations: MutableList<String>,
                           val timezone:MutableList<Int>, val context: Context, private val liststations:JSONObject?,
                           private val coord:MutableList<String>): RecyclerView.Adapter<ViewHolder>() {

    private val session = SessionPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.search_row, parent, false))
    }

    override fun getItemCount(): Int {
        return if (cities.size == 0) 1 else cities.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = holder.itemView.city
        val country = holder.itemView.country
        val numberstation = holder.itemView.numberstation
        val searchtext = holder.itemView.searchtext
        val showmaps = holder.itemView.showmaps
        val flag = holder.itemView.flag
        val street = holder.itemView.street
        val showstations = holder.itemView.showstations
        val recyclercity = holder.itemView.citystations

        street.visibility = View.GONE

        if(cities.size == 0){
            showmaps.visibility = View.GONE
            city.text = context.getString(R.string.emptysearch)
            showstations.visibility = View.GONE
        }else {
            val url = "drawable/" + if (countries[position] == "do") "doo" else countries[position].toLowerCase(Locale.getDefault())

            val imageKey = context.resources.getIdentifier(url, "drawable", context.packageName)
            flag.setImageResource(imageKey)
            country.text = countries[position]
            city.text = cities[position]
            recyclercity.layoutManager = LinearLayoutManager(context)
            showstations.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            var arrowchange = true
            val privatestation = liststations != null
            numberstation.text = if(privatestation) stations[position] else ""


            city.setOnClickListener {
                val activecities = session.getPref("stations")
                val activeweather = session.getPref("weathers")
                val activeforecasts = session.getPref("forecasts")
                val json = Json(JsonConfiguration.Stable)
                val searchvalue = if(privatestation) countries[position] + "/" + cities[position] else stations[position]

                val station = StationsData(
                    "city",
                    cities[position],
                    timezone[position],
                    searchvalue,
                    cities[position],
                    "",
                    false,
                    privatestation
                )

                val selectedcity = json.stringify(StationsData.serializer(), station) + "|"

                val weather = WeatherData()
                val addweather = json.stringify(WeatherData.serializer(), weather) + "|"

                val forecast = ForecastData()
                val addedforecast = json.stringify(ForecastData.serializer(), forecast) + "|"


                if (activecities.indexOf("\"searchvalue\":\"$searchvalue\"") != -1) {
                    Toast.makeText(context, context.getString(R.string.alreadyadd), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    session.setPref("stations", activecities + selectedcity)
                    session.setPref("weathers", activeweather + addweather)
                    session.setPref("forecasts", activeforecasts + addedforecast)

                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("setweather", activeweather.split("|").size)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            }

            if (privatestation) {
                showstations.setOnClickListener {
                    if (arrowchange) {
                        street.visibility = View.VISIBLE
                        showstations.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
                        recyclercity.visibility = View.VISIBLE
                    } else {
                        showstations.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                        recyclercity.visibility = View.GONE
                        street.visibility = View.GONE
                    }
                    arrowchange = !arrowchange

                    recyclercity.adapter = SearchStreetAdapter(
                        context,
                        liststations!!.getJSONArray(cities[position]),
                        cities[position]
                    )
                }
            }else{
                searchtext.visibility = View.GONE
                showstations.visibility = View.GONE
            }

             showmaps.setOnClickListener {
                    val gmmIntentUri = Uri.parse("geo:${coord[position]}?z=6&q=${coord[position]}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

                 session.setPref("mapstutorial", "true")
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    }
                }
        }
    }
}
