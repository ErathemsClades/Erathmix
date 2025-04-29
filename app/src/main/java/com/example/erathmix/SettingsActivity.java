package com.example.erathmix;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchAutoStart;
    private Switch switchAutoPause;

    private static final String PREFS_NAME = "ErathmixPrefs";
    private static final String KEY_AUTO_START = "auto_start";
    private static final String KEY_AUTO_PAUSE = "auto_pause";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchAutoStart = findViewById(R.id.switchAutoStart);
        switchAutoPause = findViewById(R.id.switchAutoPause);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean autoStart = preferences.getBoolean(KEY_AUTO_START, true);
        boolean autoPause = preferences.getBoolean(KEY_AUTO_PAUSE, true);

        switchAutoStart.setChecked(autoStart);
        switchAutoPause.setChecked(autoPause);

        switchAutoStart.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            preferences.edit().putBoolean(KEY_AUTO_START, isChecked).apply();
        });

        switchAutoPause.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            preferences.edit().putBoolean(KEY_AUTO_PAUSE, isChecked).apply();
        });
    }
}
