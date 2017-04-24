package com.limon.PrayerTiming.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.limon.PrayerTiming.GPS.GPSTracker;
import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.service.FetchDataService;

/**
 * Created by Limon on 3/8/2017.
 */

public class PrayerTimeReciever extends BroadcastReceiver {

    Prayer mPrayer;
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.mContext = context;
        mPrayer = new Prayer(mContext);

        if (mPrayer.isNeedFetchTime()) {
            try {
                GPSTracker gpsTracker = new GPSTracker(mContext);
                Intent fetchIntent = new Intent(mContext, FetchDataService.class);
                fetchIntent.putExtra("IS_BACKGROUND_PROCESS", true);
                fetchIntent.putExtra("latitude", gpsTracker.getLatitude());
                fetchIntent.putExtra("longitude", gpsTracker.getLongitude());
                mContext.startService(fetchIntent);
            } catch (Exception e) {

            }
        } else {
            int secondToNextPrayer = mPrayer.getNextPrayerInSecond();
            mPrayer.setPrayerAlarm(secondToNextPrayer);
        }
    }
}
