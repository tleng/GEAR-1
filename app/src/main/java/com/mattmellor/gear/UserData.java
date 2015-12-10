package com.mattmellor.gear;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to store data associated with user behavior
 * TODO: Make this non-static, and make UserDataCollection static,
 * TODO: but this first requires implementing user login
 */
public class UserData {

    // Identifies unique users
    private final String userId;

    // Store all words looked up by the user along with associated data
    // Associated data such as when the word is looked up, # times looked up, etc.
    // is stored in the WordLookup structure
    private static final HashMap<String, WordLookup> wordsLookedUp = new HashMap<String, WordLookup>();

    // Stores user ratings of different articles
    private static final HashMap<String, Integer> articleRatings = new HashMap<String, Integer>();

    // Stores total time user spent on each article
    // TODO: Update this when article is opened and closed
    private static final Map<String, Long> articleTime = new HashMap<String, Long>();


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
    public static void addWord(String word){
        String wordLowerCase = word.toLowerCase();

        // update WordLookup data if user looks up word a second time
        if (wordsLookedUp.containsKey(wordLowerCase)){
            wordsLookedUp.get(wordLowerCase).update();
        }
        else {
            wordsLookedUp.put(wordLowerCase, new WordLookup(wordLowerCase));
        }
    }

    /**
     * Updates tracking of how long a user spends on a single article
     * For now assumes that user only interacts with an article once, or
     * tracks the latest session with the article
     * @param article name of the article
     * @param timeSpent total time user spent in session on article
     */
    public static void setTimeSpentOnArticle(String article, Long timeSpent) {
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
    public static HashMap<String, WordLookup> getWordsLookedUp() {
        return wordsLookedUp;
    }

//    Not sure if logic of this equals method is correct?
//    @Override
//    public boolean equals(Object thatObject){
//        if(thatObject instanceof UserData){
//            return false;
//        }
//
//        else{
//            UserData that = (UserData) thatObject;
//            if(this.userId == that.userId && this.articleTitle.equals(that.articleTitle)){
//                return true;
//            }
//            else{
//                return false;
//            }
//        }
//    }

    @Override
    public int hashCode(){
        return this.userId.hashCode();
    }

//    @Override
//    public String toString(){
//        String words = "[";
//        for(String word: wordsLookedUp.keySet()){
//            words += word + " : " +wordsLookedUp.get(word)+",";
//        }
//        words += "]";
//
//        String outputString = "[ USER : " + userId + ", " + "article title : " + articleTitle +", "
//                + "startTime : " + startTime +", " + "exit : " + exitTime + ", "
//                + "words : " + words + " ]";
//
//        return outputString;
//    }




}