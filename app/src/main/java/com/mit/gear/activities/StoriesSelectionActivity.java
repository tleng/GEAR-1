package com.mit.gear.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mattmellor.gear.R;
import com.mit.gear.RSS.RssListListener;
import com.mit.gear.RSS.RssArticle;
import com.mit.gear.RSS.RssReader;
import com.mit.gear.NavDrawer.NavDrawerListAdapter;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mattmellor.gear.R.id.app_article_bar;

/**
 * Activity where user can browse and select which story to read
 */
public class StoriesSelectionActivity extends Fragment {
    private android.support.v7.widget.Toolbar toolbar;
    private StoriesSelectionActivity local;
    static public Context context;
    public static ProgressDialog progress;
    private List<RssArticle> ListRssArticle;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_stories_selection, container, false);
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        toolbar = (android.support.v7.widget.Toolbar) view.findViewById(app_article_bar);
        context = getActivity();
        //setSupportActionBar(toolbar);
        // try {
        //    generateRecommendationButtons();
        //} catch (IOException e) {
        //  e.printStackTrace();
        //    }


        if (savedInstanceState == null) {           //if news was generating for the first time
            if(needsUpdate()){                      //check if today date is the same as last updated date
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); //lock the current orientation
                progress = new ProgressDialog(getActivity());
                progress.setMessage("Generating news");
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setProgress(0);
                progress.setCancelable(false);
                local = this;

                //get rss items
                GetRSSDataTask task = new GetRSSDataTask();
                task.execute(getResources().getString(R.string.rssLink));

                //save today date as the last update date
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("date", getTodayDate());
                editor.commit();
            }

           else{    //if today date is not after the last update date
                prepareTheList(getNewsFromStorage()); //get news from storage and view them
            }
       }
        else{                                   //if news was previously generated, get them from the bundle

            ListRssArticle= (List<RssArticle>) savedInstanceState.getSerializable("RssArticle");
            prepareTheList(ListRssArticle);

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
    *This method returns a list of new articles from a given RSS url
     */
    public List<RssArticle> getRssArticles(String urls){
        try {
            // Create RSS reader
            RssReader rssReader = new RssReader(urls);
            return rssReader.getItems();

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
            ListRssArticle = getRssArticles(urls[0]);

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
            for (int i = 0; i < ListRssArticle.size(); i++) {   //access every article to get content from http request
                publishProgress(i);                             //update the progress dialog
                String content = "";


                Document doc;
                Elements newsHeadlines = null;
                try {
                    publishProgress(i);
                    String link = ListRssArticle.get(i).getLink();
                    String link2 = link.substring(0, link.lastIndexOf("."))+"-druck"+link.substring(link.lastIndexOf(".")); //request a printing version
                    doc = Jsoup.connect(link2).get();
                    if (doc != null) {
                        //filter the content
                        newsHeadlines = doc.select("body");
                        newsHeadlines.select("p a").unwrap();
                        newsHeadlines = newsHeadlines.select("p");
                        newsHeadlines.select("b").unwrap();
                        newsHeadlines.select("strong").unwrap();
                        newsHeadlines.select("a").unwrap();
                        newsHeadlines.select("br").remove();
                        newsHeadlines.select("i").unwrap();
                        newsHeadlines.select("sub").unwrap();
                        newsHeadlines.select("span").unwrap();
                        newsHeadlines.select("font").unwrap();

                    }
                } catch (HttpStatusException e) {
                    continue;
                } catch (Exception e )  {
                    continue;
                }


                if (newsHeadlines != null) {
                    content= newsHeadlines.html();
                } else continue;


                if (content == null) { //if there is no content
                    ListRssArticle.remove(i);
                } else
                {
                    ListRssArticle.get(i).setContent(content);
                }
                // saving the news as a file in the internal storage under rssArticles directory
                String filename = ListRssArticle.get(i).getTitle();
                String string = ListRssArticle.get(i).getContent();

                try {
                    File fileWithinMyDir = new File(myDir, filename); //Getting a file within the dir.
                    FileOutputStream out = new FileOutputStream(fileWithinMyDir);
                    out.write(string.getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ListRssArticle;
        }

        @Override
        protected void onPostExecute(List<RssArticle> result) {
            if(result!= null)
            {
                ListRssArticle = result;
                prepareTheList(ListRssArticle);
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                progress.dismiss();
            }

        }
    }


    /*

    * this method access the news files in the device internal storage
    * returns a list of news

     */
    private List<RssArticle> getNewsFromStorage(){
        File myDir = context.getDir("rssArticles", Context.MODE_PRIVATE); //Creating an internal dir;
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

                    String readString = bufferedReader.readLine () ;
                    while ( readString != null ) {
                        content.append(readString);
                        content.append('\n');
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
    *
     */
    private void prepareTheList(List<RssArticle> result){

        ListRssArticle = result;
        ListView rssItems = (ListView) getActivity().findViewById(R.id.listView);
        MainActivity.navDrawerItems.get(0).setCounterVisibility(true);
        MainActivity.navDrawerItems.get(0).setCount(String.valueOf(result.size()));
        MainActivity.adapter = new NavDrawerListAdapter(MainActivity.context,
                MainActivity.navDrawerItems);
        MainActivity.mDrawerList.setAdapter(MainActivity.adapter);
        MainActivity.mDrawerList.setItemChecked(0, true);
        MainActivity.mDrawerList.setSelection(0);
        ArrayAdapter<RssArticle> adapter = new ArrayAdapter<RssArticle>(getActivity(), android.R.layout.simple_list_item_1, result);
        rssItems.setAdapter(adapter);
        rssItems.setOnItemClickListener(new RssListListener(result, getActivity()));

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("RssArticle", (Serializable) ListRssArticle);

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
        Long lastUpdateDate = sharedPreferences.getLong("date", 0);
        Long todayDate = getTodayDate();
        if(lastUpdateDate <todayDate)
        {
            return true;
        }
        return false;
    }

    }

