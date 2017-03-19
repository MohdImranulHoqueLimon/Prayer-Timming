package com.limon.PrayerTiming.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlarmReceiverActivity extends Activity {

    private MediaPlayer mMediaPlayer;
    private PowerManager.WakeLock mWakeLock;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Wake Log");
        mWakeLock.acquire();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        setContentView(R.layout.alarm);
        ButterKnife.bind(this);
        playAzanAlert();
    }


    @OnClick(R.id.btnStopAlarm)
    public void stopAlarm() {
        mMediaPlayer.stop();
        finish();
    }

    private void playAzanAlert() {
        Prayer prayer = new Prayer(getApplicationContext());
        int nextTimeInSecond = prayer.getNextPrayerInSecond();
        prayer.setPrayerAlarm(nextTimeInSecond);

        try{
            mMediaPlayer = MediaPlayer.create(AlarmReceiverActivity.this, R.raw.azan);
            mMediaPlayer.start();
        } catch (Exception exception) {

        }
    }

    protected void onStop() {
        super.onStop();
        mMediaPlayer.stop();
        mWakeLock.release();
    }

}

