package com.example.clockwidgets.widget;

import com.example.clockwidgets.ClockRemoteViews;

/** Minimal clock: transparent background, time only by default. */
public class MinimalClockWidgetProvider extends BaseClockWidgetProvider {

    @Override
    protected int kind() {
        return ClockRemoteViews.KIND_MINIMAL;
    }
}