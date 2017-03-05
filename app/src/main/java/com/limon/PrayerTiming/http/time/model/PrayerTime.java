package com.limon.PrayerTiming.http.time.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PrayerTime {

    @SerializedName("code")
    private int status_code;

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private ArrayList<PrayerTimeData> prayerTimeData = new ArrayList<>();

    public PrayerTime(String status, int status_code, ArrayList<PrayerTimeData> prayerTimeData) {
        this.status = status;
        this.status_code = status_code;
        this.prayerTimeData = prayerTimeData;
    }

    public ArrayList<PrayerTimeData> getPrayertimeData(){
        return this.prayerTimeData;
    }

    public int getStatusCode() {
        return status_code;
    }

    public String getStatus() {
        return status;
    }
}
