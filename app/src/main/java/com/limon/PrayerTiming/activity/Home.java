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

    ProgressDialog mProgressBar;
    private Prayer prayer;

    public static int res = 1;
    private BroadcastReceiver broadcastReceiver;

    public static CheckBox fajrCheckBox, dhuhrCheckBox, asrCheckBox, magribCheckBox, ishaCheckBox;
    public static final String PREFS_NAME = "TunePrefs";
    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        fajrCheckBox   =  	(CheckBox) findViewById(R.id.checkBoxFajrTune);
        dhuhrCheckBox  =  	(CheckBox) findViewById(R.id.checkBoxDhuhrTune);
        asrCheckBox    =	(CheckBox) findViewById(R.id.checkBoxAsrTune);
        magribCheckBox =	(CheckBox) findViewById(R.id.checkBoxMagribTune);
        ishaCheckBox   =	(CheckBox) findViewById(R.id.checkBoxIshaTun);
        loadSavedTunePreferences();

        // A broadcast receiver fired when time fetching will done
        IntentFilter intentFilter = new IntentFilter("com.prayertime.FETCH_FINISHED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(mProgressBar != null && mProgressBar.isShowing()) {
                    mProgressBar.hide();
                }
                showTimingOnView();
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
        prayer = null;
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

    private void loadSavedTunePreferences() {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if( settings.getString("fajartune", "").toString().equals("yes") ){
            fajrCheckBox.setChecked(true);
        }
        if( settings.getString("dhuhrtune", "").toString().equals("yes") ){
            dhuhrCheckBox.setChecked(true);
        }
        if( settings.getString("asrtune", "").toString().equals("yes") ){
            asrCheckBox.setChecked(true);
        }
        if( settings.getString("magribtune", "").toString().equals("yes") ){
            magribCheckBox.setChecked(true);
        }
        if( settings.getString("ishatune", "").toString().equals("yes") ){
            ishaCheckBox.setChecked(true);
        }
    }

    public void setTuneSetting(View view) {

        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {

            case R.id.checkBoxFajrTune:
                if (checked){
                    fajrCheckBox.setChecked(true);
                    mToast = Toast.makeText(getApplicationContext(), "Fajr Azan On", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("fajartune", "yes");
                    editor.commit();
                }
                else{
                    fajrCheckBox.setChecked(false);
                    mToast = Toast.makeText(getApplicationContext(), "Fajr Azan Off", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("fajartune", "no");
                    editor.commit();
                }
                break;

            case R.id.checkBoxDhuhrTune:
                if (checked){
                    dhuhrCheckBox.setChecked(true);
                    mToast = Toast.makeText(getApplicationContext(), "Dhuhr Azan On", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("dhuhrtune", "yes");
                    editor.commit();
                }
                else{
                    fajrCheckBox.setChecked(false);
                    mToast = Toast.makeText(getApplicationContext(), "Dhuhr Azan Off", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("dhuhrtune", "no");
                    editor.commit();
                }
                break;

            case R.id.checkBoxAsrTune:
                if (checked){
                    asrCheckBox.setChecked(true);
                    mToast = Toast.makeText(getApplicationContext(), "Asr Azan On", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("asrtune", "yes");
                    editor.commit();
                }
                else{
                    asrCheckBox.setChecked(false);
                    mToast = Toast.makeText(getApplicationContext(), "Asr Azan Off", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("asrtune", "no");
                    editor.commit();
                }
                break;

            case R.id.checkBoxMagribTune:
                if (checked){
                    magribCheckBox.setChecked(true);
                    mToast = Toast.makeText(getApplicationContext(), "Magrib Azan On", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("magribtune", "yes");
                    editor.commit();
                }
                else{
                    magribCheckBox.setChecked(false);
                    mToast = Toast.makeText(getApplicationContext(), "Magrib Azan Off", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("magribtune", "no");
                    editor.commit();
                }
                break;

            case R.id.checkBoxIshaTun:
                if (checked){
                    ishaCheckBox.setChecked(true);
                    mToast = Toast.makeText(getApplicationContext(), "Isha Azan On", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("ishatune", "yes");
                    editor.commit();
                }
                else{
                    ishaCheckBox.setChecked(false);
                    mToast = Toast.makeText(getApplicationContext(), "Isha Azan Off", Toast.LENGTH_LONG);
                    mToast.show();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("ishatune", "no");
                    editor.commit();
                }
                break;

        }
    }
}
