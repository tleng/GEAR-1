package com.mit.gear.reading;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.data.UserDataCollection;
import com.mit.gear.words.DefinitionRequest;
import com.mit.gear.words.WordLookup;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity where user reads article
 */
public class ReadArticleActivity extends AppCompatActivity {
    private static String LOG_APP_TAG = "ReadArticleActivity-tag";
    private static ReadArticleActivity instance;
    private android.support.v7.widget.Toolbar toolbar;
    public static HashMap<String,ArrayList<String>> offlineDictionary;
    private DefinitionRequest currentDefinitionRequest;

    public static String currentDefinition = "No definition";
    public static String currentLemma = "None";
    private Integer currentPosition = 0;

    private Long startTime;
    private String currentArticle;

    private ViewPager pagesView;

    public ReadArticleActivity() {
        instance = this;
    }

    private HashMap<String, WordLookup> dictionary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pages);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_article_bar);
        setSupportActionBar(toolbar);
        dictionary = new DataStorage(getApplicationContext()).loadJSONDictionary();
        currentDefinitionRequest = null;

        // Getting rid of title for the action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Log start time for when user opened article
        startTime = System.currentTimeMillis();

        pagesView = (ViewPager) findViewById(R.id.pages);
        pagesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                PageSplitter pageSplitter = new PageSplitter(pagesView.getWidth(), pagesView.getHeight(), 1, 0);

                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
                AssetManager assetManager = getAssets();
                String story = getIntent().getExtras().getString("story");
                currentArticle = story;
                InputStream input;
                String text;
                try {
                    input = assetManager.open(story);
                    int size = input.available();
                    byte[] buffer = new byte[size];
                    input.read(buffer);
                    input.close();

                    // byte buffer into a string
                    text = new String(buffer).trim();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    text = "Error Occurred";
                }
                pageSplitter.append(text, textPaint);
                pagesView.setAdapter(new TextPagerAdapter(getSupportFragmentManager(), pageSplitter.getPages()));
                pagesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        DataStorage dataStorage = new DataStorage(getApplicationContext());
        offlineDictionary = dataStorage.loadOfflineDictionary();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Method that lets users click on words
     *
     * @param word
     * @return
     */
    public ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord = word;

            public void onClick(View widget) {
                if (currentDefinitionRequest != null) {
                    Log.d("Cancel","Cancel current definition request: " + currentDefinitionRequest.toString());
                    currentDefinitionRequest.cancel(true);
                }
                Log.d("No cancel","current definition request not cancelled");
                Log.d("tapped on:", mWord);
                Context context = getApplicationContext();
                CharSequence message = mWord + " ausgewählt.";

                TextView definitionBox = (TextView) findViewById(R.id.definition_box);
                definitionBox.setText(message);


                //getAndDisplayDefinition.execute(mWord);
                if (offlineDictionary.containsKey(mWord)) {
                    Log.d("Offline Dictionary",mWord);
                }
                DefinitionRequest definitionRequest = new DefinitionRequest(mWord);
                currentDefinitionRequest = definitionRequest;
                definitionRequest.execute(mWord);
                Log.d("lookup", mWord);
            }

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
                if (dictionary.containsKey(mWord)) {
                    ds.setColor(Color.BLUE);
                }

            }
        };
    }


    /**
     * Stores relevant data
     * @param word
     * @param definition
     * @param lemma
     */
    public void updateDataStorage(String word, String definition, String lemma) {
        // Update data collection structures
        if (word != null) {
            UserDataCollection.addWord(word, definition, lemma);
            DataStorage dataStorage = new DataStorage(getApplicationContext());
            try {
                dataStorage.addToJSONDictionary(word);
                HashMap<String, WordLookup> map = dataStorage.loadJSONDictionary();
                Log.d("Dictionary",map.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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


    public void onClickUpPopWindow(View view) {
        Intent intent = new Intent(ReadArticleActivity.this, popUpRateArticle.class);
        startActivity(intent);
//        intent.putExtra("currentArticle", (String) currentUserData.getArticle());
//        intent.putExtra("currentUserId", (String) currentUserData.getUserId());
    }


    @Override
    protected void onPause() {
        super.onPause();


        // updates user data with time spent
        Long endTime = System.currentTimeMillis();
        Long timeSpent = endTime - startTime;
        UserDataCollection.setTimeSpentOnArticle(currentArticle, timeSpent);
    }

    protected void OnResume() {
        super.onResume();
    }

    public static ReadArticleActivity getReadArticleActivityInstance() {
        return instance;
    }
}
