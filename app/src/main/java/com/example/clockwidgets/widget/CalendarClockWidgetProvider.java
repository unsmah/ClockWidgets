package com.example.clockwidgets.widget;

import com.example.clockwidgets.ClockRemoteViews;

/** Date focus clock: big day number with the weekday, month and a smaller time. */
public class CalendarClockWidgetProvider extends BaseClockWidgetProvider {

    @Override
    protected int kind() {
        return ClockRemoteViews.KIND_CALENDAR;
    }
}