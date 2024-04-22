package com.project.weatherapp.dto.forecast;

import com.project.weatherapp.dto.WeatherDescriptionDto;

public class SingleTimestampDto {
    private Long dt;
    private MainForecastData main;
    private WeatherDescriptionDto weather;

    public Long getDt() {
        return dt;
    }

    public MainForecastData getMain() {
        return main;
    }

    public WeatherDescriptionDto getWeather() {
        return weather;
    }
}
