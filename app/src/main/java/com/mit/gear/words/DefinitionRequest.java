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
import java.util.HashMap;

/**
 * Created by Michael on 2/9/16.
 */
public class DefinitionRequest extends AsyncTask<String, Void, GearBackendDefinition> {

    private String mWord;
    private String localDefintion = null;
    public DefinitionRequest(String word) {
        mWord = word;
    }

    @Override
    protected GearBackendDefinition doInBackground(String... words) {
        // Retrieve service handle.
        HashMap<String,String> offlineDictionary = ReadArticleActivity.getReadArticleActivityInstance().offlineDictionary;
        if (offlineDictionary.containsKey(words[0])) {
            localDefintion = offlineDictionary.get(words[0]);
            return null;
        }
        Gear apiServiceHandle = AppConstants.getApiServiceHandle();
        GearBackendDefinition definition = new GearBackendDefinition().setMessage(words[0]);

        try {
            Gear.Gearapi.Define getDefinition = apiServiceHandle.gearapi().define(definition);
            GearBackendDefinition gearDefinition = getDefinition.execute();
            return gearDefinition;
        } catch (IOException e) {
            Log.e("Uh Oh", "Exception during API call", e);
        }
        return null;
    }

    // Update the definition display with the definition retrieved from backend
    @Override
    protected void onPostExecute(GearBackendDefinition definition) {
        ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();
        final TextView readingDictionary = (TextView) activityInstance.findViewById(R.id.definition_box);
        String definitionResult = "Word looked up: " + mWord + "\n";
        if (localDefintion!=null) {
            Log.d("LocalDefinition", localDefintion);
            definitionResult = definitionResult + "English translation: " + localDefintion;
            readingDictionary.setText(definitionResult);
        } else {
            if (definition != null) {
                String[] response = definition.getMessage().split("\\+\\+");
                Log.d("Server Response", response.toString());
                if (response.length > 1) {
                    activityInstance.currentDefinition = response[0];
                    activityInstance.currentLemma = response[1];
                    Log.d("serverResponse", response.toString());
                    definitionResult = definitionResult + "English translation: " + activityInstance.currentDefinition;
                    readingDictionary.setText(definitionResult);

                    activityInstance.updateDataStorage(mWord, activityInstance.currentDefinition, activityInstance.currentLemma);
                } else {
                    activityInstance.currentDefinition = "None";
                    activityInstance.currentLemma = "None";
                    Log.d("serverResponse", response.toString());
                    readingDictionary.setText("Error");
                    activityInstance.updateDataStorage(mWord, activityInstance.currentDefinition, activityInstance.currentLemma);
                }
            } else {
                readingDictionary.setText("");
                Log.e("Uh Oh", "No definitions were returned by the API.");
            }
        }
    }
}
