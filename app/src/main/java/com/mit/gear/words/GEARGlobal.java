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
    }
}
