package com.limon.PrayerTiming.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TimeZoneApiClient {

    //https://maps.googleapis.com/maps/api/timezone/json?location=23.810332,90.4125181&timestamp=1458000000&key=AIzaSyDVGyCIPH2uaxZQPUWvp_xn2khSwEdy8xE
    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder().connectTimeout(100, TimeUnit.SECONDS).readTimeout(100, TimeUnit.SECONDS).writeTimeout(1000, TimeUnit.SECONDS);

            // add logging as last interceptor
            httpClient.addInterceptor(logging);

            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setLenient().create();
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).client(httpClient.build()).build();
        }
        return retrofit;
    }

}
