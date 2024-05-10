package com.project.weatherapp.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.project.weatherapp.AppState;
import com.project.weatherapp.R;
import com.project.weatherapp.dto.forecast.ForecastResponseDto;
import com.project.weatherapp.dto.forecast.SingleTimestampDto;
import com.project.weatherapp.enums.Unit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeatherForecastFragment extends BasicWeatherDataFragment {
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E");
    private final DateTimeFormatter dayTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WeatherForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        appState = new ViewModelProvider(requireActivity()).get(AppState.class);
        appState.getUnit().observe(getViewLifecycleOwner(), value -> {
            unit = value;
            ForecastResponseDto forecastResponseDto = appState.getForecastData().getValue();
            if (forecastResponseDto != null) {
                setForecastData(view, forecastResponseDto);
            }
        });
        appState.getForecastData().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(ForecastResponseDto response) {
        View view = getView();
        if (view != null) {
            setForecastData(view, response);
        }
    }

    private void setForecastData(View view, ForecastResponseDto response) {
        Map<LocalDate, Integer> averageTemperatureByDate = prepareForecastData(response);
        String temperatureUnit =
                unit == Unit.METRIC ? getString(R.string.celsius) : getString(R.string.fahrenheit);

        LinearLayout forecastTable = view.findViewById(R.id.forecastTable);
        if (forecastTable != null) {
            int i = 0;
            for (Map.Entry<LocalDate, Integer> entry : averageTemperatureByDate.entrySet()) {
                LocalDate date = entry.getKey();
                Integer temperature = entry.getValue();

                LinearLayout row = (LinearLayout) forecastTable.getChildAt(i);
                if (row != null) {
                    ((TextView) row.getChildAt(0)).setText(date.format(dayFormatter));
                    ((TextView) row.getChildAt(1)).setText(String.format("%s %s", temperature, temperatureUnit));
                }

                i++;
            }
        }
    }

    private Map<LocalDate, Integer> prepareForecastData(ForecastResponseDto data) {
        List<SingleTimestampDto> timestamps = data.getList();
        Map<LocalDate, List<SingleTimestampDto>> groupedByDate = timestamps.stream()
                .collect(Collectors.groupingBy(dto -> LocalDateTime.parse(dto.getDt_txt(), dayTimeFormatter).toLocalDate(),
                        LinkedHashMap::new, Collectors.toList()));

        Map<LocalDate, Integer> averageTemperatureByDate = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (Map.Entry<LocalDate, List<SingleTimestampDto>> entry : groupedByDate.entrySet()) {
            LocalDate date = entry.getKey();
            if (date.equals(today)) {
                continue;
            }

            List<SingleTimestampDto> forecasts = entry.getValue();
            double sumTemperature = 0.0;
            for (SingleTimestampDto forecast : forecasts) {
                sumTemperature += forecast.getMain().getTemp();
            }

            int averageTemperature = (int) (sumTemperature / forecasts.size());
            averageTemperatureByDate.put(date, prepareTemperature(averageTemperature));
        }

        return averageTemperatureByDate;
    }
}
