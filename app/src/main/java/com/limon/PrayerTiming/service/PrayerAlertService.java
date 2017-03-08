package com.limon.PrayerTiming.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.dbhelper.TimeDbHelper;

/**
 * Created by Limon on 3/6/2017.
 */

public class PrayerAlertService extends Service {

    private TimeDbHelper mTimeDbHelper;
    private Prayer mPrayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mTimeDbHelper = new TimeDbHelper(getApplicationContext());
        mPrayer = new Prayer(getApplicationContext());

        if (mPrayer.isNeedFetchTime()) {
            try {
                Results.showLog("Service Need to fetch time");
                Intent fetchIntent = new Intent(getBaseContext(), FetchDataService.class);
                fetchIntent.putExtra("IS_BACKGROUND_PROCESS", true);
                getApplicationContext().startService(fetchIntent);
            } catch (Exception e) {

            }
        } else {
            Results.showLog("Service Need to fetch time");
            int secondToNextPrayer = mPrayer.getNextPrayerInSecond();
            mPrayer.setPrayerAlarm(secondToNextPrayer);
        }
        return START_STICKY;
    }
}
