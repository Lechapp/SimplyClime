package pl.simplyinc.simplyclime.adapters

import kotlinx.android.synthetic.main.street_row.view.*
import org.json.JSONArray
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.elements.SessionPref
import javax.sql.StatementEvent


class SearchStreetAdapter(val context:Context, val liststations:JSONArray, val city:String): RecyclerView.Adapter<ViewH>() {

    private val session = SessionPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewH {
        val layoutInflater = LayoutInflater.from(context)
        return ViewH(layoutInflater.inflate(R.layout.street_row, parent, false))
    }

    override fun getItemCount(): Int {
        return liststations.length()
    }

    override fun onBindViewHolder(holder: ViewH, position: Int) {
        val street = holder.itemView.street
        val bulding = holder.itemView.buldingnumber

        val dataArray = liststations.getJSONArray(position)
        street.text = dataArray.getString(1)
        bulding.text = dataArray.getString(2)
        street.setOnClickListener {
            val activestations = session.getPref("stations")
            val selectedstreet = city + "/" + dataArray.getString(1)+ "/" +dataArray.getString(0) + ","
            if(activestations.indexOf(selectedstreet) != -1){
                Toast.makeText(context, context.getString(R.string.alreadyadd), Toast.LENGTH_SHORT).show()
            }else {
                session.setPref("stations", activestations + selectedstreet)
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("stations", true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
        }
    }

class ViewH(view: View):RecyclerView.ViewHolder(view)