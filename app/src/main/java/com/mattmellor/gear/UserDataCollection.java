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
    public static void addWord(String word) {
        UserData user = allUserData.get(currentUser);
        user.addWord(word);
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

//    public void writeToFile(String filename) throws IOException {
//
//
//        String content = "";
//        for (UserData userData : getAllUserData()) {
//            content += userData.toString() + ", "; // might want to and a new line
//        }
//
//        String string = "Hello world!";
//        FileOutputStream outputStream;
//
//        try {
//            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
//            outputStream.write(content.getBytes());
//            outputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Context context = getApplicationContext();
//
//        File file = new File(context.getFilesDir(), filename); // need to add some relative path
//
//        // if file doesnt exists, then create it
//        if (!file.exists()) {
//            file.createNewFile();
//
//        }
//
//        FileWriter fw = new FileWriter(file.getAbsoluteFile()); // how do I make sure that the file is not overwritten? or maybe I'm ok with it being overwritten
//        BufferedWriter bw = new BufferedWriter(fw);
//        bw.write(content);
//        bw.close();
//    }
