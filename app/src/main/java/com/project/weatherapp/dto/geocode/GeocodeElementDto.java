package com.project.weatherapp.dto.geocode;

public class GeocodeElementDto {
    private String name;
    private String lat;
    private String lon;
    private String country;
    private String state;

    public GeocodeElementDto() {
        super();
    }

    public GeocodeElementDto(String lat, String lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getDisplayName() {
        return name + ", " + (state != null ? (state + ", ") : "") + country;
    }
}
