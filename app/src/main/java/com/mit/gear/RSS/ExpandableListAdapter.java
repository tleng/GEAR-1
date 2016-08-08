package com.mit.gear.RSS;

/**
 * Created by najlaalghofaily on 7/24/16.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.mattmellor.gear.R;
import com.mit.gear.activities.StarredNewsFragment;
import com.mit.gear.reading.ReadArticleActivity;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    Map<RssArticle,Double> articleAndScoreMap;
    private boolean debugMode;
    private String Dir;
    private Map<String, List<RssArticle>> _listDataChild;// child data, mapping between <header,child List>
    private Integer maxOfStarredArticles = 50;

    /**
     * if the mode was not debug mode
     */

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 Map<String, List<RssArticle>> listChildData, String Dir) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        articleAndScoreMap=null;
        debugMode =false;
        this.Dir=Dir;
    }

    /**
     * if the mode was debug mode
     */

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 Map<String, List<RssArticle>> listChildData, Map<RssArticle,Double> ArticleAndScoreMap, String Dir) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        articleAndScoreMap=ArticleAndScoreMap;
        debugMode =true;
        this.Dir=Dir;
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
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText =  getChild(groupPosition, childPosition).getTitle();
        final RssArticle rssArticle = getChild(groupPosition, childPosition);
        if(!debugMode){
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rss_list_item, null);
        }else{
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rss_list_item_debug_mode, null);
        }


        MaterialFavoriteButton favorite = (MaterialFavoriteButton) convertView.findViewById(R.id.fav);

        favorite.setFavorite(getChild(groupPosition, childPosition).isStarred());
        favorite.setOnFavoriteChangeListener(
                new MaterialFavoriteButton.OnFavoriteChangeListener() {
                    @Override
                    public void onFavoriteChanged(final MaterialFavoriteButton buttonView, final boolean favorite) {

                        final String filename = rssArticle.getTitle();
                        final File myDir = _context.getDir(Dir, Context.MODE_PRIVATE); //the dir either the news or lite news
                        final File starredDir = _context.getDir("StarredArticles", Context.MODE_PRIVATE); //Creating an internal dir;

                        if(favorite){                                   //if the click was to favorite

                            if(isMaximumReached()){                     // checks if the allowed maximum num of starred articles is reached
                                buttonView.toggleFavorite();            //reset the button (un favorite)
                                return;
                            }
                            rssArticle.setStarred(true);
                            String string = rssArticle.getCategory()+"\n"+rssArticle.isStarred()+"\n"+rssArticle.getContent();

                            writeFile(myDir, filename, string);      //updating the existing file (either in news dir or lite news dir)
                            writeFile(starredDir, filename, string);   //Adding a file to starred dir


                        }else{                                            //if the click was to un favorite

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(_context);   //prepare a dialog
                            alertDialogBuilder.setMessage("Are you sure you want to un star this?");
                            alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {                     //if yes is clicked
                                    rssArticle.setStarred(false);
                                    String string = rssArticle.getCategory() + "\n" + rssArticle.isStarred() + "\n" + rssArticle.getContent();

                                    writeFile(myDir,filename,string);
                                    for(File file: starredDir.listFiles()){
                                        if (file.getName().trim().equals(rssArticle.getTitle().trim())) {
                                            file.delete();
                                            break;
                                        }
                                    }
                                    updateStarredCount();

                                }
                            });

                            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() { //if no button was clicked
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    buttonView.toggleFavorite();                                            //reset the toggle to its prev state
                                }
                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        }
                        updateStarredCount();
                    }
                });
        favorite.setOnFavoriteAnimationEndListener(
                new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                    }
                });


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
                scoreTextView.setText(getChild(groupPosition, childPosition).getCount()+"\n"+String.valueOf(score*100));
            }
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

    /*
     * This method checks if the total number of starred article in the storage is the maximum
     */

    private boolean isMaximumReached(){
        File StarredDir = _context.getDir("StarredArticles", Context.MODE_PRIVATE); //Creating an internal dir;
        File[] files = StarredDir.listFiles();
        if(files.length==maxOfStarredArticles){
            Toast.makeText(_context,"You can not star more than 50 news",Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }


    /*
     * This method writes a file to a specific dir
     */

    private void writeFile(File dir, String fileName, String content){
        try {
            File fileWithinMyDir = new File(dir, fileName);
            FileOutputStream out = new FileOutputStream(fileWithinMyDir);
            out.write(content.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateStarredCount(){
        File starredDir = _context.getDir("StarredArticles", Context.MODE_PRIVATE);
        StarredNewsFragment.setTheCountBox(starredDir.listFiles().length);
    }
}