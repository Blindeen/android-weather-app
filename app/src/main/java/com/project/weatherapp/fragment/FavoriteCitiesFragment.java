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

import com.project.weatherapp.AppContext;
import com.project.weatherapp.R;

import java.util.List;

public class FavoriteCitiesFragment extends BasicWeatherDataFragment {
    public FavoriteCitiesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_cities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        appContext = new ViewModelProvider(requireActivity()).get(AppContext.class);
        appContext.getFavoriteCities().observe(getViewLifecycleOwner(), this::updateUI);
        setSpinnerListener();
    }

    private void updateUI(List<String> favoriteCities) {
        View view = getView();
        if (view != null) {
            Spinner spinner = view.findViewById(R.id.favoriteCitiesSpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item, favoriteCities);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }

    private void setSpinnerListener() {
        Spinner spinner = requireView().findViewById(R.id.favoriteCitiesSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                appContext.setCurrentCity(spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
