package com.mit.gear.reading;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mattmellor.gear.R;

import java.util.List;
import java.util.Map;

/**
 * Created by najlaalghofaily on 7/22/16.
 */
public class StoryListAdapter extends ArrayAdapter<StoryItem> {
    List<StoryItem> listStoryItem;
    Map<String,Double> articleAndScoreMap;
    boolean debugMode;

    /*
     *This constructor is used if debug mode was on (show the article score)
     */

    public StoryListAdapter(Context context, int resource, List<StoryItem> ListStoryItem, Map<String,Double> ArticleAndScoreMap) {
        super(context, resource, ListStoryItem);
        listStoryItem = ListStoryItem;
        articleAndScoreMap=ArticleAndScoreMap;
        debugMode =true;
    }

    /*
     *This constructor is used if debug mode was off (Don't show the article score)
    */

    public StoryListAdapter(Context context, int resource, List<StoryItem> ListStoryItem) {
        super(context, resource, ListStoryItem);
        listStoryItem = ListStoryItem;
        articleAndScoreMap=null;
        debugMode=false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view ;

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.rss_list_item_debug_mode, null);


         StoryItem storyItem = listStoryItem.get(position);

        if (storyItem != null) {
            TextView titleTextView = (TextView) view.findViewById(R.id.title);
            TextView scoreTextView = (TextView) view.findViewById(R.id.rate);
            titleTextView.setText(storyItem.getTitle());

			if(ReadArticleActivity.articlesOpened.contains(storyItem.getTitle())){
				titleTextView.setTextColor(Color.GRAY);
				scoreTextView.setTextColor(Color.GRAY);
			}
			else {
				titleTextView.setTextColor(Color.BLACK);
				scoreTextView.setTextColor(Color.BLACK);
			}
            scoreTextView.setText(storyItem.getStoryCount());

        }

        return view;
    }
}
