package com.mit.gear.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.mattmellor.gear.R;
import com.mit.gear.reading.ReadArticleActivity;

/**
 *
 */
public class SavePopupActivity extends Activity {
    //creating savePopupActivity instance
    public static SavePopupActivity savePopupActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        savePopupActivity = this;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_save);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //sets this activity to be a pop up
        //Following lines set the size of the activity
        getWindow().setLayout((int) (width*0.5) , (int) (height* 0.2));

    }

    public void dontSave(View view){
        //dismissing the savePopupActivity
        savePopupActivity.finish();
    }

    public void saveProgress(View view) {
        ReadArticleActivity.getReadArticleActivityInstance().saveProgress(view);
    }

}