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
    //private DefinitionRequest currentDefinitionRequest;
    //create object to translate words
    private Translator currentDefinitionRequest;
    private Context context =
			ReadArticleActivity.getReadArticleActivityInstance().getApplicationContext();
    private static HashMap<String,Word> userDictionary =
			ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
    static View clearWidget;
    private Integer index;
    public static boolean colorChoice;
    public static boolean speakChoice;
    private ReadArticleActivity readArticleActivity =
			ReadArticleActivity.getReadArticleActivityInstance();
    private static String lemma;

    public GEARClickableSpan(String word) {
        mWord = word;
    }

    @Override
    public void onClick(View widget) {
        StoriesSelectionActivity.needsToScore=true;
        Log.w(TAG,"onClickIndex: "+index.toString());
        readArticleActivity.pagesView.getAdapter().notifyDataSetChanged();         //update the view for all the preloaded fragments (max 3)
        readArticleActivity.setProgressSaved(false);                               //set progressSaved to false to popup the savePopupActivity in case user did not save
        //readArticleActivity.menu.getItem(0).setEnabled(true);                      //enable Undo menu option
		readArticleActivity.UndoView.setTextColor
				(readArticleActivity.getResources().getColor(R.color.table_header_text));
		readArticleActivity.UndoClicks++;
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
        HashMap<String, Word> userDictionary =
				ReadArticleActivity.getReadArticleActivityInstance().userDictionary; 						//access the latest userDictionary
        ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();        //creating ReadArticleActivity instance to access the currentSessionWords
        if (colorChoice) {         //if the color choice is true(color switch is on)
            if(activityInstance.currentSessionWords.containsKey(mWord)){    //if the word in the currentSessionWords word ( clicked in the current session )
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
                    //if(!readArticleActivity.stillInSameSession)     										//checks if we are in same session, do not color passed word
                    ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
							.getColor(R.color.passed_word));
                    //else
                      // ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.default_word));
                }
            }else{                                                  										//else color the rest of the word with the default color
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
						.getColor(R.color.default_word));
            }
			if((MainActivity.WordToColor.containsKey(mWord)&&MainActivity.WordToColor.get(mWord))){
				ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
						.getColor(R.color.clicked_word));
				ds.bgColor=(ReadArticleActivity.getReadArticleActivityInstance().getResources()
						.getColor(R.color.clicked_word_background));
			}
			if((MainActivity.WordToColor.containsKey(mWord)&&!MainActivity.WordToColor.get(mWord))){
				ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
						.getColor(R.color.clicked_word));
			}
        }
        else{                                                       										//if the user turned off the text coloring color with the default color
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
					.getColor(R.color.default_word));
        }
    }

    public void color(View widget) {
        updateDrawState(textPaint);
        widget.invalidate();
    }

/*    public static void clear() {
        if (clearWidget != null) {
            //clearing the current session words
            ReadArticleActivity.getReadArticleActivityInstance().currentSessionWords.clear();
            userDictionary.clear();
            clearWidget.invalidate();
            ReadArticleActivity.getReadArticleActivityInstance().pagesView.getAdapter().notifyDataSetChanged();
            Log.d("Clear","View updated");
        }
        //clearing the last word clicked
        GEARGlobal.setLastWordClickedIndex(-1);
        GEARGlobal.setLastWordClicked("None");
        //setting the saveProgress to true (do not show the SavePopupActivity)
        ReadArticleActivity.getReadArticleActivityInstance().setProgressSaved(true);
    }*/

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
    * */
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
    * */

    public void speakWord(){
        if(speakChoice) {
            readArticleActivity.textToSpeech.speak(mWord, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /*
    * this method call translator class to translate the clicked word  and sets its lemma
    * if the translator return empty lemma, set it to None
    * */

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

	public void SetWordToSave(){
		if (readArticleActivity.currentSessionWords.containsKey(mWord)) {
			Integer count = readArticleActivity.currentSessionWords.get(mWord);
			count += 1;
			readArticleActivity.currentSessionWords.put(mWord, count);
			SaveWord(mWord,"None");
			return;
		}else{
			if(Character.isUpperCase(mWord.charAt(0))){
				if (readArticleActivity.currentSessionWords.containsKey(mWord.toLowerCase())) {
					Integer count = readArticleActivity.currentSessionWords.get(mWord.toLowerCase());
					count += 1;
					readArticleActivity.currentSessionWords.put(mWord.toLowerCase(), count);
					MainActivity.WordToColor.put(mWord,true);
					SaveWord(mWord.toLowerCase(),"None");
					return;
				}else{
					readArticleActivity.currentSessionWords.put(mWord, 1);
					MainActivity.WordToColor.put(mWord.toLowerCase(),true);
					SaveWord(mWord,"None");
					return;
				}
			}else if(Character.isLowerCase(mWord.charAt(0))){
				Character first = Character.toUpperCase(mWord.charAt(0));
				String WordToCheck = first+mWord.substring(1);
				if (readArticleActivity.currentSessionWords.containsKey(WordToCheck)){
					Integer count = readArticleActivity.currentSessionWords.get(WordToCheck);
					count += 1;
					readArticleActivity.currentSessionWords.put(WordToCheck, count);
					MainActivity.WordToColor.put(mWord,true);
					SaveWord(WordToCheck,mWord);
					return;
				}else{
					readArticleActivity.currentSessionWords.put(mWord, 1);
					MainActivity.WordToColor.put(WordToCheck,true);
					SaveWord(mWord,"None");
					return;
				}
			}
		}

	}

	public void SaveWord(String WordToSave, String WordSamll){
		Log.d("currentSession",readArticleActivity.currentSessionWords.toString());
		Log.d("WordToColor",MainActivity.WordToColor.toString());
		DataStorage dataStorage = new DataStorage(context);
		Word wordData = new Word(WordToSave,lemma);
		wordData.setClicked(true);
		wordData.totalWordClicks();
		//get the latest userDictionary to add new words clicked
		userDictionary = readArticleActivity.userDictionary;
		userDictionary.put(WordToSave, wordData);
		try {
			dataStorage.addToUserDictionary(WordToSave, lemma, WordSamll,readArticleActivity.currentArticle, true);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
