/**
 * Created by Limon on 12/24/2016.
 */

package com.limon.PrayerTiming.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.limon.PrayerTiming.http.time.model.LogData;
import com.limon.PrayerTiming.http.time.model.Timing;

public class TimeDbHelper extends SQLiteOpenHelper {

    private Context mContext;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "prayer";

    private static final String TIME_TABLE = "time";
    private static final String LOG_DATA_TABLE = "log_data";

    private static final String KEY_ID = "id";
    private static final String DAY_NUMBER = "day_number";
    private static final String DATE = "date";
    private static final String FAJR = "fajr";
    private static final String SUNRISE = "sunrise";
    private static final String DHUHR = "dhuhr";
    private static final String ASR = "asr";
    private static final String SUNSET = "sunset";
    private static final String MAGHRIB = "maghrib";
    private static final String ISHA = "isha";
    private static final String IMSAK = "imsak";
    private static final String MIDNIGHT = "midnight";
    private static final String TIMESTAMP = "timestamp";
    private static final String LAST_UPDATED = "last_updated";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String LOCATION = "location";

    public TimeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TIME_TABLE = "" +
                "CREATE TABLE IF NOT EXISTS " + TIME_TABLE + " ("
                + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
                + DATE + " TEXT, "
                + FAJR + " TEXT, "
                + SUNRISE + " TEXT, "
                + DHUHR + " TEXT, "
                + ASR + " TEXT, "
                + SUNSET + " TEXT, "
                + MAGHRIB + " TEXT, "
                + ISHA + " TEXT, "
                + IMSAK + " TEXT, "
                + MIDNIGHT + " TEXT, "
                + TIMESTAMP + " TEXT);";

        db.execSQL(CREATE_TIME_TABLE);

        String CREATE_LOG_DATA_TABLE = "" +
                "CREATE TABLE IF NOT EXISTS " + LOG_DATA_TABLE + " ("
                + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
                + LAST_UPDATED + " TEXT, "
                + LATITUDE + " REAL, "
                + LONGITUDE + " REAL, "
                + LOCATION + " TEXT);";

        db.execSQL(CREATE_LOG_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TIME_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LOG_DATA_TABLE);
        onCreate(db);
    }

    public boolean insertPrayerTime(
            String date, String fajr, String sunrise, String dhuhr, String asr, String sunset,
            String maghrib, String isha, String imsak, String midnight, String timestamp
    ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DATE, date);
        contentValues.put(FAJR, fajr);
        contentValues.put(SUNRISE, sunrise);
        contentValues.put(DHUHR, dhuhr);
        contentValues.put(ASR, asr);
        contentValues.put(SUNSET, sunset);
        contentValues.put(MAGHRIB, maghrib);
        contentValues.put(ISHA, isha);
        contentValues.put(IMSAK, imsak);
        contentValues.put(MIDNIGHT, midnight);
        contentValues.put(TIMESTAMP, timestamp);

        db.insert(TIME_TABLE, null, contentValues);
        return true;
    }

    public Timing getPrayerTime(String dateTime) {

        String selectQuery = "SELECT * FROM " + TIME_TABLE + " WHERE " + DATE + " = '" + dateTime + "' LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Timing namazTiming = null;
        if (cursor.moveToFirst()) {
            String fajrTime = cursor.getString(2);
            String sunriseTime = cursor.getString(3);
            String dhuhrTime = cursor.getString(4);
            String asrTime = cursor.getString(5);
            String sunsetTime = cursor.getString(6);
            String maghribTime = cursor.getString(7);
            String ishaTime = cursor.getString(8);
            String imsakTime = cursor.getString(9);
            String midnightTime = cursor.getString(10);

            namazTiming = new Timing(
                    fajrTime, sunriseTime, dhuhrTime, asrTime, sunsetTime, maghribTime, ishaTime, imsakTime, midnightTime
            );
        }
        return namazTiming;
    }

    public String getFajrPrayerTime(String dateTime) {

        Timing namazTiming = null;

        String selectQuery = "SELECT " + FAJR + " FROM " + TIME_TABLE + " WHERE " + DATE + " = '" + dateTime + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        String fajrTime = cursor.getString(0);

        return fajrTime;
    }

    public void clearTimeTableTruncate() {
        String deleteQuery = "DROP TABLE IF EXISTS " + TIME_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(deleteQuery);
        onCreate(db);
    }

    public boolean insertLog(String lastUpdated, double latitude, double longitude, String location) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(LAST_UPDATED, lastUpdated);
        contentValues.put(LATITUDE, latitude);
        contentValues.put(LONGITUDE, longitude);
        contentValues.put(LOCATION, location);

        db.insert(LOG_DATA_TABLE, null, contentValues);
        return true;
    }

    public LogData getLastLogData() {

        LogData logData = null;

        String selectQuery = "SELECT * FROM " + LOG_DATA_TABLE + " ORDER BY id DESC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            String lastUpdated = cursor.getString(1);
            double latitude = cursor.getFloat(2);
            double longitude = cursor.getFloat(3);
            String location = cursor.getString(4);
            logData = new LogData(latitude, longitude, lastUpdated, location);
        }

        return logData;
    }

    public void clearLogDataTableTruncate() {
        String deleteQuery = "DROP TABLE IF EXISTS " + LOG_DATA_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(deleteQuery);
        onCreate(db);
    }
}
