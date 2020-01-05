package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable
import pl.simplyinc.simplyclime.R

@Serializable
data class WeatherData(val tempout:String = "", val tempin:String = "", val humidityout:String = "", val humidityin:String = "",
                       val pressure:String = "", val rainfall:String = "", val windspeed:String = "",
                       val airpollution10:String = "", val airpollution25: String = "", val insolation:String = "",
                       val batterylvl:String = "", val time:Int = 0, val updatedtime:Int = 0, val tempimg:Int = R.drawable.temp2_w,
                       val description:String = "", val main:String = "",  val temp1:String = "",  val temp2:String = "",
                       val temp3:String = "", val temp4:String = "", val temp5:String = "", val temp6:String = "",
                       val temp7:String = "", val temp8:String = "", val humidity1:String = "", val humidity2:String = "",
                       val humidity3:String = "",val humidity4:String = "",val humidity5:String = "",val humidity6:String = "",
                       val humidity7:String = "", val humidity8:String = "")