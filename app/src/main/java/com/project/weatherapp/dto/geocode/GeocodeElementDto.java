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

    public String getAdditionalInfo() {
        return (state != null ? (state + ", ") : "") + country;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s %s %s %s %s", name, lat, lon, country, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GeocodeElementDto) {
            GeocodeElementDto other = (GeocodeElementDto) obj;
            return this.name.equals(other.name) && this.lat.equals(other.lat) && this.lon.equals(other.lon) && this.country.equals(other.country) && this.state.equals(other.state);
        }
        return false;
    }

    public static GeocodeElementDto fromString(String geocodeElementDtoString) {
        String[] parts = geocodeElementDtoString.split(" ");
        return new GeocodeElementDto(
                parts[0], parts[1], parts[2], parts[3], parts[4]
        );
    }
}
