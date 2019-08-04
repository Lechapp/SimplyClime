package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable

@Serializable
data class ForecastData(val time:Int = 0, val day0:String = "", val day1:String = "",val day2:String = "",val day3:String = "", val day4:String = "")
//day JSON OBJECT: tempmin, tempmax, iconid