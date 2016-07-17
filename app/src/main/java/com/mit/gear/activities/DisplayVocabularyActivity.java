package com.mit.gear.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.Word;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that displays user vocabulary
 */
public class DisplayVocabularyActivity extends Fragment {
    TextView vocabView;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_display_user_vocabulary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        // display current user vocabulary
        vocabView = (TextView) v.findViewById(R.id.vocabularyData);
        vocabView.setText(getVocabularyString());

    }
    /*@Override
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
    }*/


    /**
     * TODO: Determine meaningful way to display vocabulary – what does user want?
     * Turns user vocabulary into string representation for display
     * @return string to display
     */
    private String getVocabularyString() {
        DataStorage dataStorage = new DataStorage(getActivity().getApplicationContext());
        HashMap<String, Word> vocabulary = dataStorage.loadUserDictionary();

        String vocabString = "";
        if (vocabulary.isEmpty()) {
            vocabString = "No vocabulary words looked up yet.\n";
        }

        // list vocabulary words
        for (Map.Entry<String, Word> entry : vocabulary.entrySet()) {
            String key = entry.getKey();
            Word word = entry.getValue();
            if(word.getLemma().equals("None"))
                vocabString += key +"\nClicked: " + Integer.toString(word.totalWordClicks()) + "\t\t\tPassed: " + Integer.toString(word.totalWordPasses()) + "\n\n";
            else
                vocabString += key + " , "+word.getLemma()+"\nClicked: " + Integer.toString(word.totalWordClicks()) + "\t\t\tPassed: " + Integer.toString(word.totalWordPasses()) + "\n\n";
        }

        return vocabString;
    }
}
