package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable
import pl.simplyinc.simplyclime.R

@Serializable
data class WeatherData(val tempout:String = "", val tempin:String = "", val humidityout:String = "", val humidityin:String = "",
                       val pressure:String = "", val rainfall:String = "", val windspeed:String = "",
                       val airpollution10:String = "", val airpollution25: String = "", val insolation:String = "",
                       val batterylvl:String = "", val time:Int = 0, val updatedtime:Int = 0, val tempimg:Int = R.drawable.temp2_w,
                       val description:String = "", val main:String = "")