package com.limon.PrayerTiming.Result;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Limon on 12/29/2016.
 */

public class Results {

    //TODO; remove all logs from project
    public static void showLog(String logMessage) {
        //Log.d("salat_log", logMessage);
    }

    public static void showLog(String tag, String logMessage) {
        //Log.d(tag, logMessage);
    }

    public static void showToast(Context context, String toastMessage) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
    }
}
