package com.mattmellor.gear;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
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

        getWindow().setLayout((int) (width * 0.5), (int) (height * 0.5));


    }

    public void onClick(View view){
        Button button = (Button) view;
        String rating = button.getText().toString();
        int numRating;
        if(rating.equals("One")){
            numRating = 1;
        }
        else if(rating.equals("Two")){
            numRating =2;
        }
        else{
            numRating =3;
        }
        String articleTitle = getIntent().getExtras().getString("currentArticle");
        String userId = getIntent().getExtras().getString("currentUserId");
        UserDataCollection.addRating(userId,articleTitle,numRating);
        //return rating;

    }

    public void rateArticle(View view) {
        finish();
    }




}
