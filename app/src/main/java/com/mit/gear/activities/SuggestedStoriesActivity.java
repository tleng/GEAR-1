package com.mit.gear.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.miscellaneous.MapUtil;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.Word;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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

    private  Map<String, Double> articlesWithRatings = new HashMap<>();
    private android.support.v7.widget.Toolbar toolbar;
    private ProgressDialog progress;
    private int num_recommended_articles = 10;
    private List<String> articles;

    private Map<String,Double> articleAndScoreMap = new HashMap<>();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock the current orientation
        setContentView(R.layout.activity_suggested_stories);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(app_article_bar);
        setSupportActionBar(toolbar);
        //prepare progressDialog
        progress=new ProgressDialog(this);
        progress.setMessage("Generating Recommendations");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0);
        progress.setCancelable(false);
        progress.show();

        //create thread to generate the suggested stories and update progressDialog
        new Thread(new Runnable() {
        @Override
        public void run() {

            if(savedInstanceState==null) //if suggested stories was  generating for the first time
            {
                articles = recommendKArticles2(num_recommended_articles);
            }

            else{  //if suggested stories was previously generated, get them from the bundle
                articles=savedInstanceState.getStringArrayList("recommendedArticles");
                articlesWithRatings= (Map<String, Double>) savedInstanceState.getSerializable("articlesWithRatings");
                articleAndScoreMap= (Map<String, Double>) savedInstanceState.getSerializable("articleAndScoreMap");

            }
            generateRecommendationButtons();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
            }).start();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("recommendedArticles", (ArrayList<String>) articles); //save the suggested stories in case the user changes the orientation
        outState.putSerializable("articlesWithRatings", (Serializable) articlesWithRatings);
        outState.putSerializable("articleAndScoreMap", (Serializable) articleAndScoreMap);

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
        //Replace recommendedKArticles with different algorithms
        //making variables as final to access it from inner thread
        // adjust linear layout to display articles in
        final LinearLayout ll = (LinearLayout) findViewById(R.id.suggestedStoriesLinearLayout);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setOrientation(LinearLayout.VERTICAL);
        // StackOverflow suggested code to dynamically create buttons for the articles
        for (String article:articles) {
            final Button myButton = new Button(this);
            Double rating = articlesWithRatings.get(article) * 100;
            int suggestNumber = rating.intValue();
            //String ratingString = "<b> " + suggestNumber + "%" + " </b>";
            String ratingString = "<b> "+articleAndScoreMap.get(article)+" </b>";
            myButton.setText(Html.fromHtml(article + "   " + ratingString));
            myButton.setContentDescription(article);
            myButton.setHeight(30);
            myButton.setTransformationMethod(null); // ensures text is lower case
            Log.d("button added:", myButton.toString());
            myButton.setOnClickListener(getOnClickSetStory(myButton));

            //Ui thread to attach the buttons to the linerLayout
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ll.addView(myButton, lp);
                    Log.d("number of buttons:", Integer.toString(ll.getChildCount()));
                }
            });

        }
        //dismissing the progressDialog if it was shown
        if ((this.progress != null) && this.progress.isShowing())
            this.progress.dismiss();
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
    /**
    private List<String> recommendKArticles(int k) {
        Map<String,Double> articleAndScoreMap = new HashMap<>();
        //Map<String,Double> setNumberOfArticlesWithFractions  = new HashMap<>();

        try {
            ArrayList<String> listOfArticleAssets = listAllArticles();
            for(String article: listOfArticleAssets){
                Double fraction = getFractionOfWords(article);
                articleAndScoreMap.put(article,fraction);
            }
            articleAndScoreMap = MapUtil.sortByValue(articleAndScoreMap);
            Set<String> sortedArticles = articleAndScoreMap.keySet();
            List<String> sortedArticleList = new ArrayList<>();
            this.articlesWithRatings.putAll(articleAndScoreMap);
            for(String article: sortedArticles){
                sortedArticleList.add(article);
                //This just returns the keyset as a list???
            }
            int n = sortedArticles.size();
            List<String> recommendedArticles = new ArrayList<>();
            for(int i=n-1;i>=n-k-1;i--){
                recommendedArticles.add(sortedArticleList.get(i));
                Log.d("article", sortedArticleList.get(i));
                Log.d("fraction", articleAndScoreMap.get(sortedArticleList.get(i)).toString());
            }
            return recommendedArticles;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
*/

    private List<String> recommendKArticles2(int k) {
        //Map<String,Double> setNumberOfArticlesWithFractions  = new HashMap<>();

        try {
            ArrayList<String> listOfArticleAssets = listAllArticles();
            int articleNumber = 0;
            int totalArticles = listOfArticleAssets.size();
            //setting the progressDialog maximum to the total articles
            progress.setMax(totalArticles);
            for(String article: listOfArticleAssets){
                Double fraction = getScore(article);
                articleAndScoreMap.put(article,fraction);
                articleNumber += 1;
                int percentComplete = 50;
                //updating the progressDialog with article number
                progress.setProgress(articleNumber);
            }
            articleAndScoreMap = MapUtil.sortByValue(articleAndScoreMap);
            Set<String> sortedArticles = articleAndScoreMap.keySet();
            List<String> sortedArticleList = new ArrayList<>();
            this.articlesWithRatings.putAll(articleAndScoreMap);
            for(String article: sortedArticles){
                sortedArticleList.add(article);
                //This just returns the keyset as a list???
            }
            int n = sortedArticles.size();
            List<String> recommendedArticles = new ArrayList<>();

            // check if there are sortedArticles
            if (n>0) {
                for (int i = n - 1; i >= n - k - 1; i--) {
                    recommendedArticles.add(sortedArticleList.get(i));
                    Log.d("article", sortedArticleList.get(i));
                    Log.d("fraction", articleAndScoreMap.get(sortedArticleList.get(i)).toString());
                }
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
        String assets[] = assetManager.list(GEARGlobal.articlePathName);
        ArrayList<String> assetsAsString = new ArrayList<String>();
        Collections.addAll(assetsAsString,assets);
        assetsAsString.remove("sounds");
        assetsAsString.remove("webkit");
        return assetsAsString;
    }




    /**
     * @param articleFilename = filename of the article
     * @return
     */
    private Double getScore(String articleFilename) {
        DataStorage dataStorage = new DataStorage(getApplicationContext());
        HashMap<String,Word> userDictionary = dataStorage.loadUserDictionary();
        InputStream input;
        String articleText;

        try {
            // open articleFilename and read article into articleText
            AssetManager assetManager = getResources().getAssets();
            input = assetManager.open(GEARGlobal.articlePath+articleFilename);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            articleText = new String(buffer).trim();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            articleText = "Error Occurred";  // better - signal error differently
        }


        String[] articleWords = articleText.split("[\\p{Punct}\\s]+");

//        List<String> lowerCaseArticleWords = new ArrayList<String>(); // we are considering all words
//        for (String word : articleWords) {
//            lowerCaseArticleWords.add(word.toLowerCase());
//        }

        Double counter=1.0;
        for(String word: articleWords){
            if(userDictionary.containsKey(word)){
                counter *= 1-userDictionary.get(word).score;
            } else {
                // word has never been encountered before
                counter *= 0.5;
            }
        }

        return counter;

    }

    View.OnClickListener getOnClickSetStory(final Button button)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SuggestedStoriesActivity.this, ReadArticleActivity.class);
                intent.putExtra("story", GEARGlobal.articlePath + button.getContentDescription());
                startActivity(intent);
                finish();
            }
        };
    }
}


