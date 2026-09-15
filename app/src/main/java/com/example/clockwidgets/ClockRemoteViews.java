package com.example.clockwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.example.clockwidgets.widget.AnalogClockWidgetProvider;
import com.example.clockwidgets.widget.BannerClockWidgetProvider;
import com.example.clockwidgets.widget.CalendarClockWidgetProvider;
import com.example.clockwidgets.widget.DigitalClockWidgetProvider;
import com.example.clockwidgets.widget.DuoClockWidgetProvider;
import com.example.clockwidgets.widget.MinimalClockWidgetProvider;

/**
 * Turns a {@link ClockConfig} into the RemoteViews of one of the clock designs.
 *
 * <p>Only the documented widget APIs are used ({@code setViewVisibility}, {@code setTextColor},
 * {@code setTextViewTextSize}, {@code setOnClickPendingIntent}); the layouts carry one view per
 * option and the settings are applied by toggling visibility. Text sizes are computed from the
 * real size of the widget, so every design stays readable after any resize.</p>
 */
public final class ClockRemoteViews {

    public static final int KIND_DIGITAL = 0;
    public static final int KIND_ANALOG = 1;
    public static final int KIND_MINIMAL = 2;
    public static final int KIND_BANNER = 3;
    public static final int KIND_CALENDAR = 4;
    public static final int KIND_DUO = 5;

    /** All designs, in the order they are shown in the app. */
    public static final int[] ALL_KINDS = {
            KIND_DIGITAL,
            KIND_BANNER,
            KIND_MINIMAL,
            KIND_CALENDAR,
            KIND_ANALOG,
            KIND_DUO
    };

    /** Provider class of every kind, indexed by the kind constant. */
    private static final Class<?>[] PROVIDERS = {
            DigitalClockWidgetProvider.class,
            AnalogClockWidgetProvider.class,
            MinimalClockWidgetProvider.class,
            BannerClockWidgetProvider.class,
            CalendarClockWidgetProvider.class,
            DuoClockWidgetProvider.class
    };

    /** Widget names, indexed by the kind constant. */
    private static final int[] NAME_RES = {
            R.string.widget_digital_name,
            R.string.widget_analog_name,
            R.string.widget_minimal_name,
            R.string.widget_banner_name,
            R.string.widget_calendar_name,
            R.string.widget_duo_name
    };

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

    /** Average glyph width of the light sans-serif digits, as a fraction of the text size. */
    private static final float GLYPH_RATIO = 0.62f;
    private static final float MIN_TIME_DP = 13f;
    private static final float MAX_TIME_DP = 132f;

    private ClockRemoteViews() {
    }

    public static int layoutFor(int kind) {
        switch (kind) {
            case KIND_ANALOG:
                return R.layout.widget_analog;
            case KIND_MINIMAL:
                return R.layout.widget_minimal;
            case KIND_BANNER:
                return R.layout.widget_banner;
            case KIND_CALENDAR:
                return R.layout.widget_calendar;
            case KIND_DUO:
                return R.layout.widget_duo;
            default:
                return R.layout.widget_digital;
        }
    }

    /** The provider class that publishes the given kind. */
    public static Class<?> providerClass(int kind) {
        return PROVIDERS[safe(kind)];
    }

    public static int kindFor(ComponentName provider) {
        if (provider != null) {
            String className = provider.getClassName();
            for (int kind = 0; kind < PROVIDERS.length; kind++) {
                if (PROVIDERS[kind].getName().equals(className)) {
                    return kind;
                }
            }
        }
        return KIND_DIGITAL;
    }

    /** Display name of a design, used in the app and in the "add" toast. */
    public static int nameResFor(int kind) {
        return NAME_RES[safe(kind)];
    }

    /** Description of a design, used in the app and in the widget picker. */
    public static int descriptionResFor(int kind) {
        switch (kind) {
            case KIND_ANALOG:
                return R.string.widget_analog_description;
            case KIND_MINIMAL:
                return R.string.widget_minimal_description;
            case KIND_BANNER:
                return R.string.widget_banner_description;
            case KIND_CALENDAR:
                return R.string.widget_calendar_description;
            case KIND_DUO:
                return R.string.widget_duo_description;
            default:
                return R.string.widget_digital_description;
        }
    }

    /** The analog design has no digital time line, every other design has one. */
    public static boolean hasDigitalTime(int kind) {
        return kind != KIND_ANALOG;
    }

    private static int safe(int kind) {
        return kind >= 0 && kind < PROVIDERS.length ? kind : KIND_DIGITAL;
    }

    public static RemoteViews build(Context context, int appWidgetId, int kind, ClockConfig config) {
        return build(context, appWidgetId, kind, config, true);
    }

    public static RemoteViews build(Context context, int appWidgetId, int kind, ClockConfig config,
            boolean interactive) {
        return build(context, appWidgetId, kind, config, interactive,
                WidgetSize.of(context, appWidgetId));
    }

    /**
     * @param interactive {@code false} for on-screen previews, which must not carry the
     *                    "open the settings" click action.
     * @param size        current size of the widget, used to scale the text so it always fits.
     */
    public static RemoteViews build(Context context, int appWidgetId, int kind, ClockConfig config,
            boolean interactive, WidgetSize size) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutFor(kind));

        int primary = primaryTextColor(context, config);
        int secondary = secondaryTextColor(context, config);
        boolean showDate = config.showDate && hasRoomForDate(kind, size);

        int backgroundViewId = config.backgroundViewId();
        for (int viewId : BACKGROUND_VIEW_IDS) {
            views.setViewVisibility(viewId, viewId == backgroundViewId ? View.VISIBLE : View.GONE);
        }

        views.setViewVisibility(R.id.widget_date, showDate ? View.VISIBLE : View.GONE);
        views.setTextColor(R.id.widget_date, secondary);

        if (kind == KIND_CALENDAR) {
            // The date focus design adds a big day number and a weekday line.
            views.setViewVisibility(R.id.widget_day, showDate ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.widget_weekday, showDate ? View.VISIBLE : View.GONE);
            views.setTextColor(R.id.widget_day, primary);
            views.setTextColor(R.id.widget_weekday, secondary);
        }

        if (hasDigitalTime(kind)) {
            int timeViewId = config.timeViewId();
            for (int viewId : TIME_VIEW_IDS) {
                views.setViewVisibility(viewId, viewId == timeViewId ? View.VISIBLE : View.GONE);
            }
            views.setTextColor(timeViewId, primary);
        }

        applyTextSizes(views, kind, config, size, showDate);

        if (interactive && appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            views.setOnClickPendingIntent(R.id.widget_root,
                    configPendingIntent(context, appWidgetId));
        }
        return views;
    }

    /**
     * Sizes every text of the widget from its current size: a clock stretched over half the home
     * screen gets a big face, a small one stays compact — never clipped, never tiny.
     */
    private static void applyTextSizes(RemoteViews views, int kind, ClockConfig config,
            WidgetSize size, boolean showDate) {
        int timeViewId = config.timeViewId();
        int chars = config.timeCharCount();

        switch (kind) {
            case KIND_BANNER: {
                float time = fitText(size, chars, 0.58f, 0.80f);
                setTextSize(views, timeViewId, time);
                setTextSize(views, R.id.widget_date, clamp(time * 0.46f, 10f, 28f));
                break;
            }
            case KIND_MINIMAL: {
                float time = fitText(size, chars, 0.96f, showDate ? 0.64f : 0.88f);
                setTextSize(views, timeViewId, time);
                setTextSize(views, R.id.widget_date, clamp(time * 0.30f, 9f, 20f));
                break;
            }
            case KIND_CALENDAR: {
                float day = fitText(size, 2, 0.86f, showDate ? 0.48f : 0.62f);
                setTextSize(views, R.id.widget_day, day);
                setTextSize(views, R.id.widget_weekday, clamp(day * 0.28f, 11f, 28f));
                setTextSize(views, R.id.widget_date, clamp(day * 0.30f, 11f, 30f));
                setTextSize(views, timeViewId, clamp(day * 0.32f, 12f, 42f));
                break;
            }
            case KIND_ANALOG: {
                // The dial fills the widget by itself, only the date line is scaled.
                setTextSize(views, R.id.widget_date,
                        clamp(size.shortestSideDp() * 0.11f, 10f, 22f));
                break;
            }
            case KIND_DUO: {
                float time = fitText(size, chars, 0.92f, showDate ? 0.26f : 0.34f);
                setTextSize(views, timeViewId, time);
                setTextSize(views, R.id.widget_date, clamp(time * 0.42f, 10f, 24f));
                break;
            }
            default: { // KIND_DIGITAL
                float time = fitText(size, chars, 0.92f, showDate ? 0.54f : 0.74f);
                setTextSize(views, timeViewId, time);
                setTextSize(views, R.id.widget_date, clamp(time * 0.34f, 10f, 24f));
                break;
            }
        }
    }

    /**
     * Largest text size that fits the given number of characters: limited by the available width
     * (using the average glyph width) and by the share of the height the line may take.
     */
    private static float fitText(WidgetSize size, int chars, float widthShare, float heightShare) {
        float byWidth = (size.widthDp * widthShare) / (chars * GLYPH_RATIO);
        float byHeight = size.heightDp * heightShare;
        return clamp(Math.min(byWidth, byHeight), MIN_TIME_DP, MAX_TIME_DP);
    }

    private static void setTextSize(RemoteViews views, int viewId, float sizeDp) {
        views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_DIP, sizeDp);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Designs hide their date line when the widget is too small to show it legibly. */
    private static boolean hasRoomForDate(int kind, WidgetSize size) {
        switch (kind) {
            case KIND_BANNER:
                return size.widthDp >= 150;
            case KIND_MINIMAL:
                return size.heightDp >= 44;
            case KIND_DUO:
                return size.heightDp >= 120;
            default:
                return true;
        }
    }

    private static int primaryTextColor(Context context, ClockConfig config) {
        // White text would be invisible on the light background.
        if (config.isLightBackground()
                && config.accentColor == context.getColor(R.color.accent_white)) {
            return context.getColor(R.color.widget_text_on_light);
        }
        return config.accentColor;
    }

    private static int secondaryTextColor(Context context, ClockConfig config) {
        return config.isLightBackground()
                ? context.getColor(R.color.widget_subtext_on_light)
                : context.getColor(R.color.widget_subtext_on_dark);
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