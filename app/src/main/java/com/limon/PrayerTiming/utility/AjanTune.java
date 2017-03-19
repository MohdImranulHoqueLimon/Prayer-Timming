package com.limon.PrayerTiming.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

/**
 * Created by Limon on 3/19/2017.
 */

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
}
