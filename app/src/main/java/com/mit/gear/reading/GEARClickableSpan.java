package com.mit.gear.reading;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import com.mattmellor.gear.R;
import com.mit.gear.activities.LiteNewsFragment;
import com.mit.gear.activities.StoriesSelectionActivity;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.GEARGlobal;
import com.mit.gear.words.Translator;
import com.mit.gear.words.Word;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

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
    private static String lemma = "None";
	DataStorage dataStorage = new DataStorage(context);

    public GEARClickableSpan(String word) {
        mWord = word;
    }


    @Override
    public void onClick(View widget) {
        readArticleActivity.UndoView.setEnabled(false);     //disable the undo button unless all work of onclick finishes
        if (currentDefinitionRequest != null) {
            currentDefinitionRequest.cancel(true);
        }
        clearWidget = widget;
        Log.d(TAG, "tapped on: " + mWord);
        ArrayList<String> time_place_holder = new ArrayList<>();
        time_place_holder.add("0");
       final Integer caseNo =  SetWordToSave();

        if (textPaint != null) {
            color(widget);
        }
        lemma = translate();
        new UpdateClickedWordData().execute(caseNo.toString(),lemma);


    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setUnderlineText(false);
		if (colorChoice) {         //if the color choice is true(color switch is on)
			ColorWordInDictionary(ds);
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

    }

    /*
     * this method speak the clicked word if the speak option is on
     */
    public void speakWord(){
        if(speakChoice) {
            readArticleActivity.textToSpeech.speak(mWord, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /*
     * This method call translator class to translate the clicked word and sets its lemma
     * if the translator return empty lemma, set it to None
     */
    public String translate(){
        try {
            currentDefinitionRequest=  new Translator(mWord);
            lemma= currentDefinitionRequest.execute().get();
            if(lemma.isEmpty()){
                lemma="None";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lemma;
    }

	/*
	 * Method to check if clicked word is in current session or not and update count
	 * Check word first letter case and update accordingly
	 */
	public Integer SetWordToSave(){
		//If word clicked contained in current session update count (Number of clicks)
		if (readArticleActivity.currentSessionWords.containsKey(mWord)) {
			Integer count = readArticleActivity.currentSessionWords.get(mWord);
			count += 1;
			readArticleActivity.currentSessionWords.put(mWord, count);
			return 0;
		}
		//If word not in current session check word first letter case
		else{
			//If word first letter is upper case
			if(Character.isUpperCase(mWord.charAt(0))){
				//Check if word in lower case form is in current session update count (Number of clicks)
				if (readArticleActivity.currentSessionWords.containsKey(mWord.toLowerCase())) {
					Integer count = readArticleActivity.currentSessionWords.get(mWord.toLowerCase());
					count += 1;
					readArticleActivity.currentSessionWords.put(mWord.toLowerCase(), count);
					return 1;
				}
				//Both lower and upper case not in current session word is new
				else{
					readArticleActivity.currentSessionWords.put(mWord, 1);
					return 2;
				}
			}
			//If word first letter is lower case
			else {
				Character first = Character.toUpperCase(mWord.charAt(0));
				String WordToCheck = first+mWord.substring(1);
				//Check if word in upper case form is in current session update count (Number of clicks)
				if (readArticleActivity.currentSessionWords.containsKey(WordToCheck)){
					Integer count = readArticleActivity.currentSessionWords.get(WordToCheck);
					count += 1;
					readArticleActivity.currentSessionWords.put(WordToCheck, count);
					return 3;
				}
				//Both lower and upper case not in current session word is new
				else{
					readArticleActivity.currentSessionWords.put(mWord, 1);
					return 4;
				}
			}
		}
	}

    /*
     * This method saves the clicked word in dictionary based of the case number from SetWordToSave method
     * there are 3 cases either save the same word, save the other version or replace the word with lowercase version
     *	This method will call SaveWordToDictionary with the appropriate parameters
     */
    private void SaveWordToDictionarySwitch(Integer caseNo,String lemma){
        switch (caseNo){
            case 0:
            case 2:
            case 4:
                SaveWordToDictionary(mWord, "None", lemma);
                break;
            case 1:
                SaveWordToDictionary(mWord.toLowerCase(), "None", lemma);
                break;

            case 3:
                Character first = Character.toUpperCase(mWord.charAt(0));
                String WordToCheck = first+mWord.substring(1);
                SaveWordToDictionary(WordToCheck, mWord, lemma);
                break;

        }
    }

	/*
	 * Method to create new word object and add word to dictionary
	 * WordSmall will be set if small is clicked and capital exist in dictionary
	 */
	public void SaveWordToDictionary(String WordToSave, String WordSmall, String lemma){
		Word wordData = new Word(WordToSave,lemma);
		wordData.setClicked(true);
		//If WordSmall is set get the count of the capital, remove the capital and add the small with same count
		if(!WordSmall.equals("None")){
			Integer count = readArticleActivity.currentSessionWords.get(WordToSave);
			readArticleActivity.currentSessionWords.remove(WordToSave);
			readArticleActivity.currentSessionWords.put(WordSmall, count);

		}
		userDictionary = readArticleActivity.userDictionary;
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
		readArticleActivity.setProgressSaved(false);                               //Set progressSaved to false to popup the savePopupActivity in case user did not save
		readArticleActivity.UndoClicks++;
	}

    /*
     * Method to color words in current session and in user dictionary
     */
    private void ColorWordInDictionary(TextPaint ds){
        HashMap<String, Word> userDictionary =
                ReadArticleActivity.getReadArticleActivityInstance().userDictionary; 						//Access the latest userDictionary
        String uppercaseWord = mWord.toUpperCase().charAt(0)+mWord.substring(1);
        String lowerCaseWord = mWord.toLowerCase();
        //if the word in the currentSessionWords word ( clicked in the current session )
        if(readArticleActivity.currentSessionWords.containsKey(lowerCaseWord)
				||readArticleActivity.currentSessionWords.containsKey(uppercaseWord)) {
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
                    .getColor(R.color.clicked_word));
            ds.bgColor=(ReadArticleActivity.getReadArticleActivityInstance().getResources()
                    .getColor(R.color.clicked_word_background));
        }
        //if the lowercase word in the userDictionary check if it is clicked or passed
        else if(userDictionary.containsKey(lowerCaseWord)){
            if(userDictionary.get(lowerCaseWord).clicked)
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
                        .getColor(R.color.clicked_word));
            else{
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
                        .getColor(R.color.passed_word));
            }

        }
        //if the uppercase word in the userDictionary check if it is clicked or passed
        else if(userDictionary.containsKey(uppercaseWord)){
            if(userDictionary.get(uppercaseWord).clicked)
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
                        .getColor(R.color.clicked_word));
            else{
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
                        .getColor(R.color.passed_word));
            }

        }
        //else color the rest of the word with the default color
        else{
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources()
                    .getColor(R.color.default_word));
        }
    }

	/*
	 * This method shows for the user when clicking on a word for the first time.
	 * it highlights the undo button with a proper guide message
	 * This will show only once in the application.
	 */
	private void showUserManual(){
		new MaterialShowcaseView.Builder(readArticleActivity)
				.setTarget(ReadArticleActivity.UndoView)
				.setShapePadding(96)
                .setTitleText(readArticleActivity.getResources().getString(R.string.UserManualTitle))
                .setDismissText(readArticleActivity.getResources().getString(R.string.UserManualDismissText))
                .setContentText(readArticleActivity.getResources().getString(R.string.UserManualUndoContent))
				.singleUse("undoButton")
				.setMaskColour(readArticleActivity.getResources().getColor(R.color.manualBackground))
				.setFadeDuration(300)
				.show();
	}

    /*
     * This asyncTask will run the necessary method after a word is clicked
     * When done, the undo button will be enabled again (will be disabled when a word is clicked until the task finishes)
     * This asyncTask were separated from onclick method for the huge load on main thread when onclick method run
     */
    private class UpdateClickedWordData extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... caseNoAndLemma) {
            UpdateFlags();
            updateLastClickedWord();
            speakWord();
            return caseNoAndLemma[0]+caseNoAndLemma[1] ;
        }

        protected void onPostExecute(String caseNoAndLemma) {
            readArticleActivity.UndoView.setTextColor
                    (readArticleActivity.getResources().getColor(R.color.table_header_text));
            readArticleActivity.UndoView.setEnabled(true);

            SaveWordToDictionarySwitch(Integer.parseInt(caseNoAndLemma.substring(0, 1)), caseNoAndLemma.substring(1));
            readArticleActivity.pagesView.getAdapter().notifyDataSetChanged();         //Update the view for all the preloaded fragments (max 3)
            showUserManual();
        }
    }
}
