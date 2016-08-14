package com.mit.gear.RSS;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.mattmellor.gear.R;
import com.mit.gear.activities.LiteNewsFragment;
import com.mit.gear.activities.StarredNewsFragment;
import com.mit.gear.activities.StoriesSelectionActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by najlaalghofaily on 8/8/16.
 */

/*
 * This adapter handel the starred news list in starred news section on nav menu
 * if unstar click happens, remove it from starred list/internal storage
 * search fro the file either in news or lite news and update the starred boolean
 */


public class starredListAdapter extends ArrayAdapter<RssArticle> {
    List<RssArticle> listRssArticle;
    Context context;


    public starredListAdapter(Context context, int resource, List<RssArticle> ListRssArticle) {
        super(context, resource, ListRssArticle);
        listRssArticle = ListRssArticle;
        this.context=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view ;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.rss_list_item, null);

        final RssArticle article = listRssArticle.get(position);

        if (article != null) {
            TextView titleTextView = (TextView) view.findViewById(R.id.title);
            titleTextView.setText(article.getTitle());

            titleTextView.setTextColor(Color.BLACK);

            MaterialFavoriteButton favorite = (MaterialFavoriteButton) view.findViewById(R.id.fav);

            favorite.setFavorite(article.isStarred());

            favorite.setFavorite(article.isStarred());
            favorite.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(final MaterialFavoriteButton buttonView, final boolean favorite) {
                            //
                            if (!favorite){ //if un favorite button clicked
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                                alertDialogBuilder.setMessage("Are you sure you want to un star this?");
                                alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        article.setStarred(false);
                                        String filename = article.getTitle();
                                        String content = article.getCategory() + "\n" + article.isStarred() + "\n" + article.getContent();
                                        File newsDir = context.getDir("rssArticles", Context.MODE_PRIVATE);
                                        File liteNewsDir = context.getDir("LiteRssArticles", Context.MODE_PRIVATE);

                                        try {
                                            File fileWithinNewsDir = new File(newsDir, filename);
                                            File fileWithinLiteNewsDir = new File(liteNewsDir, filename);

                                            if(fileWithinNewsDir.exists()) {         //checking if the file exists in news dir

                                                unStarNews(StoriesSelectionActivity.mappingCategory, filename);
                                                updateNewsInStorage(fileWithinNewsDir, content);

                                            }
                                            else if(fileWithinLiteNewsDir.exists()) {        //checking if the file exists in lite news dir

                                                unStarNews(LiteNewsFragment.mappingCategory,filename);
                                                updateNewsInStorage(fileWithinLiteNewsDir,content);

                                            }

                                            deleteFromStarredNews(filename);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        buttonView.toggleFavorite();

                                    }
                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.setCancelable(false);
                                alertDialog.show();
                            }
                        }
                    });


        }

        return view;
    }

    /*
     *This method search for a file name in a map (either news map or lite news map)
     * Updates the starred to be false
     */
    private void unStarNews(Map<String,List<RssArticle>> rssMap , String filename){
        for(List<RssArticle> rssList :rssMap.values()){
            for(RssArticle article : rssList){
                if(article.getTitle().equals(filename)){
                    article.setStarred(false);
                    break;
                }
            }
        }
    }



    /*
     *This method delete a file from the starred articles in the internal storage
     * Also delete the file from the StarredArticles list in StarredNewsFragment
     */

    private void deleteFromStarredNews(String fileName){
        File starredDir = context.getDir("StarredArticles", Context.MODE_PRIVATE);

        for (File file : starredDir.listFiles()) {
            if (file.getName().trim().equals(fileName)) {
                file.delete();
                for(RssArticle starredArticle : StarredNewsFragment.starredArticles){
                    if(starredArticle.getTitle().trim().equals(fileName)) {
                        StarredNewsFragment.starredArticles.remove(starredArticle);
                        updateStarredCount();
                        StarredNewsFragment.adapter.notifyDataSetChanged();
                        break;
                    }
                }
                break;
            }
        }
    }



    /*
     *This method writes a file in a certain directory
     */

    private void updateNewsInStorage(File dir,String content){
        try {
            FileOutputStream out = new FileOutputStream(dir);
            out.write(content.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     *This method updates the count of the starred news in nav menu
     */


    private void updateStarredCount(){
        File starredDir = context.getDir("StarredArticles", Context.MODE_PRIVATE);
        StarredNewsFragment.setTheCountBoxWithoutSelection(starredDir.listFiles().length,4);
    }
}

