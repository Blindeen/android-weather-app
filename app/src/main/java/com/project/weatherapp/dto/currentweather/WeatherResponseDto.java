package com.project.weatherapp.dto.currentweather;

import com.project.weatherapp.dto.WeatherDescriptionDto;

import java.util.List;

public class WeatherResponseDto {
    private String name;
    private CoordinatesDto coord;
    private Long dt;
    private BasicWeatherDataDto main;
    private Integer visibility;
    private List<WeatherDescriptionDto> weather;
    private WindDataDto wind;

    public String getName() {
        return name;
    }

    public CoordinatesDto getCoord() {
        return coord;
    }

    public Long getDt() {
        return dt;
    }

    public BasicWeatherDataDto getMain() {
        return main;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public List<WeatherDescriptionDto> getWeather() {
        return weather;
    }

    public WindDataDto getWind() {
        return wind;
    }
}
