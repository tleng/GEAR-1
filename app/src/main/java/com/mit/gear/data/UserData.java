package com.mit.gear.data;

import android.util.Log;

import com.mit.gear.words.WordLookup;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to store data associated with user behavior
 */
public class UserData {

    // Identifies unique users
    private String userId;

    // Store all words looked up by the user along with associated data
    // Associated data such as when the word is looked up, # times looked up, etc.
    // is stored in the WordLookup structure
    private final HashMap<String, WordLookup> wordsLookedUp = new HashMap<String, WordLookup>();

    // Stores user ratings of different articles
    private final HashMap<String, Integer> articleRatings = new HashMap<String, Integer>();

    // Stores total time user spent on each article
    private final Map<String, Long> articleTime = new HashMap<String, Long>();


    // TODO: Create data structure representing inferred user vocabulary

    public UserData(String userId) {
        this.userId = userId;

    }

    /**
     * Acts on user rating an article
     * @param article name of the article user is rating
     * @param rating the rating given to the article by the user
     */
    public void rateArticle(String article, int rating){
        articleRatings.put(article, rating);
    }


    /**
     * Acts on user lookup of a word
     * @param word the word the user looked up
     */
    public void addWord(String word, String definition, String lemma){
        String wordLowerCase = word.toLowerCase();

        // update WordLookup data if user looks up word a second time
        if (wordsLookedUp.containsKey(wordLowerCase)){
            wordsLookedUp.get(wordLowerCase).update();
        }
        else {
            wordsLookedUp.put(wordLowerCase, new WordLookup(wordLowerCase, definition, lemma));
        }
    }

    /**
     * Updates tracking of how long a user spends on a single article
     * For now assumes that user only interacts with an article once, or
     * tracks the latest session with the article
     * @param article name of the article
     * @param timeSpent total time user spent in session on article
     */
    public void setTimeSpentOnArticle(String article, Long timeSpent) {
        articleTime.put(article, timeSpent);
    }

    /**
     * Getter method for the user ID
     * @return
     */
    public String getUserId(){
        return userId;
    }

    /**
     * Getter method for wordsLookedUp
     * @return
     */
    public HashMap<String, WordLookup> getWordsLookedUp() {
        return wordsLookedUp;
    }


    /**
     *
     * @param name
     */
    public void setUserName(String name) {
        Log.i("userdata", "Set username to " + name);
        userId = name;
    }


    @Override
    public int hashCode(){
        return this.userId.hashCode();
    }



//    @Override
//    public boolean equals(Object thatObject){
//      TODO
//    }


//    @Override
//    public String toString() {
//      TODO
//    }

}