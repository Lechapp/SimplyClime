package pl.simplyinc.simplyclime.adapters

import kotlinx.android.synthetic.main.street_row.view.*
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.widget.NewestWeatherConfigureActivity

class WidgetChooseAdapter(val context:Context, private val title:List<String>): androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    private lateinit var lastchoice: ConstraintLayout
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.street_row, parent, false))
    }

    override fun getItemCount(): Int {
        return if(title.isEmpty()) 1 else title.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val street = holder.itemView.street
        val container = holder.itemView.cale
        holder.itemView.buldingnumber.visibility = View.GONE

        if(title.isEmpty()){
            street.text = context.getString(R.string.emptyplaces)
        }else{

            street.text = title[position]
            street.setOnClickListener {
                val act = street.context as NewestWeatherConfigureActivity
                act.choosedPlace = position
                val colorValue = ContextCompat.getColor(context, R.color.colorPrimaryaplha)
                container.setBackgroundColor(colorValue)

                if(::lastchoice.isInitialized && container != lastchoice) {
                    val noneColor = ContextCompat.getColor(context, R.color.none)
                    lastchoice.setBackgroundColor(noneColor)
                }
                lastchoice = container
            }
        }
    }


}

