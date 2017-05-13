package com.limon.PrayerTiming.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.limon.PrayerTiming.GPS.GPSTracker;
import com.limon.PrayerTiming.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Limon on 3/23/2017.
 */

public class QiblaFragment extends Fragment implements SensorEventListener {

    @BindView(R.id.imageViewCompass)
    ImageView image;
    //@BindView(R.id.arrowImage)
    //ImageView mArrow;

    @BindView(R.id.qibla_ad_view)
    AdView mAdView;

    public static final double QIBLA_LATITUDE = Math.toRadians(21.423333);
    public static final double QIBLA_LONGITUDE = Math.toRadians(39.823333);

    private float currentDegree = 0f;
    private float qiblaDirectionAngle = 0f;
    private SensorManager mSensorManager;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.compass, container, false);
        ButterKnife.bind(this, rootView);
        this.mContext = getContext();
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(mContext, "ca-app-pub-7856893858613226~5153369595");

        // Create an ad request. Check your logcat output for the hashed device ID to get test ads
        // on a physical device. e.g "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."

        //TODO; Remove .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) before deploy
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        setQiblaDirection();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    //TODO; should checkout if compass works with qibla direction perfectly
    //TODO; check if device is able to display compass animation
    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        qiblaDirectionAngle = (currentDegree + qiblaDirectionAngle) % 360;
        //RotateAnimation raDirection = new RotateAnimation(0, qiblaDirectionAngle, Animation.RELATIVE_TO_SELF,
                //0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // how long the animation will take place
        ra.setDuration(210);
        //raDirection.setDuration(210);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        //raDirection.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        //mArrow.startAnimation(ra);
        currentDegree = -degree;
    }

    private void setQiblaDirection() {
        GPSTracker gpsTracker = new GPSTracker(mContext);
        float angle = (float) getQiblaDirectionFromNorth(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        if (angle < 0) angle = 360 + angle;
        this.qiblaDirectionAngle = angle;
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(0, angle, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);
        //mArrow.startAnimation(ra);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static double getQiblaDirectionFromNorth(double degLatitude, double degLongitude) {

        double latitude2 = Math.toRadians(degLatitude);
        double longitude = Math.toRadians(degLongitude);

        double soorat = Math.sin(QIBLA_LONGITUDE - longitude);
        double makhraj = Math.cos(latitude2) * Math.tan(QIBLA_LATITUDE)
                - Math.sin(latitude2) * Math.cos(QIBLA_LONGITUDE - longitude);
        double returnValue = Math.toDegrees(Math.atan(soorat / makhraj));

        /*
        Math.atan will return value between -90...90 but arc tan of +180 degree plus is also
        the same Never remove thes if..else segments or you will get qibla direction
        with 180 degree difference Until
        */

        if (latitude2 > QIBLA_LATITUDE) {
            if ((longitude > QIBLA_LONGITUDE || longitude < (Math.toRadians(-180d) + QIBLA_LONGITUDE))
                    && (returnValue > 0 && returnValue <= 90)) {
                returnValue += 180;
            } else if (!(longitude > QIBLA_LONGITUDE || longitude < (Math
                    .toRadians(-180d) + QIBLA_LONGITUDE))
                    && (returnValue > -90 && returnValue < 0)) {

                returnValue += 180;
            }
        }
        if (latitude2 < QIBLA_LATITUDE) {

            if ((longitude > QIBLA_LONGITUDE || longitude < (Math.toRadians(-180d) + QIBLA_LONGITUDE))
                    && (returnValue > 0 && returnValue < 90)) {
                returnValue += 180;
            }
            if (!(longitude > QIBLA_LONGITUDE || longitude < (Math.toRadians(-180d) + QIBLA_LONGITUDE))
                    && (returnValue > -90 && returnValue <= 0)) {
                returnValue += 180;
            }
        }
        return returnValue - 10;
    }
}
