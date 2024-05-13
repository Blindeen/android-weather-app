package com.project.weatherapp.fragment;

import static com.project.weatherapp.Utils.deleteFavoriteCityFiles;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.project.weatherapp.AppState;
import com.project.weatherapp.R;
import com.project.weatherapp.dto.geocode.GeocodeElementDto;
import com.project.weatherapp.listener.SpinnerOnItemSelectListener;

import java.util.List;
import java.util.stream.Collectors;

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
        appState = new ViewModelProvider(requireActivity()).get(AppState.class);
        appState.getFavoriteCities().observe(getViewLifecycleOwner(), this::updateUI);
        setSpinnerListener(view);
        setBinIconListener(view);
    }

    private void updateUI(List<GeocodeElementDto> favoriteCities) {
        View view = getView();
        if (view != null) {
            Spinner spinner = view.findViewById(R.id.favoriteCitiesSpinner);
            List<String> favoriteCitiesCopy = favoriteCities.stream().map(GeocodeElementDto::getDisplayName).collect(Collectors.toList());
            favoriteCitiesCopy.add(0, "");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item, favoriteCitiesCopy);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }

    private void setSpinnerListener(View view) {
        Spinner spinner = view.findViewById(R.id.favoriteCitiesSpinner);
        spinner.setOnItemSelectedListener(new SpinnerOnItemSelectListener(spinner, appState));
    }

    private void setBinIconListener(View view) {
        TextView removeButton = view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(this::removeFavoriteCityOnClick);
    }

    private void removeFavoriteCityOnClick(View view) {
        Spinner spinner = requireView().findViewById(R.id.favoriteCitiesSpinner);
        int selectedItemPos = spinner.getSelectedItemPosition();
        if (selectedItemPos == 0) {
            return;
        }

        List<GeocodeElementDto> favoriteCities = appState.getFavoriteCities().getValue();
        if (favoriteCities == null) {
            return;
        }
        GeocodeElementDto selectedItem = favoriteCities.get(selectedItemPos - 1);
        appState.removeFavoriteCity(selectedItem);
        deleteFavoriteCityFiles(requireContext(), selectedItem);
        updateUI(favoriteCities);
    }
}
