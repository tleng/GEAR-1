package com.mit.gear.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mattmellor.gear.R;
import com.mit.gear.NavDrawer.NavDrawerListAdapter;
import com.mit.gear.RSS.RssArticle;
import com.mit.gear.RSS.starredListAdapter;
import com.mit.gear.reading.ReadArticleActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by najlaalghofaily on 8/3/16.
 */

public class StarredNewsFragment extends Fragment {

    public static ArrayAdapter<RssArticle> adapter;
    public static  List<RssArticle> starredArticles;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_starred_news_fragment, container, false);
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        starredArticles=getStarredFromStorage();
        if(starredArticles!=null && starredArticles.size()>0)
            prepareTheList();
        else{
            Toast.makeText(getActivity(),"You haven't starred any news",Toast.LENGTH_SHORT).show();
        }


    }

    private List<RssArticle> getStarredFromStorage(){
        File myDir = getActivity().getDir("StarredArticles", Context.MODE_PRIVATE); //Creating an internal dir;
        if (!myDir.exists())
        {
            return null;
        }
        List<RssArticle> result = new ArrayList();
        File[] files = myDir.listFiles();
        for (File file : files) {

            RssArticle rssArticle=new RssArticle();
            rssArticle.setTitle(file.getName());
            StringBuffer content = new StringBuffer("");
            try {
                FileInputStream fIn = new FileInputStream(new File(file.getPath())) ;
                InputStreamReader isr = new InputStreamReader ( fIn ) ;
                BufferedReader bufferedReader = new BufferedReader ( isr ) ;

                String readString = bufferedReader.readLine () ; //the first line is the article's category
                rssArticle.setCategory(readString);
                readString = bufferedReader.readLine () ;
                rssArticle.setStarred(Boolean.parseBoolean(readString));
                readString = bufferedReader.readLine () ;

                while ( readString != null ) {
                    content.append(readString);
                    content.append('\n');
                    readString = bufferedReader.readLine () ;
                }
                rssArticle.setContent(String.valueOf(content));
                result.add(rssArticle);
                isr.close() ;
            } catch ( IOException ioe ) {
                ioe.printStackTrace() ;
            }
        }
        return result;
    }

    private void prepareTheList(){
        ListView rssItems = (ListView) getActivity().findViewById(R.id.listView);
        setTheCountBox(starredArticles.size());
        adapter = new starredListAdapter(getActivity(), R.layout.rss_list_item, starredArticles);
        rssItems.setAdapter(adapter);
        rssItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ReadArticleActivity.class);
                ReadArticleActivity.articlesOpened.add(starredArticles.get(position).getTitle());
                intent.putExtra("title", starredArticles.get(position).getTitle());
                intent.putExtra("content", starredArticles.get(position).getContent());
                getActivity().startActivity(intent);
            }
        });

    }

    public static void setTheCountBox(Integer size){
        MainActivity.navDrawerItems.get(4).setCounterVisibility(true);
        MainActivity.navDrawerItems.get(4).setCount(String.valueOf(size));
        MainActivity.adapter = new NavDrawerListAdapter(MainActivity.context,
                MainActivity.navDrawerItems);
        MainActivity.mDrawerList.setAdapter(MainActivity.adapter);
        MainActivity.mDrawerList.setItemChecked(4, true);
        MainActivity.mDrawerList.setSelection(4);
    }
}
