package com.project.weatherapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.weatherapp.dto.currentweather.WeatherResponseDto;
import com.project.weatherapp.dto.forecast.ForecastResponseDto;
import com.project.weatherapp.dto.geocode.GeocodeElementDto;
import com.project.weatherapp.enums.Unit;

import java.util.List;

public class AppState extends ViewModel {
    private final MutableLiveData<String> currentCityMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<GeocodeElementDto> currentCityGeocodeMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<WeatherResponseDto> weatherResponseMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<ForecastResponseDto> forecastResponseMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Unit> temperatureUnitMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<GeocodeElementDto>> favoriteCitiesMutableLiveData = new MutableLiveData<>();

    public void setCurrentCity(String city) {
        currentCityMutableLiveData.postValue(city);
    }

    public void setCurrentCityGeocode(GeocodeElementDto geocodeElementDto) {
        currentCityGeocodeMutableLiveData.postValue(geocodeElementDto);
    }

    public LiveData<GeocodeElementDto> getCurrentCityGeocode() {
        return currentCityGeocodeMutableLiveData;
    }

    public void setWeatherData(WeatherResponseDto response) {
        weatherResponseMutableLiveData.postValue(response);
    }

    public LiveData<WeatherResponseDto> getWeatherData() {
        return weatherResponseMutableLiveData;
    }

    public void setForecastData(ForecastResponseDto response) {
        forecastResponseMutableLiveData.postValue(response);
    }

    public LiveData<ForecastResponseDto> getForecastData() {
        return forecastResponseMutableLiveData;
    }

    public void setUnit(Unit unit) {
        temperatureUnitMutableLiveData.setValue(unit);
    }

    public LiveData<Unit> getUnit() {
        return temperatureUnitMutableLiveData;
    }

    public boolean addFavoriteCity(GeocodeElementDto city) {
        List<GeocodeElementDto> favoriteCities = favoriteCitiesMutableLiveData.getValue();
        if (favoriteCities != null) {
            for (GeocodeElementDto favoriteCity : favoriteCities) {
                if (favoriteCity.getName().equals(city.getName())) {
                    return false;
                }
            }

            favoriteCities.add(0, city);
        }

        return true;
    }

    public void removeFavoriteCity(GeocodeElementDto city) {
        List<GeocodeElementDto> favoriteCities = favoriteCitiesMutableLiveData.getValue();

        GeocodeElementDto cityToRemove = null;
        if (favoriteCities != null) {
            for (GeocodeElementDto favoriteCity : favoriteCities) {
                if (favoriteCity.getName().equals(city.getName())) {
                    cityToRemove = favoriteCity;
                    break;
                }
            }
        }

        if (cityToRemove != null) {
            favoriteCities.remove(cityToRemove);
        }
    }

    public void setFavoriteCities(List<GeocodeElementDto> favoriteCities) {
        favoriteCitiesMutableLiveData.setValue(favoriteCities);
    }

    public LiveData<List<GeocodeElementDto>> getFavoriteCities() {
        return favoriteCitiesMutableLiveData;
    }
}
