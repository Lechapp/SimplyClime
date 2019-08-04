package pl.simplyinc.simplyclime.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.SuperscriptSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.one_day_row.view.*
import org.json.JSONArray
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.WeatherTools
import pl.simplyinc.simplyclime.network.DayByDayRequest
import java.text.SimpleDateFormat
import java.util.*


class DayByDayAdapter(val context:Context,val weather:MutableList<JSONArray>, val station:String, val recyc:RecyclerView,
                      val forecast:RecyclerView, val pb:ProgressBar, val detail:Boolean): RecyclerView.Adapter<ViewHolder>() {

    lateinit var tunit:String
    lateinit var wunit:String
    lateinit var st:JSONObject

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        st = JSONObject(station)
        wunit = st.getString("windunit")
        tunit = st.getString("tempunit")
        return ViewHolder(layoutInflater.inflate(R.layout.one_day_row, parent, false))
    }

    override fun getItemCount(): Int {
        return weather.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val w = weather[position]
        val tool = WeatherTools()

        val showall = holder.itemView.showmore
        val allair = holder.itemView.dayallairpol
        val allhumi = holder.itemView.dayallhumi
        val allinsol = holder.itemView.dayallinsol
        val allpress = holder.itemView.dayallpress
        val allrain = holder.itemView.dayallrain
        val alltempin = holder.itemView.dayalltempin
        val allwind = holder.itemView.dayallwind
        val tempout = holder.itemView.daytempout
        val tempin = holder.itemView.daytempin
        val humiin = holder.itemView.dayhumiin
        val humiout = holder.itemView.dayhumiout
        val insol = holder.itemView.dayinsol
        val press = holder.itemView.daypress
        val wind = holder.itemView.daywind
        val airpol10 = holder.itemView.dayairpol10
        val airpol25 = holder.itemView.dayairpol25
        val rain = holder.itemView.dayrain
        val date = holder.itemView.dayday
        val icon = holder.itemView.dayicon
        val batticon = holder.itemView.daybatteryimg
        val batt = holder.itemView.daybatterylvl

        val dateformat:String = when(detail) {
            true -> "HH:mm dd.MM"
            else -> "dd.MM"
        }

        val systemtimezone = TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings
        val weatherdate = SimpleDateFormat(dateformat, Locale(Locale.getDefault().displayLanguage))
        date.text = weatherdate.format(Date(w.getInt(11)*1000L-systemtimezone))

        if(w.getString(10) != "null"){
            val img = tool.batterylvl(w.getString(10), true)
            batticon.setImageResource(img)

            val t = tool.roundto(w.getString(10)) + "%"
            batt.text = t
        }else{
            batt.visibility = View.GONE
            batticon.visibility = View.GONE
        }

        if(w.getString(12) != "null"){
            val tempmax = tool.kelvintoTempUnit(w.getString(13), tunit)
            val tempmin = tool.kelvintoTempUnit(w.getString(12), tunit)
            val tempouttt = tool.kelvintoTempUnit(w.getString(0), tunit)

            val t:String = when(detail){
                true -> "$tempouttt $tunit"
                else -> "$tempmin/$tempmax $tunit"
            }

            tempout.text = t
        }else tempout.visibility = View.GONE

        if(w.getString(1) != "null") {
            val t =  tool.kelvintoTempUnit(w.getString(1), tunit) + " $tunit"
            tempin.text = t
        }else alltempin.visibility = View.GONE

        if(w.getString(2) != "null" || w.getString(3) != "null"){

            if(w.getString(2) != "null") {
                val t = context.getString(R.string.out) + " " + tool.roundto(w.getString(2)) + "%"
                humiout.text = t
            }else humiout.visibility = View.GONE

            if(w.getString(3) != "null") {
                val t =  context.getString(R.string.`in`)+ " " + tool.roundto(w.getString(3)) + "%"
                humiin.text = t
            }else humiin.visibility = View.GONE

        }else allhumi.visibility = View.GONE

        if(w.getString(4) != "null") {
            val t = tool.roundto(w.getString(4)) + " HPa"
            press.text = t
        }else allpress.visibility = View.GONE

        if(w.getString(5) != "null") {
            val t = tool.roundto(w.getString(5))+"%"
            rain.text = t
        }else allrain.visibility = View.GONE

        if(w.getString(6) != "null") {
            val windinunit = tool.mstoWindUnit(w.getString(6), wunit)
            val t = "$windinunit $wunit"
            wind.text = t
        }else allwind.visibility = View.GONE

        if(w.getString(7) != "null" || w.getString(8) != "null"){

            if(w.getString(7) != "null") {
                val indexgorny = tool.roundto(w.getString(7)) + " " + context.getString(R.string.pollutionuit)
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(indexgorny)
                builder.setSpan(
                    superscriptSpan,
                    indexgorny.length-1,
                    indexgorny.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                airpol10.text = builder
                val colorid = tool.PM10(w.getString(7))
                airpol10.background = ContextCompat.getDrawable(context,colorid)
            }else airpol10.visibility = View.GONE

            if(w.getString(8) != "null") {

                val colorid = tool.PM25(w.getString(8))
                airpol25.background = ContextCompat.getDrawable(context,colorid)
                val indexgorny = tool.roundto(w.getString(8)) + " " + context.getString(R.string.pollutionuit)

                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(indexgorny)
                builder.setSpan(
                    superscriptSpan,
                    indexgorny.length-1,
                    indexgorny.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                airpol25.text = builder
            }else airpol25.visibility = View.GONE

        }else allair.visibility = View.GONE


        if(w.getString(9) != "null") {
            val t = tool.roundto(w.getString(9)) + "%"
            insol.text = t
        }else allinsol.visibility = View.GONE

        val temp:String = tool.roundto(when(detail){
            true -> w.getString(0)
            else -> w.getString(13)
        })

        val iconimg = tool.weathericon(temp, tool.roundto(w.getString(5)), tool.roundto(w.getString(9)),
            st.getInt("sunrise"),st.getInt("sunset"), st.getInt("timezone"), true)
        icon.setImageResource(iconimg)

        if(!detail) {
            showall.setOnClickListener {
                val dayByDayRequest = DayByDayRequest()
                val dateto = SimpleDateFormat("dd-MM-yyyy", Locale(Locale.getDefault().displayLanguage))
                val dateinformatto = dateto.format(Date(w.getInt(11) * 1000L - systemtimezone))

                dayByDayRequest.getWeather(context, station, recyc, forecast, pb, "0", dateinformatto)
                forecast.visibility = View.GONE
                recyc.animation = AnimationUtils.loadAnimation(context, R.anim.slidein_from_right_to_left)
            }
        }
    }

    fun add(ile:Int){
        notifyItemRangeInserted(weather.size, ile)
    }
}