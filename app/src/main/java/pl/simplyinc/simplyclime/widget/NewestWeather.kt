package pl.simplyinc.simplyclime.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import org.json.JSONObject
import pl.simplyinc.simplyclime.elements.SessionPref
import pl.simplyinc.simplyclime.network.WidgetWeatherRequest
import java.lang.Exception
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.RemoteViews
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity
import pl.simplyinc.simplyclime.elements.GpsLocation
import pl.simplyinc.simplyclime.network.ForecastRequest


class NewestWeather : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        val session = SessionPref(context)

        for (appWidgetId in appWidgetIds) {
            try{
                val widgetdata = JSONObject(session.getPref("widget$appWidgetId"))
                val weatherposition = widgetdata.getInt("id")
                updateAppWidget(context, appWidgetManager, appWidgetId, weatherposition)
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("setweather", weatherposition)

                val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val views = RemoteViews(context.packageName, R.layout.newest_weather)
                views.setOnClickPendingIntent(R.id.widget, pendingIntent)

            }catch (e:Exception){}
        }

    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val session = SessionPref(context)
        for (appWidgetId in appWidgetIds) {
            session.setPref("widget$appWidgetId", "")
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, weatherposition:Int) {

            val session = SessionPref(context)
            val widgetweather = session.getPref("weathers").split("|")[weatherposition]
            val station = session.getPref("stations").split("|")[weatherposition]
            val forecast = session.getPref("forecasts").split("|")[weatherposition]

            val f = JSONObject(forecast)

            //sprawdzic aktualnosc forecastu

            val widgetinfo = JSONObject(session.getPref("widget$appWidgetId"))
            val weatherdata = JSONObject(widgetweather)
            val stationdata = JSONObject(station)
            var update = WidgetWeatherRequest(
                context,
                appWidgetManager,
                appWidgetId,
                stationdata,
                widgetinfo,
                f,
                weatherposition)

            val now = System.currentTimeMillis()/1000L
            val forecastsall = session.getPref("forecasts").split("|")
            val forecastObj = JSONObject(forecastsall[weatherposition])
            val newforecast = ForecastRequest(context, weatherposition, "", true)

            if(forecastObj.getInt("time") < (now-12800)) {
                if(!stationdata.getBoolean("gps")) {
                    newforecast.getNewestForecast("q="+stationdata.getString("city")) {
                        update = WidgetWeatherRequest(
                            context,
                            appWidgetManager,
                            appWidgetId,
                            stationdata,
                            widgetinfo,
                            it,
                            weatherposition)
                        setData(context, update, stationdata, weatherdata, widgetweather, now)
                    }
                }else{
                    setData(context,update,stationdata,weatherdata,widgetweather,now, newforecast)
                }
            }else{
                setData(context,update,stationdata,weatherdata,widgetweather,now)
            }


            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            intent.putExtra("setweather", weatherposition)
            val views = RemoteViews(context.packageName, R.layout.newest_weather)
            views.setOnClickPendingIntent(R.id.widget, pendingIntent)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)




        }

        private fun setData(context: Context, update:WidgetWeatherRequest, stationdata:JSONObject, weatherdata:JSONObject, widgetweather:String, unixTime:Long, forecastreq:ForecastRequest? = null){
            val gps = GpsLocation(
                null,
                context,
                false,
                0,
                stationdata,
                null,
                true,
                update,
                widgetweather,
                false,
                null,
                null,
                forecastreq)

            if(stationdata.getInt("refreshtime")+weatherdata.getInt("updatedtime") <= unixTime){
                if(stationdata.getBoolean("gps")) {
                    gps.getLocation()
                }else{
                    if(stationdata.getBoolean("privstation"))
                        update.getNewestWeatherWidget(widgetweather)
                    else update.getNewestWeatherWidgetOpenWeather(widgetweather)
                }
            }else{
                if(forecastreq != null){
                    gps.getLocation()
                }else {
                    //bez oczekiwania nie chce się odpalić za pierwszym razem tfu
                    Handler().postDelayed({
                        update.setWidget(widgetweather, false)
                    },700)
                }
            }
        }

    }

}

