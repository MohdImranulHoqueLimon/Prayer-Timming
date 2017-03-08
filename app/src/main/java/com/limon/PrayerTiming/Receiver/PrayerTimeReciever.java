package com.limon.PrayerTiming.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
                Results.showLog("Service Need to fetch time");
                Intent fetchIntent = new Intent(mContext, FetchDataService.class);
                fetchIntent.putExtra("IS_BACKGROUND_PROCESS", true);
                mContext.startService(fetchIntent);
            } catch (Exception e) {

            }
        } else {
            Results.showLog("Service Need to fetch time");
            int secondToNextPrayer = mPrayer.getNextPrayerInSecond();
            mPrayer.setPrayerAlarm(secondToNextPrayer);
        }
    }
}
