package com.limon.PrayerTiming.http.time.model;

import com.google.gson.annotations.SerializedName;

public class PrayerTimeData {

    @SerializedName("timings")
    private Timing timing;

    @SerializedName("date")
    private Dates dates;

    public Timing getTiming() {
        return timing;
    }
    public Dates getDate(){
        return dates;
    }
}
