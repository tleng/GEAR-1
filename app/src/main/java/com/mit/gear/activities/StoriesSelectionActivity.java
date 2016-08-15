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
import com.mit.gear.RSS.ExpandableListAdapter;
import com.mit.gear.RSS.RssArticle;
import com.mit.gear.RSS.RssGlobal;
import com.mit.gear.RSS.RssListListener;
import com.mit.gear.data.DataStorage;
import com.mit.gear.miscellaneous.MapUtil;
import com.mit.gear.words.Word;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mattmellor.gear.R.id.app_article_bar;

/**
 * Activity where user can browse and select which story to read
 */
public class StoriesSelectionActivity extends Fragment {
    private android.support.v7.widget.Toolbar toolbar;
    private StoriesSelectionActivity local;
    static public Context context;
    public static ProgressDialog progress;
    public static List<RssArticle> ListRssArticle;
    private SharedPreferences sharedPreferences;
    private static Map<RssArticle,Double> articleAndScoreMap = new HashMap<>(); // hashmap tp map the article and its score
    DataStorage dataStorage;
    HashMap<String,Word> userDictionary;
    public static boolean needsToScore = false; //boolean indicate if we need to score the news
    public static ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader = new ArrayList<String>();
    Map<String, List<RssArticle>> listDataChild;
    static Long  lastUpdateDate;
    public static Map<String,List<RssArticle>> mappingCategory= new HashMap<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_stories_selection, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(needsToScore && ListRssArticle!= null){  //check if we need to score again (if new words are clicked/saves)
            scoreArticles(RssGlobal.getNewsFromStorage(getActivity(), "rssArticles"));
        }else if(ListRssArticle!= null){
            prepareTheList(mappingCategory);
        }
    }


    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RssGlobal.loadTheOpenedArticles(getActivity());
        View view = getView();
        toolbar = (android.support.v7.widget.Toolbar) view.findViewById(app_article_bar);
        context = getActivity();
        dataStorage= new DataStorage(getActivity().getApplicationContext());


        if (savedInstanceState == null) {           //if news was generating for the first time
            if(RssGlobal.needsUpdate(RssGlobal.getLastUpdateDate(getActivity(), "date"))){                      //check if today date is the same as last updated date
                lastUpdateDate=RssGlobal.getTodayDate();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock the current orientation
                progress = new ProgressDialog(getActivity());
                progress.setMessage("Generating news");
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setProgress(0);
                progress.setCancelable(false);
                local = this;

                //get rss items
                GetRSSDataTask task = new GetRSSDataTask();
                task.execute(getResources().getString(R.string.regular_rss_news_link));

                RssGlobal.setLastUpdateDate(getActivity(),"date");

            }

           else{    //if today date is not after the last update date
                if(ListRssArticle==null){ //open app for first time
                    scoreArticles(RssGlobal.getNewsFromStorage(getActivity(), "rssArticles"));
                   // prepareTheList(); //get news from storage and view them
                   // needsToScore=true;
                }

                else {   //the news list are already loaded
                    if(mappingCategory!=null)
                    prepareTheList(mappingCategory);
                }

            }
       }
        else{                                   //if news were previously generated, get them from the bundle

            ListRssArticle= (List<RssArticle>) savedInstanceState.getSerializable("RssArticle");
            mappingCategory = (Map<String, List<RssArticle>>) savedInstanceState.getSerializable("mappingCategory");
            //scoreArticles(ListRssArticle);
            prepareTheList(mappingCategory);

       }
    }


    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_stories_selection);
//        toolbar = (android.support.v7.widget.Toolbar) findViewById(app_article_bar);
//        setSupportActionBar(toolbar);
//       // try {
//        //    generateRecommendationButtons();
//        //} catch (IOException e) {
//          //  e.printStackTrace();
//    //    }
//        context = StoriesSelectionActivity.this;
//        local = this;
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//
//        getMenuInflater().inflate(R.menu.menu_stories_suggestion_and_selection, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        // noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


//    View.OnClickListener getOnClickSetStory(final Button button)  {
//        return new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), ReadArticleActivity.class);
//                intent.putExtra("story", GEARGlobal.articlePath + button.getContentDescription());
//                getActivity().startActivity(intent);
//                //finish();
//            }
//        };
//    }

//    private void generateRecommendationButtons() throws IOException {
//        String [] articles = getActivity().getAssets().list(GEARGlobal.articlePathName);
//
//        // adjust linear layout to display articles in
//        LinearLayout ll = (LinearLayout)getActivity().findViewById(R.id.allStoriesLinearLayout);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll.setOrientation(LinearLayout.VERTICAL);
//
//
//        // StackOverflow suggested code to dynamically create buttons for the articles
//        for (String article:articles) {
//           // if (asset.equals("images") || article.equals("sounds") || article.equals("webkit")|| article.equals("pskc_schema.xsd")) {
//           //     continue;
//           // }
//
//                Button myButton = new Button(getActivity());
//                myButton.setText(Html.fromHtml(article));
//                myButton.setContentDescription(article);
//                myButton.setHeight(30);
//                myButton.setTransformationMethod(null); // ensures text is lower case
//
//                Log.d("button added:", myButton.toString());
//                myButton.setOnClickListener(getOnClickSetStory(myButton));
//
//                ll.addView(myButton, lp);
//                Log.d("number of buttons:", Integer.toString(ll.getChildCount()));
//            }
//
//        }



    /*
     * This class access every link in the rss list and get the article content
     * Saves the articles in files in the internal storage and delete the old ones if exist
     * Update progress dialog when ever article is accessed
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
            ListRssArticle = RssGlobal.getRssArticles(urls[0]);

            if(ListRssArticle==null)
                return null;
            publishProgress(-1, ListRssArticle.size()); //used to prepare the progress dialog with the maximum value and show it
            File myDir = context.getDir("rssArticles", Context.MODE_PRIVATE); //Creating an internal dir;
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
                    String link2 = link.substring(0, link.lastIndexOf("."))+"-druck"+link.substring(link.lastIndexOf(".")); //request a printing version
                    doc = Jsoup.connect(link2).get();
                    if (doc != null) {
                        //filter the content
                        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing

                        newsHeadlines = doc.select("body");
                        newsHeadlines.select("div.article-copyright p").prepend(getResources().getString(R.string.endOfArticleIndicator)+" \\n");
                        newsHeadlines.select("div.article-copyright p").append("\\n\\n" + link);

                        newsHeadlines.select("p a").unwrap();
                        newsHeadlines = newsHeadlines.select("p").prepend("\\n\\n");
                        newsHeadlines.select("b").unwrap();
                        newsHeadlines.select("strong").unwrap();
                        newsHeadlines.select("a").unwrap();
                        newsHeadlines.select("br").remove();
                        newsHeadlines.select("i").unwrap();
                        newsHeadlines.select("sub").unwrap();
                        newsHeadlines.select("span").unwrap();
                        newsHeadlines.select("font").unwrap();
                        if(newsHeadlines.hasClass("obfuscated")){ //this checks if the article is paid article(no full content), then discard it
                            articlesToDelete.add(rssArticle);
                            continue;
                        }


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
        lastUpdateDateTxt.setText("Last update: "+format.format(new Date(RssGlobal.getLastUpdateDate(getActivity(),"date")))+"\t\tSource: "+getResources().getString(R.string.regular_rss_news_sourceName));

        // get the listview
        expListView = (ExpandableListView) getActivity().findViewById(R.id.lvExp);

       // preparing list data
        listDataHeader.clear();

        listDataHeader.addAll(mappingCategory.keySet());
        listDataChild=mappingCategory;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        Boolean debugChoice = sharedPreferences.getBoolean("debug", false);
        if(debugChoice) {
            listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild,articleAndScoreMap,"rssArticles",0,getActivity());
        }

        else {
            listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild,"rssArticles",0,getActivity());
        }

        // setting list adapter
        expListView.setAdapter(listAdapter);

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

        for(int i=0; i < listAdapter.getGroupCount(); i++)
            expListView.expandGroup(i);

        if (firstVisibleItem != 0)
            expListView.setSelection(firstVisibleItem);

        RssGlobal.updateNavDrawer(0, ListRssArticle.size());

    }





    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("RssArticle", (Serializable) ListRssArticle);
        outState.putSerializable("mappingCategory", (Serializable) mappingCategory);

    }



    /*
        This method is the same as the one in SuggestedStoriesActivity  but altered for news use
     */

    private Map<String,List<RssArticle>> recommendKArticles2(List<RssArticle> articles) {

        List<RssArticle> listOfArticleAssets = articles ;
        articleAndScoreMap.clear();
        for(RssArticle article: listOfArticleAssets){
            Double fraction = RssGlobal.getScore(getActivity(), article);
			String count = RssGlobal.getCount(article, getActivity());
            articleAndScoreMap.put(article,fraction);
			article.setCount(count);
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





}

