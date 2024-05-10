package com.project.weatherapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.weatherapp.AppState;
import com.project.weatherapp.R;
import com.project.weatherapp.dto.currentweather.WeatherResponseDto;


public class AdditionalWeatherDataFragment extends BasicWeatherDataFragment {
    private static final double MS_TO_KMH_COEFF = 3.6;
    private static final int M_TO_KM_COEFF = 1000;
    private static final double MS_TO_MPH_COEFF = 2.2369;


    public AdditionalWeatherDataFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_additional_weather_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        appState = new ViewModelProvider(requireActivity()).get(AppState.class);
        appState.getUnit().observe(getViewLifecycleOwner(), value -> {
            unit = value;
            WeatherResponseDto weatherResponseDto = appState.getWeatherData().getValue();
            if (weatherResponseDto != null) {
                setTextViewValue(view, R.id.windSpeed, prepareWindSpeed(weatherResponseDto.getWind().getSpeed()));
            }
        });
        appState.getWeatherData().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(WeatherResponseDto response) {
        View view = getView();
        if (view != null) {
            setAdditionalData(view, response);
        }
    }

    private void setAdditionalData(View view, WeatherResponseDto response) {
        setTextViewValue(view, R.id.windSpeed, prepareWindSpeed(response.getWind().getSpeed()));
        setTextViewValue(view, R.id.windDirection, response.getWind().getDeg() + getString(R.string.degrees));
        setTextViewValue(view, R.id.humidity, response.getMain().getHumidity() + "%");
        setTextViewValue(view, R.id.visibility, prepareVisibility(response.getVisibility()));
    }

    private String prepareWindSpeed(float windSpeed) {
        int windSpeedValue = (int) windSpeed;
        String windSpeedUnitString = null;
        switch (unit) {
            case METRIC: {
                windSpeedUnitString = " km/h";
                windSpeedValue = (int) (windSpeed * MS_TO_KMH_COEFF);
            }
            break;
            case IMPERIAL: {
                windSpeedUnitString = " mph";
                windSpeedValue = (int) (windSpeed * MS_TO_MPH_COEFF);
            }
        }

        return windSpeedValue + windSpeedUnitString;
    }

    private String prepareVisibility(int visibility) {
        return (visibility / M_TO_KM_COEFF) + " km";
    }
}
