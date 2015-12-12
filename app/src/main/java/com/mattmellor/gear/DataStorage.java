package com.mattmellor.gear;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * Created by Michael on 12/12/15.
 */

/*
Class that will utilize JSON to store data locally on phone.
 */
public class DataStorage {
    Context context;
    AssetManager assetManager;
    String USERINFO = "gearUserInformation";
    String USERDICTIONARY = "gearUserDictionary";

    //In order to open files from assets, we need to pass a context and retrieve the assetManager from that context
    public DataStorage(Context context) {
        this.context = context;
        this.assetManager = context.getAssets();
    }

    private String loadJSON(String fileName) throws IOException {
        String json = null;
        return json;
    }

    public HashMap<String, Integer> loadJSONDictionary() throws IOException {
        HashMap<String,Integer> map = new HashMap<String, Integer>();
        try {

            InputStream in = context.openFileInput(USERDICTIONARY);

            if (in != null) {

                InputStreamReader tmp=new InputStreamReader(in);

                BufferedReader reader=new BufferedReader(tmp);

                String str;

                StringBuilder buf=new StringBuilder();

                while ((str = reader.readLine()) != null) {

                    buf.append(str);

                }

                in.close();
                map.put(buf.toString(),1);

            }

        } catch (java.io.FileNotFoundException e) {

        } catch (Throwable t) {

        } finally {
            return map;
        }
//        String jsonString = loadJSON(USERDICTIONARY);
//        HashMap<String, Integer> userDictionary = new HashMap<String, Integer>();
//        if (jsonString == null) {
//            return userDictionary;
//        }
//
//        Type type = new TypeToken<Map<String, String>>(){}.getType();
//        Gson gson = new Gson();
//        userDictionary = gson.fromJson(jsonString, type);
//        return userDictionary;
    }

    public void addToJSONDictionary(String word) throws JSONException, IOException {
        OutputStreamWriter out=

                new OutputStreamWriter(context.openFileOutput(USERDICTIONARY, 0));

        out.write(word);
        Log.d("File", "Saved " + word);

        out.close();
//        String jsonString = loadJSON(USERDICTIONARY);
//        Log.d("JSON", jsonString);
//        JSONObject jsonDictionary = new JSONObject(jsonString);
//        HashMap<String, Integer> currentDictionary = loadJSONDictionary();
//        if (currentDictionary.containsKey(word)) {
//            jsonDictionary.put(word,currentDictionary.get(word) + 1);
//        } else {
//            jsonDictionary.put(word,1);
//        }
//
//        try {
//            FileOutputStream fileOutputStream = context.openFileOutput(USERDICTIONARY,Context.MODE_PRIVATE);
//            Gson gson = new Gson();
//            fileOutputStream.write(gson.toJson(jsonDictionary).getBytes());
//            Log.d("Json Write", "Wrote user Dictionary");
//            fileOutputStream.close();
//            for (String WORD:loadJSONDictionary().keySet()) {
//                Log.d("Json File",WORD);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
