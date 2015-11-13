package com.mattmellor.gear;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SuggestedStories extends AppCompatActivity {

    private final Map<String, Double> articlesWithRatings = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_stories);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        List<String> articles = recommendKArticles(5);

        LinearLayout ll = (LinearLayout) findViewById(R.id.suggestedStoriesLinearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setOrientation(LinearLayout.VERTICAL);
        //StackOverflow
        for (String article:articles) {
            Button myButton = new Button(this);
            Double rating = articlesWithRatings.get(article) * 100;
            int suggestNumber = rating.intValue();
            String ratingString = "<b> " + suggestNumber + "%" + " </b>";
            myButton.setText(Html.fromHtml(article + "   " + ratingString));
            myButton.setContentDescription(article);

            Log.d("button added:", myButton.toString());
            myButton.setOnClickListener(getOnClickSetStory(myButton));

            ll.addView(myButton, lp);
            Log.d("number of buttons:", Integer.toString(ll.getChildCount()));
        }
    }

    /**
     *
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
                //setNumberOfArticlesWithFractions.put(sortedArticleList.get(i),allFractionMappings.get(sortedArticleList.get(i)));
                Log.d("article", sortedArticleList.get(i));
                Log.d("fraction", allFractionMappings.get(sortedArticleList.get(i)).toString());
            }
            return recommendedArticles;

            //Log.d("first article",sortedArticleList.get(0));
            //Double fractionTest = getFractionOfWords(listOfArticleAssets[0]);
            //Log.d("fraction",fractionTest.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

    private ArrayList<String> listAllArticles() throws IOException {
        AssetManager assetManager = getResources().getAssets();
        String assets[] = assetManager.list("");
        ArrayList<String> assetsAsString = new ArrayList<String>();
        Collections.addAll(assetsAsString,assets);
        assetsAsString.remove("sounds");
        assetsAsString.remove("webkit");
        return assetsAsString;
    }


    private Double getFractionOfWords(String article) {
        HashMap<String, Integer> userVocab = overallUserVocab.getUserDictionary();

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

        //for (String word : articleWords) {Log.d("word", word);};

        Double fraction = counter/lowerCaseArticleWords.size();
        return fraction;

    }

    View.OnClickListener getOnClickSetStory(final Button button)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SuggestedStories.this, MainActivity.class);
                intent.putExtra("story",button.getContentDescription());
                startActivity(intent);
                finish();
            }
        };
    }


}

class MapUtil
{
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}