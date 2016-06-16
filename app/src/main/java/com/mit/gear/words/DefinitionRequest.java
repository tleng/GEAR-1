package com.mit.gear.words;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.appspot.backendgear_1121.gear.Gear;
import com.appspot.backendgear_1121.gear.model.GearBackendDefinition;
import com.mattmellor.gear.R;
import com.mit.gear.miscellaneous.AppConstants;
import com.mit.gear.reading.ReadArticleActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael on 2/9/16.
 */
public class DefinitionRequest extends AsyncTask<String, Void, GearBackendDefinition> {

    private String mWord;
    private String localDefinition = null;
    public DefinitionRequest(String word) {
        mWord = word;
    }
    private boolean running = true;

    @Override
    protected void onCancelled() {
        Log.d("Cancelled","True");
        running = false;
    }
    @Override
    protected GearBackendDefinition doInBackground(String... words) {
        // Retrieve service handle.
        HashMap<String,ArrayList<String>> offlineDictionary = ReadArticleActivity.getReadArticleActivityInstance().offlineDictionary;
        if (offlineDictionary.containsKey(words[0])) {
            //offline dictionary is a map from word to an array list, where the first item in the array is the definition
            localDefinition = offlineDictionary.get(words[0]).get(0);
            return null;
        }

        if (isCancelled() || !running) {
            return null;
        }
        Gear apiServiceHandle = AppConstants.getApiServiceHandle();
        GearBackendDefinition definition = new GearBackendDefinition().setMessage(words[0]);

        try {
            Gear.Gearapi.Define getDefinition = apiServiceHandle.gearapi().define(definition);
            GearBackendDefinition gearDefinition = getDefinition.execute();
            return gearDefinition;
        } catch (IOException e) {
            Log.e("Server Response Error", "Exception during API call", e);
        }

        if (isCancelled() || !running) {
            return null;
        }
        return null;
    }

    // Update the definition display with the definition retrieved from backend
    @Override
    protected void onPostExecute(GearBackendDefinition definition) {
        if (isCancelled() || !running) {
        } else {
            ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();
            final TextView readingDictionary = (TextView) activityInstance.findViewById(R.id.definition_box);
            String definitionResult = "Word looked up: " + mWord + ", Index: "+GEARGlobal.getLastWordClickedIndex()+"\n";
            String currentArticle = activityInstance.currentArticle;
            if (localDefinition != null) {
                Log.d("LocalDefinition", localDefinition);
                definitionResult = definitionResult + "English translation: " + localDefinition;
                //activityInstance.updateDataStorage(mWord, localDefinition, "None", currentArticle, true);

            } else {
                if (definition != null) {
                    String[] response = definition.getMessage().split("\\+\\+");
                    Log.d("Server Response", response.toString());
                    if (response.length > 1) {
                        activityInstance.currentDefinition = response[0];
                        activityInstance.currentLemma = response[1];
                        Log.d("serverResponse", response.toString());
                        definitionResult = definitionResult + "English translation: " + activityInstance.currentDefinition;


                        //activityInstance.updateDataStorage(mWord, activityInstance.currentDefinition, activityInstance.currentLemma, currentArticle, true);
                    } else {
                        activityInstance.currentDefinition = "None";
                        activityInstance.currentLemma = "None";
                        Log.d("serverResponse", response.toString());
                        definitionResult = "Error";
                        //activityInstance.updateDataStorage(mWord, activityInstance.currentDefinition, activityInstance.currentLemma, currentArticle, true);
                    }
                } else {
                    readingDictionary.setText("");
                    Log.e("Uh Oh", "No definitions were returned by the API.");
                }
            }
            if (activityInstance.definition_scroll){
                readingDictionary.setText(definitionResult + "\n" + readingDictionary.getText().toString());
            } else {
                readingDictionary.setText(definitionResult);
            }
        }
    }
}
