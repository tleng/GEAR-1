package com.mit.gear.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.mattmellor.gear.R;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.reading.StoryItem;
import com.mit.gear.reading.StoryListAdapter;
import com.mit.gear.reading.StoryListListener;
import com.mit.gear.data.DataStorage;
import com.mit.gear.miscellaneous.MapUtil;
import com.mit.gear.NavDrawer.NavDrawerListAdapter;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.Word;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.mattmellor.gear.R.id.app_article_bar;

/**
 * Activity where user can browse and select which story to read.
 * The activity also goes through articles and the words the user has
 * looked up to recommend articles with most overlap.
 */
public class SuggestedStoriesActivity extends Fragment {

    private  Map<String, Double> articlesWithRatings = new HashMap<>();
    private android.support.v7.widget.Toolbar toolbar;
    private ProgressDialog progress;
    private int num_recommended_articles = 0;
    private List<String> articles;
    private Map<String,Double> articleAndScoreMap = new HashMap<>();
    static public Context context;
    DataStorage dataStorage;
    HashMap<String,Word> userDictionary;
    AssetManager assetManager;
    ArrayAdapter<StoryItem> adapter;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_suggested_stories, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadTheOpenedArticles();
        View v = getView();
        final Activity activity =  getActivity();
        context = getActivity();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock the current orientation
        toolbar = (android.support.v7.widget.Toolbar) v.findViewById(app_article_bar);
        dataStorage = new DataStorage(getActivity().getApplicationContext());
        progress=new ProgressDialog(activity);
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
                    articles = recommendKArticles2();
                }

                else{  //if suggested stories was previously generated, get them from the bundle
                    articles=savedInstanceState.getStringArrayList("recommendedArticles");
                    articlesWithRatings= (Map<String, Double>) savedInstanceState.getSerializable("articlesWithRatings");
                    articleAndScoreMap= (Map<String, Double>) savedInstanceState.getSerializable("articleAndScoreMap");

                }
                generateRecommendationList();
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // update the nav drawer to indicate the number of suggested stories are there
                        MainActivity.navDrawerItems.get(1).setCounterVisibility(true);
                        MainActivity.navDrawerItems.get(1).setCount(String.valueOf(num_recommended_articles));
                        MainActivity.adapter = new NavDrawerListAdapter(MainActivity.context,
                                MainActivity.navDrawerItems);
                        MainActivity.mDrawerList.setAdapter(MainActivity.adapter);
                        MainActivity.mDrawerList.setItemChecked(1, true);
                        MainActivity.mDrawerList.setSelection(1);
                    }
                });

            }
        }).start();

    }

    /*@Override
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

    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("recommendedArticles", (ArrayList<String>) articles); //save the suggested stories in case the user changes the orientation
        outState.putSerializable("articlesWithRatings", (Serializable) articlesWithRatings);
        outState.putSerializable("articleAndScoreMap", (Serializable) articleAndScoreMap);

    }

    /**
     * Gets recommended stories and add them to a list in the view
     */
    private void generateRecommendationList() {
        //making variables as final to access it from inner thread
        final List<StoryItem> listStoryItem = new ArrayList<>();
        int articleNumber = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        Boolean debugChoice = sharedPreferences.getBoolean("debug", false);
        for (String article:articles) {
            final StoryItem StoryItem =new StoryItem();
            Double rating = articlesWithRatings.get(article) * 100;
            int suggestNumber = rating.intValue();
            String ratingString = articleAndScoreMap.get(article)+" ";
            String title;
            if(debugChoice) {           //show the article score (debug mode is on)
                StoryItem.setCount( Count(article)+"\n"+ ratingString);
                //title = article + "\t" + ratingString + "\n" + Count(article);
            }
            articleNumber += 1;
            StoryItem.setTitle(article);
            StoryItem.setContentDescription(article);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listStoryItem.add(StoryItem);
                }
            });
            if(articleNumber%2==0){
                progress.setProgress(progress.getProgress()+1);
            }
        }
        final ListView lv = (ListView) getActivity().findViewById(R.id.listView2);

             adapter = new StoryListAdapter(getActivity(), R.layout.rss_list_item_debug_mode, listStoryItem);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lv.setAdapter(adapter);
                }
            });


        lv.setOnItemClickListener(new StoryListListener(listStoryItem, getActivity()));
        if ((this.progress != null) && this.progress.isShowing())        //dismissing the progressDialog if it was shown
            this.progress.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!= null)
            adapter.notifyDataSetChanged();
    }
/* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_stories_suggestion_and_selection, menu);
        return true;
    }*/


    /**
     * Gets recommended stories and adds buttons for each story to the view
     */
//    private void generateRecommendationButtons() {
//        //Replace recommendedKArticles with different algorithms
//        //making variables as final to access it from inner thread
//        // adjust linear layout to display articles in
//        final LinearLayout ll = (LinearLayout) findViewById(R.id.suggestedStoriesLinearLayout);
//        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll.setOrientation(LinearLayout.VERTICAL);
//        // StackOverflow suggested code to dynamically create buttons for the articles
//        for (String article:articles) {
//            final Button myButton = new Button(this);
//            Double rating = articlesWithRatings.get(article) * 100;
//            int suggestNumber = rating.intValue();
//            //String ratingString = "<b> " + suggestNumber + "%" + " </b>";
//            String ratingString = "<b> "+articleAndScoreMap.get(article)+" </b>";
//            myButton.setText(Html.fromHtml(article + "   " + ratingString));
//            myButton.setContentDescription(article);
//            myButton.setHeight(30);
//            myButton.setTransformationMethod(null); // ensures text is lower case
//            Log.d("button added:", myButton.toString());
//            myButton.setOnClickListener(getOnClickSetStory(myButton));
//
//            //Ui thread to attach the buttons to the linerLayout
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    ll.addView(myButton, lp);
//                    Log.d("number of buttons:", Integer.toString(ll.getChildCount()));
//                }
//            });
//
//        }
//        //dismissing the progressDialog if it was shown
//        if ((this.progress != null) && this.progress.isShowing())
//            this.progress.dismiss();
//    }


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

    private List<String> recommendKArticles2() {
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
                if(articleNumber%2==0){
                    progress.setProgress(articleNumber/2);
                }
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
            num_recommended_articles=n;
            List<String> recommendedArticles = new ArrayList<>();

            // check if there are sortedArticles
            if (n>0) {
                for (int i = n - 1; i >= 0; i--) {
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
        userDictionary = dataStorage.loadUserDictionary();
        InputStream input;
        String articleText;

        try {
            // open articleFilename and read article into articleText
            assetManager = getResources().getAssets();
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

//    View.OnClickListener getOnClickSetStory(final Button button)  {
//        return new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), ReadArticleActivity.class);
//                intent.putExtra("title", GEARGlobal.articlePath + button.getContentDescription());
//                getActivity().startActivity(intent);
//            }
//        };
//    }



    /*
     * Method to read the article and count
     * total number of words
     * number of unique words
     * number of total words in user dictionary
     * number of total unique words in user dictionary
     */
    private String Count(String articleTitle){
        //Set counters to 0
        int VocUniqueCount=0;
        int WordsInUD = 0;
        int count =0;
        assetManager = getResources().getAssets();
        InputStream inputStream;
        String ArticleText = "";
        try {
            inputStream = assetManager.open(GEARGlobal.articlePath+articleTitle);           //Open the passed article for reading
            int size = inputStream.available();                                             //Get the number of byte to read
            byte[] buffer = new byte[size];
            inputStream.read(buffer);                                                       //Read the article
            inputStream.close();
            ArticleText = new String(buffer).trim();                                        //Byte buffer into a string
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String, Integer> UniqueWordCount = new HashMap<>();                         //Keep track of unique word along with their occurrence
        userDictionary = dataStorage.loadUserDictionary();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.GERMANY);             //Set language of break iterator
        iterator.setText(ArticleText);
        int start = iterator.first();
        //Loop through each word in the article
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                    .next()) {
            String possibleWord = ArticleText.substring(start, end);
            if (Character.isLetter(possibleWord.charAt(0))) {                              //if the word start with letter increment total word count
                count++;
                if (UniqueWordCount.containsKey(possibleWord.toLowerCase())){
                    UniqueWordCount.put(possibleWord.toLowerCase(),
                            UniqueWordCount.get(possibleWord.toLowerCase()) + 1);   //Word is already contained in map increment it's occurrence by 1
                }else{
                    UniqueWordCount.put(possibleWord.toLowerCase(), 1);                                  //Word is not contained in map add it and set it's occurrence to 1
                }
            }
            if (userDictionary.containsKey(possibleWord)){                                 //Word is in user dictionary increment counter
                WordsInUD++;
            }
        }
        //Loop through user dictionary to check if word exist in both the dictionary and unique word map
        for(Map.Entry<String, Word> entry : userDictionary.entrySet()){
            String key = entry.getKey();
            if(UniqueWordCount.containsKey(key)){
                VocUniqueCount++;
            }
        }
        //Set the resulting string to contain all counters
        String result = String.valueOf(WordsInUD)+"/"+String.valueOf(count)+"\t\t\t"
                +String.valueOf(VocUniqueCount)+"/"+String.valueOf(UniqueWordCount.size());
        UniqueWordCount.clear();
        return result;
    }


    /*
     * This method loads the opened articles set from shared preference
     */

    public void loadTheOpenedArticles(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        ReadArticleActivity.articlesOpened=  new HashSet<String>(sharedPreferences.getStringSet("openedArticles", new HashSet<String>()));

    }

}


