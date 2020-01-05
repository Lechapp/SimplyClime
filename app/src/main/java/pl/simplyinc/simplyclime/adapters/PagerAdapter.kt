package pl.simplyinc.simplyclime.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.fragments.AddWeather
import pl.simplyinc.simplyclime.fragments.Weatherinfo

class PagerAdapter(fm: FragmentManager, val context:Context) :
    FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ) {
    val session = SessionPref(context)
    var stations = session.getPref("stations").split("|")
    var frags = arrayOfNulls<Weatherinfo>(stations.size+1)

    override fun getItem(position: Int): Fragment {

        if(frags[position] == null) {
            frags[position] = Weatherinfo.newInstance(position)
        }

        return when(position){
            in 0 until (stations.size-1) -> frags[position]!!
            else -> AddWeather()
        }
    }

    override fun getCount(): Int {
    return stations.size
    }

    fun changeData(){
        stations = session.getPref("stations").split("|")
        frags = arrayOfNulls(stations.size-1)
        notifyDataSetChanged()
    }

    fun hidedaylist(whichone:Int):Boolean{
        return (frags[whichone] != null && frags[whichone]!!.hideDayByDay())
    }
}