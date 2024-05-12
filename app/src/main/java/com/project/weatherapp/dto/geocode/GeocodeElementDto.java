package com.project.weatherapp.dto.geocode;

import androidx.annotation.NonNull;

public class GeocodeElementDto {
    private String name;
    private String lat;
    private String lon;
    private String country;
    private String state;

    public GeocodeElementDto() {
        super();
    }

    public GeocodeElementDto(String name, String lat, String lon, String country, String state) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.country = country;
        this.state = state;
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

    @NonNull
    @Override
    public String toString() {
        return String.format("GeocodeElementDto{name='%s', lat='%s', lon='%s', country='%s', state='%s'}", name, lat, lon, country, state);
    }


    public static GeocodeElementDto fromString(String geocodeElementDtoString) {
        String[] parts = geocodeElementDtoString.split(",");
        GeocodeElementDto geocodeElementDto = new GeocodeElementDto();
        geocodeElementDto.name = parts[0].split("=")[1];
        geocodeElementDto.lat = parts[1].split("=")[1];
        geocodeElementDto.lon = parts[2].split("=")[1];
        geocodeElementDto.country = parts[3].split("=")[1];
        geocodeElementDto.state = parts[4].split("=")[1];
        return geocodeElementDto;
    }
}
