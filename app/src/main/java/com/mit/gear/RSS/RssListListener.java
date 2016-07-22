package com.mit.gear.RSS;

/**
 * Created by najlaalghofaily on 6/29/16.
 */
import android.app.Activity;
import android.content.Intent;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


import com.mit.gear.activities.StoriesSelectionActivity;
import com.mit.gear.reading.ReadArticleActivity;

import java.util.List;


public class RssListListener implements OnItemClickListener {
    private String TAG = "RssListListener";
    List<RssArticle> listItems;
    Activity activity;
    public RssListListener(List<RssArticle> aListItems, Activity anActivity) {
        listItems = aListItems;
        activity  = anActivity;
    }

    public void onItemClick(AdapterView parent, View view, int pos, long id) {
		Log.d(TAG,"News opened: "+listItems.get(pos).getTitle());
        Intent intent = new Intent(StoriesSelectionActivity.context, ReadArticleActivity.class);
        intent.putExtra("title", listItems.get(pos).getTitle());
        intent.putExtra("content", listItems.get(pos).getContent());
        activity.startActivity(intent);
    }

}
