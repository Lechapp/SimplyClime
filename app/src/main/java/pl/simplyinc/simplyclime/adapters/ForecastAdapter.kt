package pl.simplyinc.simplyclime.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.forecast_row.view.*
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import java.text.SimpleDateFormat
import java.util.*

class ForecastAdapter(val context: Context, val forecast:JSONObject, val tempunit:String):RecyclerView.Adapter<ViewHolder>() {

    val time = forecast.getInt("time")
    val weatherdate = SimpleDateFormat("u|dd", Locale(Locale.getDefault().displayLanguage))
    val day = weatherdate.format(Date(time*1000L)).split("|")
    var dayofWeek = (day[0]).toInt()
    var dayofMonth = (day[1]).toInt()
    val lastday = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

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

        val dayofWeekText:String = when(dayofWeek){
            1 -> context.getString(R.string.monday)
            2 -> context.getString(R.string.tuesday)
            3 -> context.getString(R.string.wednesday)
            4 -> context.getString(R.string.thursday)
            5 -> context.getString(R.string.friday)
            6 -> context.getString(R.string.saturday)
            7 -> context.getString(R.string.sunday)
            else -> ""
        }

        nameday.text = "$dayofWeekText $dayofMonth"
        dayofWeek++
        dayofMonth++

        if(dayofMonth == (lastday+1))
            dayofMonth = 1

        if(dayofWeek == 8)
            dayofWeek = 1

        try {
            val dayweather = JSONObject(forecast.getString("day$position"))
            iconweather.setImageResource(dayweather.getInt("weathericon"))

            val tempconvert = dayweather.getString("tempmax") + "/" + dayweather.getString("tempmin") + " $tempunit"
            temp.text = tempconvert

        }catch (e:Exception){
            container.visibility = View.GONE
        }
    }
}