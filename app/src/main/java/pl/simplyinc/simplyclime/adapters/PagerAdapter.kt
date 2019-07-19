package pl.simplyinc.simplyclime.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.fragments.AddWeather
import pl.simplyinc.simplyclime.fragments.Weatherinfo

class PagerAdapter(fm:FragmentManager, val context:Context) :FragmentStatePagerAdapter(fm) {
    //stations mystations cities
    val session = SessionPref(context)
    var stations = session.getPref("stations").split("|")

    override fun getItem(position: Int): Fragment {
        return when(position){
            in 0 until (stations.size-1) -> Weatherinfo.newInstance(position)
            else -> AddWeather()
        }
    }

    override fun getCount(): Int {
    return (stations.size - 1) + 1
    }

    fun changeData(){
        stations = session.getPref("stations").split("|")
        notifyDataSetChanged()
    }
}