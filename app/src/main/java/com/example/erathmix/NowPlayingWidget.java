package com.example.erathmix;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class NowPlayingWidget extends AppWidgetProvider {

    private static final String PREFS_NAME = "ErathmixPrefs";
    private static final String KEY_PLAYBACK_MODE = "playback_mode";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String mode = prefs.getString(KEY_PLAYBACK_MODE, "radio");

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_now_playing);

            views.setTextViewText(R.id.widgetMode, "Mode: " + mode.substring(0, 1).toUpperCase() + mode.substring(1));
            views.setTextViewText(R.id.widgetTrack, "Now Playing: Open app");

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
