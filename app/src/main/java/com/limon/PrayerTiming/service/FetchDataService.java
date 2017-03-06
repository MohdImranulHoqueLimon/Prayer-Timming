package com.limon.PrayerTiming.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.limon.PrayerTiming.GPS.GPSTracker;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.dbhelper.TimeDbHelper;
import com.limon.PrayerTiming.helper.Helper;
import com.limon.PrayerTiming.http.ApiClient;
import com.limon.PrayerTiming.http.time.PrayerTimeApiInterface;
import com.limon.PrayerTiming.http.time.model.PrayerTime;
import com.limon.PrayerTiming.http.time.model.Timing;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Limon on 12/24/2016.
 */

public class FetchDataService extends Service {

    TimeDbHelper timeDbHelper;
    GPSTracker gpsTracker;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timeDbHelper = new TimeDbHelper(getApplicationContext());
        fetchPrayerTimeData();
        return START_STICKY;
    }

    private synchronized void fetchPrayerTimeData() {

        gpsTracker = new GPSTracker(getApplicationContext());

        String timeZoneString = Helper.getTimeZoneString();
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
                    if (prayerTime.getPrayertimeData() == null) {
                        throw new NullPointerException("Error occurred");
                    }
                    onSuccessFetchTimeData(prayerTime);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Results.showLog("Screwed up? fetch data");
                }
            }

            @Override
            public void onFailure(Call<PrayerTime> call, Throwable t) {
                Results.showLog("On failure fetch data");
            }
        });
    }

    private synchronized void onSuccessFetchTimeData(PrayerTime prayerTime) {
        if (prayerTime.getPrayertimeData() != null) {
            try {
                //Clear previous data
                timeDbHelper.clearTableTruncate();
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
                Intent intent = new Intent("com.prayertime.FETCH_FINISHED");
                getApplicationContext().sendBroadcast(intent);
                saveLogData();

            } catch (Exception ex) {

            }
        }
        stopSelf();
    }

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
