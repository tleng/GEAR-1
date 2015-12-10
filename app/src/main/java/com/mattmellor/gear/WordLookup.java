package com.mattmellor.gear;

import java.util.HashSet;

/**
 * Class to store information about a user interaction with a word
 * that the user has looked up
 */
public class WordLookup {

    // the word user looked up
    private final String word;

    // stores the times the user looked up the word
    private final HashSet<Long> timestamps;

    public WordLookup(String word) {
        this.word = word;
        timestamps = new HashSet<Long>();

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
     * @return the number of times the user has looked up the word in question
     */
    public int getTimesLookedUp() {
        return timestamps.size();
    }

}
