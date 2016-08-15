package com.mit.gear.reading;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class TextPagerAdapter extends FragmentStatePagerAdapter {
    private final List<CharSequence> pageTexts;
    private Integer fontSizeCase;

    public TextPagerAdapter(FragmentManager fm, List<CharSequence> pageTexts,Integer FontSizeCase) {
        super(fm);
        this.pageTexts = pageTexts;
        fontSizeCase = FontSizeCase;
    }

    @Override
    public Fragment getItem(int i) {
        //passing the page text and the fragment index
        PageFragment pageFragment = PageFragment.newInstance(pageTexts.get(i),i,fontSizeCase);
        return pageFragment;
    }
    //return position none to force the pagerView to update
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return pageTexts.size();
    }
}