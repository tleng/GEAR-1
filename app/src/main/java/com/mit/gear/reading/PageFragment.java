package com.mit.gear.reading;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.DefinitionRequest;
import com.mit.gear.words.GEARGlobal;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PageFragment extends Fragment {
    private final static String PAGE_TEXT = "PAGE_TEXT";
    private static Context readArticleContext = ReadArticleActivity.getReadArticleActivityInstance().getApplicationContext();
    public static HashMap<String,ArrayList<String>> offlineDictionary = GEARGlobal.getOfflineDictionary(readArticleContext);
    private HashMap<String,ArrayList<String>> userDictionary = new DataStorage(readArticleContext).loadUserDictionary();
    private String clickedWord;

    private DefinitionRequest currentDefinitionRequest = null;

    public static PageFragment newInstance(CharSequence pageText) {
        PageFragment frag = new PageFragment();
        Bundle args = new Bundle();
        args.putCharSequence(PAGE_TEXT, pageText);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CharSequence text = getArguments().getCharSequence(PAGE_TEXT);
        TextView pageView = (TextView) inflater.inflate(R.layout.page, container, false);
        //pageView.setText(text);

        String stringText = text.toString();

        pageView.setMovementMethod(LinkMovementMethod.getInstance());
        pageView.setText(text, TextView.BufferType.SPANNABLE);
        Spannable spans = (Spannable) pageView.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(stringText);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = stringText.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                //ClickableSpan clickSpan = getClickableSpan(possibleWord);
                ClickableSpan clickSpan = getClickSpan(possibleWord);
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return pageView;
    }

    private class GEARClickableSpan extends ClickableSpan {
        final String mWord;
        private TextPaint textPaint;

        public GEARClickableSpan(String word) {
            mWord = word;
        }

        @Override
        public void onClick(View widget) {
            if (currentDefinitionRequest != null) {
                Log.d("Cancel", "Cancel current definition request: " + currentDefinitionRequest.toString());
                currentDefinitionRequest.cancel(true);
            }


            Log.d("No cancel","current definition request not cancelled");
            Log.d("tapped on:", mWord);
            ArrayList<String> time_place_holder = new ArrayList<>();
            time_place_holder.add("0");
            userDictionary.put(mWord, time_place_holder);

            if (textPaint != null) {
                color(widget);
            }
            CharSequence message = mWord + " ausgewählt.";

            TextView definitionBox = (TextView) ReadArticleActivity.getReadArticleActivityInstance().findViewById(R.id.definition_box);
            definitionBox.setText(message);

            DefinitionRequest definitionRequest = new DefinitionRequest(mWord);
            currentDefinitionRequest = definitionRequest;
            definitionRequest.execute(mWord);
            Log.d("lookup", mWord);
        }

        public void updateDrawState(TextPaint ds) {
            textPaint = ds;
            ds.setUnderlineText(false);
            if (userDictionary.containsKey(mWord)) {
                ds.setColor(getResources().getColor(R.color.highlighted_word));
            }
        }

        public void color(View widget) {
            updateDrawState(textPaint);
            widget.invalidate();
        }
    }

    public ClickableSpan getClickSpan(final String word) {
        return new GEARClickableSpan(word);
    }
    /**
     * Method that lets users click on words
     *
     * @param word
     * @return
     */
    public ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord = word;

            public void onClick(View widget) {
                if (currentDefinitionRequest != null) {
                    Log.d("Cancel", "Cancel current definition request: " + currentDefinitionRequest.toString());
                    currentDefinitionRequest.cancel(true);
                }
                Log.d("No cancel","current definition request not cancelled");
                Log.d("tapped on:", mWord);
                clickedWord = mWord;
                ArrayList<String> time_place_holder = new ArrayList<>();
                time_place_holder.add("0");
                userDictionary.put(mWord, time_place_holder);
                CharSequence message = mWord + " ausgewählt.";

                TextView definitionBox = (TextView) ReadArticleActivity.getReadArticleActivityInstance().findViewById(R.id.definition_box);
                definitionBox.setText(message);

                //getAndDisplayDefinition.execute(mWord);
                if (offlineDictionary.containsKey(mWord)) {
                    Log.d("Offline Dictionary",mWord);
                }
                DefinitionRequest definitionRequest = new DefinitionRequest(mWord);
                currentDefinitionRequest = definitionRequest;
                definitionRequest.execute(mWord);
                Log.d("lookup", mWord);
            }

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
                if (userDictionary.containsKey(mWord)) {
                    ds.setColor(getResources().getColor(R.color.highlighted_word));
                }

            }
        };
    }
}