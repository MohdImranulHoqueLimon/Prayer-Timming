package com.limon.PrayerTiming.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.R;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.dbhelper.TimeDbHelper;
import com.limon.PrayerTiming.helper.Helper;
import com.limon.PrayerTiming.http.time.model.Timing;
import com.limon.PrayerTiming.service.FetchDataService;
import com.limon.PrayerTiming.utility.AjanTune;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

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
    @BindView(R.id.checkBoxFajrTune)
    CheckBox mFajrCheckBox;
    @BindView(R.id.checkBoxDhuhrTune)
    CheckBox mDhuhrCheckBox;
    @BindView(R.id.checkBoxAsrTune)
    CheckBox mAsrCheckBox;
    @BindView(R.id.checkBoxMagribTune)
    CheckBox mMaghribCheckBox;
    @BindView(R.id.checkBoxIshaTune)
    CheckBox mIshaCheckBox;

    private ProgressDialog mProgressBar;
    private Prayer mPrayer;
    private BroadcastReceiver broadcastReceiver;
    private Context mContext;

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
                if (mProgressBar != null && mProgressBar.isShowing()) {
                    mProgressBar.hide();
                }
                showTimingOnView();
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        mContext = getApplicationContext();
        loadSavedTunePreferences();
        startFetchDataService();
        super.onResume();
    }

    private void startFetchDataService() {

        mPrayer = new Prayer(getApplicationContext());
        if (mPrayer.isNeedFetchTime()) {
            mProgressBar = new ProgressDialog(this);
            mProgressBar.setMessage("Getting initial data ...");
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressBar.show();

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
        mPrayer = null;
    }

    public void showTimingOnView() {
        try {

            TimeDbHelper timeDbHelper = new TimeDbHelper(getApplicationContext());
            Timing timingObj = timeDbHelper.getPrayerTime(Helper.getCurrentDate("with space"));

            String timeFormat = "h:mm a";
            mFajrTime.setText(timingObj.getFormattedFajrTime(timeFormat));
            mDhuhrTime.setText(timingObj.getFormattedDhuhrTime(timeFormat));
            mAsarTime.setText(timingObj.getFormattedAsrTime(timeFormat));
            mMaghribTime.setText(timingObj.getFormattedMaghribTime(timeFormat));
            mIshaTime.setText(timingObj.getFormattedIshaTime(timeFormat));

            mPrayer = new Prayer(getApplicationContext());
            int secondToNextPrayer = mPrayer.getNextPrayerInSecond();

            String upcommingPrayer = mPrayer.prayerName[mPrayer.currentPrayerNumber];
            mUpcomingPrayer.setText(upcommingPrayer);
            mTimeLeft.setText((secondToNextPrayer / 3600) + " HRS " + ((secondToNextPrayer % 3600) / 60) + " MINS LEFT");

            mPrayer.setPrayerAlarm(secondToNextPrayer);

        } catch (NullPointerException nullPointerException) {
            nullPointerException.printStackTrace();
        }

        mTodayDate.setText(Helper.getCurrentDate("with space"));
    }

    //initialize azan tune checkbox from shared preferences
    private void loadSavedTunePreferences() {
        mFajrCheckBox.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.fajr)));
        mDhuhrCheckBox.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.dhuhr)));
        mAsrCheckBox.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.asr)));
        mMaghribCheckBox.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.maghrib)));
        mIshaCheckBox.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.isha)));
    }

    public void setTuneSetting(View view) {
        boolean isChecked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.checkBoxFajrTune:
                AjanTune.setTune(mContext, getResources().getString(R.string.fajr), isChecked);
                break;
            case R.id.checkBoxDhuhrTune:
                AjanTune.setTune(mContext, getResources().getString(R.string.dhuhr), isChecked);
                break;
            case R.id.checkBoxAsrTune:
                AjanTune.setTune(mContext, getResources().getString(R.string.asr), isChecked);
                break;
            case R.id.checkBoxMagribTune:
                AjanTune.setTune(mContext, getResources().getString(R.string.maghrib), isChecked);
                break;
            case R.id.checkBoxIshaTune:
                AjanTune.setTune(mContext, getResources().getString(R.string.isha), isChecked);
                break;
        }
    }
}
