package com.pechatkin.sbt.myweather;

import com.pechatkin.sbt.myweather.forecast.Forecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IApiHelper {

    @GET("data/2.5/weather")
    Call<Forecast> getForecast(
            @Query("lat") String latitude,
            @Query("lon") String longitude,
            @Query("APPID") String apiKey);

}
