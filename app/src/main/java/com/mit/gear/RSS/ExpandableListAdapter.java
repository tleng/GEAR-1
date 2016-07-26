package com.mit.gear.RSS;

/**
 * Created by najlaalghofaily on 7/24/16.
 */
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.reading.ReadArticleActivity;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    Map<RssArticle,Double> articleAndScoreMap;
    private boolean debugMode;

    private Map<String, List<RssArticle>> _listDataChild;// child data, mapping between <header,child List>

    /**
     * if the mode was not debug mode
     */

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 Map<String, List<RssArticle>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        articleAndScoreMap=null;
        debugMode =false;
    }

    /**
     * if the mode was debug mode
     */

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 Map<String, List<RssArticle>> listChildData, Map<RssArticle,Double> ArticleAndScoreMap) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        articleAndScoreMap=ArticleAndScoreMap;
        debugMode =true;
    }

    @Override
    public RssArticle getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText =  getChild(groupPosition, childPosition).getTitle();

        if(!debugMode){
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rss_list_item, null);
        }else{
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rss_list_item_debug_mode, null);
        }


        TextView rssTitle = (TextView) convertView.findViewById(R.id.title);
        TextView scoreTextView = (TextView) convertView.findViewById(R.id.rate);
        rssTitle.setText(childText);

        if(ReadArticleActivity.articlesOpened.contains(childText)) {
            rssTitle.setTextColor(Color.GRAY);
            if(debugMode){
                scoreTextView.setTextColor(Color.GRAY);
            }
        }
        else {
            rssTitle.setTextColor(Color.BLACK);
            if(debugMode){
                scoreTextView.setTextColor(Color.BLACK);
            }
        }

        if(debugMode) {
            Double score = articleAndScoreMap.get(getChild(groupPosition, childPosition));


            if(score==null){           //if rss news just generated and not ranked yet
                scoreTextView.setText(getChild(groupPosition, childPosition).getCount());
            } else{
                scoreTextView.setText(getChild(groupPosition, childPosition).getCount()+"\n"+String.valueOf(score*100));            }
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.xml_group_layout, null);
        }

        TextView CategoryHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        CategoryHeader.setTypeface(null, Typeface.BOLD);
        CategoryHeader.setText(headerTitle);


        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}