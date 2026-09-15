package com.example.clockwidgets.widget;

import com.example.clockwidgets.ClockRemoteViews;

/** Duo clock: analog dial on top, digital time and date underneath. */
public class DuoClockWidgetProvider extends BaseClockWidgetProvider {

    @Override
    protected int kind() {
        return ClockRemoteViews.KIND_DUO;
    }
}