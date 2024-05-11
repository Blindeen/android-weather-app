package com.project.weatherapp.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.project.weatherapp.AppState;
import com.project.weatherapp.R;

import java.util.ArrayList;
import java.util.List;

public class FavoriteCitiesFragment extends BasicWeatherDataFragment {
    private boolean isSpinnerInitial = true;

    public FavoriteCitiesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_cities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        appState = new ViewModelProvider(requireActivity()).get(AppState.class);
        appState.getFavoriteCities().observe(getViewLifecycleOwner(), this::updateUI);
        setSpinnerListener();
    }

    private void updateUI(List<String> favoriteCities) {
        View view = getView();
        if (view != null) {
            Spinner spinner = view.findViewById(R.id.favoriteCitiesSpinner);
            List<String> favoriteCitiesCopy = new ArrayList<>(favoriteCities);
            favoriteCitiesCopy.add(0, "");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item, favoriteCitiesCopy);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }

    private void setSpinnerListener() {
        Spinner spinner = requireView().findViewById(R.id.favoriteCitiesSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitial) {
                    isSpinnerInitial = false;
                } else {
                    String selectedCity = spinner.getSelectedItem().toString();
                    if (!selectedCity.isEmpty()) {
                        appState.setCurrentCity(selectedCity);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
