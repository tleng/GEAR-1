package com.mit.gear.reading;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class TextPagerAdapter extends FragmentStatePagerAdapter {
    private final List<CharSequence> pageTexts;

    public TextPagerAdapter(FragmentManager fm, List<CharSequence> pageTexts) {
        super(fm);
        this.pageTexts = pageTexts;
    }

    @Override
    public Fragment getItem(int i) {
        //passing the page text and the fragment index
        PageFragment pageFragment = PageFragment.newInstance(pageTexts.get(i),i);
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