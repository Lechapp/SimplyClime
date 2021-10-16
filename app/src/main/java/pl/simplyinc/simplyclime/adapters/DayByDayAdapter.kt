package pl.simplyinc.simplyclime.adapters

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.SuperscriptSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.one_day_row.view.*
import org.json.JSONArray
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.WeatherTools
import pl.simplyinc.simplyclime.network.DayByDayRequest
import java.text.SimpleDateFormat
import java.util.*


class DayByDayAdapter(val context:Context, val weather:MutableList<JSONArray>, val station:String, private val recyc: androidx.recyclerview.widget.RecyclerView,
                      val forecast: RecyclerView, private val pb:ProgressBar, private val detail:Boolean, val onlyforeacst:Boolean = false,
                      val sunset:Int = 0, val sunrise: Int = 0, val timezone: Int = 0): RecyclerView.Adapter<ViewHolder>() {

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

        val w = if(onlyforeacst) {
                weather[weather.size-position-1]
            }else weather[position]

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
        val unitGrid = holder.itemView.unitGridd
        val additionalTemp = holder.itemView.additionalTempp

        val title = listOf<TextView>(
            holder.itemView.titlee1,
            holder.itemView.titlee2,
            holder.itemView.titlee3,
            holder.itemView.titlee4,
            holder.itemView.titlee5,
            holder.itemView.titlee6,
            holder.itemView.titlee7,
            holder.itemView.titlee8
        )
        val humi = listOf<TextView>(
            holder.itemView.humm1,
            holder.itemView.humm2,
            holder.itemView.humm3,
            holder.itemView.humm4,
            holder.itemView.humm5,
            holder.itemView.humm6,
            holder.itemView.humm7,
            holder.itemView.humm8
        )
        val tempp = listOf<TextView>(
            holder.itemView.tempp1,
            holder.itemView.tempp2,
            holder.itemView.tempp3,
            holder.itemView.tempp4,
            holder.itemView.tempp5,
            holder.itemView.tempp6,
            holder.itemView.tempp7,
            holder.itemView.tempp8
        )



        val dateformat:String = when(detail) {
            true -> "HH:mm dd.MM"
            else -> "dd.MM"
        }

        val weatherdate = SimpleDateFormat(dateformat, Locale(Locale.getDefault().displayLanguage))
        weatherdate.timeZone = TimeZone.getTimeZone("GMT")

        val dayinweek = SimpleDateFormat("EEEE", Locale(Locale.getDefault().displayLanguage))
        dayinweek.timeZone = TimeZone.getTimeZone("GMT")
        val dayOfWeek = dayinweek.format(Date(w.getInt(11)*1000L))

        /*val dayOfWeek = when (day) {
            "1" -> context.getString(R.string.fmonday)
            "2" -> context.getString(R.string.ftuesday)
            "3" -> context.getString(R.string.fwednesday)
            "4" -> context.getString(R.string.fthursday)
            "5" -> context.getString(R.string.ffriday)
            "6" -> context.getString(R.string.fsaturday)
            "7" -> context.getString(R.string.fsunday)
            else -> context.getString(R.string.nextday)
        }*/
        val alldate = "${weatherdate.format(Date(w.getInt(11)*1000L))}, $dayOfWeek"
        date.text = alldate

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
            val tempmax = tool.roundto(tool.kelvintoTempUnit(w.getString(13), tunit))
            val tempmin = tool.roundto(tool.kelvintoTempUnit(w.getString(12), tunit))
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

            var out = ""
            if(w.getString(3) != "null") {
                val t =  context.getString(R.string.`in`)+ " " + tool.roundto(w.getString(3)) + "%"
                humiin.text = t
                out = context.getString(R.string.out)
            }else humiin.visibility = View.GONE

            if(w.getString(2) != "null") {
                val t = out + " " + tool.roundto(w.getString(2)) + "%"
                humiout.text = t
            }else humiout.visibility = View.GONE



        }else allhumi.visibility = View.GONE

        if(w.getString(4) != "null") {
            val t = tool.roundto(w.getString(4)) + " hPa"
            press.text = t
        }else allpress.visibility = View.GONE

        if(w.getString(5) != "null") {
            val unit = if(st.has("ecowitt") && st.getBoolean("ecowitt")){
                " mm"
            }else{
                "%"
            }
            val t = tool.roundto(w.getString(5)) + unit
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
                val colorid = tool.pm10(w.getString(7))
                airpol10.background = ContextCompat.getDrawable(context,colorid)
            }else airpol10.visibility = View.GONE

            if(w.getString(8) != "null") {

                val colorid = tool.pm25(w.getString(8))
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
            insol.text = if(st.has("ecowitt") && st.getBoolean("ecowitt")){
                val pom = tool.roundto(w.getString(9)) + " W/m2"
                val superscriptSpan = SuperscriptSpan()
                val builder = SpannableStringBuilder(pom)
                builder.setSpan(
                    superscriptSpan,
                    pom.length-1,
                    pom.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                builder
            }else{
                tool.roundto(w.getString(9)) + "%"
            }

        }else allinsol.visibility = View.GONE

        val temp:String = tool.roundto(when(detail){
            true -> w.getString(0)
            else -> w.getString(13)
        })

        val iconimg = if(onlyforeacst){
            tool.weatherIconOpenWeather(
                w.getString(14),
                w.getString(15),
                sunrise,sunset,timezone,
                true, w.getString(5),
                w.getInt(11))
        } else {
            tool.weathericon(
                temp, tool.roundto(w.getString(5)), tool.roundto(w.getString(9)),
                sunrise, sunset, timezone, st.getBoolean("ecowitt"), true
            )
        }


        //15-22 temp i+14
        //23-30 humidity i+22

        var anyone = false

        if(st.getBoolean("privstation")) {
            for (i in 1..8) {
                if ((w.getString(i + 22) != "null" && w.getString(i + 22) != "")
                    || (w.getString(i + 14) != "null") && w.getString(i + 14) != ""
                ) {

                    unitGrid.text = st.getString("tempunit")
                    anyone = true
                    if (st.getString("title${i}") != "") {
                        title[i - 1].text = st.getString("title${i}")

                    } else title[i - 1].text = "${context.getString(R.string.room)}${i}"

                    if (w.getString(i + 22) != "null") {
                        humi[i - 1].text = tool.roundto(w.getString(i + 22))

                    } else humi[i - 1].visibility = View.GONE

                    if (w.getString(i + 14) != "null") {
                        tempp[i - 1].text = tool.kelvintoTempUnit(
                                w.getString(i + 14),
                                st.getString("tempunit")
                                )

                    } else tempp[i - 1].visibility = View.GONE

                } else {
                    tempp[i - 1].visibility = View.GONE
                    humi[i - 1].visibility = View.GONE
                    title[i - 1].visibility = View.GONE
                }
            }
        }
        if(anyone){
            additionalTemp.visibility = View.VISIBLE
        }

        icon.setImageResource(iconimg)

        if(!detail) {
            showall.setOnClickListener {
                val dayByDayRequest = DayByDayRequest()
                val dateto = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                dateto.timeZone = TimeZone.getTimeZone("GMT")
                val dateinformatto = dateto.format(Date(w.getInt(11) * 1000L))

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