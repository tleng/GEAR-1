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
    public HashMap<String,ArrayList<Long>> articleClicks;
    public HashMap<String,ArrayList<Long>> articlePasses;

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
        score = 0.0; // just to test, set to 1
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


    public void scoreWord(boolean click, boolean inUserDictionary) {
        double p = totalWordClicks();
        double np = totalWordPasses();
        double clicked = 1;
        if (!click) { clicked = 0; }
        if (inUserDictionary) {
            score = score * (p + 1) / (p + np + 2) + clicked/(p+np+2);
        } else {
            score = clicked/2;
        }

        Log.d("Word Score", word + ":" + "p: " + Double.toString(p) + "np: " + Double.toString(np) + "score: " + Double.toString(score));
    }

    public int totalWordClicks() {
        int totalClicks = 0;
        for (String article : articleClicks.keySet()) {
            totalClicks += articleClicks.get(article).size();
        }
        return totalClicks;
    }

    public int totalWordPasses() {
        int totalPasses = 0;
        for (String article : articlePasses.keySet()) {
            totalPasses += articlePasses.get(article).size();
        }
        return totalPasses;
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
