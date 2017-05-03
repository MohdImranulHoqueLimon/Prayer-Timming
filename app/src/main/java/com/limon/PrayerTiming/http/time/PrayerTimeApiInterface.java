package com.limon.PrayerTiming.http.time;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.limon.PrayerTiming.http.time.model.PrayerTime;

public interface PrayerTimeApiInterface {
    @GET("calendar")
    Call<PrayerTime> getPrayerTime(
        @Query("latitude") double latitude,
        @Query("longitude") double longitude,
        @Query("timezonestring") String timezonestring,
        @Query("method") int method,
        @Query("month") int month,
        @Query("year") int year
    );
}



