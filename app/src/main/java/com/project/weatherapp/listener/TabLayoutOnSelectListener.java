package com.project.weatherapp.listener;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.project.weatherapp.R;
import com.project.weatherapp.fragment.AdditionalWeatherDataFragment;
import com.project.weatherapp.fragment.BasicWeatherDataFragment;
import com.project.weatherapp.fragment.FavoriteCitiesFragment;
import com.project.weatherapp.fragment.WeatherForecastFragment;

public class TabLayoutOnSelectListener implements TabLayout.OnTabSelectedListener {
    private final FragmentManager fragmentManager;

    public TabLayoutOnSelectListener(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

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
}
