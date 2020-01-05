package pl.simplyinc.simplyclime.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import pl.simplyinc.simplyclime.fragments.AddLocation
import pl.simplyinc.simplyclime.fragments.AddWeatherStation

class AddWeatherPagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ) {


    override fun getItem(position: Int): Fragment {


        return when(position){
            0 -> AddLocation()
            else -> AddWeatherStation()
        }
    }

    override fun getCount(): Int {
        return 2
    }

}