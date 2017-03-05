package com.limon.PrayerTiming.http.time.model;

/**
 * Created by Limon on 2/21/2017.
 */

public class LogData {
    public double latitude;
    public double longitude;
    public String lastUpdated;
    public String location;

    public LogData(double latitude, double longitude, String lastUpdated, String location) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdated = lastUpdated;
        this.location = location;
    }
}
