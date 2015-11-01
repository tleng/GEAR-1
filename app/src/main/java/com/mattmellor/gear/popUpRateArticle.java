package com.mattmellor.gear;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

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

        getWindow().setLayout((int) (width*0.5) , (int) (height* 0.5));


    }

    public void rateArticle(View view) {
        finish();
    }




}
