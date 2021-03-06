package com.mit.gear.words;

import android.content.Context;
import android.util.Log;

import com.mit.gear.data.DataStorage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael on 3/31/16.
 */
public class GEARGlobal {

    private static HashMap<String,ArrayList<String>> offlineDictionary;
    public static ArrayList<ArrayList<String>> ListLastClickedWords = new ArrayList<ArrayList<String>>();      // keeps track of the last 3 clicked words
    public static ArrayList<ArrayList<String>> MaximumLastClickedWords = new ArrayList<ArrayList<String>>();     //keeps track of the indexes set as the last clicked words index
    public static final Integer undoThreshold = 3;
    private static Integer wordIndex;
    private static String lastWordClicked = "None";
    private static Integer lastWordClickedIndex = -1;
    public final static String articlePathName = "articles";
    public final static String articlePath = articlePathName+"/";



    public static HashMap<String, ArrayList<String>> getOfflineDictionary(Context context) {
        if (offlineDictionary != null) {
            return offlineDictionary;
        } else {
            GEARGlobal gearGlobal = new GEARGlobal(context);
            return gearGlobal.offlineDictionary;
        }
    }

    private GEARGlobal(Context context) {
        DataStorage dataStorage = new DataStorage(context);
        offlineDictionary = dataStorage.loadOfflineDictionary();
        Log.d("OfflineDictionary", "Loaded");
        wordIndex = 0;
    }

    public static void incrementWordIndex() {
        wordIndex += 1;
    }

    public static void resetWordIndex() {
        wordIndex = 0;
    }

    public static Integer getWordIndex() {
        return wordIndex;
    }

    public static void setLastWordClicked(String word) {
        lastWordClicked = word;
    }

    public static String getLastWordClicked() {
        return lastWordClicked;
    }

    public static void setLastWordClickedIndex(Integer i) {
        lastWordClickedIndex = i;
    }

    public static Integer getLastWordClickedIndex() {
        return lastWordClickedIndex;
    }

    public static void setWordIndex(int index){
        wordIndex = index;
    }
}
