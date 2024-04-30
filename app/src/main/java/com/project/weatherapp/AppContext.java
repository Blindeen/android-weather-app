package com.project.weatherapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.weatherapp.dto.currentweather.WeatherResponseDto;
import com.project.weatherapp.dto.forecast.ForecastResponseDto;
import com.project.weatherapp.enums.Unit;

import java.util.List;

public class AppContext extends ViewModel {
    private final MutableLiveData<WeatherResponseDto> weatherResponseMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<ForecastResponseDto> forecastResponseMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Unit> temperatureUnitMutableLiveData = new MutableLiveData<>(Unit.METRIC);
    private final MutableLiveData<List<String>> favoriteCitiesMutableLiveData = new MutableLiveData<>(
            new java.util.ArrayList<>()
    );

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

    public void addFavoriteCity(String city) {
        List<String> favoriteCities = favoriteCitiesMutableLiveData.getValue();
        if (favoriteCities != null) {
            favoriteCities.add(city);
        }
    }

    public LiveData<List<String>> getFavoriteCities() {
        return favoriteCitiesMutableLiveData;
    }
}
