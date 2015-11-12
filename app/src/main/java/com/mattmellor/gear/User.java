package com.mattmellor.gear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ktjolsen on 10/29/15.
 */
public class User {

    private final int id;
    final private List<String> currentSessionDictionary = new ArrayList<String>();
    final private HashMap<Article,Integer> articleRatings = new HashMap<Article,Integer>();

    public User(int id){
        this.id=id;
    }

    public int id(){
        return this.id;
    }


    public void addToDictionary(String word) {
        currentSessionDictionary.add(word);
    }

    public List<String> getCurrentSessionDictionary() {
        return currentSessionDictionary;
    }

    public void rateArticle(Article article, int rating) {
        articleRatings.put(article,rating);
    }

    /*
     * Should return the user's article ratings from the server/JSON file
     */
    public HashMap<Article,Integer> getArticleRatings() {
        return articleRatings;
    }

    /*
     * Should return the user's dictionary from the server
     */
    public List<String> getTotalDictionary() throws Exception {
        throw new Exception("Unimplemented");
    }

    /*
     * Should store the user's current dictionary with the overall dictionary, as well as article ratings
     */
    public void storeData() throws Exception {
        throw new Exception("Unimplemented");
    }

}
