package com.example.clockwidgets.widget;

import com.example.clockwidgets.ClockRemoteViews;

/** Banner clock: wide layout with the time on the left and the weekday/date on the right. */
public class BannerClockWidgetProvider extends BaseClockWidgetProvider {

    @Override
    protected int kind() {
        return ClockRemoteViews.KIND_BANNER;
    }
}