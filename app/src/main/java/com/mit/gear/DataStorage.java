package com.mit.gear;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

    public HashMap<String, WordLookup> loadJSONDictionary() {
        HashMap<String, WordLookup> map = new HashMap<String, WordLookup>();
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
                Log.d("Saved File", buf.toString());
                map = new Gson().fromJson(buf.toString(), new TypeToken<HashMap<String, WordLookup>>() {
                }.getType());
                Log.d("SaveFile",map.toString());
            }

        } catch (java.io.FileNotFoundException e) {
            Log.d("Dictionary","No dictionary file found.");

        } catch (Throwable t) {

        }

        finally {
            return map;
        }
    }

    public void addToJSONDictionary(String word) throws JSONException, IOException {
        HashMap<String, WordLookup> dictionary = loadJSONDictionary();
        Log.d("File", dictionary.toString());
        dictionary.put(word,UserDataCollection.getCurrentVocabulary().get(word));
        Gson gson = new Gson();
        String json = gson.toJson(dictionary);
        OutputStreamWriter out=

                new OutputStreamWriter(context.openFileOutput(USERDICTIONARY, 0));

        out.write(json);
        Log.d("File", "Saved " + word);

        out.close();
    }
}
