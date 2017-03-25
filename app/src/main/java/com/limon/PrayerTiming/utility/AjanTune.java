package com.limon.PrayerTiming.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class AjanTune {

    public static final String PREFS_NAME = "TUNE_PREFERENCES";

    public static void setTune(Context context, String prayerName, boolean isTune) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(prayerName, isTune);
        editor.commit();
    }

    public static boolean getIsTune(Context context, String prayerTune) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(prayerTune, false);
    }

    public static boolean isInitialSetTune(Context context) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        boolean isInitial = settings.getBoolean("INITAL_INSTALL", true);

        if(isInitial) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("INITAL_INSTALL", false);
            editor.commit();
        }
        return isInitial;
    }
}
