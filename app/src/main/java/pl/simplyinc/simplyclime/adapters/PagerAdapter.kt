package pl.simplyinc.simplyclime.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.fragments.AddWeather
import pl.simplyinc.simplyclime.fragments.weatherinfo

class PagerAdapter(fm:FragmentManager, val context:Context) :FragmentStatePagerAdapter(fm) {
    //stations mystations cities
    val session = SessionPref(context)
    val cities = session.getPref("cities").split(",")
    val streets = session.getPref("stations").split(",")

    override fun getItem(position: Int): Fragment {
        return when(position){
            in 0 until (cities.size+streets.size-2) -> weatherinfo()
            else -> AddWeather()
        }
    }

    override fun getCount(): Int {
    return (cities.size+streets.size-2) + 1
    }
}