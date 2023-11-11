package com.example.services.utils;

import java.util.Calendar;

public class TimeUtils {
    public static long millisTillNextUpdate(int triggerHour) {
        Calendar now = Calendar.getInstance();

        Calendar nextTrigger = (Calendar) now.clone();

        if (now.get(Calendar.HOUR_OF_DAY) >= triggerHour) {
            nextTrigger.add(Calendar.DATE, triggerHour);
        }

        // Set the calendar to 1 AM.
        nextTrigger.set(Calendar.HOUR_OF_DAY, triggerHour);
        nextTrigger.set(Calendar.MINUTE, 0);
        nextTrigger.set(Calendar.SECOND, 0);
        nextTrigger.set(Calendar.MILLISECOND, 0);

        // Calculate the difference in milliseconds.
        return nextTrigger.getTimeInMillis() - now.getTimeInMillis();
    }
}
