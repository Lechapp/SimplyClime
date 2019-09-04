package pl.simplyinc.simplyclime.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_weather.*
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.*
import pl.simplyinc.simplyclime.elements.OnSwipeTouchListener
import pl.simplyinc.simplyclime.elements.SessionPref

class AddWeather : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_weather, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val sessionPref = SessionPref(context!!)

        addcity.setOnClickListener {
            val addcity = Intent(context, SearchWeatherActivity::class.java)
            startActivity(addcity)
        }

        howworks.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://$server/public")
            startActivity(openURL)
        }

        addstation.setOnClickListener {
            val addstation:Intent
            if(sessionPref.getPref("token") == "") {
                addstation = Intent(context, LogInActivity::class.java)
                addstation.putExtra("fromadd",true)
            }else{
                addstation = Intent(context,AddStationActivity::class.java)
            }
            startActivity(addstation)
        }

    }
}
