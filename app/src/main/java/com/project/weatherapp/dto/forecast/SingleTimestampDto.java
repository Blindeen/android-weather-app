package com.project.weatherapp.dto.forecast;

import com.project.weatherapp.dto.WeatherDescriptionDto;

import java.util.List;

public class SingleTimestampDto {
    private Long dt;
    private MainForecastData main;
    private List<WeatherDescriptionDto> weather;
    private String dt_txt;

    public Long getDt() {
        return dt;
    }

    public MainForecastData getMain() {
        return main;
    }

    public WeatherDescriptionDto getWeather() {
        return weather.get(0);
    }

    public String getDt_txt() {
        return dt_txt;
    }
}
