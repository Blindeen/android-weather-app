package com.project.weatherapp.fragment;

import static com.project.weatherapp.Utils.displayToast;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.weatherapp.AppState;
import com.project.weatherapp.R;
import com.project.weatherapp.dto.currentweather.WeatherResponseDto;
import com.project.weatherapp.enums.Unit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class BasicWeatherDataFragment extends Fragment {
    private WeatherResponseDto weatherResponseDto;
    protected Unit unit;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd");
    protected AppState appState;

    public BasicWeatherDataFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_basic_weather_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appState = new ViewModelProvider(requireActivity()).get(AppState.class);
        appState.getUnit().observe(getViewLifecycleOwner(), value -> {
            unit = value;
            WeatherResponseDto weatherResponseDto = appState.getWeatherData().getValue();
            if (weatherResponseDto != null) {
                setTemperature(view, weatherResponseDto);
            }
        });
        appState.getWeatherData().observe(getViewLifecycleOwner(), value -> {
            weatherResponseDto = value;
            updateUI(value);
        });

        TextView addToFavorites = view.findViewById(R.id.addToFavorites);
        addToFavorites.setOnClickListener(this::addFavoriteCityOnClick);
    }

    private void updateUI(WeatherResponseDto response) {
        View view = getView();
        if (view != null) {
            setBasicData(view, response);
        }
    }

    private void setBasicData(View view, WeatherResponseDto response) {
        setTextViewValue(view, R.id.cityName, response.getGeocodeElementDto().getDisplayName());
        setDate(view, response);
        setWeatherIcon(view, response);
        setTextViewValue(view, R.id.weatherDescription, response.getWeather().get(0).getMain());
        setTemperature(view, response);
        setTextViewValue(
                view,
                R.id.pressure,
                response.getMain().getPressure() + " " + getString(R.string.pressure_unit)
        );
    }

    private void setTemperature(View view, WeatherResponseDto response) {
        int temperature = prepareTemperature(response.getMain().getTemp());
        String temperatureUnitString;
        if (unit == Unit.METRIC) {
            temperatureUnitString = getString(R.string.celsius);
        } else {
            temperatureUnitString = getString(R.string.fahrenheit);
        }
        setTextViewValue(
                view,
                R.id.temperature,
                temperature + " " + temperatureUnitString
        );
    }

    protected int prepareTemperature(int celsiusTemperature) {
        if (unit == Unit.IMPERIAL) {
            return (int) (celsiusTemperature * 1.8) + 32;
        }
        return celsiusTemperature;
    }

    private void setDate(View view, WeatherResponseDto response) {
        LocalDateTime timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(response.getDt()),
                TimeZone.getDefault().toZoneId()
        );
        String date = timestamp.format(formatter);
        setTextViewValue(view, R.id.timestamp, date);
    }

    private void setWeatherIcon(View view, WeatherResponseDto response) {
        String weatherIconID = response.getWeather().get(0).getIcon();
        ((ImageView) view.findViewById(R.id.weatherImage)).setImageDrawable(getWeatherIcon(weatherIconID));
    }

    protected void setTextViewValue(View view, int elementID, String value) {
        TextView textView = view.findViewById(elementID);
        textView.setText(value);
    }

    protected Drawable getWeatherIcon(String ID) {
        int drawableID = 0;
        switch (ID) {
            case "01d":
                drawableID = R.drawable.weather_01d;
                break;
            case "01n":
                drawableID = R.drawable.weather_01n;
                break;
            case "02d":
                drawableID = R.drawable.weather_02d;
                break;
            case "02n":
                drawableID = R.drawable.weather_02n;
                break;
            case "03d":
            case "03n":
                drawableID = R.drawable.weather_03d;
                break;
            case "04d":
            case "04n":
                drawableID = R.drawable.weather_04d;
                break;
            case "09d":
            case "09n":
                drawableID = R.drawable.weather_09d;
                break;
            case "10d":
                drawableID = R.drawable.weather_10d;
                break;
            case "10n":
                drawableID = R.drawable.weather_10n;
                break;
            case "11d":
            case "11n":
                drawableID = R.drawable.weather_11d;
                break;
            case "13d":
            case "13n":
                drawableID = R.drawable.weather_13d;
                break;
            case "50d":
            case "50n":
                drawableID = R.drawable.weather_50d;
                break;
        }

        return ContextCompat.getDrawable(requireContext(), drawableID);
    }

    public void addFavoriteCityOnClick(View view) {
        boolean additionResult = appState.addFavoriteCity(weatherResponseDto.getGeocodeElementDto());
        String message = additionResult ? "City added to favorites!" : "City is already in favorites!";
        displayToast(getContext(), message);
    }
}
