package com.project.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
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
import com.project.weatherapp.fragment.WeatherForecastFragment;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;

import static com.project.weatherapp.Utils.*;

public class MainActivity extends AppCompatActivity {
    private final static long FETCH_INTERVAL_MILLIS = 900000;
    private final static String API_KEY = "e3b34d0b0066811dc7b89e8b72add1a7";
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private AppContext appContext;
    private String cityName = "London";
    private Unit units = Unit.METRIC;
    private Timer timer;
    private LocalDateTime minimizationTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        appContext = new ViewModelProvider(this).get(AppContext.class);
        configRadioListener();
        configTabLayoutListener();
        try {
            handleInternetConnection();
        } catch (IOException e) {
            Log.e("Exception", e.getMessage());
        }
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
    protected void onResume() {
        super.onResume();

        long firstFetchDelay = 0;
        if (minimizationTimestamp != null) {
            long currentTimestampMillis = System.currentTimeMillis();
            Instant instant = minimizationTimestamp.atZone(ZoneId.systemDefault()).toInstant();
            long minimizationTimestampMillis = instant.toEpochMilli();

            long elapsedTimeMillis = currentTimestampMillis - minimizationTimestampMillis;
            if (elapsedTimeMillis < FETCH_INTERVAL_MILLIS) {
                firstFetchDelay = FETCH_INTERVAL_MILLIS - elapsedTimeMillis;
            }
        }

        scheduleWeatherDataFetching(firstFetchDelay);
    }

    private void scheduleWeatherDataFetching(long firstFetchDelay) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchData(null);
                runOnUiThread(() -> displayToast(getApplicationContext(), "Data has been fetched"));
            }
        }, firstFetchDelay, FETCH_INTERVAL_MILLIS);
    }

    public void fetchWeatherData() {
        String cityName = getCityNameInputValue();
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + cityName
                + "&APPID=" + API_KEY
                + "&units=" + units;
        try {
            CompletableFuture<WeatherResponseDto> weatherResponseDto = getRequest(this, new OkHttpClient(), url, WeatherResponseDto.class);
            weatherResponseDto.handle((response, ex) -> {
                if (ex != null) {
                    runOnUiThread(() -> displayToast(this, capitalizeString(ex.getMessage())));
                } else {
                    appContext.setWeatherData(response);
                    this.cityName = cityName;
                    runOnUiThread(this::clearCityNameInput);
                }
                return null;
            });
        } catch (IOException e) {
            displayToast(this, e.getMessage());
        }
    }

    private void fetchForecastData() {
        String cityName = getCityNameInputValue();
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + cityName
                + "&APPID=" + API_KEY
                + "&units=" + units;
        try {
            CompletableFuture<ForecastResponseDto> weatherResponseDto = getRequest(this, new OkHttpClient(), url, ForecastResponseDto.class);
            weatherResponseDto.handle((response, ex) -> {
                if (ex != null) {
                    runOnUiThread(() -> displayToast(this, capitalizeString(ex.getMessage())));
                } else {
                    appContext.setForecastData(response);
                }
                return null;
            });
        } catch (IOException e) {
            displayToast(this, e.getMessage());
        }
    }

    public void fetchData(View view) {
        fetchWeatherData();
        fetchForecastData();
        if (view != null && timer != null) {
            timer.cancel();
            scheduleWeatherDataFetching(FETCH_INTERVAL_MILLIS);
        }
    }

    private void configRadioListener() {
        RadioGroup radioGroup = findViewById(R.id.unitsRadioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedID) -> {
            if (checkedID == R.id.celsiusRadio) {
                units = Unit.METRIC;
            } else {
                units = Unit.IMPERIAL;
            }

            appContext.setUnit(units);
        });
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

    private void configTabLayoutListener() {
        TabLayout tabLayout = findViewById(R.id.fragmentMenu);
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

    private void handleInternetConnection() throws IOException {
        if (!isNetworkAvailable(this)) {
            WeatherResponseDto weatherData = readWeatherDataJSON(this, WeatherResponseDto.class);
            appContext.setWeatherData(weatherData);
            displayToast(this, "No internet connection, weather data is outdated.");
        }
    }
}