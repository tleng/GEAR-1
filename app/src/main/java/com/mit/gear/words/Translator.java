package com.mit.gear.words;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.mattmellor.gear.R;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.mit.gear.reading.ReadArticleActivity;

import java.util.ArrayList;

/**
 * Created on 6/17/16.
 * Class used to translate words using Microsoft's (Bing) translator
 * The internet connection is required
 */
public class Translator extends AsyncTask<Void, Void, String> {
    public String translatedText = "";
    private String mWord ;


    public Translator(String mWord) {
        this.mWord = mWord;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            Translate.setClientId("Najla");
            Translate.setClientSecret("LS/TatMvBX0vSn2uPwXeTQzWmzhZviDv7Zg9VEggMjY=");
            translatedText = "";
            translatedText = Translate.execute(mWord, Language.ENGLISH);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return translatedText;
    }
    @Override
    protected void onPostExecute(String result) {
        ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();
        final TextView readingDictionary = activityInstance.readingDictionary;
		ArrayList<String> WordDefinition = new ArrayList<>();
		WordDefinition.add(mWord);
        if (result != null) {
			WordDefinition.add(result);
			activityInstance.DefinitionBoxList.add(WordDefinition);
			Log.d("DefinitionBoxList",activityInstance.DefinitionBoxList.toString());
          if (activityInstance.definition_scroll){
                if(readingDictionary.getText().toString().isEmpty())
					readingDictionary.setText(activityInstance.DefinitionBoxList.get(
							activityInstance.DefinitionBoxList.size()-1).get(0)+" ,\t "+
							activityInstance.DefinitionBoxList.get(activityInstance.DefinitionBoxList.size()-1).get(1));
                else
					readingDictionary.setText(readingDictionary.getText().toString() + "\n" +
							activityInstance.DefinitionBoxList.get(activityInstance.DefinitionBoxList.size()-1).get(0)+
							" ,\t "+activityInstance.DefinitionBoxList.get(activityInstance.DefinitionBoxList.size()-1).get(1));
            } else {
			  readingDictionary.setText(activityInstance.DefinitionBoxList.get(
					  activityInstance.DefinitionBoxList.size()-1).get(0)+" ,\t "+activityInstance.DefinitionBoxList.get(
					  activityInstance.DefinitionBoxList.size()-1).get(1));
            }
            final ScrollView scrollview = (ScrollView) activityInstance.findViewById(R.id.definition_scroll);
            scrollview.post(new Runnable() {
                @Override
                public void run() {

                    scrollview.fullScroll(View.FOCUS_DOWN);             //scroll the definition box to the bottom whenever word translation added
                }
            });
            super.onPostExecute(result);
        }else{
            readingDictionary.setText("");             //If no translation found
        }
    }
}
