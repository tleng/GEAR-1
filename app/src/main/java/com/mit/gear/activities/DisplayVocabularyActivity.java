package com.mit.gear.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.Word;

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
        getMenuInflater().inflate(R.menu.menu_stories_suggestion_and_selection, menu);
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
     * TODO: Determine meaningful way to display vocabulary – what does user want?
     * Turns user vocabulary into string representation for display
     * @return string to display
     */
    private String getVocabularyString() {
        DataStorage dataStorage = new DataStorage(getApplicationContext());
        HashMap<String, Word> vocabulary = dataStorage.loadUserDictionary();
        HashMap<String, Word> unclicked = dataStorage.loadUnclickedWords();

        String vocabString = "";
        if (vocabulary.isEmpty()) {
            vocabString = "No vocabulary words looked up yet.\n";
        }

        // list vocabulary words
        for (Map.Entry<String, Word> entry : vocabulary.entrySet()) {
            String key = entry.getKey();
            Word word = entry.getValue();
            vocabString += key + ": " + Integer.toString(word.getTimesLookedUp()) + "\n";
        }
        vocabString += "\nUnclicked Words:\n";
        if (unclicked.isEmpty()) {
            vocabString += "No work saved";
        }

        for (Map.Entry<String, Word> entry: unclicked.entrySet()) {
            String key = entry.getKey();
            Word word = entry.getValue();
            vocabString += key + ": " + Integer.toString(word.getTimesLookedUp()) + "\n";
        }
        return vocabString;
    }
}
