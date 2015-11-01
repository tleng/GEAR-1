package com.mattmellor.gear;

/**
 * Created by ktjolsen on 10/29/15.
 */
public class Article {

    private final String title;
    private final String asset;


    public Article(String title, String asset) {
        this.title = title;
        this.asset = asset;
    }

    public String getTitle() {
        return title;
    }

    public String getAsset() {
        return asset;
    }
}
