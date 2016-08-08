package com.mit.gear.activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.NavDrawer.NavDrawerListAdapter;
import com.mit.gear.RSS.ExpandableListAdapter;
import com.mit.gear.RSS.RssArticle;
import com.mit.gear.RSS.RssListListener;
import com.mit.gear.RSS.RssReader;
import com.mit.gear.data.DataStorage;
import com.mit.gear.miscellaneous.MapUtil;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.words.Word;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mattmellor.gear.R.id.app_article_bar;

/**
 * Created by najlaalghofaily on 7/23/16.
 */


/**
 * This Fragment represent the Rss for Lite news
 */
public class LiteNewsFragment extends Fragment {

    private android.support.v7.widget.Toolbar toolbar;
    private LiteNewsFragment local;
    static public Context context;
    public static ProgressDialog progress;
    public static List<RssArticle> ListRssArticle;
    private SharedPreferences sharedPreferences;
    private static Map<RssArticle,Double> articleAndScoreMap = new HashMap<>(); // hashmap tp map the article and its score
    DataStorage dataStorage;
    HashMap<String,Word> userDictionary;
    public static boolean needsToScore = false; //boolean indicate if we need to score the news
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader = new ArrayList<String>();
    Map<String, List<RssArticle>> listDataChild;
    public static Map<String,List<RssArticle>> mappingCategory= new HashMap<>();
    static Long  lastUpdateDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_stories_selection, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(needsToScore && ListRssArticle!= null){  //check if we need to score again (if new words are clicked/saves)
            scoreArticles(getNewsFromStorage());
        }else if(ListRssArticle!= null){
            prepareTheList(mappingCategory);
        }

    }


    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadTheOpenedArticles();
        View view = getView();
        toolbar = (android.support.v7.widget.Toolbar) view.findViewById(app_article_bar);
        context = getActivity();
        dataStorage= new DataStorage(getActivity().getApplicationContext());

        if (savedInstanceState == null) {           //if news was generating for the first time
            if(needsUpdate()){                      //check if today date is the same as last updated date
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock the current orientation
                progress = new ProgressDialog(getActivity());
                progress.setMessage("Generating Lite news");
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setProgress(0);
                progress.setCancelable(false);
                local = this;

                //get rss items
                GetRSSDataTask task = new GetRSSDataTask();
                task.execute(getResources().getStringArray(R.array.simple_rss_news_link)[0]);

                //save today date as the last update date
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("dateLite", getTodayDate());
                editor.commit();
            }

            else{    //if today date is not after the last update date
                if(ListRssArticle==null){ //open app for first time
                    scoreArticles(getNewsFromStorage());

                }

                else {   //the news list are already loaded
                    if(mappingCategory!=null)
                        prepareTheList(mappingCategory);

                }
            }
        }
        else{                                   //if news were previously generated, get them from the bundle

            ListRssArticle= (List<RssArticle>) savedInstanceState.getSerializable("RssArticleLite");
            mappingCategory = (Map<String, List<RssArticle>>) savedInstanceState.getSerializable("mappingCategoryLite");
            prepareTheList(mappingCategory);

        }
    }

    /*
    *This method returns a list of new articles from a given RSS url
     */

    public List<RssArticle> getRssArticles(String urls, String category){
        try {
            List<RssArticle> allCategoryArticles = new ArrayList<>();
            // Create RSS reader
            RssReader rssReader = new RssReader(urls);
            for(RssArticle article:rssReader.getItems()){
                article.setCategory(category);
                allCategoryArticles.add(article);
            }
            return allCategoryArticles;

        } catch (Exception e) {
            return null;
        }
    }


    /*

       *this class access every link in the rss list and get the article content
       * saves the articles in files in the internal storage and delete the old ones if exist
       * update progress dialog when ever article is accessed

        */

    private class GetRSSDataTask extends AsyncTask<String, Integer, List<RssArticle>> {
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values[0]==-1) {                 //prepare the progress dialog

                progress.setMax(values[1]);
                progress.show();
            }
            else                                //update the progress dialog
            {
                progress.setProgress(values[0]);
            }
        }

        @Override
        protected List<RssArticle> doInBackground(String... urls) {
            articleAndScoreMap.clear();
            if(ListRssArticle!=null)
            ListRssArticle.clear();
            else{
                ListRssArticle = new ArrayList<RssArticle>();
            }
            ListRssArticle.addAll(getRssArticles(urls[0],getResources().getStringArray(R.array.simple_rss_news_category)[0]));
            ListRssArticle.addAll(getRssArticles(getResources().getStringArray(R.array.simple_rss_news_link)[1],getResources().getStringArray(R.array.simple_rss_news_category)[1]));
            ListRssArticle.addAll(getRssArticles(getResources().getStringArray(R.array.simple_rss_news_link)[2],getResources().getStringArray(R.array.simple_rss_news_category)[2]));
            ListRssArticle.addAll(getRssArticles(getResources().getStringArray(R.array.simple_rss_news_link)[3],getResources().getStringArray(R.array.simple_rss_news_category)[3]));

            if(ListRssArticle==null)
                return null;
            publishProgress(-1, ListRssArticle.size()); //used to prepare the progress dialog with the maximum value and show it
            File myDir = context.getDir("LiteRssArticles", Context.MODE_PRIVATE); //Creating an internal dir;
            if (!myDir.exists())
            {
                myDir.mkdirs();
            }
            else {
                for (File file : myDir.listFiles()) {
                    file.delete();                      //deleting old news files
                }
            }
            Integer progress = 0;
            List<RssArticle> articlesToDelete = new ArrayList<>();
            for (RssArticle rssArticle :ListRssArticle ) {   //access every article to get content from http request
                publishProgress(++progress);                             //update the progress dialog
                String content = "";


                Document doc;
                Elements newsHeadlines = null;
                try {
                    String link = rssArticle.getLink();
                    String link2 =link.replace("html","print");
                    //link.substring(0, link.lastIndexOf("."))+"print"+link.substring(link.lastIndexOf(".")); //request a printing version
                    doc = Jsoup.connect(link2).get();
                    if (doc != null) {
                        //filter the content
                        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing

                        newsHeadlines = doc.select("body");

                        newsHeadlines= newsHeadlines.select("div.text p, div#pageFooter p");
                        newsHeadlines.select("p a").unwrap();
                        newsHeadlines.select("p").prepend("\\n\\n");
                        newsHeadlines.select("b").unwrap();
                        newsHeadlines.select("strong").unwrap();
                        newsHeadlines.select("a").unwrap();
                        newsHeadlines.select("br").remove();
                        newsHeadlines.select("i").unwrap();
                        newsHeadlines.select("sub").unwrap();
                        newsHeadlines.select("span").unwrap();
                        newsHeadlines.select("font").unwrap();
                        newsHeadlines.select("p.copyright").prepend(getResources().getString(R.string.endOfArticleIndicator) + " \\n");
                        newsHeadlines.select("p.copyright").append("\\n\\n" + link);

                    }
                } catch (HttpStatusException e) {
                    articlesToDelete.add(rssArticle);
                    continue;
                } catch (Exception e )  {
                    articlesToDelete.add(rssArticle);
                    continue;
                }


                if (newsHeadlines != null) {
                    String s = newsHeadlines.text().replaceAll("\\\\n","\n");
                    content= Jsoup.clean( s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
                    content=content.replaceAll("&amp;", "&");                       //for some reason the clean method don't convert ALL &amp; special character to & symbol
                    content=content.replaceAll("&nbsp;", " ");
                } else {
                    articlesToDelete.add(rssArticle);
                    continue;
                }


                if (content == null) { //if there is no content
                    articlesToDelete.add(rssArticle);
                    continue;
                } else
                {
                    rssArticle.setContent(content);
                }
                // saving the news as a file in the internal storage under rssArticles directory
                String filename = rssArticle.getTitle();
                String string = rssArticle.getCategory()+"\n"+rssArticle.isStarred()+"\n"+rssArticle.getContent();

                try {
                    File fileWithinMyDir = new File(myDir, filename); //Getting a file within the dir.
                    FileOutputStream out = new FileOutputStream(fileWithinMyDir);
                    out.write(string.getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ListRssArticle.removeAll(articlesToDelete);
            return ListRssArticle;
        }

        @Override
        protected void onPostExecute(List<RssArticle> result) {
            if(result!= null)
            {
                ListRssArticle = result;
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                progress.dismiss();
                scoreArticles(ListRssArticle);
            }

        }
    }


    /*

    * this method access the news files in the device internal storage
    * returns a list of news

     */
    private List<RssArticle> getNewsFromStorage(){
        File myDir = context.getDir("LiteRssArticles", Context.MODE_PRIVATE); //Creating an internal dir;
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

    /*
     * this method updates the nav drawer to indicate the number of news are there
     * attach the list to the layout and sets a click listener
     */

    private void prepareTheList(Map<String,List<RssArticle>> result){
        mappingCategory = result;
        int firstVisibleItem = 0;
        if( expListView!=null)
            firstVisibleItem =expListView.getFirstVisiblePosition();

        TextView lastUpdateDateTxt = (TextView) getActivity().findViewById(R.id.lastUpdateDate);
        Format format = new SimpleDateFormat("dd/MM/yyyy");
        lastUpdateDateTxt.setText("Last update: "+format.format(new Date(lastUpdateDate))+"\t\tSource: "+getResources().getString(R.string.simple_rss_news_sourceName));

        expListView = (ExpandableListView) getActivity().findViewById(R.id.lvExp);        // get the listview
        listDataHeader.clear();

        listDataHeader.addAll(mappingCategory.keySet());
        listDataChild=mappingCategory;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        Boolean debugChoice = sharedPreferences.getBoolean("debug", false);

        if(debugChoice) {
            listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild,articleAndScoreMap,"LiteRssArticles");
        }

        else {
            listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild,"LiteRssArticles");
        }

        expListView.setAdapter(listAdapter); // setting list adapter

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {


            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new RssListListener(listDataHeader, listDataChild, getActivity()));

        for(int i=0; i < listAdapter.getGroupCount(); i++) {
            expListView.expandGroup(i);
        }

        if (firstVisibleItem != 0)
            expListView.setSelection(firstVisibleItem);

        MainActivity.navDrawerItems.get(1).setCounterVisibility(true);
        MainActivity.navDrawerItems.get(1).setCount(String.valueOf(ListRssArticle.size()));
        MainActivity.adapter = new NavDrawerListAdapter(MainActivity.context,
                MainActivity.navDrawerItems);
        MainActivity.mDrawerList.setAdapter(MainActivity.adapter);
        MainActivity.mDrawerList.setItemChecked(1, true);
        MainActivity.mDrawerList.setSelection(1);



    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("RssArticleLite", (Serializable) ListRssArticle);
        outState.putSerializable("mappingCategoryLite", (Serializable) mappingCategory);

    }


    /*
     * this method get today date without time
     *
     */

    private Long getTodayDate(){
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date todayWithZeroTime = formatter.parse(formatter.format(new Date()));
            return todayWithZeroTime.getTime();
        } catch (ParseException e) {
            return (long) 0;
        }
    }


    /*
    * this method checks if the news needs to update or not
    * return true if  today date is bigger than the last update date, false otherwise

     */

    private boolean needsUpdate(){
        sharedPreferences = context.getSharedPreferences("LastUpdateDate", Context.MODE_PRIVATE);
        lastUpdateDate = sharedPreferences.getLong("dateLite", 0);
        Long todayDate = getTodayDate();
        if(lastUpdateDate <todayDate)
        {
            lastUpdateDate=todayDate;
            return true;
        }
        return false;
    }


    /*
        This method is the same as the one in SuggestedStoriesActivity  but altered for news use
     */

    private Map<String,List<RssArticle>> recommendKArticles2(List<RssArticle> articles) {

        List<RssArticle> listOfArticleAssets = articles ;
        articleAndScoreMap.clear();
        for(RssArticle article: listOfArticleAssets){
            Double fraction = getScore(article);
            articleAndScoreMap.put(article,fraction);
        }
        articleAndScoreMap = MapUtil.sortByValue(articleAndScoreMap);
        Set<RssArticle> sortedArticles = articleAndScoreMap.keySet();
        List<RssArticle> sortedArticleList = new ArrayList<>();
        // this.articlesWithRatings.putAll(articleAndScoreMap);
        for(RssArticle article: sortedArticles){
            sortedArticleList.add(article);
            //This just returns the keyset as a list???
        }
        int n = sortedArticles.size();
        List<RssArticle> recommendedArticles = new ArrayList<>();
        mappingCategory= new HashMap<>();
        // check if there are sortedArticles
        if (n>0) {
            List<RssArticle> childList;
            for (int i = n - 1; i >= 0; i--) {
                if(mappingCategory.containsKey(sortedArticleList.get(i).getCategory())){
                    childList=mappingCategory.get(sortedArticleList.get(i).getCategory());
                }else{
                    childList = new ArrayList<RssArticle>();
                }
                childList.add(sortedArticleList.get(i));
                mappingCategory.put(sortedArticleList.get(i).getCategory(), childList);
                recommendedArticles.add(sortedArticleList.get(i));
            }

        }
        ListRssArticle=recommendedArticles;
        return mappingCategory;
    }

    /*
    This method is the same as the one in SuggestedStoriesActivity  but altered for news use
     */

    private Double getScore(RssArticle rssArticle) {
        userDictionary = dataStorage.loadUserDictionary();
        String articleText;
        try {

            articleText= rssArticle.getContent();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            articleText = "Error Occurred";  // better - signal error differently
        }

        String[] articleWords = articleText.split("[\\p{Punct}\\s]+");

        Double counter=1.0;
        for(String word: articleWords){
            if(userDictionary.containsKey(word)){
                counter *= 1-userDictionary.get(word).score;
            } else {
                // word has never been encountered before
                counter *= 0.5;
            }
        }

        return counter;

    }


/*
 * this class generate the recommended Articles and call prepareTheList method on the sorted list
 *
 */

    private class rankTheNews extends AsyncTask< List<RssArticle>, Void, Map<String,List<RssArticle>>> {

        @Override
        protected Map<String, List<RssArticle>> doInBackground(List<RssArticle>... params) {
            return recommendKArticles2(params[0]);
        }

        @Override
        protected void onPostExecute(Map<String,List<RssArticle>> result) {
            if(result!=null)
            {
                mappingCategory = result;
                prepareTheList(mappingCategory);
            }
            progress.dismiss();
        }
    }


    /*
     *This method prepare the progress dialog and execute the task to rank articles
     * sets the needsToScore flag to false
     */

    private void scoreArticles(List<RssArticle> unscoredList) {
        progress = new ProgressDialog(getActivity());
        progress.setMessage("Ranking the news for you");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        new rankTheNews().execute(unscoredList);
        needsToScore=false;
    }


    /*
     * This method loads the opened articles set from shared preference
     */

    public void loadTheOpenedArticles(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        ReadArticleActivity.articlesOpened=  new HashSet<String>(sharedPreferences.getStringSet("openedArticles", new HashSet<String>()));

    }

}
