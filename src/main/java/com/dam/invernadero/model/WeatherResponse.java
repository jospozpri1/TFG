package com.dam.invernadero.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {
    @SerializedName("list")
    public List<WeatherEntry> list;

    public static class WeatherEntry {
        @SerializedName("dt_txt")
        public String dateTime;

        @SerializedName("main")
        public Main main;

        @SerializedName("weather")
        public List<WeatherDescription> weather;
    }

    public static class Main {
        @SerializedName("temp")
        public float temp;
    }

    public static class WeatherDescription {
        @SerializedName("main")
        public String main;

        @SerializedName("description")
        public String description;
    }
}


