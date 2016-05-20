package com.mit.gear.reading;

import android.content.Context;
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

    public GEARClickableSpan(String word) {
        mWord = word;
    }

    @Override
    public void onClick(View widget) {
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
        HashMap<String, Word> currentSessionWords = ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
        if (userDictionary.containsKey(mWord)) {
            if (userDictionary.get(mWord).clicked) {
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.highlighted_word));
        } else if (currentSessionWords.containsKey(mWord)) {
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.highlighted_word));
            }
        }
    }

    public void color(View widget) {
        updateDrawState(textPaint);
        widget.invalidate();
    }

    public static void clear() {
        if (clearWidget != null) {
            userDictionary.clear();
            clearWidget.invalidate();
            Log.d("Clear","View updated");
        }
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
