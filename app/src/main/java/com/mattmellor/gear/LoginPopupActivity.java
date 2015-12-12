package com.mattmellor.gear;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

/**
 *
 */
public class LoginPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_login);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //sets this activity to be a pop up
        //Following lines set the size of the activity
        getWindow().setLayout((int) (width*0.5) , (int) (height* 0.2));

    }

    public void onClick(View view){

    }

    public void completeLogin(View view) {
        EditText loginInputName = (EditText) findViewById(R.id.loginName);
        String username = loginInputName.getText().toString();
        UserDataCollection.login(username);
        finish();
    }

}