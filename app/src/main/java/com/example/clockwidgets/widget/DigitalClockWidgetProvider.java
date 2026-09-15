package com.example.clockwidgets.widget;

import com.example.clockwidgets.ClockRemoteViews;

/** Digital clock: time, weekday and date on a rounded card. */
public class DigitalClockWidgetProvider extends BaseClockWidgetProvider {

    @Override
    protected int kind() {
        return ClockRemoteViews.KIND_DIGITAL;
    }
}