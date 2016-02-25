package com.mit.gear;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.appspot.backendgear_1121.gear.Gear;
import com.appspot.backendgear_1121.gear.model.GearBackendDefinition;
import com.mattmellor.gear.R;

import java.io.IOException;

/**
 * Created by Michael on 2/9/16.
 */
public class DefinitionRequest extends AsyncTask<String, Void, GearBackendDefinition> {

    private String mWord;

    public DefinitionRequest(String word) {
        mWord = word;
    }

    @Override
    protected GearBackendDefinition doInBackground(String... words) {
        // Retrieve service handle.
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
        if (definition != null) {
            String[] response = definition.getMessage().split("\\+\\+");
            Log.d("Server Response", response.toString());
            if (response.length>1) {
                activityInstance.currentDefinition = response[0];
                activityInstance.currentLemma = response[1];
                Log.d("serverResponse", response.toString());
                String definitionResult = "Word looked up: " + mWord + "\n";
                definitionResult = definitionResult + "English translation: " + activityInstance.currentDefinition;
                readingDictionary.setText(definitionResult);

                activityInstance.updateDataStorage(mWord, activityInstance.currentDefinition, activityInstance.currentLemma);
            }
        } else {
            readingDictionary.setText("");
            Log.e("Uh Oh", "No definitions were returned by the API.");
        }
    }
}
