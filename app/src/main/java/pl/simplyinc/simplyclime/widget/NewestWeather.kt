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
import android.widget.RemoteViews
import pl.simplyinc.simplyclime.R
import pl.simplyinc.simplyclime.activities.MainActivity


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
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                intent.putExtra("fromwidget", weatherposition)

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
            val widgetinfo = JSONObject(session.getPref("widget$appWidgetId"))
            val weatherdata = JSONObject(widgetweather)
            val stationdata = JSONObject(station)
            val unixTime = System.currentTimeMillis() / 1000L
            val update = WidgetWeatherRequest()

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            intent.putExtra("fromwidget", weatherposition)
            val views = RemoteViews(context.packageName, R.layout.newest_weather)
            views.setOnClickPendingIntent(R.id.widget, pendingIntent)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)


            if(stationdata.getInt("refreshtime")+weatherdata.getInt("updatedtime") <= unixTime){

                update.getNewestWeatherWidget(context, appWidgetManager, appWidgetId, widgetweather, stationdata, widgetinfo)
            }else{
                update.setWidget(context, appWidgetManager, appWidgetId, widgetweather, stationdata, widgetinfo, false)
            }

        }

    }

}

