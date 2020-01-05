package pl.simplyinc.simplyclime.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_weather_station.*

import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.AddStationActivity
import pl.simplyinc.simplyclime.activities.LogInActivity
import pl.simplyinc.simplyclime.elements.SessionPref
import java.util.*


class AddWeatherStation : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_weather_station, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val sessionPref = SessionPref(context!!)

        howworks.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.simplyclime.pl?${Locale.getDefault().language}")
            startActivity(openURL)
        }

        addstation.setOnClickListener {
            val addstation: Intent
            if(sessionPref.getPref("token") == "") {
                addstation = Intent(context, LogInActivity::class.java)
                addstation.putExtra("fromadd",true)
            }else{
                addstation = Intent(context, AddStationActivity::class.java)
            }
            startActivity(addstation)
        }
    }
}
