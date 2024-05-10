package com.project.weatherapp.dto;

public class ForecastElement {
    private final Integer temperature;
    private final String icon;

    public ForecastElement(Integer temperature, String icon) {
        this.temperature = temperature;
        this.icon = icon;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public String getIcon() {
        return icon;
    }
}
