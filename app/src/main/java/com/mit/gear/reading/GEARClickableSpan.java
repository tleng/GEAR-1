package com.mit.gear.reading;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.DefinitionRequest;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.Word;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael on 4/6/16.
 */
public class GEARClickableSpan extends ClickableSpan {
    final String mWord;
    private TextPaint textPaint;
    private DefinitionRequest currentDefinitionRequest;
    private Context context = ReadArticleActivity.getReadArticleActivityInstance().getApplicationContext();
    private static HashMap<String,Word> userDictionary = ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
    static View clearWidget;
    private Integer index;
    //create sharedPreferences to get user choice of text coloring
    public SharedPreferences sharedPreferences;
    public boolean colorChoice;

    public GEARClickableSpan(String word) {
        mWord = word;
    }

    @Override
    public void onClick(View widget) {
        //set progressSaved to false to popup the savePopupActivity in case user did not save
        ReadArticleActivity.getReadArticleActivityInstance().setProgressSaved(false);
        GEARGlobal.setLastWordClicked(mWord);
        GEARGlobal.setLastWordClickedIndex(index);
        if (currentDefinitionRequest != null) {
            Log.d("Cancel", "Cancel current definition request: " + currentDefinitionRequest.toString());
            currentDefinitionRequest.cancel(true);
        }
        Log.d("View1", clearWidget.toString());
        clearWidget = widget;
        Log.d("View2", clearWidget.toString());

        Log.d("No cancel", "current definition request not cancelled");
        Log.d("tapped on:", mWord);
        ArrayList<String> time_place_holder = new ArrayList<>();
        time_place_holder.add("0");
        Word wordData = new Word(mWord);
        wordData.setClicked(true);
        //get the latest userDictionary to add new words clicked
        userDictionary = ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
        userDictionary.put(mWord, wordData);
        DataStorage dataStorage = new DataStorage(context);
        ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();
        if (activityInstance.currentSessionWords.containsKey(mWord)) {
            Integer count = activityInstance.currentSessionWords.get(mWord);
            count += 1;
            activityInstance.currentSessionWords.put(mWord, count);
        } else {
            activityInstance.currentSessionWords.put(mWord, 1);
        }
        try {
            dataStorage.addToUserDictionary(mWord, "None", activityInstance.currentArticle, true);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textPaint != null) {
            color(widget);
        }
        CharSequence message = mWord + " ausgewählt.";

        TextView definitionBox = (TextView) activityInstance.findViewById(R.id.definition_box);
        if (activityInstance.definition_scroll) {
            // do nothing to the definitionBox
        } else {
            definitionBox.setText(message);
        }

        DefinitionRequest definitionRequest = new DefinitionRequest(mWord);
        currentDefinitionRequest = definitionRequest;
        definitionRequest.execute(mWord);
        Log.d("lookup", mWord);
    }

    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setUnderlineText(false);
        //access the latest userDictionary
        HashMap<String, Word> userDictionary = ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
        //creating ReadArticleActivity instance to access the currentSessionWords
        ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();
        //accessing the color sharedPreferences to get user text color choice or true for default
        sharedPreferences = context.getSharedPreferences("ColorPreference", Context.MODE_PRIVATE);
        colorChoice = sharedPreferences.getBoolean("color", true);
        //if the color choice is true(color switch is on)
        if (colorChoice) {
            //if the word in the currentSessionWords word ( clicked in the current session )
            if( activityInstance.currentSessionWords.containsKey(mWord)){
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.clicked_word));
                ds.bgColor=(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.clicked_word_background));
            }
            //if the word in the userDictionary check if it is clicked or passed
            else if(userDictionary.containsKey(mWord)){
                if(userDictionary.get(mWord).clicked)
                    ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.clicked_word));
                else{
                    ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.passed_word));
                }
                //else color the rest of the word with the default color
            }else{
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.default_word));
            }
        }
        //if the user turned off the text coloring color with the default color
        else{
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.default_word));
        }
    }

    public void color(View widget) {
        updateDrawState(textPaint);
        widget.invalidate();
    }

    public static void clear() {
        if (clearWidget != null) {
            //clearing the current session words
            ReadArticleActivity.getReadArticleActivityInstance().currentSessionWords.clear();
            userDictionary.clear();
            clearWidget.invalidate();
            Log.d("Clear","View updated");
        }
        GEARGlobal.setLastWordClickedIndex(-1);
        //setting the saveProgress to true (do not show the SavePopupActivity)
        ReadArticleActivity.getReadArticleActivityInstance().setProgressSaved(true);
    }

    public void setIndex(Integer i) {
        index = i;
    }

    public Integer getIndex() {
        if (index != null) {
            return index;
        } else {
            return -1;
        }
    }
}
