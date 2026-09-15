package com.example.clockwidgets.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;

import com.example.clockwidgets.ClockConfig;
import com.example.clockwidgets.ClockPrefs;
import com.example.clockwidgets.ClockRemoteViews;

/**
 * Shared behaviour of the clock widgets: every update simply rebuilds the RemoteViews from the
 * settings stored for that widget id.
 */
public abstract class BaseClockWidgetProvider extends AppWidgetProvider {

    /** One of the {@code ClockRemoteViews.KIND_*} constants. */
    protected abstract int kind();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            refresh(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions) {
        // Called when the user resizes the widget.
        refresh(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            ClockPrefs.delete(context, appWidgetId);
        }
    }

    /** Rebuilds a single widget from its stored settings. */
    protected void refresh(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        ClockConfig config = ClockPrefs.load(context, appWidgetId, kind());
        appWidgetManager.updateAppWidget(appWidgetId,
                ClockRemoteViews.build(context, appWidgetId, kind(), config));
    }
}