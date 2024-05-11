package com.project.weatherapp.listener;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.project.weatherapp.R;

public class CityNameInputListener implements TextWatcher {
    private final Context context;

    public CityNameInputListener(Context context) {
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView actionButton = ((Activity) context).findViewById(R.id.actionButton);
        if (s.toString().isEmpty()) {
            actionButton.setText(R.string.refresh_icon);
        } else {
            actionButton.setText(R.string.search_icon);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
