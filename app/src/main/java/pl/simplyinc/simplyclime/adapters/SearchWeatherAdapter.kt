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
import org.json.JSONObject
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.elements.SessionPref


class SearchWeatherAdapter(val cities: MutableList<String>, val countries: MutableList<String>, val stations: MutableList<String>,val context:Context, val liststations:JSONObject): RecyclerView.Adapter<VH>() {

    private val session = SessionPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): VH {
        val layoutInflater = LayoutInflater.from(context)
        return VH(layoutInflater.inflate(R.layout.search_row, parent, false))
    }

    override fun getItemCount(): Int {
        return if (cities.size == 0) 1 else cities.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
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
                val activecities = session.getPref("cities")
                val selectedcity =  countries[position] + "/" + cities[position] + ","
                if(activecities.indexOf(selectedcity) != -1) {
                    Toast.makeText(context, context.getString(R.string.alreadyadd), Toast.LENGTH_SHORT).show()
                }else {
                    session.setPref("cities", activecities + selectedcity)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("cities", true)
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
class VH(view: View):RecyclerView.ViewHolder(view)