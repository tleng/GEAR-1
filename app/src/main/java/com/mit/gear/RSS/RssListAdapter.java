package com.mit.gear.RSS;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mattmellor.gear.R;

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

    public RssListAdapter(Context context, int resource, List<RssArticle> ListRssArticle, Map<RssArticle,Double> ArticleAndScoreMap) {
        super(context, resource, ListRssArticle);
        listRssArticle = ListRssArticle;
        articleAndScoreMap=ArticleAndScoreMap;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rss_list_item, null);
        }
        RssArticle article = listRssArticle.get(position);
        Double score = articleAndScoreMap.get(article);



        if (article != null) {
            TextView btd = (TextView) view.findViewById(R.id.title);
            btd.setText(article.getTitle());
            TextView btd2 = (TextView) view.findViewById(R.id.rate);

            if(score==null) //if rss news just generated and not ranked yet
                btd2.setText("");
            else
                btd2.setText(String.valueOf(score*100));
        }

        return view;
    }
}
