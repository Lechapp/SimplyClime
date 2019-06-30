package pl.simplyinc.simplyclime.adapters

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.name_row.view.*
import pl.simplyinc.simplyclime.R

class StationNameAdapter(val items: ArrayList<String>, val context:Context, val viewPager:ViewPager): RecyclerView.Adapter<ViewHolder>() {

    var activeborder = 0

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return ViewHolder(layoutInflater.inflate(R.layout.name_row, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = holder.itemView.name
        val border = holder.itemView.bottomline

        if(position == activeborder)
            border.visibility = View.VISIBLE
        else
            border.visibility = View.INVISIBLE

        name.text = items[position]

        name.setOnClickListener {
            viewPager.setCurrentItem(position,true)
        }
    }

    fun setActive(position:Int){

        activeborder = position
        notifyDataSetChanged()
    }
}
class ViewHolder(view: View):RecyclerView.ViewHolder(view)