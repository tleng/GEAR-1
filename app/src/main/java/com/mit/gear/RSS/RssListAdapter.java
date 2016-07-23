package com.mit.gear.RSS;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.reading.ReadArticleActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by najlaalghofaily on 7/20/16.
 */


/*
 * This class is rss news list adapter if the mode was debug mode
 * shows the article ranking (score)  bellow the article title
 */

public class RssListAdapter extends ArrayAdapter<RssArticle> {
    List<RssArticle> listRssArticle;
    Map<RssArticle,Double> articleAndScoreMap;
    boolean debugMode;

    /*
     *This constructor is used if debug mode was on (show the article score)
     */

    public RssListAdapter(Context context, int resource, List<RssArticle> ListRssArticle, Map<RssArticle,Double> ArticleAndScoreMap) {
        super(context, resource, ListRssArticle);
        listRssArticle = ListRssArticle;
        articleAndScoreMap=ArticleAndScoreMap;
        debugMode =true;
    }

    /*
     *This constructor is used if debug mode was off (Don't show the article score)
    */

    public RssListAdapter(Context context, int resource, List<RssArticle> ListRssArticle) {
        super(context, resource, ListRssArticle);
        listRssArticle = ListRssArticle;
        articleAndScoreMap=null;
        debugMode=false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view ;
        if(!debugMode){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rss_list_item, null);
        }else{
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rss_list_item_debug_mode, null);
        }

        RssArticle article = listRssArticle.get(position);

        if (article != null) {
            TextView titleTextView = (TextView) view.findViewById(R.id.title);
            TextView scoreTextView = (TextView) view.findViewById(R.id.rate);
            titleTextView.setText(article.getTitle());

            if(ReadArticleActivity.articlesOpened.contains(article.getTitle())){
                titleTextView.setTextColor(Color.GRAY);
                if(debugMode){
                    scoreTextView.setTextColor(Color.GRAY);
                }
            }
            else {
                titleTextView.setTextColor(Color.BLACK);
                if(debugMode){
                    scoreTextView.setTextColor(Color.BLACK);
                }
            }

            if(debugMode) {
                Double score = articleAndScoreMap.get(article);
                if(score==null){           //if rss news just generated and not ranked yet
                    scoreTextView.setText(article.getCount());
                } else{
                    scoreTextView.setText(article.getCount()+"\n"+String.valueOf(score*100));
                }
            }
        }

        return view;
    }
}
