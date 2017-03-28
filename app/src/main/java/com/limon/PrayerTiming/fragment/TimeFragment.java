package com.limon.PrayerTiming.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

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
import butterknife.OnClick;

public class TimeFragment extends Fragment {

    @BindView(R.id.txtFajrTime)
    TextView mFajrTime;
    @BindView(R.id.txtDhuhrTime)
    TextView mDhuhrTime;
    @BindView(R.id.txtAsrTime)
    TextView mAsarTime;
    @BindView(R.id.txtMaghribTime)
    TextView mMaghribTime;
    @BindView(R.id.txtIshaTime)
    TextView mIshaTime;
    @BindView(R.id.txtEnglishDate)
    TextView mTodayDate;
    @BindView(R.id.txtUpcomingPrayer)
    TextView mUpcomingPrayer;
    @BindView(R.id.textTimeLeft)
    TextView mTimeLeft;
    @BindView(R.id.fajrSwitch)
    Switch mFajrSwitch;
    @BindView(R.id.dhuhrSwitch)
    Switch mDhuhrSwitch;
    @BindView(R.id.asrSwitch)
    Switch mAsrSwitch;
    @BindView(R.id.maghribSwitch)
    Switch mMaghribSwitch;
    @BindView(R.id.ishaSwitch)
    Switch mIshaSwitch;

    private ProgressDialog mProgressBar;
    private Prayer mPrayer;
    private BroadcastReceiver broadcastReceiver;
    private Context mContext;
    private static final int MY_PERMISSIONS_REQUEST = 99;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_time, container, false);
        ButterKnife.bind(this, rootView);

        //A broadcast receiver fired when time fetching will done
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
        this.mContext = getContext();
        mContext.registerReceiver(broadcastReceiver, intentFilter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.mContext = getContext();

        if (AjanTune.isInitialSetTune(mContext)) {
            loadInitalTunePreferences();
        } else {
            loadSavedTunePreferences();
        }
        Results.showLog("infinity", "infinity loop");
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mContext = getContext();
        if (!checkPermissionGranted()) {
            askForPermission();
        } else {
            startFetchDataService();
        }
    }

    private boolean checkPermissionGranted() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Results.showToast(getContext(), "Location service permission is denied !");
            return false;
        }
        return true;
    }

    private void askForPermission() {
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                MY_PERMISSIONS_REQUEST
        );
    }

    private boolean isLocationServiceOn() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startFetchDataService();
                }
                return;
            }
        }
    }

    private void startFetchDataService() {

        mPrayer = new Prayer(mContext);
        if (mPrayer.isNeedFetchTime()) {
            mProgressBar = new ProgressDialog(this.mContext);
            mProgressBar.setMessage("Getting initial data ...");
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressBar.show();

            try {
                Intent intent = new Intent(mContext, FetchDataService.class);
                mContext.startService(intent);
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
            TimeDbHelper timeDbHelper = new TimeDbHelper(mContext);
            Timing timingObj = timeDbHelper.getPrayerTime(Helper.getCurrentDate("with space"));

            if (timingObj != null) {
                String timeFormat = "h:mm a";
                mFajrTime.setText(timingObj.getFormattedFajrTime(timeFormat));
                mDhuhrTime.setText(timingObj.getFormattedDhuhrTime(timeFormat));
                mAsarTime.setText(timingObj.getFormattedAsrTime(timeFormat));
                mMaghribTime.setText(timingObj.getFormattedMaghribTime(timeFormat));
                mIshaTime.setText(timingObj.getFormattedIshaTime(timeFormat));

                mPrayer = new Prayer(mContext);
                int secondToNextPrayer = mPrayer.getNextPrayerInSecond();
                Results.showLog("nexttime", secondToNextPrayer + "");
                String upcommingPrayer = mPrayer.prayerName[mPrayer.currentPrayerNumber];
                mUpcomingPrayer.setText(upcommingPrayer);
                mTimeLeft.setText((secondToNextPrayer / 3600) + " HRS " + ((secondToNextPrayer % 3600) / 60) + " MINS LEFT");

                mPrayer.setPrayerAlarm(secondToNextPrayer);
            }
        } catch (NullPointerException nullPointerException) {
            nullPointerException.printStackTrace();
        }

        mTodayDate.setText(Helper.getCurrentDate("with space"));
    }

    private void loadInitalTunePreferences() {
        AjanTune.setTune(mContext, getResources().getString(R.string.fajr), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.dhuhr), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.asr), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.maghrib), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.isha), true);

        mFajrSwitch.setChecked(true);
        mDhuhrSwitch.setChecked(true);
        mAsrSwitch.setChecked(true);
        mMaghribSwitch.setChecked(true);
        mIshaSwitch.setChecked(true);
    }

    //initialize azan tune checkbox from shared preferences
    private void loadSavedTunePreferences() {
        mFajrSwitch.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.fajr)));
        mDhuhrSwitch.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.dhuhr)));
        mAsrSwitch.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.asr)));
        mMaghribSwitch.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.maghrib)));
        mIshaSwitch.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.isha)));
    }

    @OnClick({R.id.fajrSwitch, R.id.dhuhrSwitch, R.id.asrSwitch, R.id.maghribSwitch, R.id.ishaSwitch})
    public void setTuneSetting(View view) {
        boolean isChecked = ((Switch) view).isChecked();
        switch (view.getId()) {
            case R.id.fajrSwitch:
                AjanTune.setTune(mContext, getResources().getString(R.string.fajr), isChecked);
                break;
            case R.id.dhuhrSwitch:
                AjanTune.setTune(mContext, getResources().getString(R.string.dhuhr), isChecked);
                break;
            case R.id.asrSwitch:
                AjanTune.setTune(mContext, getResources().getString(R.string.asr), isChecked);
                break;
            case R.id.maghribSwitch:
                AjanTune.setTune(mContext, getResources().getString(R.string.maghrib), isChecked);
                break;
            case R.id.ishaSwitch:
                AjanTune.setTune(mContext, getResources().getString(R.string.isha), isChecked);
                break;
        }
    }
}
