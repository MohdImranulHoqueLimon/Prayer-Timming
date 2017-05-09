package com.limon.PrayerTiming;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.limon.PrayerTiming.GPS.GPSTracker;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.activity.AlarmReceiverActivity;
import com.limon.PrayerTiming.dbhelper.TimeDbHelper;
import com.limon.PrayerTiming.helper.Helper;
import com.limon.PrayerTiming.http.time.model.LogData;
import com.limon.PrayerTiming.http.time.model.Timing;

import java.util.Calendar;
import java.util.concurrent.Exchanger;

/**
 * Created by Limon on 2/21/2017.
 */

public class Prayer {

    private Context mContext;
    public static int nextPrayerNumber;
    public String prayerName[] = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};

    public Prayer(Context context) {
        this.mContext = context;
    }

    public int getNextPrayerInSecond() {

        int timeDifferenceCurentToNext = 0;
        int findingFlag = 1;
        try {
            TimeDbHelper timeDbHelper = new TimeDbHelper(this.mContext);
            Timing timingObj = timeDbHelper.getPrayerTime(Helper.getCurrentDate("with space"));

            String fajrTime = timingObj.getFajrTime();
            String dhuhrTime = timingObj.getDhuhrTime();
            String asrTime = timingObj.getAsrTime();
            String maghribTime = timingObj.getMaghribeTime();
            String ishaTime = timingObj.getIshaTime();

            String formatedFajrTime = Helper.get24TimeTo12HourTime(fajrTime, "h-mm-a");
            String formatedDhuhrTime = Helper.get24TimeTo12HourTime(dhuhrTime, "h-mm-a");
            String formatedAsrTime = Helper.get24TimeTo12HourTime(asrTime, "h-mm-a");
            String formatedMaghribtime = Helper.get24TimeTo12HourTime(maghribTime, "h-mm-a");
            String formatedIshaTime = Helper.get24TimeTo12HourTime(ishaTime, "h-mm-a");

            String[] splitArray = {formatedFajrTime, formatedDhuhrTime, formatedAsrTime,
                    formatedMaghribtime, formatedIshaTime};

            boolean find = false;
            Calendar calendar = Calendar.getInstance();
            int curentHour = calendar.get(Calendar.HOUR);
            int curentMinute = calendar.get(Calendar.MINUTE);
            int curentTimeInSecond = (curentHour * 3600) + (curentMinute * 60);

            //If current time is pm
            int amOrPm = calendar.get(Calendar.AM_PM);
            if (amOrPm == 1) {
                curentTimeInSecond = curentTimeInSecond + (12 * 3600);
            }

            int cnt = 0;
            while (find == false) {

                String hourAndMinute[] = splitArray[cnt].split("-");

                int nextHour = Integer.parseInt(hourAndMinute[0]);
                int nextMinute = Integer.parseInt(hourAndMinute[1]);
                int nextTimeInSecond = (nextHour * 3600) + (nextMinute * 60);
                if ("pm".equals(hourAndMinute[2]) && nextHour != 12) {
                    nextTimeInSecond = nextTimeInSecond + (12 * 3600);
                }
                if (curentTimeInSecond < nextTimeInSecond) {
                    timeDifferenceCurentToNext = nextTimeInSecond - curentTimeInSecond;
                    this.nextPrayerNumber = cnt;
                    find = true;
                }
                /*
                  if prayer time not find for this day then take the next days first
                  prayer time and get the index by (date_of_the_year + 1) % 365 because
                  when the day will be 365 ans for the next day 365 = (365+1)%365 = 1
                 */
                if (cnt == 4 && find == false) {
                    this.nextPrayerNumber = 0;
                    findingFlag = 0;
                    find = true;
                }
                cnt++;
            }

            if (findingFlag == 1) return timeDifferenceCurentToNext;

            fajrTime = timeDbHelper.getFajrPrayerTime(Helper.getNextDate());
            fajrTime = Helper.get24TimeTo12HourTime(fajrTime, "h-mm-a");
            String hourAndMinute[] = fajrTime.split("-");
            int nextHour = Integer.parseInt(hourAndMinute[0]);
            int nextMinute = Integer.parseInt(hourAndMinute[1]);
            timeDifferenceCurentToNext = ((24 * 3600) - (curentTimeInSecond)) + ((nextHour * 3600) + (nextMinute * 60));
        } catch (Exception exception) {

        }
        return timeDifferenceCurentToNext;
    }

    public boolean isNeedFetchTime() {
        boolean isNeed = false;
        GPSTracker gpsTracker = new GPSTracker(this.mContext);

        double currentLat = gpsTracker.getLatitude();
        double currentLong = gpsTracker.getLongitude();
        gpsTracker = null;

        TimeDbHelper timeDbHelper = new TimeDbHelper(this.mContext);
        LogData logData = timeDbHelper.getLastLogData();
        Timing timing = timeDbHelper.getPrayerTime(Helper.getCurrentDate("with space"));

        if (timing == null) {
            isNeed = true;
        } else {
            if (logData != null) {
                double lastLat = logData.latitude;
                double lastLong = logData.longitude;
                if (getDistance(currentLat, currentLong, lastLat, lastLong) > 50.0) {
                    isNeed = true;
                }
            } else {
                isNeed = true;
            }
        }
        return isNeed;
    }

    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void setPrayerAlarm(int secondAfter) {
        Intent intent = new Intent(mContext, AlarmReceiverActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm_manager = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        alarm_manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (secondAfter * 1000), pendingIntent);
        Toast.makeText(mContext, "Azan after : " + secondAfter / 3600 + " Hour " + ((secondAfter % 3600) / 60) + " Minute", Toast.LENGTH_LONG).show();
    }

    public static int getCurrentPrayer() {
        if (nextPrayerNumber == 0) {
            return 4;
        }
        return nextPrayerNumber - 1;
    }
}
