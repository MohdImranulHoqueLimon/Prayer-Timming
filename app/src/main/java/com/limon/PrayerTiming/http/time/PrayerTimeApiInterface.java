package com.limon.PrayerTiming.http.time;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.limon.PrayerTiming.http.time.model.PrayerTime;

public interface PrayerTimeApiInterface {
    //@GET("http://api.aladhan.com/calendar?latitude=51.5073509&longitude=-0.1277583&timezonestring=Europe/London&method=2&month=12&year=2016")
    //Call<PrayerTime> getPrayerTime();

    /*@GET("calendar?latitude={latitude}&longitude={longitude}&timezonestring={timezonestring}&method={method}&month={month}&year={year}")
    Call<PrayerTime> getPrayerTime(
        @Header("Authorization") String authorization,
        @Path("latitude") int latitude,
        @Path("longitude") int longitude,
        @Path("timezonestring") String timezonestring,
        @Path("method") int method,
        @Path("month") int month,
        @Path("year") int year
    );*/

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



