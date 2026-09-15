package com.example.clockwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.example.clockwidgets.widget.AnalogClockWidgetProvider;
import com.example.clockwidgets.widget.DigitalClockWidgetProvider;
import com.example.clockwidgets.widget.MinimalClockWidgetProvider;

/**
 * Turns a {@link ClockConfig} into the RemoteViews of one of the three clock widgets.
 *
 * <p>Only the documented RemoteViews API is used ({@code setViewVisibility},
 * {@code setTextColor}, {@code setOnClickPendingIntent}); the layouts carry one view per
 * option and the settings are applied by toggling visibility.</p>
 */
public final class ClockRemoteViews {

    public static final int KIND_DIGITAL = 0;
    public static final int KIND_ANALOG = 1;
    public static final int KIND_MINIMAL = 2;

    private static final int[] TIME_VIEW_IDS = {
            R.id.widget_time_24,
            R.id.widget_time_24s,
            R.id.widget_time_12,
            R.id.widget_time_12s
    };

    private static final int[] BACKGROUND_VIEW_IDS = {
            R.id.bg_dark,
            R.id.bg_light,
            R.id.bg_transparent
    };

    private ClockRemoteViews() {
    }

    public static int layoutFor(int kind) {
        switch (kind) {
            case KIND_ANALOG:
                return R.layout.widget_analog;
            case KIND_MINIMAL:
                return R.layout.widget_minimal;
            default:
                return R.layout.widget_digital;
        }
    }

    /** The provider class that publishes the given kind. */
    public static Class<?> providerClass(int kind) {
        switch (kind) {
            case KIND_ANALOG:
                return AnalogClockWidgetProvider.class;
            case KIND_MINIMAL:
                return MinimalClockWidgetProvider.class;
            default:
                return DigitalClockWidgetProvider.class;
        }
    }

    public static int kindFor(ComponentName provider) {
        if (provider != null) {
            String className = provider.getClassName();
            if (AnalogClockWidgetProvider.class.getName().equals(className)) {
                return KIND_ANALOG;
            }
            if (MinimalClockWidgetProvider.class.getName().equals(className)) {
                return KIND_MINIMAL;
            }
        }
        return KIND_DIGITAL;
    }

    /** Display name of the widget, shown in the app and in the "add" toast. */
    public static int nameResFor(int kind) {
        switch (kind) {
            case KIND_ANALOG:
                return R.string.widget_analog_name;
            case KIND_MINIMAL:
                return R.string.widget_minimal_name;
            default:
                return R.string.widget_digital_name;
        }
    }

    public static RemoteViews build(Context context, int appWidgetId, int kind, ClockConfig config) {
        return build(context, appWidgetId, kind, config, true);
    }

    /**
     * @param interactive {@code false} for on-screen previews, which must not carry the
     *                    "open the settings" click action.
     */
    public static RemoteViews build(Context context, int appWidgetId, int kind, ClockConfig config,
            boolean interactive) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutFor(kind));

        int backgroundViewId = config.backgroundViewId();
        for (int viewId : BACKGROUND_VIEW_IDS) {
            views.setViewVisibility(viewId, viewId == backgroundViewId ? View.VISIBLE : View.GONE);
        }

        views.setViewVisibility(R.id.widget_date, config.showDate ? View.VISIBLE : View.GONE);
        views.setTextColor(R.id.widget_date,
                config.isLightBackground()
                        ? context.getColor(R.color.widget_subtext_on_light)
                        : context.getColor(R.color.widget_subtext_on_dark));

        // The analog widget has no digital time line.
        if (kind != KIND_ANALOG) {
            int timeViewId = config.timeViewId();
            for (int viewId : TIME_VIEW_IDS) {
                views.setViewVisibility(viewId, viewId == timeViewId ? View.VISIBLE : View.GONE);
            }
            views.setTextColor(timeViewId, textColor(context, config));
        }

        if (interactive && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            views.setOnClickPendingIntent(R.id.widget_root,
                    configPendingIntent(context, appWidgetId));
        }
        return views;
    }

    private static int textColor(Context context, ClockConfig config) {
        // White text would be invisible on the light background.
        if (config.isLightBackground() && config.accentColor == context.getColor(R.color.accent_white)) {
            return context.getColor(R.color.widget_text_on_light);
        }
        return config.accentColor;
    }

    /** Tapping a clock on the home screen opens its settings. */
    private static PendingIntent configPendingIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, ClockConfigActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse("clockwidget://configure/" + appWidgetId));
        return PendingIntent.getActivity(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}