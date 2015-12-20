package com.mattmellor.gear;

import android.util.Log;

import java.util.HashSet;

/**
 * Class to store information about a user interaction with a word
 * that the user has looked up
 */
public class WordLookup {

    // the word user looked up
    private final String word;

    private final String definition;
    private final String lemma;

    // stores the times the user looked up the word
    private final HashSet<Long> timestamps = new HashSet<Long>();;

    /**
     * CONSTRUCTOR
     * @param word the word looked up
     * @param definition the English translation of the word
     * @param lemma the German lemma of the word
     */
    public WordLookup(String word, String definition, String lemma) {
        this.word = word;
        this.definition = definition;
        this.lemma = lemma;
        timestamps.add(System.currentTimeMillis());
        Log.d("wordlookup", "Created WordLookup for " + word + " " + definition + " " + lemma);
    }

    /**
     * Updates structure to account for new user lookup
     */
    public void update() {
        timestamps.add(System.currentTimeMillis());
    }

    /**
     * @return the word the WordLookup structure stores information for
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

}
