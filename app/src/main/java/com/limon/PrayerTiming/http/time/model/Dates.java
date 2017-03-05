package com.limon.PrayerTiming.http.time.model;

import com.google.gson.annotations.SerializedName;

public class Dates {
    @SerializedName("readable")
    private String readable;

    @SerializedName("timestamp")
    private String timestamp;

    public String getReadable() {
        return readable;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
