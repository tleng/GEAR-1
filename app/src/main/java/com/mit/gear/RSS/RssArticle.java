package com.mit.gear.RSS;

import java.io.Serializable;

/**
 * Created by najlaalghofaily on 6/29/16.
 */

/*
This class represent a single rss article
 */
public class RssArticle implements Serializable{
    private String title;
    private String link;
    private String content;
    private String count;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    @Override
    public String toString() {
        return title;
    }
	public void setCount(String count) {
		this.count = count;
	}

	public String getCount() {
		return count;
	}
}
