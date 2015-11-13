package com.mattmellor.gear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class start extends AppCompatActivity {

    public static  User currentUser; //TODO make this final, and add a constructor?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //Getting rid of the title in the action bar
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        User user = new User(1);// there should be an input that actually puts an id here
        currentUser =  user;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }


    public void getStartedClick(View view){
        //Go the the second page
        startActivity(new Intent(start.this, MainActivity.class));
    }

    public void goToStoriesSelectionOnClick(View view){
        startActivity(new Intent(start.this, StoriesSelection.class));
    }

    public void goToSuggestedStories(View view) {
        startActivity(new Intent(start.this, SuggestedStories.class));
    }

    public void goToOverallUserVocabOnClick(View view){
        startActivity(new Intent(start.this, overallUserVocab.class));
    }

    public void goToUserSettingsOnClick(View view){
        startActivity(new Intent(start.this, userSettings.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
