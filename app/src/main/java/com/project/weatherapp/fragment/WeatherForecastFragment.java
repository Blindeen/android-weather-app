package com.project.weatherapp.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.project.weatherapp.AppContext;
import com.project.weatherapp.R;
import com.project.weatherapp.dto.forecast.ForecastResponseDto;

public class WeatherForecastFragment extends BasicWeatherDataFragment {
    public WeatherForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        appContext = new ViewModelProvider(requireActivity()).get(AppContext.class);
        appContext.getUnit().observe(getViewLifecycleOwner(), value -> unit = value);
        appContext.getForecastData().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(ForecastResponseDto response) {
        View view = getView();
        if (view != null) {
            setForecastData(view, response);
        }
    }

    private void setForecastData(View view, ForecastResponseDto response) {
        LinearLayout forecastTable = view.findViewById(R.id.forecastTable);
        if (forecastTable != null) {
            for (int i = 0; i < forecastTable.getChildCount(); i++) {
                LinearLayout row = (LinearLayout) forecastTable.getChildAt(i);
                if (row != null) {
                    //TODO: Handle filling in each row
                }
            }
        }
    }

    private void prepareForecastData(ForecastResponseDto data) {
        //TODO: Handle response data mapping
    }
}