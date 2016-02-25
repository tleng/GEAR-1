package com.mit.gear;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattmellor.gear.R;

import java.text.BreakIterator;
import java.util.Locale;

public class PageFragment extends Fragment {
    private final static String PAGE_TEXT = "PAGE_TEXT";

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
                ReadArticleActivity rAA = ReadArticleActivity.getReadArticleActivityInstance();
                ClickableSpan clickSpan = rAA.getClickableSpan(possibleWord);
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return pageView;
    }
}