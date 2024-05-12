package com.project.weatherapp.listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.project.weatherapp.AppState;
import com.project.weatherapp.dto.geocode.GeocodeElementDto;

import java.util.List;

public class SpinnerOnItemSelectListener implements AdapterView.OnItemSelectedListener {
    private final Spinner spinner;
    private final AppState appState;

    public SpinnerOnItemSelectListener(Spinner spinner, AppState appState) {
        this.spinner = spinner;
        this.appState = appState;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedCity = spinner.getSelectedItem().toString();
        if (!selectedCity.isEmpty()) {
            List<GeocodeElementDto> favoriteCities = appState.getFavoriteCities().getValue();
            if (favoriteCities != null) {
                favoriteCities.stream()
                        .filter(city -> city.getDisplayName().equals(selectedCity))
                        .findFirst().ifPresent(appState::setCurrentCityGeocode);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
