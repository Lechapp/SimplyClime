package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable

@Serializable
data class StationsData(val type:String = "", val city:String = "", val timezone:Int,val searchvalue:String = "",
                        val title:String = "", val apikey:String="", val gps:Boolean = false, val privstation:Boolean = true,
                        val tempunit:String = "°C", val windunit:String = "km/h", val refreshtime:Int = 0, val sunset:Int = 0,
                        val sunrise:Int = 0, val lat:Double=0.0, val lon:Double = 0.0, val tempunitsave:String = "",
                        val windunitsave:String = "", val ecowitt:Boolean = false, val title1:String = "", val title2:String = "", val title3:String = "",
                        val title4:String = "", val title5:String = "", val title6:String = "", val title7:String = "",
                        val title8:String = "")
