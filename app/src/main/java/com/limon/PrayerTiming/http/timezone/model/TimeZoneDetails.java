package com.limon.PrayerTiming.http.timezone.model;

import com.google.gson.annotations.SerializedName;

public class TimeZoneDetails {
    @SerializedName("status")
    private String status;

    @SerializedName("timeZoneId")
    private String timeZoneId;

    @SerializedName("timeZoneName")
    private String timeZoneName;

    public String getStatus() {
        return this.status;
    }

    public String getTimeZoneId() {
        return this.timeZoneId;
    }

    public String getTimeZoneName(){
        return this.timeZoneName;
    }
}
