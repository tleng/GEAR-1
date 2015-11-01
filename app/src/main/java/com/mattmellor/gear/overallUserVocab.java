package com.mattmellor.gear;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class overallUserVocab extends AppCompatActivity {
    private static HashMap<String, Integer> userDictionary = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overall_user_vocab);
        TextView vocabView = (TextView) findViewById(R.id.textView4);
        String vocabString = "";
        for (String key:userDictionary.keySet()) {
            vocabString += key + ": " + Integer.toString(userDictionary.get(key)) + "\n";
        }
        if (userDictionary.isEmpty()) {
            vocabString = "No vocabulary words looked up yet.\n";
        }
        vocabView.setText(vocabString);

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

    public static void addWordToUserDictionary(String word) {
        String lowerCaseWord = word.toLowerCase();
        if (!userDictionary.containsKey(lowerCaseWord)) {
            userDictionary.put(lowerCaseWord, 1);
        } else {
            userDictionary.put(lowerCaseWord,userDictionary.get(lowerCaseWord)+1);
        }
    }

    public static HashMap<String, Integer> getUserDictionary() {
        HashMap<String, Integer> copyOfDictionary = new HashMap<String, Integer>();
        for (String word : userDictionary.keySet()) {
            copyOfDictionary.put(word, userDictionary.get(word));
        }
        return copyOfDictionary;
    }
}
