package com.mit.gear.reading;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
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
    //HashMap to keep track of each fragment starting-index
    public static HashMap<Integer,Integer> wordIndexing= new HashMap<>();
    private HashMap<String, Word> userDictionary = new DataStorage(readArticleContext).loadUserDictionary();
    private String clickedWord;
    private TextView pageView;
    private static CharSequence articleText;
    public static Integer fragmentIndex =0 ;

    private DefinitionRequest currentDefinitionRequest = null;

    public static PageFragment newInstance(CharSequence pageText, int fragmentIndex) {
        articleText = pageText;
        PageFragment frag = new PageFragment();
        Bundle args = new Bundle();
        args.putCharSequence(PAGE_TEXT, pageText);
        //saving the fragment index into its args
        args.putInt("index",fragmentIndex);
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
        //the first fragment default starting index
        wordIndexing.put(0,0);
        CharSequence text = getArguments().getCharSequence(PAGE_TEXT);
        fragmentIndex = getArguments().getInt("index");
        pageView = (TextView) inflater.inflate(R.layout.page, container, false);
        //removing the highlight color for the textView if clicked
        pageView.setHighlightColor(Color.TRANSPARENT);
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
        Boolean copyRightReachedMulti = false;
        //accessing the hashMap to get the fragment's starting index
        try{
            GEARGlobal.setWordIndex(wordIndexing.get(fragmentIndex));
        }catch (NullPointerException e){
            return pageView;
        }

        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = stringText.substring(start, end);
            if(possibleWord.matches(getResources().getString(R.string.endOfArticleIndicator))){ //if copyRight text reached
                ReadArticleActivity.copyRightReachedFirstTime = true;
                spans.setSpan(new ForegroundColorSpan(Color.TRANSPARENT), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //make copy right indicator to transparent
                ReadArticleActivity.CopyRightFragmentIndex =fragmentIndex; //set the fragment index of copy right text appearing
                copyRightReachedMulti = true;
                continue;
            }
            if(ReadArticleActivity.copyRightReachedFirstTime && Math.abs(fragmentIndex-ReadArticleActivity.CopyRightFragmentIndex)>=0 && ReadArticleActivity.CopyRightFragmentIndex!=-1){
                if(fragmentIndex==ReadArticleActivity.CopyRightFragmentIndex){

                    if(copyRightReachedMulti)
                    {
                        spans.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.default_word)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        continue;
                    }

                }
                else if (fragmentIndex>ReadArticleActivity.CopyRightFragmentIndex){
                    spans.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.default_word)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    continue;
                }

//                else{
//                    spans.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.default_word)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    continue;
//                }
            }
            if (Character.isLetter(possibleWord.charAt(0))) {
                //ClickableSpan clickSpan = getClickableSpan(possibleWord);
                GEARClickableSpan clickSpan = getClickSpan(possibleWord);
                clickSpan.setIndex(GEARGlobal.getWordIndex());
                GEARGlobal.incrementWordIndex();
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else{
                spans.setSpan(new ForegroundColorSpan(Color.BLACK), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        //putting the next fragment starting index into the hashMap
        wordIndexing.put(fragmentIndex + 1, GEARGlobal.getWordIndex());
        return pageView;
    }

    public GEARClickableSpan getClickSpan(final String word) {
        return new GEARClickableSpan(word);
    }

}