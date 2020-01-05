package pl.simplyinc.simplyclime.adapters

import kotlinx.android.synthetic.main.street_row.view.*
import org.json.JSONArray
import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.elements.ForecastData
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.elements.StationsData
import pl.simplyinc.simplyclime.elements.WeatherData


class SearchStreetAdapter(val context:Context,private val liststations:JSONArray, val city:String): androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    private val session = SessionPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.street_row, parent, false))
    }

    override fun getItemCount(): Int {
        return liststations.length()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val street = holder.itemView.street
        val bulding = holder.itemView.buldingnumber

        val dataArray = liststations.getJSONArray(position)
        street.text = dataArray.getString(1)
        bulding.text = dataArray.getString(2)
        street.setOnClickListener {
            val activestations = session.getPref("stations")
            val activeweathers = session.getPref("weathers")
            val activeforecasts = session.getPref("forecasts")

            val json = Json(JsonConfiguration.Stable)

            val station = StationsData("station", city, dataArray.getInt(3),dataArray.getString(0),
                city + " " + dataArray.getString(1), "", false, true, "°C",
                "km/h", 0,0,0,0.0, 0.0, "", "",
                dataArray.getBoolean(4))
            val selectedcity = json.stringify(StationsData.serializer(), station) + "|"

            val weather = WeatherData()
            val addweather = json.stringify(WeatherData.serializer(), weather) + "|"

            val forecast = ForecastData()
            val addedforecast = json.stringify(ForecastData.serializer(), forecast) + "|"

            if(activestations.indexOf("\"searchvalue\":\""+dataArray.getString(0)+"\"") != -1){
                Toast.makeText(context, context.getString(R.string.alreadyadd), Toast.LENGTH_SHORT).show()
            }else {
                session.setPref("stations", activestations + selectedcity)
                session.setPref("weathers", activeweathers + addweather)
                session.setPref("forecasts", activeforecasts + addedforecast)

                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("setweather", activeweathers.split("|").size-1)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
        }
    }