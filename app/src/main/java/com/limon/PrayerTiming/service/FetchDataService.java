package com.limon.PrayerTiming.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
    double curLatitude, curLongitude;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFetchIntent = intent;
        timeDbHelper = new TimeDbHelper(getApplicationContext());
        setCurrentLocation();
        getTimeZoneName();
        return START_STICKY;
    }

    private void setCurrentLocation() {
        try {
            curLatitude = mFetchIntent.getDoubleExtra("latitude", 0.0);
            curLongitude = mFetchIntent.getDoubleExtra("longitude", 0.0);
            if (curLongitude == 0.0 || curLongitude == 0.0) {
                gpsTracker = new GPSTracker(getApplicationContext());
                curLatitude = gpsTracker.getLatitude();
                curLongitude = gpsTracker.getLongitude();
            }
        } catch (Exception exception) {

        }
    }

    private void getTimeZoneName() {

        if(curLongitude != 0.0 || curLongitude != 0.0) {
            String location = curLatitude + "," + curLongitude;
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
                        fetchPrayerTimeData(Helper.getTimeZoneString());
                    }
                }

                @Override
                public void onFailure(Call<TimeZoneDetails> call, Throwable t) {
                    fetchPrayerTimeData(Helper.getTimeZoneString());
                }
            });
        } else {
            stopSelf();
        }
    }

    private synchronized void fetchPrayerTimeData(String timeZoneString) {

        if(curLatitude != 0.0 && curLongitude != 0.0) {

            int currentYear = Helper.getCurrentYear();
            int currentMonth = Helper.getCurrentMonth() + 1;

            PrayerTimeApiInterface prayerTimeApiInterface = ApiClient.getClient().create(PrayerTimeApiInterface.class);
            Call<PrayerTime> call = prayerTimeApiInterface.getPrayerTime(curLatitude, curLongitude, timeZoneString, 2, currentMonth, currentYear);

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
                        onFailedFetchData();
                    }
                }

                @Override
                public void onFailure(Call<PrayerTime> call, Throwable t) {
                    onFailedFetchData();
                }
            });
        } else {
            stopSelf();
        }
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

    private void onFailedFetchData() {
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
        String location = "";
        timeDbHelper.insertLog(currentDateTime, curLatitude, curLongitude, location);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
