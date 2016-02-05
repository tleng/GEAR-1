package com.mit.gear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mattmellor.gear.R;

/**
 * Activity that represents the starting screen of the app
 */
public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // login with dummy user
        String defaultUserName = "defaultUser";
        UserDataCollection.login(defaultUserName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    /**
     * Go to overview page to select among all stories
     * @param view
     */
    public void goToStoriesSelectionOnClick(View view){
        startActivity(new Intent(StartActivity.this, StoriesSelectionActivity.class));
    }

    /**
     * Go to page with suggested stories
     * @param view
     */
    public void goToSuggestedStories(View view) {
        startActivity(new Intent(StartActivity.this, SuggestedStoriesActivity.class));
    }

    /**
     * Go to page that shows user history with vocabulary / word lookups
     * @param view
     */
    public void goToOverallUserVocabOnClick(View view){
        startActivity(new Intent(StartActivity.this, DisplayVocabularyActivity.class));
    }


    /**
     * TODO: this method of creating a popup is based on how popUpRateArticle was
     * TODO: written, but not sure if this is the recommended way?
     * Create login popup
     * @param view
     */
    public void goToLoginOnClick(View view){
        startActivity(new Intent(StartActivity.this, LoginPopupActivity.class));
    }


//    // In the future, let users specify user settings
//    public void goToUserSettingsOnClick(View view){
//        startActivity(new Intent(StartActivity.this, userSettings.class));
//    }

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

