package com.example.clockwidgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

/**
 * The size of a placed widget in dp.
 *
 * <p>Widgets can be resized to any cell count, so the text sizes are computed from the real size
 * of the widget instead of being fixed: see {@code ClockRemoteViews}. The platform reports
 * {@code OPTION_APPWIDGET_MIN_*} for the portrait orientation and {@code OPTION_APPWIDGET_MAX_*}
 * for the landscape one; the smaller value of each axis is used, because whatever fits in the
 * tightest orientation fits in both.</p>
 */
public class WidgetSize {

    private static final int FALLBACK_WIDTH_DP = 180;
    private static final int FALLBACK_HEIGHT_DP = 110;

    public final int widthDp;
    public final int heightDp;

    public WidgetSize(int widthDp, int heightDp) {
        this.widthDp = Math.max(1, widthDp);
        this.heightDp = Math.max(1, heightDp);
    }

    public static WidgetSize of(Context context, int appWidgetId) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return new WidgetSize(FALLBACK_WIDTH_DP, FALLBACK_HEIGHT_DP);
        }
        Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId);
        return new WidgetSize(
                smallestPositive(
                        options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0),
                        options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0),
                        FALLBACK_WIDTH_DP),
                smallestPositive(
                        options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
                        options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
                        FALLBACK_HEIGHT_DP));
    }

    private static int smallestPositive(int first, int second, int fallback) {
        int smallest = 0;
        if (first > 0) {
            smallest = first;
        }
        if (second > 0) {
            smallest = smallest == 0 ? second : Math.min(smallest, second);
        }
        return smallest > 0 ? smallest : fallback;
    }

    /** The dial of an analog widget is square, so it uses the shortest side. */
    public int shortestSideDp() {
        return Math.min(widthDp, heightDp);
    }
}