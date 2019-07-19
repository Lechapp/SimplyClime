package pl.simplyinc.simplyclime.elements

import pl.simplyinc.simplyclime.R
import java.lang.Exception
import kotlin.math.roundToInt

class WeatherTools {


    fun weathericon(tempout:String, rainfall:String, insolation:String, sunrise:Int, sunset:Int, weathertime:Int, black:Boolean):Int{

        var weathericon = 0
        var rain = -1
        try{
            rain = rainfall.toInt()
        }catch (e: Exception){}

        var temp = -1
        try{
            temp = tempout.toInt()
        }catch (e: Exception){}

        var insol = -1
        try{
            insol = insolation.toInt()
        }catch (e: Exception){}


        if(insol != -1) {

            if(rain > 10){
                if(weathertime in sunrise..sunset || sunset == 0) {

                    if(insol > 30){

                        if (temp != -1 && temp < 273){
                            weathericon = when(black){
                                true->R.drawable.cloud_sun_snow_w
                                false->R.drawable.cloud_sun_snow_b
                            }
                        }else{
                            weathericon = when(black){
                                true->R.drawable.cloud_sun_rain_w
                                false->R.drawable.cloud_sun_rain_b
                            }
                        }
                    }else{
                        if (temp != -1 && temp < 273){
                            weathericon = when(black){
                                true->R.drawable.cloud_snow_w
                                false->R.drawable.cloud_snow_b
                            }
                        }else{
                            weathericon = when(black){
                                true->R.drawable.cloud_rain_w
                                false->R.drawable.cloud_rain_b
                            }
                        }
                    }

                }else{
                    if (temp != -1 && temp < 273)
                        weathericon = when(black){
                            true->R.drawable.cloud_moon_snow_w
                            false->R.drawable.cloud_moon_snow_b
                        }
                    else weathericon = when(black){
                        true->R.drawable.cloud_moon_rain_w
                        false->R.drawable.cloud_moon_rain_b
                    }
                }

            }else{

                if(weathertime in sunrise..sunset || sunset == 0) {

                    if(insol > 70){
                        weathericon = when(black){
                            true->R.drawable.sun_w
                            false->R.drawable.sun_b
                        }
                    }else if(insol in 30..70){
                        weathericon = when(black){
                            true->R.drawable.cloud_sun_w
                            false->R.drawable.cloud_sun_b
                        }
                    }else weathericon = when(black){
                        true->R.drawable.cloud_w
                        false->R.drawable.cloud_b
                    }

                }else{
                    weathericon = when(black){
                        true->R.drawable.moon_w
                        false->R.drawable.moon_b
                    }
                }
            }

        }else{
            if(rain != -1){
                if(rain > 10){
                    if(weathertime in sunrise..sunset || sunset == 0) {
                        if (temp != -1 && temp < 273)
                            weathericon = when(black){
                                true->R.drawable.cloud_snow_w
                                false->R.drawable.cloud_snow_b
                            }
                        else weathericon = when(black){
                                true->R.drawable.cloud_rain_w
                                false->R.drawable.cloud_rain_b
                            }
                    }else{
                        if (temp != -1 && temp < 273)
                            weathericon = when(black){
                                true->R.drawable.cloud_moon_snow_w
                                false->R.drawable.cloud_moon_snow_b
                            }
                        else
                            weathericon = when(black){
                                true->R.drawable.cloud_moon_rain_w
                                false->R.drawable.cloud_moon_rain_b
                            }
                    }
                }else{

                    if(weathertime in sunrise..sunset || sunset == 0) {
                        weathericon = when(black){
                            true->R.drawable.sun_w
                            false->R.drawable.sun_b
                        }
                    }else{
                        weathericon = when(black){
                            true->R.drawable.moon_w
                            false->R.drawable.moon_b
                        }
                    }
                }
            }

        }
        return weathericon
    }

    fun PM10(value:String):Int{

        var airpollution10 = 0

        try {
            airpollution10 = value.toInt()
        }catch (e: Exception){}

        return when(airpollution10){
            in 0..20 -> R.color.verygood
            in 21..60 -> R.color.good
            in 60..100 -> R.color.moderate
            in 101..140 -> R.color.sufficient
            in 141..200 -> R.color.bad
            in 200..99999 -> R.color.verybad
            else -> R.color.none
        }
    }

    fun PM25(value:String):Int{

        var airpollution25 = 0

        try {
            airpollution25 = value.toInt()
        }catch (e: Exception){}

        return when(airpollution25){
            in 0..12 -> R.color.verygood
            in 13..36 -> R.color.good
            in 37..60 -> R.color.moderate
            in 61..84 -> R.color.sufficient
            in 85..120 -> R.color.bad
            in 121..99999 -> R.color.verybad
            else -> R.color.none
        }
    }

    fun roundto(value:String):String{
        if(value == "null")
            return "null"

        var valu = "null"
        try{
            val w = value.toDouble()
            valu = ((w*10).roundToInt()/10).toString()

        }catch (e: Exception){}
        return valu
    }

    fun kelvintoTempUnit(number:String,tempunit:String):String{

        val numb:Double

        if(number == "null")
            return "null"
        else {
            try {
                numb = number.toDouble()
                val temp = when (tempunit) {
                    "°F" -> (numb * 1.8) - 459.67
                    "°C" -> (numb - 273.15)
                    else -> numb
                }

                return ((temp * 10).roundToInt() / 10).toString()
            }catch (e: Exception){ }

            return "null"
        }
    }

    fun mstoWindUnit(number:String, windunit: String):String{

        val numb:Double
        if(number == "null")
            return "null"
        else {
            try {
                numb = number.toDouble()
                val windspeed = when (windunit) {
                    "km/h" -> (numb * 3.6)
                    "mph" -> (numb * (3600 / 1609.344))
                    else -> numb
                }

                return ((windspeed * 10).roundToInt() / 10).toString()
            }catch (e: Exception){}
        }

        return "null"
    }

    fun getTempImgId(temp:String, black:Boolean):Int{

        var number = -1
        try {
            number = temp.toInt()
        }catch (e: Exception){}

        return when(black){
            true -> when(number){
                in 309..2001 -> R.drawable.temp4_w
                in 293..309 -> R.drawable.temp3_w
                in 280..293 -> R.drawable.temp2_w
                in 265..280 -> R.drawable.temp1_w
                in 0..265 -> R.drawable.temp0_w
                else -> R.drawable.temp2_w
            }
            false -> when(number){
                    in 309..2001 -> R.drawable.temp4_b
                    in 293..309 -> R.drawable.temp3_b
                    in 280..293 -> R.drawable.temp2_b
                    in 265..280 -> R.drawable.temp1_b
                    in 0..265 -> R.drawable.temp0_b
                    else -> R.drawable.temp2_b
                }
        }
    }

    fun batterylvl(batlvl:String, blackbg:Boolean):Int{
        var battery = R.drawable.ic_battery_50_black_24dp
        try {
            battery = batlvl.toInt()
        }catch (e:Exception){}

        return when(blackbg) {
            true -> when (battery) {
                in 85..100 -> R.drawable.ic_battery_90_white_24dp
                in 68..85 -> R.drawable.ic_battery_80_white_24dp
                in 55..68 -> R.drawable.ic_battery_60_white_24dp
                in 40..55 -> R.drawable.ic_battery_50_white_24dp
                in 28..40 -> R.drawable.ic_battery_30_white_24dp
                in 0..28 -> R.drawable.ic_battery_20_white_24dp
                else -> R.drawable.ic_battery_50_white_24dp
            }
            false -> when (battery) {
                in 85..100 -> R.drawable.ic_battery_90_black_24dp
                in 68..85 -> R.drawable.ic_battery_80_black_24dp
                in 55..68 -> R.drawable.ic_battery_60_black_24dp
                in 40..55 -> R.drawable.ic_battery_50_black_24dp
                in 28..40 -> R.drawable.ic_battery_30_black_24dp
                in 0..28 -> R.drawable.ic_battery_20_black_24dp
                else -> R.drawable.ic_battery_50_black_24dp
            }
        }
    }
}