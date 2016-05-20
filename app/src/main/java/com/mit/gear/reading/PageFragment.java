package com.mit.gear.reading;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.DefinitionRequest;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.Word;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PageFragment extends Fragment {
    private final static String PAGE_TEXT = "PAGE_TEXT";
    private static Context readArticleContext = ReadArticleActivity.getReadArticleActivityInstance().getApplicationContext();
    public static HashMap<String,ArrayList<String>> offlineDictionary = GEARGlobal.getOfflineDictionary(readArticleContext);
    private HashMap<String, Word> userDictionary = new DataStorage(readArticleContext).loadUserDictionary();
    private String clickedWord;
    private TextView pageView;
    private static CharSequence articleText;

    private DefinitionRequest currentDefinitionRequest = null;

    public static PageFragment newInstance(CharSequence pageText) {
        articleText = pageText;
        PageFragment frag = new PageFragment();
        Bundle args = new Bundle();
        args.putCharSequence(PAGE_TEXT, pageText);
        frag.setArguments(args);
        return frag;
    }

//    public static void update() {
//        PageFragment frag = new PageFragment();
//        Bundle args = new Bundle();
//        args.putCharSequence(PAGE_TEXT, articleText);
//        frag.setArguments(args);
//        return frag;
//    }

//    private static View setText() {
//        pageView = (TextView) inflater.inflate(R.layout.page, container, false);
//        pageView.setText(text, TextView.BufferType.SPANNABLE);
//        Spannable spans = (Spannable) pageView.getText();
//        BreakIterator iterator = BreakIterator.getWordInstance(Locale.GERMANY);
//        iterator.setText(stringText);
//        int start = iterator.first();
//        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
//                .next()) {
//            String possibleWord = stringText.substring(start, end);
//            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
//                //ClickableSpan clickSpan = getClickableSpan(possibleWord);
//                GEARClickableSpan clickSpan = getClickSpan(possibleWord);
//                clickSpan.setIndex(GEARGlobal.getWordIndex());
//                GEARGlobal.incrementWordIndex();
//                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//        }
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CharSequence text = getArguments().getCharSequence(PAGE_TEXT);
        pageView = (TextView) inflater.inflate(R.layout.page, container, false);
        Log.d("Viewthis",pageView.toString());
        if (GEARClickableSpan.clearWidget == null) {
            GEARClickableSpan.clearWidget = pageView;
        }
        //pageView.setText(text);

        String stringText = text.toString();

        pageView.setMovementMethod(LinkMovementMethod.getInstance());
        pageView.setText(text, TextView.BufferType.SPANNABLE);
        Spannable spans = (Spannable) pageView.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.GERMANY);
        iterator.setText(stringText);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = stringText.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                //ClickableSpan clickSpan = getClickableSpan(possibleWord);
                GEARClickableSpan clickSpan = getClickSpan(possibleWord);
                clickSpan.setIndex(GEARGlobal.getWordIndex());
                GEARGlobal.incrementWordIndex();
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return pageView;
    }

    public GEARClickableSpan getClickSpan(final String word) {
        return new GEARClickableSpan(word);
    }

}