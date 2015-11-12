package com.mattmellor.gear;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Saya on 11/8/2015.
 */
public class UserData {

    private final int userId;
    private final String articleTitle;
    private int rating = -1; //unrated
    private final Map<String,Integer> wordsLookedUp = new HashMap<>();
    private Long startTime; // change this to timestamp later... this should actually measure time spent
    private Long exitTime;

    public UserData(int userId, String articleTitle){
        this.userId = userId;
        this.articleTitle = articleTitle;

    }

    public void rate(int rating){
        this.rating = rating;
    }
    
    public void addWord(String word){ // should I handle lower case?
        if(this.wordsLookedUp.containsKey(word)){
            int freq = this.wordsLookedUp.get(word);
            this.wordsLookedUp.put(word,freq);
        }
        else{
            this.wordsLookedUp.put(word,1);
        }
    }

    public void setStartTime(Long startTime){
        this.startTime =startTime;

    }
    public void setExitTime(Long exitTime){
        this.exitTime = exitTime;

    }

    public String getUserId(){
        return new Integer(this.userId).toString();
    }

    public String getArticle(){
        return this.articleTitle;
    }

    @Override
    public boolean equals(Object thatObject){
        if(thatObject instanceof UserData){
            return false;
        }

        else{
            UserData that = (UserData) thatObject;
            if(this.userId == that.userId && this.articleTitle.equals(that.articleTitle)){
                return true;
            }
            else{
                return false;
            }
        }
    }

    @Override
    public int hashCode(){
        return this.userId + this.articleTitle.hashCode();
    }

    @Override
    public String toString(){
        String words = "[";
        for(String word: wordsLookedUp.keySet()){
            words += word + " : " +wordsLookedUp.get(word)+",";
        }
        words += "]";

        String outputString = "[ USER : " + userId + ", " + "article title : " + articleTitle +", "
                                + "startTime : " + startTime +", " + "exit : " + exitTime + ", "
                                + "words : " + words + " ]";

        return outputString;
    }




}
