package com.project.weatherapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.project.weatherapp.dto.currentweather.WeatherResponseDto;
import com.project.weatherapp.dto.forecast.ForecastResponseDto;
import com.project.weatherapp.dto.geocode.GeocodeElementDto;
import com.project.weatherapp.enums.Unit;
import com.project.weatherapp.listener.CityNameInputListener;
import com.project.weatherapp.listener.TabLayoutOnSelectListener;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;

import static com.project.weatherapp.Utils.*;

public class MainActivity extends AppCompatActivity {
    private AppState appState;
    private SharedPreferences sharedPreferences;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private GeocodeElementDto currentCity;
    private Unit units;
    private Timer timer;
    private LocalDateTime minimizationTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeContext();
        configRadioListener();
        configTabLayoutListener();
        configTextInputListener();

        if (savedInstanceState == null) {
            try {
                handleInternetConnection();
            } catch (IOException e) {
                displayToast(this, "Not able to load old data");
            }
        } else {
            restoreSavedState(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (sharedPreferences == null) {
            sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        }

        loadFavoriteCities();
        String cityName = sharedPreferences.getString(Constants.SAVED_CITY_KEY, "Warsaw");
        String lat = sharedPreferences.getString(Constants.SAVED_LAT_KEY, "52.2319581");
        String lon = sharedPreferences.getString(Constants.SAVED_LON_KEY, "21.0067249");
        String country = sharedPreferences.getString(Constants.SAVED_COUNTRY_KEY, "PL");
        String state = sharedPreferences.getString(Constants.SAVED_STATE_KEY, "Masovian Voivodeship");
        currentCity = new GeocodeElementDto(cityName, lat, lon, country, state);
        loadUnit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        long firstFetchDelay = 0;
        if (minimizationTimestamp != null) {
            long currentTimestampMillis = System.currentTimeMillis();
            Instant instant = minimizationTimestamp.atZone(ZoneId.systemDefault()).toInstant();
            long minimizationTimestampMillis = instant.toEpochMilli();

            long elapsedTimeMillis = currentTimestampMillis - minimizationTimestampMillis;
            if (elapsedTimeMillis < Constants.FETCH_INTERVAL_MILLIS) {
                firstFetchDelay = Constants.FETCH_INTERVAL_MILLIS - elapsedTimeMillis;
            }
        }

        scheduleWeatherDataFetching(firstFetchDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        minimizationTimestamp = LocalDateTime.now();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.SAVED_TIMESTAMP_KEY, System.currentTimeMillis());
        TabLayout tabLayout = findViewById(R.id.fragmentMenu);
        if (tabLayout != null) {
            outState.putInt(Constants.SAVED_TAB_KEY, tabLayout.getSelectedTabPosition());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        LinkedHashSet<GeocodeElementDto> favoriteCities = new LinkedHashSet<>(appState.getFavoriteCities().getValue());
        LinkedHashSet<String> favoriteCitiesStrings = favoriteCities.stream().map(GeocodeElementDto::toString).collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        editor.putStringSet(Constants.FAV_CITIES_KEY, favoriteCitiesStrings);

        editor.putString(Constants.SAVED_CITY_KEY, currentCity.getName());
        editor.putString(Constants.SAVED_LAT_KEY, currentCity.getLat());
        editor.putString(Constants.SAVED_LON_KEY, currentCity.getLon());
        editor.putString(Constants.SAVED_COUNTRY_KEY, currentCity.getCountry());
        editor.putString(Constants.SAVED_STATE_KEY, currentCity.getState());
        editor.putInt(Constants.SAVED_UNIT_KEY, units.ordinal());
        editor.apply();
    }

    private void initializeContext() {
        appState = new ViewModelProvider(this).get(AppState.class);
        appState.getCurrentCityGeocode().observe(this, city -> {
            if (!currentCity.equals(city)) {
                currentCity = city;
                userActionDataRefresh();
            }
        });
    }

    private void configRadioListener() {
        RadioGroup radioGroup = findViewById(R.id.unitsRadioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedID) -> {
            if (checkedID == R.id.celsiusRadio) {
                units = Unit.METRIC;
            } else {
                units = Unit.IMPERIAL;
            }

            appState.setUnit(units);
        });
    }

    private void configTabLayoutListener() {
        TabLayout tabLayout = findViewById(R.id.fragmentMenu);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayoutOnSelectListener(fragmentManager));
        }
    }

    private void configTextInputListener() {
        EditText cityNameInput = findViewById(R.id.cityNameInput);
        TextView actionButton = findViewById(R.id.actionButton);
        if (cityNameInput != null && actionButton != null) {
            cityNameInput.addTextChangedListener(new CityNameInputListener(this));
        }
    }

    private void handleInternetConnection() throws IOException {
        if (!isNetworkAvailable(this)) {
            WeatherResponseDto weatherData = readWeatherDataJSON(this, WeatherResponseDto.class.getSimpleName(), WeatherResponseDto.class);
            ForecastResponseDto forecastData = readWeatherDataJSON(this, ForecastResponseDto.class.getSimpleName(), ForecastResponseDto.class);
            appState.setWeatherData(weatherData);
            appState.setForecastData(forecastData);
            displayToast(this, "No internet connection, weather data is outdated.");
        }
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        long timestamp = savedInstanceState.getLong(Constants.SAVED_TIMESTAMP_KEY);
        int selectedTab = savedInstanceState.getInt(Constants.SAVED_TAB_KEY);
        minimizationTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        TabLayout tabLayout = findViewById(R.id.fragmentMenu);
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(selectedTab);
            if (tab != null) {
                tab.select();
            }
        }
    }

    private void loadFavoriteCities() {
        LinkedHashSet<String> defaultValue = new LinkedHashSet<>();
        Set<String> favoriteCities = sharedPreferences.getStringSet(Constants.FAV_CITIES_KEY, defaultValue);
        List<GeocodeElementDto> favoriteCitiesGeocode = favoriteCities.stream().map(GeocodeElementDto::fromString).collect(Collectors.toList());

        appState.setFavoriteCities(favoriteCitiesGeocode);
    }

    private void loadUnit() {
        int currentUnitOrdinal = sharedPreferences.getInt(Constants.SAVED_UNIT_KEY, Unit.METRIC.ordinal());
        units = Unit.values()[currentUnitOrdinal];
        appState.setUnit(units);

        RadioGroup radioGroup = findViewById(R.id.unitsRadioGroup);
        if (radioGroup != null) {
            int radioButtonId = (units == Unit.METRIC) ? R.id.celsiusRadio : R.id.fahrenheitRadio;
            radioGroup.check(radioButtonId);
        }
    }

    private void scheduleWeatherDataFetching(long firstFetchDelay) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchAllWeatherData();
            }
        }, firstFetchDelay, Constants.FETCH_INTERVAL_MILLIS);
    }

    public void fetchWeatherData() {
        String url = String.format("%s/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s",
                Constants.API_URL, currentCity.getLat(), currentCity.getLon(), Constants.API_KEY, Constants.UNITS);
        CompletableFuture<WeatherResponseDto> weatherResponseDto = getRequest(httpClient, url, WeatherResponseDto.class);
        weatherResponseDto.handle((response, ex) -> {
            if (ex != null) {
                runOnUiThread(() -> displayToast(this, capitalizeString(ex.getMessage())));
            } else {
                response.setGeocodeElementDto(currentCity);
                appState.setWeatherData(response);
                runOnUiThread(() -> {
                    clearCityNameInput();
                    displayToast(getApplicationContext(), "Data has been fetched");
                });
                saveWeatherDataJSON(this, response, WeatherResponseDto.class.getSimpleName() + ".json");
                saveDataForFavoriteCity(response, "_weather.json");
            }
            return null;
        });
    }

    private void fetchForecastData() {
        String url = String.format("%s/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=%s",
                Constants.API_URL, currentCity.getLat(), currentCity.getLon(), Constants.API_KEY, Constants.UNITS);
        CompletableFuture<ForecastResponseDto> forecastResponseDto = getRequest(httpClient, url, ForecastResponseDto.class);
        forecastResponseDto.handle((response, ex) -> {
            if (ex == null) {
                appState.setForecastData(response);
                saveWeatherDataJSON(this, response, ForecastResponseDto.class.getSimpleName() + ".json");
                saveDataForFavoriteCity(response, "_forecast.json");
            }
            return null;
        });
    }

    private void saveDataForFavoriteCity(Object response, String filenameEnding) {
        List<GeocodeElementDto> favoriteCities = appState.getFavoriteCities().getValue();
        if (favoriteCities != null) {
            for (GeocodeElementDto favoriteCity : favoriteCities) {
                if (Objects.equals(favoriteCity.getLat(), currentCity.getLat()) && Objects.equals(favoriteCity.getLon(), currentCity.getLon())) {
                    saveWeatherDataJSON(this, response, favoriteCity.getLat() + favoriteCity.getLon() + filenameEnding);
                }
            }
        }
    }

    private void fetchAllWeatherData() {
        fetchWeatherData();
        fetchForecastData();
    }

    private void fetchGeocodingData() {
        String cityName = getCityNameInputValue();
        String url = String.format("%s/geo/1.0/direct?q=%s&appid=%s&limit=5", Constants.API_URL, cityName, Constants.API_KEY);
        CompletableFuture<GeocodeElementDto[]> geocodeResponseDto = getRequest(httpClient, url, GeocodeElementDto[].class);
        geocodeResponseDto.handle((response, ex) -> {
            if (ex != null) {
                runOnUiThread(() -> displayToast(this, capitalizeString(ex.getMessage())));
            } else {
                runOnUiThread(() -> {
                    if (response.length == 0) {
                        displayToast(this, "No results found");
                        return;
                    }
                    showDialog(new ArrayList<>(Arrays.asList(response)));
                });
            }
            return null;
        });
    }

    private String getCityNameInputValue() {
        String cityNameString = "";

        EditText cityNameInput = findViewById(R.id.cityNameInput);
        if (cityNameInput != null) {
            cityNameString = cityNameInput.getText().toString();
        }

        return cityNameString;
    }

    private void clearCityNameInput() {
        EditText cityNameInput = findViewById(R.id.cityNameInput);
        if (cityNameInput != null) {
            cityNameInput.setText("");
        }
    }

    private void userActionDataRefresh() {
        fetchAllWeatherData();
        if (timer != null) {
            timer.cancel();
        }
        scheduleWeatherDataFetching(Constants.FETCH_INTERVAL_MILLIS);
    }

    public void onRefreshDataButtonClick(View view) {
        EditText cityNameInput = findViewById(R.id.cityNameInput);
        if (cityNameInput != null && cityNameInput.getText().toString().isEmpty()) {
            userActionDataRefresh();
        } else {
            fetchGeocodingData();
        }
    }

    private void showDialog(List<GeocodeElementDto> availablePlaces) {
        String[] availablePlaceStrings = availablePlaces.stream().map(GeocodeElementDto::getDisplayName).toArray(String[]::new);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(R.string.dialog_title)
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .setPositiveButton("Choose", (dialog, which) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    int selectedPosition = alertDialog.getListView().getCheckedItemPosition();
                    if (selectedPosition != -1) {
                        fetchAllWeatherData();
                    } else {
                        displayToast(this, "No city selected");
                    }
                })
                .setSingleChoiceItems(availablePlaceStrings, -1, (dialog, which) -> currentCity = availablePlaces.get(which));

        builder.create().show();
    }
}
