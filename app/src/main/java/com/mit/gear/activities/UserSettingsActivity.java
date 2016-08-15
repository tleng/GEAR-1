package com.mit.gear.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import com.mattmellor.gear.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Activity where user can manage the settings.
 * the user can turn on or off the text coloring. the user choice will be save in a sharedPreferences
 */

public class UserSettingsActivity extends AppCompatActivity {
    private String TAG = "UserSettings";
    //create sharedPreferences to save user choice of text coloring
    private SharedPreferences sharedPreferences;
    private Switch colorSwitch;
    private Switch speakSwitch;
    private Switch debugSwitch;
	private CheckBox clickedCheckBox;
	private CheckBox seenCheckBox;
    private boolean colorChoice;
    private boolean speakChoice;
    private boolean debugChoice;
    private boolean showClicked;
    private boolean showSeen;
    private RadioGroup swipeRadioGroup;
    private Spinner spinner;
    private Integer textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_settings);
        colorSwitch =(Switch)findViewById(R.id.color);
        speakSwitch =(Switch)findViewById(R.id.speak);
        debugSwitch =(Switch)findViewById(R.id.debug);
		clickedCheckBox = (CheckBox)findViewById(R.id.ClickcheckBox);
		seenCheckBox = (CheckBox)findViewById(R.id.SeencheckBox);
        swipeRadioGroup=(RadioGroup)findViewById(R.id.swipeRadioGroup);
        spinner = (Spinner) findViewById(R.id.spinner);
        //Access the shared preference
        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        //getting user preference or true/false for default
        colorChoice=sharedPreferences.getBoolean("color", true);
        speakChoice=sharedPreferences.getBoolean("speak", true);
        debugChoice=sharedPreferences.getBoolean("debug", false);
		showClicked=sharedPreferences.getBoolean("showClicked", true);
		showSeen=sharedPreferences.getBoolean("showSeen", true);
        if(sharedPreferences.getString("swipeRadio", "Hswipe").equals("Hswipe")) {
            swipeRadioGroup.check(R.id.Hswipe);
        }
        else{
            swipeRadioGroup.check(R.id.Vswipe);
        }

        textSize=sharedPreferences.getInt("fontSize", 1);


        //set the switch to user preference
        colorSwitch.setChecked(colorChoice);
        speakSwitch.setChecked(speakChoice);
        debugSwitch.setChecked(debugChoice);
		clickedCheckBox.setChecked(showClicked);
		seenCheckBox.setChecked(showSeen);

        //updating shredPreference to switch state
        colorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("color", isChecked);
                Log.d(TAG,"colorSwitch turned "+isChecked);
                editor.commit();

            }
        });
        speakSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("speak", isChecked);
                Log.d(TAG,"speakSwitch turned "+isChecked);
                editor.commit();

            }
        });
        debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("debug", isChecked);
                Log.d(TAG,"debugSwitch turned "+isChecked);
                editor.commit();

            }
        });
		clickedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean("showClicked", isChecked);
				Log.d(TAG,"showClicked turned "+isChecked);
				editor.commit();
			}
		});
		seenCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean("showSeen", isChecked);
				Log.d(TAG,"showSeen turned "+isChecked);
				editor.commit();
			}
		});
        swipeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.Hswipe){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("swipeRadio", "Hswipe");
                    Log.d(TAG,"swipeRadio turned to Horizontal");
                    editor.commit();
                }else if(checkedId==R.id.Vswipe){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("swipeRadio", "Vswipe");
                    Log.d(TAG, "swipeRadio turned to vertical");
                    editor.commit();

                }
            }
        });


        //get font sizes array from resources
        String[] fontSizesArray = getResources().getStringArray(R.array.font_size_category);

        // Creating adapter for spinner
        ArrayAdapter<String> fontSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fontSizesArray);

        // Drop down layout style - list view with radio button
        fontSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(fontSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("fontSize", position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner.setSelection(textSize.intValue());

    }
}
