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


class SearchWeatherAdapter(val cities: MutableList<String>, val countries: MutableList<String>, val stations: MutableList<String>,
                           val timezone:MutableList<Int>, val context:Context, val liststations:JSONObject): RecyclerView.Adapter<ViewHolder>() {

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
        val flag = holder.itemView.flag
        val street = holder.itemView.street
        val showstations = holder.itemView.showstations
        val recyclercity = holder.itemView.citystations

        street.visibility = View.GONE

        if(cities.size == 0){
            country.visibility = View.GONE
            numberstation.visibility = View.GONE
            searchtext.visibility = View.GONE
            city.text = context.getString(R.string.emptysearch)
        }else{
            val url = "drawable/"+if(countries[position] == "do") "doo" else countries[position].toLowerCase()
            val imageKey = context.resources.getIdentifier(url, "drawable", context.packageName)
            flag.setImageResource(imageKey)
            country.text = countries[position]
            city.text = cities[position]
            numberstation.text = stations[position]
            recyclercity.layoutManager = LinearLayoutManager(context)
            showstations.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            var arrowchange = true

            city.setOnClickListener {
                val activecities = session.getPref("stations")
                val activeweather = session.getPref("weathers")
                val activeforecasts = session.getPref("forecasts")
                val json = Json(JsonConfiguration.Stable)

                val station = StationsData("city", cities[position],timezone[position],
                    countries[position] + "/" + cities[position], cities[position])

                val selectedcity = json.stringify(StationsData.serializer(), station) + "|"

                val weather = WeatherData()
                val addweather = json.stringify(WeatherData.serializer(), weather) + "|"

                val forecast = ForecastData()
                val addedforecast = json.stringify(ForecastData.serializer(), forecast) + "|"

                if(activecities.indexOf("\"searchvalue\":\""+countries[position] + "/" + cities[position]+"\"") != -1) {
                    Toast.makeText(context, context.getString(R.string.alreadyadd), Toast.LENGTH_SHORT).show()
                }else {
                    session.setPref("stations", activecities + selectedcity)
                    session.setPref("weathers", activeweather + addweather)
                    session.setPref("forecasts", activeforecasts + addedforecast)

                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("station", true)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            }

            showstations.setOnClickListener {
                if(arrowchange) {
                    street.visibility = View.VISIBLE
                    showstations.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
                    recyclercity.visibility = View.VISIBLE
                }
                else {
                    showstations.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                    recyclercity.visibility = View.GONE
                    street.visibility = View.GONE
                }
                arrowchange = !arrowchange

                recyclercity.adapter = SearchStreetAdapter(context, liststations.getJSONArray(cities[position]), cities[position])
            }
        }
    }
}
