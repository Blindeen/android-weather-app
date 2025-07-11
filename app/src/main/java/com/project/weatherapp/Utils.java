package com.project.weatherapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.weatherapp.dto.currentweather.ErrorResponseDto;
import com.project.weatherapp.dto.geocode.GeocodeElementDto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Utils {
    public static <T> CompletableFuture<T> getRequest(OkHttpClient client, String url, Class<T> classType) {
        CompletableFuture<T> future = new CompletableFuture<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                future.completeExceptionally(new Exception("Failed to fetch data. Please check your internet connection."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                String responseBodyString = responseBody != null ? responseBody.string() : "";

                if (response.isSuccessful()) {
                    T responseDto = objectMapper.readValue(responseBodyString, classType);
                    future.complete(responseDto);
                } else {
                    ErrorResponseDto error = objectMapper.readValue(responseBodyString, ErrorResponseDto.class);
                    future.completeExceptionally(new Exception(error.getMessage()));
                }
            }
        });

        return future;
    }

    public static void displayToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }

        return false;
    }

    public static void saveWeatherDataJSON(Context context, Object obj, String filename) {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            displayToast(context, "Error occurred while converting object to JSON");
            return;
        }

        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (IOException e) {
            displayToast(context, "Error occurred while saving weather data");
        }
    }

    public static <T> T readWeatherDataJSON(Context context, String filename, Class<T> classType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        FileInputStream fileInputStream = context.openFileInput(filename + ".json");
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return objectMapper.readValue(stringBuilder.toString(), classType);
    }

    public static String capitalizeString(String text) {
        return text != null ? text.substring(0, 1).toUpperCase() + text.substring(1) : "";
    }

    public static void deleteFavoriteCityFiles(Context context, GeocodeElementDto elementToDelete) {
        File appPrivateDir = context.getFilesDir();
        File[] files = appPrivateDir.listFiles();
        if (files == null) {
            return;
        }

        String filePrefix = String.format("%s%s", elementToDelete.getLat(), elementToDelete.getLon());
        for (File file : files) {
            if (file.getName().startsWith(filePrefix)) {
                file.delete();
            }
        }
    }
}
