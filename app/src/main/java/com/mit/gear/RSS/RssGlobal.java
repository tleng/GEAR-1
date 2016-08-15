package com.mit.gear.RSS;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.mit.gear.NavDrawer.NavDrawerListAdapter;
import com.mit.gear.RSS.RssArticle;
import com.mit.gear.RSS.RssReader;
import com.mit.gear.activities.MainActivity;
import com.mit.gear.data.DataStorage;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.words.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by najlaalghofaily on 8/14/16.
 */


/*
 *
 * This class contains all method shared between LiteNewsFragment and StoriesSelectionActivity
 *
 */

public class RssGlobal {

    static HashMap<String,Word> userDictionary;
    static DataStorage dataStorage;
    private static SharedPreferences sharedPreferences;


    /*
     * This method updates the update date shared preference in local storage
     */

   public static void setLastUpdateDate(Activity activity,String SPstring){

       //save today date as the last update date
       sharedPreferences = activity.getSharedPreferences("LastUpdateDate", Context.MODE_PRIVATE);
       SharedPreferences.Editor editor = sharedPreferences.edit();
       editor.putLong(SPstring, getTodayDate());
       editor.commit();

   }


    /*
    * This method access the news files in the device internal storage
    * returns a list of news
    */


    public static List<RssArticle> getNewsFromStorage(Activity activity, String DirName){
        File myDir = activity.getDir(DirName, Context.MODE_PRIVATE); //Creating an internal dir;
        if (!myDir.exists())
        {
            return null;
        }
        List<RssArticle> result = new ArrayList();
        File[] files = myDir.listFiles();
        for (File file : files) {

            RssArticle rssArticle=new RssArticle();
            rssArticle.setTitle(file.getName());
            StringBuffer content = new StringBuffer("");
            try {
                FileInputStream fIn = new FileInputStream(new File(file.getPath())) ;
                InputStreamReader isr = new InputStreamReader ( fIn ) ;
                BufferedReader bufferedReader = new BufferedReader ( isr ) ;

                String readString = bufferedReader.readLine () ; //the first line is the article's category
                rssArticle.setCategory(readString);
                readString = bufferedReader.readLine () ;
                rssArticle.setStarred(Boolean.parseBoolean(readString));
                readString = bufferedReader.readLine () ;
                while ( readString != null ) {
                    content.append(readString);
                    content.append('\n');


                    readString = bufferedReader.readLine () ;
                }
                rssArticle.setContent(String.valueOf(content));
                result.add(rssArticle);
                isr.close() ;
            } catch ( IOException ioe ) {
                ioe.printStackTrace() ;
            }
        }
        return result;
    }



    /*
     * This method get today date without time
     *
     */

    public static Long getTodayDate(){
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date todayWithZeroTime = formatter.parse(formatter.format(new Date()));
            return todayWithZeroTime.getTime();
        } catch (ParseException e) {
            return (long) 0;
        }
    }


    /*
     * This method checks if the news needs to update or not
     * return true if  today date is bigger than the last update date, false otherwise
     */


    public static boolean needsUpdate(long lastUpdateDate){
        Long todayDate = getTodayDate();
        if(lastUpdateDate < todayDate)
        {
            return true;
        }
        return false;
    }


    /*
     * This method gets the last update date shared preference from internal storage
     */


    public static long getLastUpdateDate(Activity activity,String SPstring){
        sharedPreferences = activity.getSharedPreferences("LastUpdateDate", Context.MODE_PRIVATE);
        return  sharedPreferences.getLong(SPstring, 0);

    }


     /*
      * This method loads the opened articles set from shared preference
      */

    public static void loadTheOpenedArticles(Activity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        ReadArticleActivity.articlesOpened=  new HashSet<String>(sharedPreferences.getStringSet("openedArticles", new HashSet<String>()));

    }


    /*
     * Method to read the article and count
     * total number of words
     * number of unique words
     * number of total words in user dictionary
     * number of total unique words in user dictionary
     */


    public static   String getCount(RssArticle rssArticle,Activity activity){

        dataStorage= new DataStorage(activity.getApplicationContext());
        userDictionary = dataStorage.loadUserDictionary();
        int VocUniqueCount=0;
        int WordsInUD = 0;
        int count =0;
        HashMap<String, Integer> UniqueWordCount = new HashMap<>();                  //Keep track of unique word along with their occurrence
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.GERMANY);      //Set language of break iterator
        iterator.setText(rssArticle.getContent());
        int start = iterator.first();
        //Loop through each word in the article
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = rssArticle.getContent().substring(start, end);
            if (Character.isLetter(possibleWord.charAt(0))) {                        //if the word start with letter increment total word count
                count++;
                if (UniqueWordCount.containsKey(possibleWord.toLowerCase())){
                    UniqueWordCount.put(possibleWord.toLowerCase(),
                            UniqueWordCount.get(possibleWord.toLowerCase()) + 1);   //Word is already contained in map increment it's occurrence by 1
                }else{
                    UniqueWordCount.put(possibleWord.toLowerCase(), 1);             //Word is not contained in map add it and set it's occurrence to 1
                }
            }
            if (userDictionary.containsKey(possibleWord)){                          //Word is in user dictionary increment counter
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
     * This method is the same as the one in SuggestedStoriesActivity  but altered for news use
     */

    public static Double getScore(Activity activity,RssArticle rssArticle) {
        dataStorage= new DataStorage(activity);
        userDictionary = dataStorage.loadUserDictionary();
        String articleText;
        try {

            articleText= rssArticle.getContent();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            articleText = "Error Occurred";  // better - signal error differently
        }

        String[] articleWords = articleText.split("[\\p{Punct}\\s]+");

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



    /*
     * This method updates a certain nav drawer tab with total number of news
     * select the specified tab position
     *
     */


    public static void updateNavDrawer(Integer itemPosition, Integer ListSize){

        MainActivity.navDrawerItems.get(itemPosition).setCounterVisibility(true);
        MainActivity.navDrawerItems.get(itemPosition).setCount(String.valueOf(ListSize));
        MainActivity.adapter = new NavDrawerListAdapter(MainActivity.context,
                MainActivity.navDrawerItems);
        MainActivity.mDrawerList.setAdapter(MainActivity.adapter);
        MainActivity.mDrawerList.setItemChecked(itemPosition, true);
        MainActivity.mDrawerList.setSelection(itemPosition);

    }



    /*
     *This method returns a list of new articles from a given RSS url
     * This method user in Lite news since the category is not spicifies in their XML file
     */

    public static List<RssArticle> getRssArticles(String urls, String category){
        try {
            List<RssArticle> allCategoryArticles = new ArrayList<>();
            // Create RSS reader
            RssReader rssReader = new RssReader(urls);
            for(RssArticle article:rssReader.getItems()){
                article.setCategory(category);
                allCategoryArticles.add(article);
            }
            return allCategoryArticles;

        } catch (Exception e) {
            return null;
        }
    }



    /*
     *This method returns a list of new articles from a given RSS url
     * This method is used in StoriesSelectionActivity
     */

    public static List<RssArticle> getRssArticles(String urls){
        try {
            // Create RSS reader
            RssReader rssReader = new RssReader(urls);
            return rssReader.getItems();

        } catch (Exception e) {
            return null;
        }
    }

}
