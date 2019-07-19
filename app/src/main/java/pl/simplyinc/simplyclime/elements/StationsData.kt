package pl.simplyinc.simplyclime.elements

import kotlinx.serialization.Serializable

@Serializable
data class StationsData(val type:String = "", val city:String = "", val timezone:Int,val searchvalue:String = "",
                        val title:String = "", val tempunit:String = "°C", val windunit:String = "km/h",
                        val refreshtime:Int = 0, val sunset:Int = 0, val sunrise:Int = 0)
//&#47; - front slash /
/*
@Serializable
data class Data(val a: Int, val b: Int)
val json = Json(JsonConfiguration.Stable)
val jsonData = json.stringify(Data.serializer(), Data(42, 29))
*/