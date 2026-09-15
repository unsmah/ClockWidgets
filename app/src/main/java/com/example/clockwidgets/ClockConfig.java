package com.example.clockwidgets;

import android.content.Context;
import android.text.format.DateFormat;

/**
 * Settings of a single placed clock widget. Every widget keeps its own copy, stored per
 * app widget id by {@link ClockPrefs}.
 */
public class ClockConfig {

    /** Background styles, see {@link #backgroundStyle}. */
    public static final int BG_DARK = 0;
    public static final int BG_LIGHT = 1;
    public static final int BG_TRANSPARENT = 2;

    public boolean use24Hour = true;
    public boolean showSeconds = false;
    public boolean showDate = true;
    public int accentColor = 0xFF7C8CFF;
    public int backgroundStyle = BG_DARK;

    /** Defaults for a freshly added widget of the given kind. */
    public static ClockConfig defaults(Context context, int kind) {
        ClockConfig config = new ClockConfig();
        // Follow the phone's 12/24 hour setting until the user says otherwise.
        config.use24Hour = DateFormat.is24HourFormat(context);
        config.showSeconds = false;
        config.showDate = kind != ClockRemoteViews.KIND_MINIMAL;
        config.backgroundStyle = kind == ClockRemoteViews.KIND_MINIMAL
                ? BG_TRANSPARENT
                : BG_DARK;
        config.accentColor = context.getColor(kind == ClockRemoteViews.KIND_MINIMAL
                ? R.color.accent_white
                : R.color.accent_blue);
        return config;
    }

    public boolean isLightBackground() {
        return backgroundStyle == BG_LIGHT;
    }

    /**
     * The id of the TextClock that matches the requested time format. The layouts contain one
     * TextClock per format and only the matching one is made visible, because widget layouts
     * cannot be re-styled with arbitrary format strings from RemoteViews.
     */
    public int timeViewId() {
        if (use24Hour) {
            return showSeconds ? R.id.widget_time_24s : R.id.widget_time_24;
        }
        return showSeconds ? R.id.widget_time_12s : R.id.widget_time_12;
    }

    /** The id of the background layer that matches the requested style. */
    public int backgroundViewId() {
        switch (backgroundStyle) {
            case BG_LIGHT:
                return R.id.bg_light;
            case BG_TRANSPARENT:
                return R.id.bg_transparent;
            default:
                return R.id.bg_dark;
        }
    }
}