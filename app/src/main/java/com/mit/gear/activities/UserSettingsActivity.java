package com.mit.gear.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.mattmellor.gear.R;

/**
 * Activity where user can manage the settings.
 * the user can turn on or off the text coloring. the user choice will be save in a sharedPreferences
 */

public class UserSettingsActivity extends AppCompatActivity {
    //create sharedPreferences to save user choice of text coloring
    private SharedPreferences sharedPreferences;
    private Switch colorSwitch;
    private Switch speakSwitch;
    private Switch debugSwitch;
    private boolean colorChoice;
    private boolean speakChoice;
    private boolean debugChoice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        colorSwitch =(Switch)findViewById(R.id.color);
        speakSwitch =(Switch)findViewById(R.id.speak);
        debugSwitch =(Switch)findViewById(R.id.debug);
        //Access the shared preference
        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        //getting user preference or true/false for default
        colorChoice=sharedPreferences.getBoolean("color", true);
        speakChoice=sharedPreferences.getBoolean("speak", true);
        debugChoice=sharedPreferences.getBoolean("debug", false);
        //set the switch to user preference
        colorSwitch.setChecked(colorChoice);
        speakSwitch.setChecked(speakChoice);
        debugSwitch.setChecked(debugChoice);
        //updating shredPreference to switch state
        colorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("color", isChecked);
                editor.commit();

            }
        });
        speakSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("speak", isChecked);
                editor.commit();

            }
        });
        debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("debug", isChecked);
                editor.commit();

            }
        });
    }
}
