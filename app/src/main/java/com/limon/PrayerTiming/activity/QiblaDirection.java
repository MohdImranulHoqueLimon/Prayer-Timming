/**
 * Created by Limon on 2/23/2017.
 */

package com.limon.PrayerTiming.activity;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.limon.PrayerTiming.GPS.GPSTracker;
import com.limon.PrayerTiming.R;
import com.limon.PrayerTiming.Result.Results;

import butterknife.BindView;
import butterknife.ButterKnife;

//This class represent logic for computing Qibla angle form north
public class QiblaDirection extends AppCompatActivity implements SensorEventListener{

    @BindView(R.id.imageViewCompass)
    ImageView image;
    @BindView(R.id.arrowImage)
    ImageView mArrow;
    @BindView(R.id.tvHeading)
    TextView tvHeading;

    public static final double QIBLA_LATITUDE = Math.toRadians(21.423333);
    public static final double QIBLA_LONGITUDE = Math.toRadians(39.823333);

    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        ButterKnife.bind(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        setQiblaDirection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // how long the animation will take place
        ra.setDuration(210);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

    private void setQiblaDirection() {
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

        float angle = (float) getQiblaDirectionFromNorth(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        Results.showLog("qibla direction 1 ", angle + "");
        if(angle < 0) angle = 360 + angle;
        Results.showLog("qibla direction 2 ", angle + "");
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(0, angle, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);
        mArrow.startAnimation(ra);
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

