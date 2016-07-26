package com.mit.gear.RSS;

/**
 * Created by najlaalghofaily on 6/29/16.
 */
import android.app.Activity;
import android.content.Intent;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;


import com.mit.gear.activities.StoriesSelectionActivity;
import com.mit.gear.reading.ReadArticleActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RssListListener implements ExpandableListView.OnChildClickListener {
    private String TAG = "RssListListener";
    Map<String,List<RssArticle>> listItems;
    List<String> headersList;
    Activity activity;
    public RssListListener(List<String> headers,Map<String,List<RssArticle>> aListItems, Activity anActivity) {
        headersList=headers;
        listItems = aListItems;
        activity  = anActivity;
    }



    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Log.d(TAG,"News opened: "+listItems.get(headersList.get(groupPosition)).get(childPosition).getTitle());

        Intent intent = new Intent(activity, ReadArticleActivity.class);
        listItems.get(headersList.get(groupPosition)).get(childPosition).getTitle();
        ReadArticleActivity.articlesOpened.add(listItems.get(headersList.get(groupPosition)).get(childPosition).getTitle());
        intent.putExtra("title", listItems.get(headersList.get(groupPosition)).get(childPosition).getTitle());
        intent.putExtra("content", listItems.get(headersList.get(groupPosition)).get(childPosition).getContent());
        activity.startActivity(intent);
        return false;
    }
}
