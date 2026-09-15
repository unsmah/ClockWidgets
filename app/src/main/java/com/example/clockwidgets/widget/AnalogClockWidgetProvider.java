package com.example.clockwidgets.widget;

import com.example.clockwidgets.ClockRemoteViews;

/** Analog clock: classic dial with the date underneath. */
public class AnalogClockWidgetProvider extends BaseClockWidgetProvider {

    @Override
    protected int kind() {
        return ClockRemoteViews.KIND_ANALOG;
    }
}