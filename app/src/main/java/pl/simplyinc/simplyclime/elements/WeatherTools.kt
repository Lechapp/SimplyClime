package pl.simplyinc.simplyclime.elements

import android.util.Log
import pl.simplyinc.simplyclime.R
import java.lang.Exception
import java.util.*
import kotlin.math.roundToInt

class WeatherTools {


    fun weathericon(tempout:String, rainfall:String, insolation:String, sunrise:Int, sunset:Int, timezone:Int, black:Boolean):Int{

        var weathericon = when (black) {
            true -> R.drawable.cloud_sun_w
            false -> R.drawable.cloud_sun_b
        }
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

        val systemtime = System.currentTimeMillis()/1000L
        val systemtimezone = (TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings)/1000L
        val weathertimetoday = systemtime - systemtimezone + timezone

        if(insol != -1) {

            if(rain > 10){

                if(rain > 65){

                    if (weathertimetoday in sunrise..sunset || sunset == 0) {

                        if (insol > 30) {

                            weathericon = if (temp != -1 && temp < 273) {
                                when (black) {
                                    true -> R.drawable.cloud_sun_snow_w
                                    false -> R.drawable.cloud_sun_snow_b
                                }
                            } else {
                                when (black) {
                                    true -> R.drawable.cloud_sun_rain_w
                                    false -> R.drawable.cloud_sun_rain_b
                                }
                            }
                        } else {
                            weathericon = if (temp != -1 && temp < 273) {
                                when (black) {
                                    true -> R.drawable.cloud_snow_w
                                    false -> R.drawable.cloud_snow_b
                                }
                            } else {
                                when (black) {
                                    true -> R.drawable.cloud_rain_w
                                    false -> R.drawable.cloud_rain_b
                                }
                            }
                        }

                    } else {
                        weathericon = if (temp != -1 && temp < 273)
                            when (black) {
                                true -> R.drawable.cloud_moon_snow_w
                                false -> R.drawable.cloud_moon_snow_b
                            }
                        else {
                            when (black) {
                                true -> R.drawable.cloud_moon_rain_w
                                false -> R.drawable.cloud_moon_rain_b
                            }
                        }
                    }

                }else{

                    if (weathertimetoday in sunrise..sunset || sunset == 0) {

                        if (insol > 30) {

                            weathericon = if (temp != -1 && temp < 273) {
                                when (black) {
                                    true -> R.drawable.cloud_sun_snow_w
                                    false -> R.drawable.cloud_sun_snow_b
                                }
                            } else {
                                when (black) {
                                    true -> R.drawable.cloud_sun_little_rain_w
                                    false -> R.drawable.cloud_sun_little_rain_b
                                }
                            }
                        } else {
                           weathericon = if (temp != -1 && temp < 273) {
                                when (black) {
                                    true -> R.drawable.cloud_snow_w
                                    false -> R.drawable.cloud_snow_b
                                }
                            } else {
                                when (black) {
                                    true -> R.drawable.cloud_little_rain_w
                                    false -> R.drawable.cloud_little_rain_b
                                }
                            }
                        }

                    } else {
                        weathericon = if (temp != -1 && temp < 273)
                            when (black) {
                                true -> R.drawable.cloud_moon_snow_w
                                false -> R.drawable.cloud_moon_snow_b
                            }
                        else {
                            when (black) {
                                true -> R.drawable.cloud_moon_little_rain_w
                                false -> R.drawable.cloud_moon_little_rain_b
                            }
                        }
                    }
                }
            }else{

                if(weathertimetoday in sunrise..sunset || sunset == 0) {

                    when(insol){
                        in 70..100 -> {
                            weathericon = when(black){
                                true->R.drawable.sun_w
                                false->R.drawable.sun_b
                            }
                        }
                        in 30..70 -> {
                            weathericon = when(black){
                                true->R.drawable.cloud_sun_w
                                false->R.drawable.cloud_sun_b
                            }
                        }
                        else -> {
                            weathericon = when (black) {
                                true -> R.drawable.cloud_w
                                false -> R.drawable.cloud_b
                            }
                        }
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

                    if(rain > 65){
                        if (weathertimetoday in sunrise..sunset || sunset == 0) {
                           weathericon = if (temp != -1 && temp < 273)
                                when (black) {
                                    true -> R.drawable.cloud_snow_w
                                    false -> R.drawable.cloud_snow_b
                                }
                            else {
                                when (black) {
                                   true -> R.drawable.cloud_rain_w
                                   false -> R.drawable.cloud_rain_b
                               }
                           }
                        } else {
                            weathericon = if (temp != -1 && temp < 273)
                                when (black) {
                                    true -> R.drawable.cloud_moon_snow_w
                                    false -> R.drawable.cloud_moon_snow_b
                                }
                            else {
                                when (black) {
                                    true -> R.drawable.cloud_moon_rain_w
                                    false -> R.drawable.cloud_moon_rain_b
                                }
                            }
                        }
                    }else {
                        if (weathertimetoday in sunrise..sunset || sunset == 0) {
                            weathericon = if(temp != -1 && temp < 273)
                                when (black) {
                                    true -> R.drawable.cloud_snow_w
                                    false -> R.drawable.cloud_snow_b
                                }
                            else {
                                when (black) {
                                    true -> R.drawable.cloud_little_rain_w
                                    false -> R.drawable.cloud_little_rain_b
                                }
                            }
                        } else {
                            weathericon = if (temp != -1 && temp < 273)
                                when (black) {
                                    true -> R.drawable.cloud_moon_snow_w
                                    false -> R.drawable.cloud_moon_snow_b
                                }
                            else
                                when (black) {
                                    true -> R.drawable.cloud_moon_little_rain_w
                                    false -> R.drawable.cloud_moon_little_rain_b
                                }
                        }
                    }
                }else{

                    weathericon = if(weathertimetoday in sunrise..sunset || sunset == 0) {
                         when(black){
                            true->R.drawable.sun_w
                            false->R.drawable.sun_b
                        }
                    }else{
                         when(black){
                            true->R.drawable.moon_w
                            false->R.drawable.moon_b
                        }
                    }
                }
            }

        }
        return weathericon
    }

    fun pm10(value:String):Int{

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

    fun pm25(value:String):Int{

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


    fun setBackground(tempout:String, rainfall:String, insolation:String, sunrise:Int, sunset:Int, timezone:Int, tempunit: String):Int{

        var background:Int
        val x = (0..1).random()

        val systemtime = System.currentTimeMillis()/1000L
        val systemtimezone = (TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings)/1000L
        val weathertimetoday = systemtime - systemtimezone + timezone

        if(x == 1){
            //suncloud 0-3
            val y = (0..5).random()
            background = when(y){
                0 -> R.drawable.sun_cloud_0
                1 -> R.drawable.sun_cloud_1
                2 -> R.drawable.sun_cloud_2
                3 -> R.drawable.sun_cloud_3
                else -> R.drawable.sun_cloud_2
            }
        }else{
            //cloud 0-5
            val y = (0..5).random()
            background = when(y){
                0 -> R.drawable.cloud_0
                1 -> R.drawable.cloud_1
                2 -> R.drawable.cloud_2
                3 -> R.drawable.cloud_3
                4 -> R.drawable.cloud_4
                5 -> R.drawable.cloud_5
                else -> R.drawable.cloud_2
            }
        }

        var rain = -1
        try{
            rain = rainfall.toInt()
        }catch (e: Exception){}

        var temp = (-1).toDouble()
        try{
            temp = tempout.toDouble()
            if(tempunit == "°C"){
                temp +=  275.15
            }else if(tempunit == "°F"){
                temp = ((5 / 9) * (temp - 32 )) + 273.15
            }

        }catch (e: Exception){}

        var insol = -1
        try{
            insol = insolation.toInt()
        }catch (e: Exception){}

        if(weathertimetoday in (sunrise-2200)..(sunrise+2200) || weathertimetoday in (sunset-2200)..(sunset+2200)){
        //sunet 0-4
            val z = (0..4).random()
            background = when(z){
                0 -> R.drawable.sun_sunset_0
                1 -> R.drawable.sun_sunset_1
                2 -> R.drawable.sun_sunset_2
                3 -> R.drawable.sun_sunset_3
                4 -> R.drawable.sun_sunset_4
                else -> R.drawable.sun_sunset_3
            }
        }else if((weathertimetoday > sunset || weathertimetoday < sunrise) && sunset != 0){
        //moon 0-3
            val z = (0..3).random()
            background = when(z){
                0 -> R.drawable.moon_0
                1 -> R.drawable.moon_1
                2 -> R.drawable.moon_2
                3 -> R.drawable.moon_3
                else -> R.drawable.moon_3
            }
        }else{

            if(rain > 10){

                background = if(rain > 90){
                    R.drawable.rain_1
                }else {
                    when ((0..3).random()) {
                        0 -> R.drawable.rain_0
                        1 -> R.drawable.rain_2
                        2 -> R.drawable.rain_3
                        3 -> R.drawable.dew_0
                        else -> R.drawable.rain_0
                    }
                }
            }else{

                if(temp != (-1).toDouble() && temp < 270){
                    val z = (0..2).random()
                    if(z != 1){
                        val w = (0..2).random()
                        background = when(w){
                            0 -> R.drawable.snow_0
                            1 -> R.drawable.snow_1
                            2 -> R.drawable.snow_2
                            else -> R.drawable.snow_1
                        }
                    }
                }else{
                    when(insol){
                        in 70..100 -> {
                            val z = (0..4).random()
                            background = when(z){
                                0 -> R.drawable.sun_0
                                1 -> R.drawable.sun_1
                                2 -> R.drawable.sun_2
                                3 -> R.drawable.sun_3
                                4 -> R.drawable.sun_4
                                else -> R.drawable.sun_4
                            }
                        }
                        in 30..70 -> {
                            val z = (0..3).random()
                            background = when(z){
                                0 -> R.drawable.sun_cloud_0
                                1 -> R.drawable.sun_cloud_1
                                2 -> R.drawable.sun_cloud_2
                                3 -> R.drawable.sun_cloud_3
                                else -> R.drawable.sun_cloud_2
                            }
                        }
                        else -> {
                            val z = (0..5).random()
                            background = when(z){
                                0 -> R.drawable.cloud_0
                                1 -> R.drawable.cloud_1
                                2 -> R.drawable.cloud_2
                                3 -> R.drawable.cloud_3
                                4 -> R.drawable.cloud_4
                                5 -> R.drawable.cloud_5
                                else -> R.drawable.cloud_2
                            }
                        }
                    }
                }
            }
        }

        return background
    }


    fun setBackgroundOpenWeather(mainn:String, descriptionn:String, timezone:Int, sunrise: Int, sunset: Int, rainn:String):Int{

        var main:String = mainn
        var description:String = descriptionn

        if(rainn != "null") {
            try{
                val rain = rainn.toInt()
                if(rain < 5){
                    main = "Clouds"
                    description = "few clouds"
                }else if(rain < 11){
                    main = "Clouds"
                    description = "broken clouds"
                }
            }catch (e:Exception){ }
        }
        val background:Int
        //val systemtime = System.currentTimeMillis()/1000L
        //val systemtimezone = (TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings)/1000L
        //val weathertimetoday = systemtime - systemtimezone + timezone
        val weathertimetoday = System.currentTimeMillis()/1000L

        if(weathertimetoday in (sunrise-2200)..(sunrise+2200) || weathertimetoday in (sunset-2200)..(sunset+2200)){
            //sunet 0-4
            val z = (0..4).random()
            background = when(z){
                0 -> R.drawable.sun_sunset_0
                1 -> R.drawable.sun_sunset_1
                2 -> R.drawable.sun_sunset_2
                3 -> R.drawable.sun_sunset_3
                4 -> R.drawable.sun_sunset_4
                else -> R.drawable.sun_sunset_3
            }
        }else if(weathertimetoday in sunrise..sunset){

            when(main){
                "Clear" -> {
                    val z = (0..4).random()
                    background = when(z){
                        0 -> R.drawable.sun_0
                        1 -> R.drawable.sun_1
                        2 -> R.drawable.sun_2
                        3 -> R.drawable.sun_3
                        4 -> R.drawable.sun_4
                        else -> R.drawable.sun_4
                    }
                }
                "Clouds" -> {
                    when(description){
                        "few clouds", "scattered clouds" -> {
                            val z = (0..3).random()
                            background = when(z){
                                0 -> R.drawable.sun_cloud_0
                                1 -> R.drawable.sun_cloud_1
                                2 -> R.drawable.sun_cloud_2
                                3 -> R.drawable.sun_cloud_3
                                else -> R.drawable.sun_cloud_2
                            }
                        }
                        else ->{
                            val z = (0..5).random()
                            background = when(z){
                                0 -> R.drawable.cloud_0
                                1 -> R.drawable.cloud_1
                                2 -> R.drawable.cloud_2
                                3 -> R.drawable.cloud_3
                                4 -> R.drawable.cloud_4
                                5 -> R.drawable.cloud_5
                                else -> R.drawable.cloud_2
                            }
                        }

                    }
                }
                "Rain", "Drizzle", "Thunderstorm" -> {
                    val z = (0..4).random()
                    background = when(z){
                        0 -> R.drawable.rain_0
                        1 -> R.drawable.rain_1
                        2 -> R.drawable.rain_2
                        3 -> R.drawable.rain_3
                        4 -> R.drawable.dew_0
                        else -> R.drawable.rain_0
                    }
                }
                "Snow" -> {
                    val w = (0..2).random()
                    background = when(w){
                        0 -> R.drawable.snow_0
                        1 -> R.drawable.snow_1
                        2 -> R.drawable.snow_2
                        else -> R.drawable.snow_1
                    }
                }
                else -> {
                    val z = (0..5).random()
                    background = when(z){
                        0 -> R.drawable.cloud_0
                        1 -> R.drawable.cloud_1
                        2 -> R.drawable.cloud_2
                        3 -> R.drawable.cloud_3
                        4 -> R.drawable.cloud_4
                        5 -> R.drawable.cloud_5
                        else -> R.drawable.cloud_2
                    }
                }
            }
        }else{
            //moon 0-3

            val z = (0..3).random()
            if(sunset == 0) {
                background = when (z) {
                    0 -> R.drawable.sun_cloud_0
                    1 -> R.drawable.sun_cloud_1
                    2 -> R.drawable.sun_cloud_2
                    3 -> R.drawable.sun_cloud_3
                    else -> R.drawable.sun_cloud_2
                }
            }else {
                background = when (z) {
                    0 -> R.drawable.moon_0
                    1 -> R.drawable.moon_1
                    2 -> R.drawable.moon_2
                    3 -> R.drawable.moon_3
                    else -> R.drawable.moon_3
                }
            }
        }



        return background
    }

    fun weatherIconOpenWeather(mainn:String,descriptionn: String, sunrise:Int, sunset:Int, timezone:Int, black:Boolean, rainn:String):Int{

        val icon:Int
        val systemtime = System.currentTimeMillis()/1000L
        val systemtimezone = (TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings)/1000L
        val weathertimetoday = systemtime - systemtimezone + timezone

        var main:String = mainn
        var description:String = descriptionn

        if(rainn != "null") {
            try{
                val rain = rainn.toInt()
                if(rain < 5){
                    main = "Clouds"
                    description = "scattered clouds"
                }else if(rain < 11){
                    main = "Clouds"
                    description = "all clouds"
                }
            }catch (e:Exception){ }
        }

        if((weathertimetoday > sunrise || weathertimetoday < sunset)){
            when(main){
                "Clear" -> {
                    icon = when (black) {
                        true -> R.drawable.sun_w
                        false -> R.drawable.sun_b
                    }
                }
                "Clouds" -> {
                    icon = when(description) {
                        "few clouds" -> {
                            when (black) {
                                true -> R.drawable.little_cloud_sun_w
                                false -> R.drawable.little_cloud_sun_b
                            }
                        }
                        "scattered clouds", "broken clouds" -> {
                            when (black) {
                                true -> R.drawable.cloud_sun_w
                                false -> R.drawable.cloud_sun_b
                            }
                        }
                        else -> {
                            when (black) {
                                true -> R.drawable.cloud_w
                                false -> R.drawable.cloud_b
                            }
                        }
                    }
                }
                "Rain", "Drizzle", "Thunderstorm" -> {
                    icon = if(description.contains("light")) {
                         when (black) {
                             true -> R.drawable.cloud_sun_little_rain_w
                             false -> R.drawable.cloud_sun_little_rain_b
                         }
                        }else {
                        when (black) {
                            true -> R.drawable.cloud_sun_rain_w
                            false -> R.drawable.cloud_sun_rain_b
                        }
                    }
                }
                "Snow" -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_snow_w
                        false -> R.drawable.cloud_snow_b
                    }
                }
                else -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_w
                        false -> R.drawable.cloud_b
                    }
                }
            }
        }else{
            when(main){
                "Clear" -> {
                    icon = when (black) {
                        true -> R.drawable.moon_w
                        false -> R.drawable.moon_b
                    }
                }
                "Clouds" -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_moon_w
                        false -> R.drawable.cloud_moon_b
                    }
                }
                "Rain", "Drizzle", "Thunderstorm" -> {
                    icon = if(description.contains("light")) {
                        when (black) {
                            true -> R.drawable.cloud_moon_little_rain_w
                            false -> R.drawable.cloud_moon_little_rain_b
                        }
                    }else{
                        when (black) {
                            true -> R.drawable.cloud_moon_rain_w
                            false -> R.drawable.cloud_moon_rain_b
                        }
                    }
                }
                "Snow" -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_moon_snow_w
                        false -> R.drawable.cloud_moon_snow_b
                    }
                }
                else -> {
                    icon = when (black) {
                        true -> R.drawable.cloud_moon_w
                        false -> R.drawable.cloud_moon_b
                    }
                }
            }
        }

        return icon
    }
}