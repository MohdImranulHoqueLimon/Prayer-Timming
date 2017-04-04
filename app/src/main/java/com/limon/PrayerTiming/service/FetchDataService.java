package com.limon.PrayerTiming.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.limon.PrayerTiming.GPS.GPSTracker;
import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.dbhelper.TimeDbHelper;
import com.limon.PrayerTiming.helper.Helper;
import com.limon.PrayerTiming.http.ApiClient;
import com.limon.PrayerTiming.http.TimeZoneApiClient;
import com.limon.PrayerTiming.http.time.PrayerTimeApiInterface;
import com.limon.PrayerTiming.http.time.model.PrayerTime;
import com.limon.PrayerTiming.http.time.model.Timing;
import com.limon.PrayerTiming.http.timezone.TimeZoneApiInterface;
import com.limon.PrayerTiming.http.timezone.model.TimeZoneDetails;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Limon on 12/24/2016.
 */

public class FetchDataService extends Service {

    TimeDbHelper timeDbHelper;
    GPSTracker gpsTracker;
    Intent mFetchIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mFetchIntent = intent;
        timeDbHelper = new TimeDbHelper(getApplicationContext());
        getTimeZoneName();
        return START_STICKY;
    }

    private void getTimeZoneName() {

        gpsTracker = new GPSTracker(getApplicationContext());
        String location = gpsTracker.getLatitude() + "," + gpsTracker.getLongitude();
        long unixTimeStamp = System.currentTimeMillis() / 1000L;

        TimeZoneApiInterface timeZoneApiInterface = TimeZoneApiClient.getClient().create(TimeZoneApiInterface.class);
        Call<TimeZoneDetails> call = timeZoneApiInterface.getTimeZoneDetails(
                location, unixTimeStamp, Helper.TIME_ZONE_API_KEY
        );

        call.enqueue(new Callback<TimeZoneDetails>() {
            @Override
            public void onResponse(Call<TimeZoneDetails> call, Response<TimeZoneDetails> response) {
                try {
                    TimeZoneDetails timeZoneDetails = response.body();
                    if (timeZoneDetails.getStatus() == null) {
                        throw new NullPointerException("Error occurred");
                    }
                    fetchPrayerTimeData(timeZoneDetails.getTimeZoneId());
                } catch (Exception ex) {
                    Results.showLog("Screwed up? fetch time zone");
                    fetchPrayerTimeData(Helper.getTimeZoneString());
                }
            }

            @Override
            public void onFailure(Call<TimeZoneDetails> call, Throwable t) {
                Results.showLog("On failure fetch time zone string");
                fetchPrayerTimeData(Helper.getTimeZoneString());
            }
        });
    }

    private synchronized void fetchPrayerTimeData(String timeZoneString) {

        gpsTracker = new GPSTracker(getApplicationContext());

        int currentYear = Helper.getCurrentYear();
        int currentMonth = Helper.getCurrentMonth() + 1;
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        PrayerTimeApiInterface prayerTimeApiInterface = ApiClient.getClient().create(PrayerTimeApiInterface.class);
        Call<PrayerTime> call = prayerTimeApiInterface.getPrayerTime(latitude, longitude, timeZoneString, 2, currentMonth, currentYear);

        call.enqueue(new Callback<PrayerTime>() {
            @Override
            public void onResponse(Call<PrayerTime> call, Response<PrayerTime> response) {
                try {
                    PrayerTime prayerTime = response.body();
                    if (prayerTime.getPrayertimeData() == null || prayerTime.getStatusCode() == 400) {
                        throw new NullPointerException("Error occurred");
                    }
                    onSuccessFetchTimeData(prayerTime);
                } catch (Exception ex) {
                    Results.showToast(getApplicationContext(), "Problem: Check network connection and/or location service");
                    Results.showLog("Screwed up? fetch data");
                    onFailedFetchData();
                }
            }

            @Override
            public void onFailure(Call<PrayerTime> call, Throwable t) {
                Results.showToast(getApplicationContext(), "Check Network Connection");
                Results.showLog("On failure fetch data");
                onFailedFetchData();
            }
        });
    }

    private synchronized void onSuccessFetchTimeData(PrayerTime prayerTime) {
        if (prayerTime.getPrayertimeData() != null) {
            try {
                //Clear previous data
                timeDbHelper.clearTimeTableTruncate();
                int total_data = prayerTime.getPrayertimeData().size();

                for (int i = 0; i < total_data; i++) {

                    Timing timing = prayerTime.getPrayertimeData().get(i).getTiming();
                    String readableDate = prayerTime.getPrayertimeData().get(i).getDate().getReadable();
                    String timestamp = prayerTime.getPrayertimeData().get(i).getDate().getTimestamp();

                    timeDbHelper.insertPrayerTime(
                            readableDate,
                            timing.getFajrTime(),
                            timing.getSunriseTime(),
                            timing.getDhuhrTime(),
                            timing.getAsrTime(),
                            timing.getSunset(),
                            timing.getMaghribeTime(),
                            timing.getIshaTime(),
                            timing.getImsakTime(),
                            timing.getMidnightTime(),
                            timestamp
                    );
                }
                onFinishDataProcess();

            } catch (Exception ex) {

            }
        }
        stopSelf();
    }

    private void onFailedFetchData(){
        Intent intent = new Intent("com.prayertime.FETCH_FINISHED");
        getApplicationContext().sendBroadcast(intent);
    }

    private void onFinishDataProcess() {
        saveLogData();
        boolean isBackgroundProcess = mFetchIntent.getBooleanExtra("IS_BACKGROUND_PROCESS", false);
        if (isBackgroundProcess) {
            Prayer prayer = new Prayer(getApplicationContext());
            int nextPrayerInSecond = prayer.getNextPrayerInSecond();
            prayer.setPrayerAlarm(nextPrayerInSecond);
        } else {
            Intent intent = new Intent("com.prayertime.FETCH_FINISHED");
            getApplicationContext().sendBroadcast(intent);
        }
    }

    //Save last fetched time, location info
    private void saveLogData() {
        String currentDateTime = Helper.getCurrentDate();
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        String location = "";
        timeDbHelper.insertLog(currentDateTime, latitude, longitude, location);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
