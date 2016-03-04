package com.mit.gear.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mattmellor.gear.R;
import com.mit.gear.miscellaneous.MapUtil;
import com.mit.gear.data.UserDataCollection;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.words.WordLookup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mattmellor.gear.R.id.app_article_bar;

/**
 * Activity where user can browse and select which story to read.
 * The activity also goes through articles and the words the user has
 * looked up to recommend articles with most overlap.
 */
public class SuggestedStoriesActivity extends AppCompatActivity {

    private final Map<String, Double> articlesWithRatings = new HashMap<>();
    private android.support.v7.widget.Toolbar toolbar;

    private int num_recommended_articles = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_stories);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(app_article_bar);
        setSupportActionBar(toolbar);

        generateRecommendationButtons();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_stories_suggestion_and_selection, menu);
        return true;
    }


    /**
     * Gets recommended stories and adds buttons for each story to the view
     */
    private void generateRecommendationButtons() {
        List<String> articles = recommendKArticles(num_recommended_articles);

        // adjust linear layout to display articles in
        LinearLayout ll = (LinearLayout) findViewById(R.id.suggestedStoriesLinearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setOrientation(LinearLayout.VERTICAL);


        // StackOverflow suggested code to dynamically create buttons for the articles
        for (String article:articles) {
            Button myButton = new Button(this);
            Double rating = articlesWithRatings.get(article) * 100;
            int suggestNumber = rating.intValue();
            String ratingString = "<b> " + suggestNumber + "%" + " </b>";
            myButton.setText(Html.fromHtml(article + "   " + ratingString));
            myButton.setContentDescription(article);
            myButton.setHeight(30);
            myButton.setTransformationMethod(null); // ensures text is lower case

            Log.d("button added:", myButton.toString());
            myButton.setOnClickListener(getOnClickSetStory(myButton));

            ll.addView(myButton, lp);
            Log.d("number of buttons:", Integer.toString(ll.getChildCount()));
        }
    }

    // TODO: replace with either recommendation from backend (needs
    // further setup of backend) or more sophisticated inference of user vocabulary
    // rather than words the user has clicked on
    /**
     * Recommends k articles based on fraction of words in the article that are
     * among words the user looked up
     * @param k is number of articles to recommmend, requires k< number of articles
     * @return k articles with highest fraction of words in userDictionary
     */
    private List<String> recommendKArticles(int k) {
        Map<String,Double> allFractionMappings = new HashMap<>();
        //Map<String,Double> setNumberOfArticlesWithFractions  = new HashMap<>();

        try {
            ArrayList<String> listOfArticleAssets = listAllArticles();
            for(String article: listOfArticleAssets){
                Double fraction = getFractionOfWords(article);
                allFractionMappings.put(article,fraction);
            }
            allFractionMappings = MapUtil.sortByValue(allFractionMappings);
            Set<String> sortedArticles = allFractionMappings.keySet();
            List<String> sortedArticleList = new ArrayList<>();
            this.articlesWithRatings.putAll(allFractionMappings);
            for(String article: sortedArticles){
                sortedArticleList.add(article);
                //This just returns the keyset as a list???
            }
            int n = sortedArticles.size();
            List<String> recommendedArticles = new ArrayList<>();
            for(int i=n-1;i>=n-k-1;i--){
                recommendedArticles.add(sortedArticleList.get(i));
                Log.d("article", sortedArticleList.get(i));
                Log.d("fraction", allFractionMappings.get(sortedArticleList.get(i)).toString());
            }
            return recommendedArticles;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Lists all articles in the article folder
     * @return
     * @throws IOException
     */
    private ArrayList<String> listAllArticles() throws IOException {
        AssetManager assetManager = getResources().getAssets();
        String assets[] = assetManager.list("");
        ArrayList<String> assetsAsString = new ArrayList<String>();
        Collections.addAll(assetsAsString,assets);
        assetsAsString.remove("sounds");
        assetsAsString.remove("webkit");
        return assetsAsString;
    }


    /**
     * Gets fraction of words in the article that are also in the
     * set of words the user has looked up previously
     * @param article
     * @return
     */
    private Double getFractionOfWords(String article) {
        HashMap<String, WordLookup> userVocab = UserDataCollection.getCurrentVocabulary();

        InputStream input;
        String text = article;
        try {
            AssetManager assetManager = getResources().getAssets();
            input = assetManager.open(article);
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

        String[] articleWords = text.split("[\\p{Punct}\\s]+");
        List<String> lowerCaseArticleWords = new ArrayList<String>(); // we are considering all words
        for (String word : articleWords) {
            lowerCaseArticleWords.add(word.toLowerCase());
        }

        Double counter=0.0;
        for(String word: lowerCaseArticleWords){
            if(userVocab.containsKey(word)){
                counter += 1;
            }
        }

        Double fraction = counter/lowerCaseArticleWords.size();
        return fraction;

    }

    View.OnClickListener getOnClickSetStory(final Button button)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SuggestedStoriesActivity.this, ReadArticleActivity.class);
                intent.putExtra("story",button.getContentDescription());
                startActivity(intent);
                finish();
            }
        };
    }
}


