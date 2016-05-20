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
        PageFragment pageFragment = PageFragment.newInstance(pageTexts.get(i));
        return pageFragment;
    }

    @Override
    public int getCount() {
        return pageTexts.size();
    }
}