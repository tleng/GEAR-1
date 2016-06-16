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
    private Switch aSwitch;
    private boolean colorChoice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        aSwitch =(Switch)findViewById(R.id.color);
        //Access the shared preference
        sharedPreferences = getSharedPreferences("ColorPreference", Context.MODE_PRIVATE);
        //getting user preference or true for default
        colorChoice=sharedPreferences.getBoolean("color", true);
        //set the switch to user preference
        aSwitch.setChecked(colorChoice);
        //updating shredPreference to switch state
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("color", isChecked);
                editor.commit();

            }
        });
    }
}
