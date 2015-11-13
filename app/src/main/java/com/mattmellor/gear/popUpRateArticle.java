package com.mattmellor.gear;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

/**
 * Created by Matthew on 10/23/2015.
 */
public class popUpRateArticle extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popupratearticle);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //sets this activity to be a pop up
        //Following lines set the size of the activity
        getWindow().setLayout((int) (width*0.5) , (int) (height* 0.2));


    }

    public String onClickRate(View view){
        Button rate = (Button) view;
        String rating = rate.getText().toString();
        return rating;
    }




    public void rateArticle(View view) {
        finish();
    }




}
