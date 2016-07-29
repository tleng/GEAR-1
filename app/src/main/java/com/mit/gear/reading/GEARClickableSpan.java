package com.mit.gear.reading;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mattmellor.gear.R;
import com.mit.gear.activities.LiteNewsFragment;
import com.mit.gear.activities.MainActivity;
import com.mit.gear.activities.StoriesSelectionActivity;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.DefinitionRequest;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.Translator;
import com.mit.gear.words.Word;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Michael on 4/6/16.
 */
public class GEARClickableSpan extends ClickableSpan {
	private String TAG = "GEARClickableSpan";
	final String mWord;
    private TextPaint textPaint;
    private Translator currentDefinitionRequest;						//Create object to translate words
    private Context context =
			ReadArticleActivity.getReadArticleActivityInstance().getApplicationContext();
    private static HashMap<String,Word> userDictionary =
			ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
    static View clearWidget;
    private Integer index;
    public static boolean colorChoice;
    public static boolean speakChoice;
    private ReadArticleActivity readArticleActivity =
			ReadArticleActivity.getReadArticleActivityInstance();		//Creating ReadArticleActivity instance to access variable and objects
    private static String lemma;
	DataStorage dataStorage = new DataStorage(context);

    public GEARClickableSpan(String word) {
        mWord = word;
    }

    @Override
    public void onClick(View widget) {
		UpdateFlags();
        updateLastClickedWord();
        if (currentDefinitionRequest != null) {
            currentDefinitionRequest.cancel(true);
        }
        clearWidget = widget;
        Log.d(TAG,"tapped on: "+ mWord);
        ArrayList<String> time_place_holder = new ArrayList<>();
        time_place_holder.add("0");
        translate();
		SetWordToSave();
        if (textPaint != null) {
            color(widget);
        }
        speakWord(); //speak the word when clicked
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setUnderlineText(false);
		if (colorChoice) {         //if the color choice is true(color switch is on)
			ColorWordInDictionary(ds);
			ColorWordInColorFile(ds);
        }else{                     //if the user turned off the text coloring color with the default color
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
					.getColor(R.color.default_word));
        }
    }

    public void color(View widget) {
        updateDrawState(textPaint);
        widget.invalidate();
    }

    public void setIndex(Integer i) {
        index = i;
    }

    public Integer getIndex() {
        if (index != null) {
            return index;
        } else {
            return -1;
        }
    }

    /*
     * this method updates the last clicked word to the maximum index of  clicked words
     * add the index to the MaximumLastClickedWords if it was set as the LastWordClickedIndex
     * add the new clicked word to ListLastClickedWords
     * the MaximumLastClickedWords list keeps track of the indexes that was set as the last clicked words index
     * the ListLastClickedWords list keeps track of the last 3 clicked words
     */
    public void updateLastClickedWord(){
        if(GEARGlobal.getLastWordClickedIndex()<=index) {
            GEARGlobal.setLastWordClickedIndex(index);
            GEARGlobal.setLastWordClicked(mWord);
            ArrayList<String> word = new ArrayList<String>();
            word.add(mWord);
            word.add(String.valueOf(index));
            GEARGlobal.MaximumLastClickedWords.add(word);
        }
        if (GEARGlobal.ListLastClickedWords.size() < GEARGlobal.undoThreshold){
            ArrayList<String> word = new ArrayList<String>();
            word.add(mWord);
            word.add(String.valueOf(index));
            GEARGlobal.ListLastClickedWords.add(word);
        }
        else {
            GEARGlobal.ListLastClickedWords.remove(0);
            ArrayList<String> word = new ArrayList<String>();
            word.add(mWord);
            word.add(String.valueOf(index));
            GEARGlobal.ListLastClickedWords.add(word);
        }
		Log.d("MaximumLastClicked",GEARGlobal.MaximumLastClickedWords.toString());
		Log.d("ListLastClicked",GEARGlobal.ListLastClickedWords.toString());
    }

    /*
     * this method speak the clicked word if the speack option is on
     */
    public void speakWord(){
        if(speakChoice) {
            readArticleActivity.textToSpeech.speak(mWord, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /*
     * this method call translator class to translate the clicked word  and sets its lemma
     * if the translator return empty lemma, set it to None
     */
    public void translate(){
        try {
            currentDefinitionRequest=  new Translator(mWord);
            lemma= currentDefinitionRequest.execute().get();
            if(lemma.isEmpty()){
                lemma="None";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/*
	 * Method to check if word is clicked in current session or not and update count
	 * Check word first letter case and update accordingly
	 */
	public void SetWordToSave(){
		DataStorage dataStorage = new DataStorage(context);
		//If word clicked contained in current session update count (Number of clicks)
		if (readArticleActivity.currentSessionWords.containsKey(mWord)) {
			Integer count = readArticleActivity.currentSessionWords.get(mWord);
			count += 1;
			readArticleActivity.currentSessionWords.put(mWord, count);
			Log.d("SetWordToSave","Word in currentSessionWords update count");
			Log.d("SetWordToSave1",readArticleActivity.currentSessionWords.toString());
			SaveWord(mWord,"None");
			return;
		}
		//If word not in current session check word first letter case
		else{
			//If word first letter is upper case
			if(Character.isUpperCase(mWord.charAt(0))){
				Log.d("SetWordToSave","Word is in upper case");
				//Check if word in lower case form is in current session update count (Number of clicks)
				if (readArticleActivity.currentSessionWords.containsKey(mWord.toLowerCase())) {
					Integer count = readArticleActivity.currentSessionWords.get(mWord.toLowerCase());
					count += 1;
					readArticleActivity.currentSessionWords.put(mWord.toLowerCase(), count);
					Log.d("SetWordToSave","Word small version is in currentSessionWords update count");
					Log.d("SetWordToSave2",readArticleActivity.currentSessionWords.toString());
					try {
						dataStorage.addToColorFile(mWord,true);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					SaveWord(mWord.toLowerCase(),"None");
					return;
				}
				//Both lower and upper case not in current session word is new
				else{
					Log.d("SetWordToSave","Word is not in currentSessionWords");
					readArticleActivity.currentSessionWords.put(mWord, 1);
					Log.d("SetWordToSave3",readArticleActivity.currentSessionWords.toString());
					try {
						dataStorage.addToColorFile(mWord.toLowerCase(),true);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					SaveWord(mWord,"None");
					return;
				}
			}
			//If word first letter is lower case
			else if(Character.isLowerCase(mWord.charAt(0))){
				Log.d("SetWordToSave","Word is in lower case");
				Character first = Character.toUpperCase(mWord.charAt(0));
				String WordToCheck = first+mWord.substring(1);
				//Check if word in upper case form is in current session update count (Number of clicks)
				if (readArticleActivity.currentSessionWords.containsKey(WordToCheck)){
					Log.d("SetWordToSave","Word capital version is in currentSessionWords update count");
					Integer count = readArticleActivity.currentSessionWords.get(WordToCheck);
					count += 1;
					readArticleActivity.currentSessionWords.put(WordToCheck, count);
					Log.d("SetWordToSave4",readArticleActivity.currentSessionWords.toString());
					try {
						dataStorage.addToColorFile(mWord,true);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					SaveWord(WordToCheck,mWord);
					return;
				}
				//Both lower and upper case not in current session word is new
				else{
					Log.d("SetWordToSave","Word is not in currentSessionWords");
					readArticleActivity.currentSessionWords.put(mWord, 1);
					Log.d("SetWordToSave5",readArticleActivity.currentSessionWords.toString());
					try {
						dataStorage.addToColorFile(WordToCheck,true);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					SaveWord(mWord,"None");
					return;
				}
			}
		}

	}


	/*
	 * Method to create new word object and add word to dictionary
	 * WordSmall will be set if small is clicked and capital exist in dictionary
	 */
	public void SaveWord(String WordToSave, String WordSmall){
		Log.d("SaveWord","WordToSave = "+WordToSave+" WordSmall = "+WordSmall);
		Log.d("currentSession",readArticleActivity.currentSessionWords.toString());
		Word wordData = new Word(WordToSave,lemma);
		wordData.setClicked(true);
		//If WordSmall is set get the count of the capital, remove the capital and add the small with same count
		if(!WordSmall.equals("None")){
			Integer count = readArticleActivity.currentSessionWords.get(WordToSave);
			readArticleActivity.currentSessionWords.remove(WordToSave);
			readArticleActivity.currentSessionWords.put(WordSmall, count);
			try {
				dataStorage.deleteFromColorFile(WordSmall);
				dataStorage.addToColorFile(WordToSave,true);			//Add the capital to the coloring file
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.d("currentSession2",readArticleActivity.currentSessionWords.toString());
		userDictionary = readArticleActivity.userDictionary;
		Log.d("userDictionary1",userDictionary.toString());
		try {
			dataStorage.addToUserDictionary(WordToSave, lemma, WordSmall,readArticleActivity.currentArticle, true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Method to update flags
	 */
	private void UpdateFlags(){
		StoriesSelectionActivity.needsToScore=true;
		LiteNewsFragment.needsToScore=true;
		readArticleActivity.pagesView.getAdapter().notifyDataSetChanged();         //Update the view for all the preloaded fragments (max 3)
		readArticleActivity.setProgressSaved(false);                               //Set progressSaved to false to popup the savePopupActivity in case user did not save
		readArticleActivity.UndoView.setTextColor
				(readArticleActivity.getResources().getColor(R.color.table_header_text));
		readArticleActivity.UndoClicks++;
	}

	/*
	 * Method to color words in current session and in user dictionary
	 */
	private void ColorWordInDictionary(TextPaint ds){
		HashMap<String, Word> userDictionary =
				ReadArticleActivity.getReadArticleActivityInstance().userDictionary; 						//Access the latest userDictionary
		if(readArticleActivity.currentSessionWords.containsKey(mWord)){    //if the word in the currentSessionWords word ( clicked in the current session )
			ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
					.getColor(R.color.clicked_word));
			ds.bgColor=(ReadArticleActivity.getReadArticleActivityInstance().getResources()
					.getColor(R.color.clicked_word_background));
		}
		else if(userDictionary.containsKey(mWord)){             										//if the word in the userDictionary check if it is clicked or passed
			if(userDictionary.get(mWord).clicked)
				ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
						.getColor(R.color.clicked_word));
			else{
				ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
						.getColor(R.color.passed_word));
			}
		}else{                                                  										//else color the rest of the word with the default color
			ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
					.getColor(R.color.default_word));
		}
	}

	/*
	 * Method to color words in color file
	 */
	private void ColorWordInColorFile(TextPaint ds){
		HashMap<String, Word> userDictionary =
				ReadArticleActivity.getReadArticleActivityInstance().userDictionary; 						//Access the latest userDictionary
		HashMap<String, Boolean> WordToColor = dataStorage.loadColorFile();									//Access the latest file that contains the word to color
		if((WordToColor.containsKey(mWord)&&WordToColor.get(mWord))){
			if(Character.isUpperCase(mWord.charAt(0))){
				if (readArticleActivity.currentSessionWords.containsKey(mWord.toLowerCase())){
					ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
							.getColor(R.color.clicked_word));
					ds.bgColor=(ReadArticleActivity.getReadArticleActivityInstance().getResources()
							.getColor(R.color.clicked_word_background));
				}else if (userDictionary.containsKey(mWord.toLowerCase())){
					ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
							.getColor(R.color.clicked_word));
				}
			}
			if (Character.isLowerCase(mWord.charAt(0))){
				Character first = Character.toUpperCase(mWord.charAt(0));
				String cWord = first+mWord.substring(1);
				if (readArticleActivity.currentSessionWords.containsKey(cWord)){
					ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
							.getColor(R.color.clicked_word));
					ds.bgColor=(ReadArticleActivity.getReadArticleActivityInstance().getResources()
							.getColor(R.color.clicked_word_background));
				}else if (userDictionary.containsKey(cWord)){
					ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
							.getColor(R.color.clicked_word));
				}
			}
		}
		if((WordToColor.containsKey(mWord)&&!WordToColor.get(mWord))){
			if(Character.isUpperCase(mWord.charAt(0))){
				if (userDictionary.containsKey(mWord.toLowerCase())){
					if(userDictionary.get(mWord.toLowerCase()).clicked){
						ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
								.getColor(R.color.clicked_word));
					}else {
						ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
								.getColor(R.color.passed_word));
					}
				}
			}
			if (Character.isLowerCase(mWord.charAt(0))){
				Character first = Character.toUpperCase(mWord.charAt(0));
				String cWord = first+mWord.substring(1);
				if (userDictionary.containsKey(cWord)){
					if(userDictionary.get(cWord).clicked){
						ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
								.getColor(R.color.clicked_word));
					}else {
						ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
								.getColor(R.color.passed_word));
					}
				}
			}
		}

	}

}
