package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable

@Serializable
data class OneDayForecast(val tempmin:Int = 0, val tempmax:Int = 0, val weathericon:Int = 0, val weathericonblack:Int = 0, val time:Int = 0)
