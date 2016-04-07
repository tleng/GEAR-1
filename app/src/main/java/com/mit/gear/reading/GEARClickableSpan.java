package com.mit.gear.reading;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.words.DefinitionRequest;

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
    private static HashMap<String,ArrayList<String>> userDictionary = ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
    static View clearWidget;

    public GEARClickableSpan(String word) {
        mWord = word;
    }

    @Override
    public void onClick(View widget) {
        if (currentDefinitionRequest != null) {
            Log.d("Cancel", "Cancel current definition request: " + currentDefinitionRequest.toString());
            currentDefinitionRequest.cancel(true);
        }
        Log.d("View1", clearWidget.toString());
        clearWidget = widget;
        Log.d("View2", clearWidget.toString());

        Log.d("No cancel","current definition request not cancelled");
        Log.d("tapped on:", mWord);
        ArrayList<String> time_place_holder = new ArrayList<>();
        time_place_holder.add("0");
        userDictionary.put(mWord, time_place_holder);

        if (textPaint != null) {
            color(widget);
        }
        CharSequence message = mWord + " ausgewählt.";

        ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();

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
        if (userDictionary.containsKey(mWord)) {
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.highlighted_word));
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
}
