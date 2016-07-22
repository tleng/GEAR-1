package com.mit.gear.reading;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.mit.gear.activities.SuggestedStoriesActivity;
import com.mit.gear.words.GEARGlobal;

import java.util.List;

/**
 * Created by najlaalghofaily on 7/17/16.
 */
public class StoryListListener implements OnItemClickListener  {
    private String TAG = "StoryListListener";
    List<StoryItem> listItems;
    Activity activity;
    public StoryListListener(List<StoryItem> aListItems, Activity anActivity) {
        listItems = aListItems;
        activity  = anActivity;
    }

    public void onItemClick(AdapterView parent, View view, int pos, long id) {
		Log.d(TAG,"Story opened: "+listItems.get(pos).getContentDescription());
        Intent intent = new Intent(SuggestedStoriesActivity.context, ReadArticleActivity.class);
        intent.putExtra("title", GEARGlobal.articlePath + listItems.get(pos).getContentDescription());
        activity.startActivity(intent);
    }

}
