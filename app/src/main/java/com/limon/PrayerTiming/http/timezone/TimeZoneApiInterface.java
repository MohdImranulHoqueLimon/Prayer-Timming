package com.limon.PrayerTiming.http.timezone;

import com.limon.PrayerTiming.http.timezone.model.TimeZoneDetails;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TimeZoneApiInterface {
    @GET("timezone/json")
    Call<TimeZoneDetails> getTimeZoneDetails(
            @Query("location") String location,
            @Query("timestamp") long timestamp,
            @Query("key") String key
    );
}



