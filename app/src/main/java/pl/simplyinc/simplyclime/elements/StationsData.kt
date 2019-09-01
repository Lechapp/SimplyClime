package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable

@Serializable
data class StationsData(val type:String = "", val city:String = "", val timezone:Int,val searchvalue:String = "",
                        val title:String = "", val apikey:String="", val gps:Boolean = false, val privstation:Boolean = true,
                        val tempunit:String = "°C", val windunit:String = "km/h", val refreshtime:Int = 0, val sunset:Int = 0,
                        val sunrise:Int = 0, val lat:Double=0.0, val lon:Double = 0.0)
