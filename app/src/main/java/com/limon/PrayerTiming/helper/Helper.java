package com.limon.PrayerTiming.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Limon on 2/11/2017.
 */

public class Helper {

    static String[] Months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public static double getCurrentLatitude() {
        double latitude = 0.0;
        return latitude;
    }

    public static String getTimeZoneString() {
        //to-do get from api cause this return timezone from mobile phone
        String timeZoneString = TimeZone.getDefault().getID();
        return timeZoneString;
    }

    public static int getCurrentYear() {
        int year = 2017;
        year = Calendar.getInstance().get(Calendar.YEAR);
        return year;
    }

    public static int getCurrentMonth() {
        int month = Calendar.getInstance().get(Calendar.MONTH);
        return month;
    }

    public static String getCurrentMonthShortName() {
        String monthName = Months[getCurrentMonth()];
        return monthName;
    }

    public static String getNextDate() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);

        return calendar.get(Calendar.YEAR) + "-" + Months[calendar.get(Calendar.MONTH)] + "-"
                + String.format("%02d", calendar.get(Calendar.DATE));
    }

    public static String getCurrentDate() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DATE);
        return (year + "-" + month + "-" + date);
    }

    public static String getCurrentDateTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int second = Calendar.getInstance().get(Calendar.SECOND);
        return (getCurrentDate() + " " + hour + ":" + minute + ":" + second);
    }

    public static String getCurrentDate(String format) {
        int date = Calendar.getInstance().get(Calendar.DATE);
        return (String.format("%02d", date) + " " + getCurrentMonthShortName() + " " + getCurrentYear());
    }

    public static String get24TimeTo12HourTime(String time, String timeFormat) {
        DateFormat f1 = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = f1.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat f2 = new SimpleDateFormat(timeFormat);
        return f2.format(date).toLowerCase();
    }

    public static String getSplitTime(String time) {
        String[] splitedTime = time.split("\\s+");
        return splitedTime[0];
    }


}
