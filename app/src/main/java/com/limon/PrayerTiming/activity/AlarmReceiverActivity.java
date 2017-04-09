package com.limon.PrayerTiming.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import com.limon.PrayerTiming.Prayer;
import com.limon.PrayerTiming.R;
import com.limon.PrayerTiming.Result.Results;
import com.limon.PrayerTiming.helper.Helper;
import com.limon.PrayerTiming.utility.AjanTune;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlarmReceiverActivity extends Activity {

    private MediaPlayer mMediaPlayer;
    private PowerManager.WakeLock mWakeLock;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //TODO; Deprecated full wake lock should remove
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
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        finish();
    }

    private void playAzanAlert() {
        Prayer prayer = new Prayer(getApplicationContext());
        int nextTimeInSecond = prayer.getNextPrayerInSecond();
        prayer.setPrayerAlarm(nextTimeInSecond);

        try {
            if (isRingToneMode()) {
                int currentPrayer = Prayer.getCurrentPrayer();
                if (currentPrayer == 0) {
                    //TODO; AZAN MP3 SHOULD CHANGE SIZE MINIMISE AND ALSO THERE IS EXTRA SPECH AT THE LAST OF THE AZAN
                    mMediaPlayer = MediaPlayer.create(AlarmReceiverActivity.this, R.raw.azan_fajr);
                } else {
                    mMediaPlayer = MediaPlayer.create(AlarmReceiverActivity.this, R.raw.azan);
                }
                mMediaPlayer = MediaPlayer.create(AlarmReceiverActivity.this, R.raw.azan);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        finish();
                    }
                });
                mMediaPlayer.start();
            }
        } catch (Exception exception) {}
        prayer = null;
    }

    private boolean isRingToneMode() {

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        boolean isRingToneNormal = (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL);

        int currentPrayer = Prayer.getCurrentPrayer();
        Context context = getApplicationContext();

        boolean isAlertActive = true;
        if (currentPrayer == 0) {
            isAlertActive = AjanTune.getIsTune(context, getResources().getString(R.string.fajr));
        } else if (currentPrayer == 1) {
            isAlertActive = AjanTune.getIsTune(context, getResources().getString(R.string.dhuhr));
        } else if (currentPrayer == 2) {
            isAlertActive = AjanTune.getIsTune(context, getResources().getString(R.string.asr));
        } else if (currentPrayer == 3) {
            isAlertActive = AjanTune.getIsTune(context, getResources().getString(R.string.maghrib));
        } else {
            isAlertActive = AjanTune.getIsTune(context, getResources().getString(R.string.isha));
        }

        return (isRingToneNormal & isAlertActive);
    }

    protected void onStop() {
        try {
            mMediaPlayer.stop();
            mWakeLock.release();
        } catch (Exception exception) {

        }
        super.onStop();
    }

}

