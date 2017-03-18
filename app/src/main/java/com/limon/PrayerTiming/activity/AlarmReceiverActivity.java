package com.limon.PrayerTiming.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Window;
import android.view.WindowManager;

public class AlarmReceiverActivity extends Activity {

    public static int azan_duration_in_milisecond;
    public static boolean sound = true;
    private MediaPlayer mMediaPlayer;
    private PowerManager.WakeLock mWakeLock;
    public static SharedPreferences settings;
    public static final String PREFS_NAME = "TunePrefs";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sound = true;
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

        setContentView(com.limon.PrayerTiming.R.layout.alarm);

		/*Button stopAlarm = (Button) findViewById(R.id.btnStopAlarm);
		stopAlarm.setOnClickListener( new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				if(sound == true){
					mMediaPlayer.stop();
				}
				finish();
			}
		});

        playAzanSound();*/
    }

	
	/*private void playAzanSound() {
			
		    Time time = new Time();
		    long nextPrayeTimeinSecond = time.getPrayerTime();
		    int currentPrayerAzan = time.currentPrayer;
		    
		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String yesorno = "";
			String ajan = "";
			
		    if( currentPrayerAzan == 1 &&  isTune("fajrtune") == false ){
		    	sound = false;
		    	yesorno = settings.getString("fajrtune", "").toString();
		    	ajan = "fajrtune";
		    }
		    else if( currentPrayerAzan == 2 &&  isTune("dhuhrtune") == false ){
		    	sound = false;
		    	yesorno = settings.getString("dhuhrtune", "").toString();
		    	ajan = "dhuhrtune";
		    }
		    else if( currentPrayerAzan == 3 &&  isTune("asrtune") == false ){
		    	sound = false;
		    	yesorno = settings.getString("asrtune", "").toString();
		    	ajan = "asrtune";
		    }
		    else if( currentPrayerAzan == 4 &&  isTune("magribtune") == false ){
		    	sound = false;
		    	yesorno = settings.getString("magribtune", "").toString();
		    	ajan = "magribtune";
		    }
		    else if( currentPrayerAzan == 5 &&  isTune("ishatune") == false ){
		    	sound = false;
		    	yesorno = settings.getString("ishatune", "").toString();
		    	ajan = "ishatune";
		    }
		    else{
		    	sound = true;
		    }
		    
		   
		    mMediaPlayer = MediaPlayer.create(AlarmReceiverActivity.this, R.raw.azan);
		    if(sound == true){ 	
		    	mMediaPlayer.start();
		    }
		    azan_duration_in_milisecond = mMediaPlayer.getDuration();
		
			Toast mToast;
			
			Intent intent = new Intent(AlarmReceiverActivity.this, AlarmReceiverActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(AlarmReceiverActivity.this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (nextPrayeTimeinSecond*1000), pendingIntent);
			
			mToast = Toast.makeText(getApplicationContext(), ajan + " " + yesorno + " in : " + nextPrayeTimeinSecond + " seconds", Toast.LENGTH_LONG);
			mToast.show();
	}*/

    protected void onStop() {
        super.onStop();
        mWakeLock.release();
    }

}

