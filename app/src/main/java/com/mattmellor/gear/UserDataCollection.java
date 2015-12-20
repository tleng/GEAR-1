package com.mattmellor.gear;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/*
 * Class to store data from all users that use the application
 */

public class UserDataCollection {

    // Tracks who is currently logged in
    private static String currentUser;

    // Stores data from all different users using the application
    public static HashMap<String, UserData> allUserData = new HashMap<String, UserData>();


    public static void login(String user) {
        if (!allUserData.keySet().contains(user)) {
            allUserData.put(user, new UserData(user));
        }
        currentUser = user;
    }

    /**
     *
     * @param userData
     */
    public static void addUser(UserData userData) {
        allUserData.put(userData.getUserId(), userData);
    }

    /**
     * Gets all data associated with a given user
     * @param userID the user to retrieve data for
     * @return UserData instance for specified user
     */
    public static UserData getUserData(String userID) {
        return allUserData.get(userID);
    }

    /**
     * Specifies who the current user is
     * @param name the username of the user to set
     */
    public static void setCurrentUser(String name) {
        currentUser = name;
    }

    /**
     * Stores a  rating from the current user for a specified article
     * @param article the article being rated
     * @param rating  the rating given to the article
     */
    public static void addRating(String article, int rating) {
        UserData user = allUserData.get(currentUser);
        user.rateArticle(article, rating);
    }

    /**
     * Adds a word that was looked up to the current user UserData instance
     * @param word the word that was looked up
     */
    public static void addWord(String word, String definition, String lemma) {
        UserData user = allUserData.get(currentUser);
        user.addWord(word, definition, lemma);
    }

    /**
     * Sets the amount time the current user spent on a specified article
     * @param article article user read
     * @param time time user spent on article
     */
    public static void setTimeSpentOnArticle(String article, Long time) {
        UserData user = allUserData.get(currentUser);
        user.setTimeSpentOnArticle(article, time);
    }

    /**
     * Returns a set of all user data
     * @return a set of all UserData instances
     */
    public static Set<UserData> getAllUserData() {
        Set<UserData> copyOfData = new HashSet<UserData>();
        for (UserData data : allUserData.values()) {
            copyOfData.add(data);
        }
        return copyOfData;
    }

    /**
     * Gets vocabulary of current user
     * @return a map of word to WordLookup instances for the current user
     */

    public static HashMap<String, WordLookup> getCurrentVocabulary() {
        UserData user = allUserData.get(currentUser);
        return user.getWordsLookedUp();
    }
}

