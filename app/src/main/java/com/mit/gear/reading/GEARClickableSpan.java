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

import com.mattmellor.gear.R;
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
    final String mWord;
    private TextPaint textPaint;
    //private DefinitionRequest currentDefinitionRequest;
    //create object to translate words
    private Translator currentDefinitionRequest;
    private Context context = ReadArticleActivity.getReadArticleActivityInstance().getApplicationContext();
    private static HashMap<String,Word> userDictionary = ReadArticleActivity.getReadArticleActivityInstance().userDictionary;
    static View clearWidget;
    private Integer index;
    public static boolean colorChoice;
    public static boolean speakChoice;
    private ReadArticleActivity readArticleActivity =ReadArticleActivity.getReadArticleActivityInstance();
    private static String lemma;

    public GEARClickableSpan(String word) {
        mWord = word;
    }

    @Override
    public void onClick(View widget) {
        readArticleActivity.pagesView.getAdapter().notifyDataSetChanged();         //update the view for all the preloaded fragments (max 3)
        readArticleActivity.setProgressSaved(false);                               //set progressSaved to false to popup the savePopupActivity in case user did not save
        readArticleActivity.menu.getItem(1).setEnabled(true);                      //enable Undo menu option
        readArticleActivity.UndoClicks++;

        updateLastClickedWord();

        if (currentDefinitionRequest != null) {
            Log.d("Cancel", "Cancel current definition request: " + currentDefinitionRequest.toString());
            currentDefinitionRequest.cancel(true);
        }
        Log.d("View1", clearWidget.toString());
        clearWidget = widget;
        Log.d("View2", clearWidget.toString());

        Log.d("No cancel", "current definition request not cancelled");
        Log.d("tapped on:", mWord);
        ArrayList<String> time_place_holder = new ArrayList<>();
        time_place_holder.add("0");

        translate();

        Word wordData = new Word(mWord,lemma);
        wordData.setClicked(true);
        //get the latest userDictionary to add new words clicked
        userDictionary = readArticleActivity.userDictionary;
        userDictionary.put(mWord, wordData);
        DataStorage dataStorage = new DataStorage(context);
        if (readArticleActivity.currentSessionWords.containsKey(mWord)) {
            Integer count = readArticleActivity.currentSessionWords.get(mWord);
            count += 1;
            readArticleActivity.currentSessionWords.put(mWord, count);
        } else {
            readArticleActivity.currentSessionWords.put(mWord, 1);
        }
        try {
            dataStorage.addToUserDictionary(mWord, lemma, readArticleActivity.currentArticle, true);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textPaint != null) {
            color(widget);
        }
        speakWord(); //speak the word when clicked

        /*
        CharSequence message = mWord + " ausgewählt.";
        TextView definitionBox = (TextView) readArticleActivity.findViewById(R.id.definition_box);
        if (readArticleActivity.definition_scroll) {
            // do nothing to the definitionBox
        } else {
            definitionBox.setText(message);
        }
        DefinitionRequest definitionRequest = new DefinitionRequest(mWord);
        currentDefinitionRequest = definitionRequest;
        definitionRequest.execute(mWord);
        */
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setUnderlineText(false);
        HashMap<String, Word> userDictionary = ReadArticleActivity.getReadArticleActivityInstance().userDictionary; //access the latest userDictionary
        ReadArticleActivity activityInstance = ReadArticleActivity.getReadArticleActivityInstance();         //creating ReadArticleActivity instance to access the currentSessionWords
        if (colorChoice) {         //if the color choice is true(color switch is on)

            if( activityInstance.currentSessionWords.containsKey(mWord)){       //if the word in the currentSessionWords word ( clicked in the current session )
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.clicked_word));
                ds.bgColor=(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.clicked_word_background));
            }
            else if(userDictionary.containsKey(mWord)){             //if the word in the userDictionary check if it is clicked or passed
                if(userDictionary.get(mWord).clicked)
                    ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.clicked_word));
                else{
                    ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.passed_word));
                }
            }else{                                                  //else color the rest of the word with the default color
                ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.default_word));
            }
        }
        else{                                                       //if the user turned off the text coloring color with the default color
            ds.setColor(ReadArticleActivity.getReadArticleActivityInstance().getResources().getColor(R.color.default_word));
        }
    }

    public void color(View widget) {
        updateDrawState(textPaint);
        widget.invalidate();
    }

    public static void clear() {
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
    * */
    public void updateLastClickedWord(){
        if(GEARGlobal.getLastWordClickedIndex()<index) {
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
}
