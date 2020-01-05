package pl.simplyinc.simplyclime.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.forecast_row.view.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.WeatherTools
import java.text.SimpleDateFormat
import java.util.*

class ForecastAdapter(val context: Context, val forecast:JSONObject, val tempunit:String):
    RecyclerView.Adapter<ViewHolder>() {

    var time = forecast.getInt("time")

    private val weatherdate = SimpleDateFormat("dd", Locale(Locale.getDefault().displayLanguage))
    private val weatherDay = SimpleDateFormat("EEEE", Locale(Locale.getDefault().displayLanguage))


    private val lastday = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
    val tool = WeatherTools()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.forecast_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nameday = holder.itemView.forecastday
        val iconweather = holder.itemView.forecastimg
        val temp = holder.itemView.forecasttemp
        val container = holder.itemView.daycontainer

        val dayofWeek = weatherDay.format( Date(time*1000L)).substring(0,3)
        val dayofMonth = weatherdate.format( Date(time*1000L)).toInt()

        nameday.text = "$dayofWeek $dayofMonth"
        time += (24*3600)

        try {
            val dayweather = JSONObject(forecast.getString("day$position"))
            iconweather.setImageResource(dayweather.getInt("weathericon"))

            val tempconvert = tool.roundto(tool.kelvintoTempUnit(dayweather.getString("tempmax"), tempunit)) + "/" +
                    tool.roundto(tool.kelvintoTempUnit(dayweather.getString("tempmin"), tempunit)) + " $tempunit"
            temp.text = tempconvert

        }catch (e:Exception){
            container.visibility = View.GONE
        }
    }
}