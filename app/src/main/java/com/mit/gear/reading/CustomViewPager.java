package com.mit.gear.reading;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by najlaalghofaily on 8/13/16.
 */


/*
 * This class represent the viewPager in ReadArticleActivity
 * This class controls whither the swipe is vertical or horizontal based of the verticalSwipeDirection flag in ReadArticleActivity
 * If the swipe was vertical, swap the x and y  coordinate of the touch
 * Special thanks to Brett on StackOverFlow
 */

public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
        init();
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        if(ReadArticleActivity.verticalSwipeDirection){
            setPageTransformer(true, new VerticalPageTransformer());
        }
        // The easiest way to get rid of the overscroll drawing that happens on the left and right
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private class VerticalPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {


            if (position <= -1) { // [-Infinity,-1]
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position < 0) { // (-1,0)

                // Fade the page out.
                view.setAlpha(position+1);

                //Counteract the default slide transition
                view.setTranslationX(view.getWidth() * -position);

                //set Y position to swipe in from top
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);

            } else if (position <= 1) { // [0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                //Counteract the default slide transition
                view.setTranslationX(view.getWidth() * -position);

                //set Y position to swipe in from top
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }

        }
    }

    /**
     * Swaps the X and Y coordinates of your touch event.
     */
    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;

        ev.setLocation(newX, newY);

        return ev;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        boolean intercepted;
        if(ReadArticleActivity.verticalSwipeDirection){
            intercepted = super.onInterceptTouchEvent(swapXY(ev));
            swapXY(ev); // return touch coordinates to original reference frame for any child views
        }else{
            intercepted = super.onInterceptTouchEvent(ev);
        }
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(ReadArticleActivity.verticalSwipeDirection){
            return super.onTouchEvent(swapXY(ev));
        }else
            return super.onTouchEvent(ev);
    }

}

