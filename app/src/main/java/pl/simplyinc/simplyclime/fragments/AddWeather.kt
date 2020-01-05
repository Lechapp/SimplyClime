package pl.simplyinc.simplyclime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_add_weather.*
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.adapters.AddWeatherPagerAdapter


class AddWeather : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_weather, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val pagerAdapter = AddWeatherPagerAdapter(childFragmentManager)
        pagerAdd.adapter = pagerAdapter
        dots_indicator.setViewPager(pagerAdd)

    }
}
