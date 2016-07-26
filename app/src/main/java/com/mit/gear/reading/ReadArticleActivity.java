package com.mit.gear.reading;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import com.mit.gear.activities.LiteNewsFragment;
import com.mit.gear.activities.MainActivity;
import com.mit.gear.activities.SavePopupActivity;
import com.mit.gear.activities.StoriesSelectionActivity;
import com.mit.gear.data.DataStorage;
import com.mit.gear.data.UserDataCollection;
import com.mit.gear.words.DefinitionRequest;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.MailSender;
import com.mit.gear.words.Word;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity where user reads article
 */
public class ReadArticleActivity extends AppCompatActivity {
    private String TAG = "ReadArticleActivity";
    private static ReadArticleActivity instance;
    //private android.support.v7.widget.Toolbar toolbar;
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
    public static Boolean copyRightReachedFirstTime ;   //checks if the copy right text is reached for first time
    public static Integer CopyRightFragmentIndex ;       //the fragment index which has the copy right text
    public static boolean stillInSameSession;           //checks if is still in same session, used to not update passed words if user in same session
    public static TextView UndoView;
    public static Set<String> articlesOpened;
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
        stillInSameSession = false;
        copyRightReachedFirstTime = false;
        CopyRightFragmentIndex =-1;
        GEARGlobal.ListLastClickedWords.clear();
        GEARGlobal.MaximumLastClickedWords.clear();
        GEARGlobal.resetWordIndex();
        GEARGlobal.setLastWordClickedIndex(-1);                   //clearing the last word index every time the story loads
        GEARGlobal.setLastWordClicked("None");
        if (definition_scroll) {
            setContentView(R.layout.pages_scrolling_definition);
			UndoView = (TextView)findViewById(R.id.UndotextViewScrolling);
        } else {
            setContentView(R.layout.pages);
			UndoView = (TextView)findViewById(R.id.UndotextView);
        }
   //     toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_article_bar);
     //   setSupportActionBar(toolbar);
        offlineDictionary = GEARGlobal.getOfflineDictionary(getApplicationContext());
        userDictionary = new DataStorage(getApplicationContext()).loadUserDictionary();
        currentDefinitionRequest = null;

//        getSupportActionBar().setDisplayShowTitleEnabled(false);    // Getting rid of title for the action bar
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
                textPaint.setColor(255);
                AssetManager assetManager = getAssets();
                String story = getIntent().getExtras().getString("title");
                String storyContent = getIntent().getExtras().getString("content");

                currentArticle = story;
                if(storyContent==null) {
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
                    storyText=text;
                }
                else{
                    storyText = storyContent;
                }
                pageSplitter.append(storyText, textPaint);
                numberOfPages = pageSplitter.getPages().size(); //getting total number of pages in current atricle
                pagesView.setAdapter(new TextPagerAdapter(getSupportFragmentManager(), pageSplitter.getPages()));
                pagesView.getAdapter().notifyDataSetChanged();
                pagesView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                TextView pageIndicator = (TextView) getReadArticleActivityInstance().findViewById(R.id.pageIndicator);
                pageIndicator.setText("Page " + String.valueOf(1) + " of " + numberOfPages);
            }
        });
        pagesView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                fragmentIndex = position; //update the fragment index to current page position
                TextView pageIndicator = (TextView) getReadArticleActivityInstance().findViewById(R.id.pageIndicator);
                Log.d(TAG, "Page Chnaged to: " + String.valueOf(fragmentIndex + 1));
                pageIndicator.setText("Page " + String.valueOf(fragmentIndex + 1) + " of " + numberOfPages);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
		pagesView.setOffscreenPageLimit(1);         //limiting the preloading to one page per side
        UndoView.setTextColor(getResources().getColor(R.color.passed_word));
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
                dataStorage.addToUserDictionary(word, lemma,"None" ,article, click);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    public void onClickUpPopWindow(View view) {
        Intent intent = new Intent(ReadArticleActivity.this, popUpRateArticle.class);
        startActivity(intent);
//        intent.putExtra("currentArticle", (String) currentUserData.getArticle());
//        intent.putExtra("currentUserId", (String) currentUserData.getUserId());
    }

    public void saveProgress(View view) {
		Log.d(TAG,"Save progress clicked");
        StoriesSelectionActivity.needsToScore=true;
        LiteNewsFragment.needsToScore=true;
        stillInSameSession = true; //used to not color passed word if user in same session
		ResetUndo();
		PrepareProgressBar(view);
        if (progressSaved) {
            Toast.makeText(this, "No progress to save", Toast.LENGTH_LONG).show();
            return;
        } else {
            progress.show();
        }
        setProgressSaved(true);
        Log.d(TAG,"Progress saved on word index " + GEARGlobal.getLastWordClickedIndex().toString());
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
					Character first = Character.toUpperCase(possibleWord.charAt(0));
					String WordToCheck = first + possibleWord.substring(1);
					if (possibleWord.matches(getResources().getString(R.string.endOfArticleIndicator))) { //if copyRight text is reached
						break;
					}
					if (Character.isLetter(possibleWord.charAt(0))) {
						if (count >= GEARGlobal.getLastWordClickedIndex()) {
							break;
						}
						if (currentSessionWords.containsKey(possibleWord) ||
								MainActivity.WordToColor.containsKey(possibleWord)) {
							continue;
						}
						ArrayList<Object> wordArrayList = new ArrayList<>();
						wordArrayList.add("None");
						wordArrayList.add(currentArticle);
						wordArrayList.add(false);
						Integer wordCount = 1;
						if (wordsToSave.containsKey(possibleWord)) {
							wordCount = (Integer) wordsToSave.get(possibleWord).get(3) + 1;
						}
						if (Character.isUpperCase(possibleWord.charAt(0))) {
							if (wordsToSave.containsKey(possibleWord.toLowerCase())) {
								wordCount = (Integer) wordsToSave.get
										(possibleWord.toLowerCase()).get(3) + 1;
							}
						}
						if (Character.isLowerCase(possibleWord.charAt(0))) {
							if (wordsToSave.containsKey(WordToCheck)) {
								wordCount = (Integer) wordsToSave.get
										(WordToCheck).get(3) + 1;
							}
						}
						wordArrayList.add(wordCount);
						if (!currentSessionWords.containsKey(possibleWord)) {
							if (Character.isUpperCase(possibleWord.charAt(0))){
								if (wordsToSave.containsKey(possibleWord.toLowerCase())){
									wordsToSave.put(possibleWord.toLowerCase(), wordArrayList);
								}else{
									wordsToSave.put(possibleWord, wordArrayList);
								}
							}else if(Character.isLowerCase(possibleWord.charAt(0))){
								if (wordsToSave.containsKey(WordToCheck)){
									wordsToSave.remove(WordToCheck);
									wordsToSave.put(possibleWord, wordArrayList);
								}else{
									wordsToSave.put(possibleWord, wordArrayList);
								}
							}
							//wordsToSave.put(possibleWord, wordArrayList);
							newWords += 1;
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

        SaveOpenedArticles();
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
		SendUserDictionary();
        for (Map.Entry<String, Boolean> entry : MainActivity.WordToColor.entrySet()) {
            MainActivity.WordToColor.put(entry.getKey(),false);
        }
        super.onPause();
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
    public void Undo(View view) {
        load();
        DataStorage dataStorage = new DataStorage(this);
		Integer numOfClicks = 0;
        //userDictionary = dataStorage.loadUserDictionary();
		if(UndoView.getCurrentTextColor()==getResources().getColor(R.color.table_header_text)){
			try {
				Log.d("UndoWord",ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0));
				dataStorage.deleteFromWordFile(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0), "None", dataStorage.USERDICTIONARY, currentArticle, true);
                HashMap<String, Word> userDictionaryNEW = dataStorage.loadUserDictionary();

                //check if the undo word is still exist in the new dictionary
                if(userDictionaryNEW.containsKey(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0))){
                    if(!userDictionaryNEW.get(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0)).clicked){
                        userDictionary.get(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0)).setClicked(false);
                    }
                }else{ //if does not exist, delete it from the un updated dictionary
                    userDictionary.remove(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0));
					String WordToDelete = ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0);
					if(Character.isUpperCase(WordToDelete.charAt(0))){
						if(MainActivity.WordToColor.containsKey(WordToDelete.toLowerCase())){
							MainActivity.WordToColor.remove(WordToDelete.toLowerCase());
						}
						if(currentSessionWords.containsKey(WordToDelete)){
							numOfClicks = currentSessionWords.get(WordToDelete);
							Log.d("numOfClicks1", String.valueOf(numOfClicks));
						}else{
							numOfClicks = currentSessionWords.get(WordToDelete.toLowerCase());
							Log.d("numOfClicks2", String.valueOf(numOfClicks));
						}
					}else if(Character.isLowerCase(WordToDelete.charAt(0))) {
						Character first = Character.toUpperCase(WordToDelete.charAt(0));
						WordToDelete = first+WordToDelete.substring(1);
						if (MainActivity.WordToColor.containsKey(WordToDelete)) {
							MainActivity.WordToColor.remove(WordToDelete);
						}
						if(currentSessionWords.containsKey(WordToDelete)){
							numOfClicks = currentSessionWords.get(WordToDelete);
							Log.d("numOfClicks3", String.valueOf(numOfClicks));
						}else{
							numOfClicks = currentSessionWords.get(WordToDelete.toLowerCase());
							Log.d("numOfClicks4", String.valueOf(numOfClicks));
						}
					}
                }
                //check if the word clicked more than once and decrement one or remove it from current session
                if(numOfClicks==1)
                    currentSessionWords.remove(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0));
                else if (numOfClicks>1){
                    numOfClicks--;
                    currentSessionWords.put(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0),numOfClicks);
                }

				ListLastClickedWords = GEARGlobal.ListLastClickedWords;
				Character first = Character.toUpperCase(ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0).charAt(0));
				String cWord = first+ListLastClickedWords.get(ListLastClickedWords.size() - 1).get(0).substring(1);
				pagesView.getAdapter().notifyDataSetChanged();
				//userDictionary = dataStorage.loadUserDictionary();
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
						//menu.getItem(0).setEnabled(false);
						UndoView.setTextColor(getResources().getColor(R.color.passed_word));
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
					//menu.getItem(0).setEnabled(false);
					UndoView.setTextColor(getResources().getColor(R.color.passed_word));
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
		}else {
			Toast.makeText(this, "Cannot undo, only two undoes allowed", Toast.LENGTH_LONG).show();
		}
    }

    private void load(){
        ListLastClickedWords = GEARGlobal.ListLastClickedWords;
        MaximumLastClickedWords = GEARGlobal.MaximumLastClickedWords;
    }

	/*
	 * Method to generate vocabulary string by reading user dictionary
	 */
    private String getVocabularyString() {
        DataStorage dataStorage = new DataStorage(getApplicationContext());
        HashMap<String, Word> vocabulary = dataStorage.loadUserDictionary();
        String vocabString = "";
        if (vocabulary.isEmpty()) {
            vocabString = " ";
        }
        //Loop through user dictionary and list vocabulary words
        for (Map.Entry<String, Word> entry : vocabulary.entrySet()) {
            String key = entry.getKey();
            Word word = entry.getValue();
            if(word.getLemma().equals("None"))
                vocabString += key +", Clicked: " + Integer.toString(word.totalWordClicks()) +
						", Passed: " + Integer.toString(word.totalWordPasses()) + "\n";
            else
                vocabString += key + ", "+word.getLemma()+", Clicked: "
						+ Integer.toString(word.totalWordClicks()) +
						", Passed: " + Integer.toString(word.totalWordPasses()) + "\n";
        }
        return vocabString;
    }

	/*
	 * Method to get device id and serial number
	 * Call MailSender to send data
	 */
    private void SendUserDictionary(){
        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),  	//Get device id
                Settings.Secure.ANDROID_ID);
        String android_serial = Build.SERIAL;                                                          //Get device serial nember
        String message = "Device ID: "+android_id+"\t\t\tDevice Serial Number: "+android_serial +"\n"; //Prepare mail content to send
        message += getVocabularyString();
		if(getVocabularyString().equals(" ")){                     										//If Vocabulary list is empty
			return;
		}
		//Create new mail sender object and set sender mail and password, receiver mail, message to send
        try {
            MailSender sender = new MailSender("gearmit16@gmail.com", "GEARmit2016");
            sender.sendMail("UserDictionary "+android_id, message, "gearmit16@gmail.com", "gearmit16@gmail.com");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }

    /*
     * This method saves the open articles set into shared prefrence
     * This method is called onDestroy ReadArticleActivity
     */
    private void SaveOpenedArticles(){
        sharedPreferences = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.putStringSet("openedArticles", articlesOpened);
        editor.commit();
    }

	private void ResetUndo(){
		GEARGlobal.ListLastClickedWords.clear();
		UndoView.setTextColor(getResources().getColor(R.color.table_header));
		MaximumUndoClicks = 2;
		UndoClicks = 0;
	}

	private void PrepareProgressBar(View view){
		progress = new ProgressDialog(view.getContext());
		progress.setMessage("Saving Progress");
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setProgress(0);
		progress.setCancelable(false);
		//setting the progressDialog to the last clicked word index
		progress.setMax(GEARGlobal.getLastWordClickedIndex());

	}

}
