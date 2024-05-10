package com.project.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.project.weatherapp.dto.currentweather.WeatherResponseDto;
import com.project.weatherapp.dto.forecast.ForecastResponseDto;
import com.project.weatherapp.enums.Unit;
import com.project.weatherapp.fragment.AdditionalWeatherDataFragment;
import com.project.weatherapp.fragment.BasicWeatherDataFragment;
import com.project.weatherapp.fragment.FavoriteCitiesFragment;
import com.project.weatherapp.fragment.WeatherForecastFragment;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;

import static com.project.weatherapp.Utils.*;

public class MainActivity extends AppCompatActivity {
    private AppState appState;
    private SharedPreferences sharedPreferences;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private String cityName;
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
        cityName = sharedPreferences.getString(Constants.SAVED_CITY_KEY, Constants.DEFAULT_CITY);
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
        editor.putStringSet(Constants.FAV_CITIES_KEY, new LinkedHashSet<>(appState.getFavoriteCities().getValue()));
        editor.putString(Constants.SAVED_CITY_KEY, cityName);
        editor.putInt(Constants.SAVED_UNIT_KEY, units.ordinal());
        editor.apply();
    }

    private void initializeContext() {
        appState = new ViewModelProvider(this).get(AppState.class);
        appState.getCurrentCity().observe(this, city -> cityName = city);
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
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    int position = tab.getPosition();
                    switch (position) {
                        case 0:
                            fragmentTransaction.replace(R.id.fragmentContainer, BasicWeatherDataFragment.class, null);
                            break;
                        case 1:
                            fragmentTransaction.replace(R.id.fragmentContainer, AdditionalWeatherDataFragment.class, null);
                            break;
                        case 2:
                            fragmentTransaction.replace(R.id.fragmentContainer, WeatherForecastFragment.class, null);
                            break;
                        case 3:
                            fragmentTransaction.replace(R.id.fragmentContainer, FavoriteCitiesFragment.class, null);
                    }

                    fragmentTransaction.commit();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        }
    }

    private void handleInternetConnection() throws IOException {
        if (!isNetworkAvailable(this)) {
            WeatherResponseDto weatherData = readWeatherDataJSON(this, WeatherResponseDto.class);
            ForecastResponseDto forecastData = readWeatherDataJSON(this, ForecastResponseDto.class);
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
        appState.setFavoriteCities(new ArrayList<>(favoriteCities));
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
                fetchData();
            }
        }, firstFetchDelay, Constants.FETCH_INTERVAL_MILLIS);
    }

    public void fetchWeatherData() {
        String cityName = getCityNameInputValue();
        String url = Constants.API_URL + "weather?q=" + cityName + "&appid=" + Constants.API_KEY
                + "&units=" + Constants.UNITS;
        CompletableFuture<WeatherResponseDto> weatherResponseDto = getRequest(this, httpClient, url, WeatherResponseDto.class);
        weatherResponseDto.handle((response, ex) -> {
            if (ex != null) {
                runOnUiThread(() -> displayToast(this, capitalizeString(ex.getMessage())));
            } else {
                appState.setWeatherData(response);
                appState.setCurrentCity(cityName);
                runOnUiThread(() -> {
                    clearCityNameInput();
                    displayToast(getApplicationContext(), "Data has been fetched");
                });
            }
            return null;
        });
    }

    private void fetchForecastData() {
        String cityName = getCityNameInputValue();
        String url = Constants.API_URL + "forecast?q=" + cityName + "&appid=" + Constants.API_KEY
                + "&units=" + Constants.UNITS;
        CompletableFuture<ForecastResponseDto> forecastResponseDto = getRequest(this, httpClient, url, ForecastResponseDto.class);
        forecastResponseDto.handle((response, ex) -> {
            if (ex == null) {
                appState.setForecastData(response);
            }
            return null;
        });
    }

    private void fetchData() {
        fetchWeatherData();
        fetchForecastData();
    }

    private String getCityNameInputValue() {
        EditText cityNameInput = findViewById(R.id.cityNameInput);
        if (cityNameInput != null && !cityNameInput.getText().toString().isEmpty()) {
            return cityNameInput.getText().toString();
        }

        return cityName;
    }

    private void clearCityNameInput() {
        EditText cityNameInput = findViewById(R.id.cityNameInput);
        if (cityNameInput != null) {
            cityNameInput.setText("");
        }
    }

    public void onRefreshDataButtonClick(View view) {
        fetchData();
        if (timer != null) {
            timer.cancel();
        }
        scheduleWeatherDataFetching(Constants.FETCH_INTERVAL_MILLIS);
    }
}
