package com.limon.PrayerTiming.http.time.model;

import com.google.gson.annotations.SerializedName;
import com.limon.PrayerTiming.helper.Helper;

public class Timing {

    @SerializedName("Fajr")
    private String mFajrTime;

    @SerializedName("Sunrise")
    private String mSunriseTime;

    @SerializedName("Dhuhr")
    private String mDhuhrTime;

    @SerializedName("Sunset")
    private String mSunset;

    @SerializedName("Asr")
    private String mAsrTime;

    @SerializedName("Maghrib")
    private String mMaghribeTime;

    @SerializedName("Isha")
    private String mIshaTime;

    @SerializedName("Imsak")
    private String mImsakTime;

    @SerializedName("Midnight")
    private String mMidnightTime;

    public Timing(
            String fajrTime, String sunriseTime, String dhuhrTime, String asrTime, String sunsetTime,
            String maghribeTime, String ishaTime, String imsakTime, String midnightTime
    ) {
        this.mFajrTime = fajrTime;
        this.mSunriseTime = sunriseTime;
        this.mDhuhrTime = dhuhrTime;
        this.mSunset = sunsetTime;
        this.mAsrTime = asrTime;
        this.mMaghribeTime = maghribeTime;
        this.mIshaTime = ishaTime;
        this.mImsakTime = imsakTime;
        this.mMidnightTime = midnightTime;
    }

    public String getFajrTime() {
        return Helper.getSplitTime(this.mFajrTime);
    }

    public String getSunriseTime() {
        return Helper.getSplitTime(this.mSunriseTime);
    }

    public String getDhuhrTime() {
        return Helper.getSplitTime(this.mDhuhrTime);
    }

    public String getSunset() {
        return Helper.getSplitTime(this.mSunset);
    }

    public String getAsrTime() {
        return Helper.getSplitTime(this.mAsrTime);
    }

    public String getMaghribeTime() {
        return Helper.getSplitTime(this.mMaghribeTime);
    }

    public String getIshaTime() {
        return Helper.getSplitTime(this.mIshaTime);
    }

    public String getImsakTime() {
        return Helper.getSplitTime(this.mImsakTime);
    }

    public String getMidnightTime() {
        return Helper.getSplitTime(this.mMidnightTime);
    }
}
