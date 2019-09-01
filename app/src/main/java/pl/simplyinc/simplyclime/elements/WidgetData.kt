package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable
import pl.simplyinc.simplyclime.R

@Serializable
data class WidgetData(val id:Int, val background:Int = R.color.whitealpha, val blackbg:Boolean = false, val tempout:Boolean = true, val tempin:Boolean = true,
                      val humidityout:Boolean = true, val humidityin:Boolean = true, val pressure:Boolean = true,
                      val rainfall:Boolean = true, val windspeed:Boolean = true, val airpollution10:Boolean = true,
                      val airpollution25:Boolean = true, val insolation:Boolean = true, val icon:Boolean = true)