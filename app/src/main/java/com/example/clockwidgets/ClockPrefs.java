package com.example.clockwidgets;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores one {@link ClockConfig} per app widget id. */
public final class ClockPrefs {

    private static final String PREFS_NAME = "clock_widgets";
    private static final String KEY_24_HOUR = "use24";
    private static final String KEY_SECONDS = "seconds";
    private static final String KEY_DATE = "date";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_BACKGROUND = "background";

    private ClockPrefs() {
    }

    public static ClockConfig load(Context context, int appWidgetId, int kind) {
        ClockConfig config = ClockConfig.defaults(context, kind);
        SharedPreferences prefs = prefs(context);
        config.use24Hour = prefs.getBoolean(key(KEY_24_HOUR, appWidgetId), config.use24Hour);
        config.showSeconds = prefs.getBoolean(key(KEY_SECONDS, appWidgetId), config.showSeconds);
        config.showDate = prefs.getBoolean(key(KEY_DATE, appWidgetId), config.showDate);
        config.accentColor = prefs.getInt(key(KEY_ACCENT, appWidgetId), config.accentColor);
        config.backgroundStyle = prefs.getInt(key(KEY_BACKGROUND, appWidgetId), config.backgroundStyle);
        return config;
    }

    public static void save(Context context, int appWidgetId, ClockConfig config) {
        prefs(context).edit()
                .putBoolean(key(KEY_24_HOUR, appWidgetId), config.use24Hour)
                .putBoolean(key(KEY_SECONDS, appWidgetId), config.showSeconds)
                .putBoolean(key(KEY_DATE, appWidgetId), config.showDate)
                .putInt(key(KEY_ACCENT, appWidgetId), config.accentColor)
                .putInt(key(KEY_BACKGROUND, appWidgetId), config.backgroundStyle)
                .apply();
    }

    /** Forgets the settings of a removed widget. */
    public static void delete(Context context, int appWidgetId) {
        prefs(context).edit()
                .remove(key(KEY_24_HOUR, appWidgetId))
                .remove(key(KEY_SECONDS, appWidgetId))
                .remove(key(KEY_DATE, appWidgetId))
                .remove(key(KEY_ACCENT, appWidgetId))
                .remove(key(KEY_BACKGROUND, appWidgetId))
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String key(String name, int appWidgetId) {
        return name + "_" + appWidgetId;
    }
}