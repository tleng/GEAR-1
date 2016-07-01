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
        final TextView readingDictionary = (TextView) activityInstance.findViewById(R.id.definition_box);
        String definitionResult = mWord+" " ;
        if (result != null) {
            definitionResult = definitionResult +",\t"+ result;

            if (activityInstance.definition_scroll){
                if(readingDictionary.getText().toString().isEmpty())
                    readingDictionary.setText(definitionResult);
                else
                readingDictionary.setText(readingDictionary.getText().toString() + "\n" +definitionResult);
            } else {
                readingDictionary.setText(definitionResult);
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
