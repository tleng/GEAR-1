package com.mit.gear.reading;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mattmellor.gear.R;
import com.mit.gear.activities.SavePopupActivity;
import com.mit.gear.data.DataStorage;
import com.mit.gear.data.UserDataCollection;
import com.mit.gear.words.DefinitionRequest;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.Word;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.os.Handler;
import android.widget.Toast;

/**
 * Activity where user reads article
 */
public class ReadArticleActivity extends AppCompatActivity {
    private static String LOG_APP_TAG = "ReadArticleActivity-tag";
    private static ReadArticleActivity instance;
    private android.support.v7.widget.Toolbar toolbar;
    public static HashMap<String, ArrayList<String>> offlineDictionary;
    public HashMap<String, Word> userDictionary;
    private DefinitionRequest currentDefinitionRequest;
    public static String currentDefinition = "No definition";
    public static String currentLemma = "None";
    private Integer currentPosition = 0;
    private Long startTime;
    public String currentArticle;
    public static ViewPager pagesView;
    private String storyText = "None";
    public boolean definition_scroll = true;                // Set definition_scroll to true when using a scrolling definition textbox
    public HashMap<String, Integer> currentSessionWords = new HashMap<>();
    private boolean progressSaved = true;                   //setting the progressSaved to true (Assuming no clicks happened)
    private ProgressDialog progress;
    private Handler progressBarHandler = new Handler();     //Handler to update the progress
    public Integer fragmentIndex = 0;                       //index of the current fragment(page) in current article
    public Integer numberOfPages = 0;                         //total number of pages in current article
    public Menu menu;
    private Integer MaximumUndoClicks = 2;                 //limit the undo clicks to 2
    public Integer UndoClicks = 0;                           //keep track of undo clicks
    public TextToSpeech textToSpeech;
    final private Locale LanguageSpeak = Locale.GERMAN;
    public SharedPreferences sharedPreferences;             //sharedPreferences to get user choice of text coloring/speak choice
    ArrayList<ArrayList<String>> ListLastClickedWords = GEARGlobal.ListLastClickedWords;
    ArrayList<ArrayList<String>> MaximumLastClickedWords = GEARGlobal.MaximumLastClickedWords;

    public ReadArticleActivity() {
        instance = this;
    }

    //checking if user saved the progress or not
    public boolean isProgressSaved() {
        return progressSaved;
    }

    public void setProgressSaved(boolean progressSaved) {
        this.progressSaved = progressSaved;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GEARGlobal.ListLastClickedWords.clear();
        GEARGlobal.MaximumLastClickedWords.clear();
        GEARGlobal.resetWordIndex();
        GEARGlobal.setLastWordClickedIndex(-1);                   //clearing the last word index every time the story loads
        GEARGlobal.setLastWordClicked("None");
        if (definition_scroll) {
            setContentView(R.layout.pages_scrolling_definition);
        } else {
            setContentView(R.layout.pages);
        }
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_article_bar);
        setSupportActionBar(toolbar);
        offlineDictionary = GEARGlobal.getOfflineDictionary(getApplicationContext());
        userDictionary = new DataStorage(getApplicationContext()).loadUserDictionary();
        currentDefinitionRequest = null;

        getSupportActionBar().setDisplayShowTitleEnabled(false);    // Getting rid of title for the action bar
        startTime = System.currentTimeMillis();                     // Log start time for when user opened article
        setPagesView();
        initTextToSpeech();
        getUserSettings();

    }

    public void setPagesView() {
        PageFragment.wordIndexing.clear();         //clearing the hash map that contains the fragments starting-indexes
        pagesView = (ViewPager) findViewById(R.id.pages);
        pagesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                PageSplitter pageSplitter = new PageSplitter(pagesView.getWidth(), pagesView.getHeight(), 1, 0);

                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
                AssetManager assetManager = getAssets();
                String story = getIntent().getExtras().getString("story");
                currentArticle = story;
                InputStream input;
                String text;
                try {
                    input = assetManager.open(story);
                    int size = input.available();
                    byte[] buffer = new byte[size];
                    input.read(buffer);
                    input.close();

                    // byte buffer into a string
                    text = new String(buffer).trim();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    text = "Error Occurred";
                }
                storyText = text;
                pageSplitter.append(text, textPaint);
                numberOfPages = pageSplitter.getPages().size(); //getting total number of pages in current atricle
                pagesView.setAdapter(new TextPagerAdapter(getSupportFragmentManager(), pageSplitter.getPages()));
                pagesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        pagesView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                fragmentIndex = position; //update the fragment index to current page position
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pagesView.setOffscreenPageLimit(1);         //limiting the preloading to one page per side
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        menu.getItem(1).setEnabled(false); //disabling the Undo option
        return true;
    }


    /**
     * Stores relevant data
     *
     * @param word
     * @param definition
     * @param lemma
     */
    public void updateDataStorage(String word, String definition, String lemma, String article, boolean click) {
        // Update data collection structures
        if (word != null) {
            UserDataCollection.addWord(word, definition, lemma);
            DataStorage dataStorage = new DataStorage(getApplicationContext());
            try {
                dataStorage.addToUserDictionary(word, lemma, article, click);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     Handle action bar item clicks here. The action bar will
     automatically handle clicks on the Home/Up button, so long
     as you specify a parent activity in AndroidManifest.xml.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear:
                try {
                    DataStorage dataStorage = new DataStorage(getApplicationContext());
                    dataStorage.clearUserDictionary();
                    userDictionary = dataStorage.loadUserDictionary();
                    GEARClickableSpan.clear();
                    GEARGlobal.ListLastClickedWords.clear();
                    GEARGlobal.MaximumLastClickedWords.clear();
                    menu.getItem(1).setEnabled(false);
                    MaximumUndoClicks = 2;
                    UndoClicks = 0;
                    //setPagesView();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_undo:
                Undo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    public void onClickUpPopWindow(View view) {
        Intent intent = new Intent(ReadArticleActivity.this, popUpRateArticle.class);
        startActivity(intent);
//        intent.putExtra("currentArticle", (String) currentUserData.getArticle());
//        intent.putExtra("currentUserId", (String) currentUserData.getUserId());
    }

    public void saveProgress(View view) {
        //preparing the progressDialog
        progress = new ProgressDialog(view.getContext());
        progress.setMessage("Saving Progress");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0);
        progress.setCancelable(false);
        //setting the progressDialog to the last clicked word index
        progress.setMax(GEARGlobal.getLastWordClickedIndex());
        if (progressSaved) {
            Toast.makeText(this, "No progress to save", Toast.LENGTH_LONG).show();
            return;
        } else {
            progress.show();
        }
        setProgressSaved(true);
        Log.d("Save Progress", "Clicked index " + GEARGlobal.getLastWordClickedIndex().toString());
        //Toast toast = Toast.makeText(getApplicationContext(), "Saving work...", Toast.LENGTH_LONG);
        //toast.show();
        final DataStorage dataStorage = new DataStorage(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, ArrayList<Object>> wordsToSave = new HashMap<String, ArrayList<Object>>();
                //userDictionary = dataStorage.loadUserDictionary();
                BreakIterator iterator = BreakIterator.getWordInstance(Locale.GERMANY);
                iterator.setText(storyText);
                int start = iterator.first();
                Integer count = 0;
                Integer newWords = 0;
                for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                        .next()) {
                    String possibleWord = storyText.substring(start, end);
                    if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                        if (count >= GEARGlobal.getLastWordClickedIndex()) {
                            break;
                        }
                        //try {
                        if (currentSessionWords.containsKey(possibleWord)) {
                            Integer sessionCount = currentSessionWords.get(possibleWord);
                            if (sessionCount > 0) {
                                sessionCount -= 1;
                                currentSessionWords.put(possibleWord, sessionCount);
                                continue;
                            }
                        }
                        ArrayList<Object> wordArrayList = new ArrayList<>();
                        wordArrayList.add("None");
                        wordArrayList.add(currentArticle);
                        wordArrayList.add(false);
                        Integer wordCount = 1;
                        if (wordsToSave.containsKey(possibleWord)) {
                            wordCount = (Integer) wordsToSave.get(possibleWord).get(3) + 1;
                        }
                        wordArrayList.add(wordCount);
                        if (!currentSessionWords.containsKey(possibleWord)) {
                            wordsToSave.put(possibleWord, wordArrayList);
                            //dataStorage.addToUserDictionary(possibleWord, "None", currentArticle, false);
                            newWords += 1;

//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                            count += 1;
                        }
                    }
                    try {
                        //sleep the thread for user interface experience
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final Integer finalCount = count;
                    progressBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //updating the progress with words count
                            progress.setProgress(finalCount);
                        }
                    });
                }

                try {
                    dataStorage.addGroupToUserDictionary(wordsToSave);
                    //dismiss the progress and finish the SavePopupActivity if exist
                    progress.dismiss();
                    if (SavePopupActivity.savePopupActivity != null) {
                        SavePopupActivity.savePopupActivity.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //Toast endToast = Toast.makeText(getApplicationContext(), "Updated " + newWords.toString() + " unclicked words.", Toast.LENGTH_SHORT);
        //endToast.show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i("On pause", String.valueOf(fragmentIndex) + " / " + String.valueOf(numberOfPages));
        //fragment index starts from zero
        if (fragmentIndex == numberOfPages - 1) {
            Intent intent = new Intent(ReadArticleActivity.this, SavePopupActivity.class);
            progressSaved = false;
            intent.putExtra("saveProgressQuery", "You reached the end of the article do you want to save?");
            intent.putExtra("isLastPage", true);
            intent.putExtra("numberOfPages", numberOfPages);
            startActivity(intent);
        }
        //show SavePopupActivity if the user did not save when exiting
        else if (!isProgressSaved()) {
            Intent intent = new Intent(ReadArticleActivity.this, SavePopupActivity.class);
            intent.putExtra("saveProgressQuery", "Save Progress?");
            intent.putExtra("isLastPage", false);
            startActivity(intent);
        }

        Long endTime = System.currentTimeMillis();         // updates user data with time spent
        Long timeSpent = endTime - startTime;
        UserDataCollection.setTimeSpentOnArticle(currentArticle, timeSpent);
    }

    protected void OnResume() {
        super.onResume();
        //update the userDictionary
        ReadArticleActivity.getReadArticleActivityInstance().userDictionary = new DataStorage(getApplicationContext()).loadUserDictionary();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textToSpeech.shutdown();
    }

    public static ReadArticleActivity getReadArticleActivityInstance() {
        return instance;
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(ReadArticleActivity.getReadArticleActivityInstance(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(LanguageSpeak);
                }
            }
        });
    }

    /*
    *this method access the color/speak sharedPreferences to get user
    *text color choice or true for default
     */
    private void getUserSettings() {
        sharedPreferences = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        GEARClickableSpan.colorChoice = sharedPreferences.getBoolean("color", true);
        GEARClickableSpan.speakChoice = sharedPreferences.getBoolean("speak", true);
    }

    /*
    *this method remove the last clicked word and update the view
    * keeps track of undoClicks
    * remove the last clicked word from  ListLastClickedWords and MaximumUndoClicks
     */
    private void Undo() {
        load();
        DataStorage dataStorage = new DataStorage(this);
        userDictionary = dataStorage.loadUserDictionary();
        try {
            dataStorage.deleteFromWordFile(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0), "None", dataStorage.USERDICTIONARY, currentArticle, true);
            currentSessionWords.remove(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0));
            ListLastClickedWords = GEARGlobal.ListLastClickedWords;
            pagesView.getAdapter().notifyDataSetChanged();
            userDictionary = dataStorage.loadUserDictionary();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (UndoClicks >= 3) {
            if (MaximumUndoClicks > 0) {
                MaximumUndoClicks--;
                int l1size = Integer.valueOf(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(1));
                int l2size = Integer.valueOf(MaximumLastClickedWords.get(MaximumLastClickedWords.size() - 1).get(1));
                if (l1size - l2size == 0) {
                    int listSize = ListLastClickedWords.size();
                    ListLastClickedWords.remove(listSize - 1);
                    load();
                    listSize = MaximumLastClickedWords.size();
                    MaximumLastClickedWords.remove(listSize - 1);
                    load();
                    listSize = MaximumLastClickedWords.size();
                    GEARGlobal.setLastWordClickedIndex(Integer.valueOf(MaximumLastClickedWords.get(listSize - 1).get(1)));
                    GEARGlobal.setLastWordClicked(MaximumLastClickedWords.get(listSize - 1).get(0));
                } else {
                    ListLastClickedWords.remove(GEARGlobal.ListLastClickedWords.size() - 1);
                    load();
                }

                if (MaximumUndoClicks == 0) {
                    menu.getItem(1).setEnabled(false);
                    MaximumUndoClicks = 2;
                }
            }
        }
        //if clicked for the first one/two times
        else {
            if (ListLastClickedWords.size() == 1) {
                UndoClicks = 0;
                GEARGlobal.setLastWordClickedIndex(-1);
                GEARGlobal.setLastWordClicked("None");
                menu.getItem(1).setEnabled(false);
                ListLastClickedWords.remove(ListLastClickedWords.size() - 1);
                MaximumLastClickedWords.remove(MaximumLastClickedWords.size() - 1);
                load();
                progressSaved=true;
            } else {
                int l1size = Integer.valueOf(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(1));
                int l2size = Integer.valueOf(MaximumLastClickedWords.get(MaximumLastClickedWords.size() - 1).get(1));
                if (l1size - l2size == 0) {
                    ListLastClickedWords.remove(ListLastClickedWords.size() - 1);
                    MaximumLastClickedWords.remove(MaximumLastClickedWords.size() - 1);
                    load();
                    GEARGlobal.setLastWordClickedIndex(Integer.valueOf(MaximumLastClickedWords.get(MaximumLastClickedWords.size() - 1).get(1)));
                    GEARGlobal.setLastWordClicked(MaximumLastClickedWords.get(MaximumLastClickedWords.size() - 1).get(0));
                } else {
                    ListLastClickedWords.remove(ListLastClickedWords.size() - 1);
                    load();
                }
                UndoClicks = 1;
            }
        }
    }
    private void load(){
        ListLastClickedWords = GEARGlobal.ListLastClickedWords;
        MaximumLastClickedWords = GEARGlobal.MaximumLastClickedWords;
    }
}
