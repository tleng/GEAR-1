package com.mit.gear.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mattmellor.gear.R;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
    //USERDICTIONARY is stored on the local file storage of the android device
    String USERDICTIONARY = "gearUserDictionary";
    //OFFLINEDICTIONARY is stored in assets
    String OFFLINEDICTIONARY = "offline_dictionary_json";
    String TESTDICTIONARY = "offline_dictionary_maparray_json";

    //In order to open files from assets, we need to pass a context and retrieve the assetManager from that context
    public DataStorage(Context context) {
        this.context = context;
        this.assetManager = context.getAssets();
    }

    public HashMap<String, ArrayList<String>> loadOfflineDictionary() {
        return loadJSONDictionary(context.getString(R.string.offline_dictionary));
    }

    public HashMap<String, ArrayList<String>> loadUserDictionary() {
        HashMap<String, ArrayList<String>> userDictionary = new HashMap<>();
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
                userDictionary = new Gson().fromJson(buf.toString(), new TypeToken<HashMap<String, ArrayList<String>>>() {
                }.getType());
                Log.d("userDictionary",userDictionary.toString());
            }
            Log.d("userDict input","null");

        } catch (java.io.FileNotFoundException e) {
            Log.d("Dictionary","No dictionary file found.");

        } catch (Throwable t) {

        }

        finally {
            return userDictionary;
        }

    }

    public HashMap<String, ArrayList<String>> loadJSONDictionary(String filename) {
        InputStream input;
        String text;
        try {
            input = assetManager.open(filename);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            // byte buffer into a string
            text = new String(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            text = "{\"dictionary\":{\"error\":\"occurred\"}}";
        }

        Log.d("OfflineDictionary","Text Loaded");
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        try {
            map = new Gson().fromJson(text, new TypeToken<HashMap<String, ArrayList<String>>>() {
            }.getType());
        } catch (Exception e) {
            Log.d("DictionaryError",e.getMessage());
        }
        return map;

    }

    public void clearUserDictionary() throws IOException {
        HashMap<String,ArrayList<String>> userDictionary = new HashMap<>();
        Gson gson = new Gson();
        String json = gson.toJson(userDictionary);
        OutputStreamWriter out=

                new OutputStreamWriter(context.openFileOutput(USERDICTIONARY, 0));

        out.write(json);
        out.close();
    }

    public void addToUserDictionary(String word) throws JSONException, IOException {
        HashMap<String, ArrayList<String>> dictionary = loadUserDictionary();
        Log.d("UserDictionary", dictionary.toString());
        ArrayList<String> userData;
        if (dictionary.containsKey(word)) {
            userData = dictionary.get(word);
        } else {
            userData = new ArrayList<>();
        }
        userData.add(Long.toString(System.currentTimeMillis()));
        dictionary.put(word, userData);
        Gson gson = new Gson();
        String json = gson.toJson(dictionary);
        OutputStreamWriter out=

                new OutputStreamWriter(context.openFileOutput(USERDICTIONARY, 0));

        out.write(json);
        Log.d("File", "Saved " + word);

        out.close();
    }
}
