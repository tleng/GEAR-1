package com.mattmellor.gear;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Activity where user can browse and select which story to read
 */
public class StoriesSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories_selection);
        // TODO: Make this dynamically read articles in folder and
        // create buttons, like in stories selection, instead of hardcoding
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // TODO: make sure this shows menu
        getMenuInflater().inflate(R.menu.menu_stories_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts the ReadArticleActivity for the selected story
     * @param view
     */
    public void openStory(View view){
        Intent intent = new Intent(StoriesSelectionActivity.this, ReadArticleActivity.class);
        intent.putExtra("story", (String) view.getTag());
        startActivity(intent);
        finish();
    }
}
