package com.mit.gear.reading;


/**
 * Created by najlaalghofaily on 7/17/16.
 */

/*
*This class represent a single story item with title and content description
*
 */
public class StoryItem {

    private String title;
    private String contentDescription;

    public String getTitle() {
        return title;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContentDescription(String content) {
        this.contentDescription = content;
    }

    public StoryItem(String title, String content) {
        this.title = title;
        this.contentDescription = content;
    }
    public StoryItem() {
    }
    @Override
    public String toString() {
        return title;
    }
}
