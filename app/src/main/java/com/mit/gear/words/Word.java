package com.mit.gear.words;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class to store information about a user interaction with a word
 * that the user has looked up
 */
public class Word {

    // the word user looked up
    private final String word;
    private final String lemma;
    public double score;
    public boolean clicked;
    private HashMap<String,ArrayList<Long>> articleClicks;
    private HashMap<String,ArrayList<Long>> articlePasses;

    // stores the times the user looked up the word
    private final HashSet<Long> timestamps = new HashSet<Long>();

    /**
     * CONSTRUCTOR
     * @param word the word looked up
     * @param lemma the German lemma of the word
     */
    public Word(String word, String lemma) {
        this.word = word;
        this.lemma = lemma;
        timestamps.add(System.currentTimeMillis());
        Log.d("Word", "Created Word for " + word + " " + lemma);
        score = 0;
        clicked = false;
        articleClicks = new HashMap<>();
        articlePasses = new HashMap<>();
    }

    public Word(String word) {
        this.word = word;
        this.lemma = "None";
        timestamps.add(System.currentTimeMillis());
        clicked = false;
        Log.d("Word", "Created Word without lemma for " + word + " " + lemma);
        articleClicks = new HashMap<>();
        articlePasses = new HashMap<>();
    }



    /**
     * Updates structure to account for new user lookup
     */
    public void update(String article, boolean click) {
        Long lookupTime = System.currentTimeMillis();
        timestamps.add(lookupTime);
        if (click) {
            clicked = click;
            if (!articleClicks.containsKey(article)) {
                articleClicks.put(article, new ArrayList<Long>());
            }
            ArrayList<Long> times = articleClicks.get(article);
            times.add(lookupTime);
            articleClicks.put(article, times);
        } else {
            if (!articlePasses.containsKey(article)) {
                articlePasses.put(article, new ArrayList<Long>());
            }
            ArrayList<Long> times = articlePasses.get(article);
            times.add(lookupTime);
            articlePasses.put(article, times);
        }
    }

    /**
     * @return the word the Word structure stores information for
     */
    public String getWord() {
        return word;
    }

    /**
     *
     * @return
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * @return the number of times the user has looked up the word in question
     */
    public int getTimesLookedUp() {
        return timestamps.size();
    }

    public void setClicked(boolean click) {
        clicked = click;
    }

}
