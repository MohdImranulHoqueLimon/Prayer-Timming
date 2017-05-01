package com.limon.PrayerTiming.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.limon.PrayerTiming.GPS.GPSTracker;
import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.R;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.dbhelper.TimeDbHelper;
import com.limon.PrayerTiming.helper.Helper;
import com.limon.PrayerTiming.http.time.model.Timing;
import com.limon.PrayerTiming.service.FetchDataService;
import com.limon.PrayerTiming.utility.AjanTune;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimeFragment extends Fragment {

    @BindView(R.id.textAddress)
    TextView mLocationAddress;

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
    @BindView(R.id.txtArabicDate)
    TextView mTodayArabicDate;

    @BindView(R.id.txtUpcomingPrayer)
    TextView mUpcomingPrayer;
    @BindView(R.id.textTimeLeft)
    TextView mTimeLeft;

    @BindView(R.id.txtFajr)
    TextView mTxtFajr;
    @BindView(R.id.txtDhuhr)
    TextView mTxtDhuhr;
    @BindView(R.id.txtAsr)
    TextView mTxtAsr;
    @BindView(R.id.txtMaghrib)
    TextView mTxtMaghrib;
    @BindView(R.id.txtIsha)
    TextView mTxtIsha;

    @BindView(R.id.fajrToggleButton)
    ToggleButton mFajrToggleButton;
    @BindView(R.id.dhuhrToggleButton)
    ToggleButton mDhuhrToggleButton;
    @BindView(R.id.asrToggleButton)
    ToggleButton mAsrToggleButton;
    @BindView(R.id.maghribToggleButton)
    ToggleButton mMaghribToggleButton;
    @BindView(R.id.ishaToggleButton)
    ToggleButton mIshaToggleButton;

    @BindView(R.id.ad_view)
    AdView mAdView;

    private Prayer mPrayer;
    private BroadcastReceiver broadcastReceiver;
    private Context mContext;
    private static final int MY_PERMISSIONS_REQUEST = 99;
    private View rootView;
    private GPSTracker gpsTracker;
    private double currentLatitude, currentLongitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_time, container, false);
        ButterKnife.bind(this, rootView);

        this.rootView = rootView;
        gpsTracker = new GPSTracker(getContext());
        this.currentLatitude = gpsTracker.getLatitude();
        this.currentLongitude = gpsTracker.getLongitude();
        this.mContext = getContext();

        // Initialize the Mobile Ads SDK.
        //TODO; Do I need this line of Code?
        //MobileAds.initialize(mContext, "ca-app-pub-3940256099942544~3347511713");

        // Create an ad request. Check your logcat output for the hashed device ID to get test ads
        // on a physical device. e.g "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."

        //TODO; Remove .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) before deploy
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.mContext = getContext();
        setTimeBroadcastReceiver();

        if (AjanTune.isInitialSetTune(mContext)) {
            loadInitalTunePreferences();
        } else {
            loadSavedTunePreferences();
        }

        if (mAdView != null) {
            mAdView.resume();
        }
    }

    public void setTimeBroadcastReceiver() {
        //A broadcast receiver fired when time fetching will done
        IntentFilter intentFilter = new IntentFilter("com.prayertime.FETCH_FINISHED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                rootView.findViewById(R.id.anim).setVisibility(View.GONE);
                showTimingOnView();
                setStreetLocationName();
            }
        };
        mContext.registerReceiver(broadcastReceiver, intentFilter);
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
        if (isLocationServiceOn() == false) {
            showSettingsAlert();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
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
        mPrayer = new Prayer(getContext());
        if (mPrayer.isNeedFetchTime()) {
            rootView.findViewById(R.id.anim).setVisibility(View.VISIBLE);
            try {
                Intent intent = new Intent(getActivity(), FetchDataService.class);
                getActivity().startService(intent);
            } catch (Exception e) {

            }
        } else {
            showTimingOnView();
        }
        mPrayer = null;
    }

    private void showTimingOnView() {
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
                setHeaderInfoOnView();
            }
        } catch (NullPointerException nullPointerException) {
            nullPointerException.printStackTrace();
        }
        mTodayDate.setText(Helper.getCurrentDate("with space"));
        mTodayArabicDate.setText(Helper.getCurrentArabicDate());
    }

    private void setHeaderInfoOnView() {

        mPrayer = new Prayer(mContext);
        int secondToNextPrayer = mPrayer.getNextPrayerInSecond();
        String upcomingPrayer = mPrayer.prayerName[mPrayer.nextPrayerNumber];
        mUpcomingPrayer.setText(upcomingPrayer);
        mTimeLeft.setText((secondToNextPrayer / 3600) + " HRS " + ((secondToNextPrayer % 3600) / 60) + " MINS LEFT");

        mPrayer.setPrayerAlarm(secondToNextPrayer);
        setStreetLocationName();

        if (mPrayer.nextPrayerNumber == 0) {
            selectUpcomingPrayerView(mTxtFajr, mFajrTime);
        } else if (mPrayer.nextPrayerNumber == 1) {
            selectUpcomingPrayerView(mTxtDhuhr, mDhuhrTime);
        } else if (mPrayer.nextPrayerNumber == 2) {
            selectUpcomingPrayerView(mTxtAsr, mAsarTime);
        } else if (mPrayer.nextPrayerNumber == 3) {
            selectUpcomingPrayerView(mTxtMaghrib, mMaghribTime);
        } else if (mPrayer.nextPrayerNumber == 4) {
            selectUpcomingPrayerView(mTxtIsha, mIshaTime);
        }
    }

    private void selectUpcomingPrayerView(TextView prayerNameView, TextView prayerTimeView) {
        prayerNameView.setTextColor(getResources().getColor(R.color.colorLIGHTCORAL));
        prayerNameView.setTypeface(null, Typeface.BOLD);
        prayerTimeView.setTextColor(getResources().getColor(R.color.colorLIGHTCORAL));
    }

    private void loadInitalTunePreferences() {
        AjanTune.setTune(mContext, getResources().getString(R.string.fajr), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.dhuhr), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.asr), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.maghrib), true);
        AjanTune.setTune(mContext, getResources().getString(R.string.isha), true);

        mFajrToggleButton.setChecked(true);
        mDhuhrToggleButton.setChecked(true);
        mAsrToggleButton.setChecked(true);
        mMaghribToggleButton.setChecked(true);
        mIshaToggleButton.setChecked(true);
    }

    //initialize azan tune checkbox from shared preferences
    private void loadSavedTunePreferences() {
        mFajrToggleButton.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.fajr)));
        mDhuhrToggleButton.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.dhuhr)));
        mAsrToggleButton.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.asr)));
        mMaghribToggleButton.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.maghrib)));
        mIshaToggleButton.setChecked(AjanTune.getIsTune(mContext, getResources().getString(R.string.isha)));
    }

    @OnClick({R.id.fajrToggleButton, R.id.dhuhrToggleButton, R.id.asrToggleButton, R.id.maghribToggleButton, R.id.ishaToggleButton})
    public void setTuneSetting(View view) {
        boolean isChecked = ((ToggleButton) view).isChecked();
        switch (view.getId()) {
            case R.id.fajrToggleButton:
                AjanTune.setTune(mContext, getResources().getString(R.string.fajr), isChecked);
                break;
            case R.id.dhuhrToggleButton:
                AjanTune.setTune(mContext, getResources().getString(R.string.dhuhr), isChecked);
                break;
            case R.id.asrToggleButton:
                AjanTune.setTune(mContext, getResources().getString(R.string.asr), isChecked);
                break;
            case R.id.maghribToggleButton:
                AjanTune.setTune(mContext, getResources().getString(R.string.maghrib), isChecked);
                break;
            case R.id.ishaToggleButton:
                AjanTune.setTune(mContext, getResources().getString(R.string.isha), isChecked);
                break;
        }
    }

    //TODO; Get permission at runtime for marshmallow or upper version
    public void setStreetLocationName() {
        AddressTask addressTask = new AddressTask();
        addressTask.execute(currentLatitude, currentLongitude);
    }

    private class AddressTask extends AsyncTask<Double, Object, String> {

        protected String doInBackground(Double... locations) {
            String locationName = "";
            try {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(locations[0], locations[1], 4);

                String cityName = addresses.get(0).getAddressLine(0);
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);
                locationName = cityName + " " + stateName + " " + countryName;
            } catch (Exception exception) {

            }
            return locationName.replace("null", "");
        }

        protected void onPostExecute(String result) {
            mLocationAddress.setText(result);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            getActivity().unregisterReceiver(this.broadcastReceiver);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
