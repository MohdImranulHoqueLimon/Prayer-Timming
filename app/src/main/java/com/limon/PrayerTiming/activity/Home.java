package com.limon.PrayerTiming.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.R;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.dbhelper.TimeDbHelper;
import com.limon.PrayerTiming.helper.Helper;
import com.limon.PrayerTiming.http.time.model.Timing;
import com.limon.PrayerTiming.service.FetchDataService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Home extends AppCompatActivity {

    @BindView(R.id.fajrTime)
    TextView mFajrTime;
    @BindView(R.id.dhuhrTime)
    TextView mDhuhrTime;
    @BindView(R.id.asr_time)
    TextView mAsarTime;
    @BindView(R.id.maghrib_time)
    TextView mMaghribTime;
    @BindView(R.id.ishatime)
    TextView mIshaTime;
    @BindView(R.id.todayDate)
    TextView mTodayDate;
    @BindView(R.id.prayerTime)
    TextView mUpcomingPrayer;
    @BindView(R.id.timeleft)
    TextView mTimeLeft;

    ProgressDialog progressBar;
    private Prayer prayer;

    public static int res = 1;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // A broadcast receiver fired when time fetching will done
        IntentFilter intentFilter = new IntentFilter("com.prayertime.FETCH_FINISHED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showTimingOnView();
                progressBar.hide();
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        startFetchDataService();
        super.onResume();
    }

    private void startFetchDataService() {

        prayer = new Prayer(getApplicationContext());
        if (prayer.isNeedFetchTime()) {
            Results.showLog("new fetch");
            progressBar = new ProgressDialog(this);
            progressBar.setMessage("Getting initial data ...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();

            try {
                Intent intent = new Intent(getBaseContext(), FetchDataService.class);
                getApplicationContext().startService(intent);
            } catch (Exception e) {
                Log.d("salat_time", "Exception starting fetch service");
            }
        } else {
            Results.showLog("Did not fetch");
            showTimingOnView();
        }
        prayer = null;
    }

    public void showTimingOnView() {
        try {
            TimeDbHelper timeDbHelper = new TimeDbHelper(getApplicationContext());
            Timing timingObj = timeDbHelper.getPrayerTime(Helper.getCurrentDate("with space"));

            String fajrTime = timingObj.getFajrTime();
            String dhuhrTime = timingObj.getDhuhrTime();
            String asrTime = timingObj.getAsrTime();
            String maghribTime = timingObj.getMaghribeTime();
            String ishaTime = timingObj.getIshaTime();

            String formatedFajrTime = Helper.get24TimeTo12HourTime(fajrTime, "h:mm a");
            String formatedDhuhrTime = Helper.get24TimeTo12HourTime(dhuhrTime, "h:mm a");
            String formatedAsrTime = Helper.get24TimeTo12HourTime(asrTime, "h:mm a");
            String formatedMaghribtime = Helper.get24TimeTo12HourTime(maghribTime, "h:mm a");
            String formatedIshaTime = Helper.get24TimeTo12HourTime(ishaTime, "h:mm a");

            mFajrTime.setText(formatedFajrTime);
            mDhuhrTime.setText(formatedDhuhrTime);
            mAsarTime.setText(formatedAsrTime);
            mMaghribTime.setText(formatedMaghribtime);
            mIshaTime.setText(formatedIshaTime);

            prayer = new Prayer(getApplicationContext());
            int secondToNextPrayer = prayer.getNextPrayerInSecond();

            String upcommingPrayer = prayer.prayerName[prayer.currentPrayerNumber];
            mUpcomingPrayer.setText(upcommingPrayer);
            mTimeLeft.setText((secondToNextPrayer/3600)+ " HRS " + ((secondToNextPrayer%3600)/60 ) + " MINS LEFT");

            prayer.setPrayerAlarm(secondToNextPrayer);

        } catch (NullPointerException nullPointerException) {
            nullPointerException.printStackTrace();
        }

        mTodayDate.setText(Helper.getCurrentDate("with space"));
    }
}
