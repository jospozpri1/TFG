package com.dam.invernadero.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherService {
    @GET("data/2.5/forecast")
    Call<JsonObject> getForecast(
            @Query("q") String city,
            @Query("units") String units,
            @Query("lang") String lang,
            @Query("appid") String apiKey);
}

