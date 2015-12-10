package com.mattmellor.gear;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that displays user vocabulary
 */
public class DisplayVocabularyActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_user_vocabulary);

        // display current user vocabulary
        TextView vocabView = (TextView) findViewById(R.id.vocabularyData);
        vocabView.setText(getVocabularyString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overall_user_vocab, menu);
        return true;
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


    /**
     * Turns user vocabulary into string representation for display
     * @return string to display
     */
    private String getVocabularyString() {
        Map<String, WordLookup> vocabulary = UserData.getWordsLookedUp();

        String vocabString = "";
        if (vocabulary.isEmpty()) {
            vocabString = "No vocabulary words looked up yet.\n";
        }

        // list vocabulary words
        for (String key:vocabulary.keySet()) {
            vocabString += key + "\n";
        }
        return vocabString;
    }
}
