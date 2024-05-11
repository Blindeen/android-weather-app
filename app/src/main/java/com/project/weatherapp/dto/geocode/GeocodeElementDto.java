package com.project.weatherapp.dto.geocode;

public class GeocodeElementDto {
    private String name;
    private Double lat;
    private Double lon;
    private String country;
    private String state;

    public String getName() {
        return name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }
}
