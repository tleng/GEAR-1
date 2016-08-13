package com.mit.gear.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mattmellor.gear.R;
import com.mit.gear.words.Word;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael on 12/12/15.
 */

/*
Class that will utilize JSON to store data locally on phone.
 */
public class DataStorage {
    Context context;
    AssetManager assetManager;
    String USERINFO = "gearUserInformation";
    public static String USERDICTIONARY = "gearUserDictionary";								//USERDICTIONARY is stored on the local file storage of the android device
	public static String COLORFILE = "wordtocolor";											//COLORFILE  is stored on the local file storage if android device
	String OFFLINEDICTIONARY = "offline_dictionary_json";									//OFFLINEDICTIONARY is stored in assets
    String TESTDICTIONARY = "offline_dictionary_maparray_json";
	String UNCLICKEDWORDS = "unclickedWords";
    HashMap<String, Word> userDictionary = new HashMap<>();
	HashMap<String, Boolean> WordToColor = new HashMap<>();
    HashMap<String, Word> unclickedWords = new HashMap<>();

    //In order to open files from assets, we need to pass a context and retrieve the assetManager from that context
    public DataStorage(Context context) {
        this.context = context;
        this.assetManager = context.getAssets();
    }

    public HashMap<String, ArrayList<String>> loadOfflineDictionary() {
        return loadJSONDictionary(context.getString(R.string.offline_dictionary));
    }

    public HashMap<String, Word> loadUserDictionary() {
        userDictionary = loadWordsFile(USERDICTIONARY);
        return userDictionary;
    }



    public HashMap<String, Word> loadUnclickedWords() {
        unclickedWords = loadWordsFile(UNCLICKEDWORDS);
        return unclickedWords;
    }

    private HashMap<String, Word> loadWordsFile(String file) {
        HashMap<String, Word> loadingFile = new HashMap<>();
        try {
            InputStream in = context.openFileInput(file);
            if (in != null) {
                InputStreamReader tmp=new InputStreamReader(in);
                BufferedReader reader=new BufferedReader(tmp);
                String str;
                StringBuilder buf=new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    buf.append(str);
                }

                in.close();
                loadingFile = new Gson().fromJson(buf.toString(), new TypeToken<HashMap<String, Word>>() {
                }.getType());
            }

        } catch (java.io.FileNotFoundException e) {
            Log.d("Dictionary","No dictionary file found.");

        } catch (Throwable t) {

        }

        finally {
            return loadingFile;
        }

    }

    public HashMap<String, ArrayList<String>> loadJSONDictionary(String filename) {
        InputStream input;
        String text;
        try {
            input = assetManager.open(filename);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            // byte buffer into a string
            text = new String(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            text = "{\"dictionary\":{\"error\":\"occurred\"}}";
        }

        Log.d("OfflineDictionary","Text Loaded");
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        try {
            map = new Gson().fromJson(text, new TypeToken<HashMap<String, ArrayList<String>>>() {
            }.getType());
        } catch (Exception e) {
            Log.d("DictionaryError",e.getMessage());
        }
        return map;

    }

    public void clearUserDictionary() throws IOException {
        HashMap<String,Word> userDictionary = new HashMap<>();
        Gson gson = new Gson();
        String json = gson.toJson(userDictionary);
        OutputStreamWriter out=

                new OutputStreamWriter(context.openFileOutput(USERDICTIONARY, 0));

        out.write(json);
        out.close();
    }

    public void addToUserDictionary(String word, String lemma,String wordSmall ,String article, boolean click) throws JSONException, IOException {
        addToWordsFile(word, lemma, wordSmall, USERDICTIONARY, article, click);
    }

    //The ArrayList is made up of String Lemma, String Article, Boolean Click
    public void addGroupToUserDictionary(HashMap<String,ArrayList<Object>> wordWithLemmaArticleClick) throws JSONException, IOException {
        HashMap<String, Word> dictionary = loadWordsFile(USERDICTIONARY);
        for (String word : wordWithLemmaArticleClick.keySet()) {
            Word userData;
            ArrayList<Object> lemmaArticleClick = wordWithLemmaArticleClick.get(word);
            String lemma = (String) lemmaArticleClick.get(0); // Lemma
            String article = (String) lemmaArticleClick.get(1); // Article filename
            boolean click = (boolean) lemmaArticleClick.get(2); // click should be false
            String wordToCheck ;                    //other version of the word (uppercase/lowercase)
            boolean otherVersionExist = false;      //this flag indicates if there is either a lowercase or uppercase version of the word in the dictionary
            boolean needsToSwap;                    //this flag indicate if there is the need to swap a uppercase word from dictionary with lowercase word

            if(Character.isUpperCase(word.charAt(0))){

                wordToCheck=Character.toLowerCase(word.charAt(0))+word.substring(1);
                needsToSwap=false;                      //regardless of whether there is another version on dictionary, there is no need to swap the lowercase in dic with uppercase

            }else{

                wordToCheck=Character.toUpperCase(word.charAt(0))+word.substring(1);
                needsToSwap=true;

            }

            userData = null;
            for (int i = 0; i < (Integer) lemmaArticleClick.get(3); i++) {
                if (dictionary.containsKey(word)) {
                    userData = dictionary.get(word);
                    userData.update(article, click);
                    userData.scoreWord(click, true);
                }else if(dictionary.containsKey(wordToCheck)){
                    userData = dictionary.get(wordToCheck);
                    userData.update(article, click);
                    userData.scoreWord(click, true);
                    otherVersionExist = true;
                } else {
                    userData = new Word(word, lemma);
                    userData.update(article, click);
                    userData.scoreWord(click, false);
                    dictionary.put(word, userData);
                }
            }
            if(otherVersionExist){              //if other version of the word exist in dic (lowercase or uppercase)

                if(needsToSwap){                //if uppercase exist in dic and the word we are processing is lower, then update the dic with lowercase word
                    userData.setWord(word);
                    dictionary.remove(wordToCheck);
                    dictionary.put(word,userData);
                }
                else{                           //if lowercase word exist in dic just update its parameter
                    dictionary.put(wordToCheck, userData);
                }
            }
            else{
                dictionary.put(word, userData);
            }
        }

        Gson gson = new Gson();
        String json = gson.toJson(dictionary);
        OutputStreamWriter out=

                new OutputStreamWriter(context.openFileOutput(USERDICTIONARY, 0));

        out.write(json);

        out.close();
    }

    public void addToWordsFile(String word, String lemma, String wordSmall,String file, String article, boolean click) throws JSONException, IOException {
        HashMap<String, Word> dictionary = loadWordsFile(file);
        Word userData = null;
        if (dictionary.containsKey(word))
		{
            userData = dictionary.get(word);
			if (userData.getLemma().equals("None")){
				userData.setLemma(lemma);
			}
            if(!wordSmall.equals("None")){
				userData.setWord(wordSmall);
				dictionary.remove(word);
				word = wordSmall;
			}
            userData.update(article, click);
            userData.scoreWord(click, true);
        }
		else if(Character.isUpperCase(word.charAt(0)))
		{
			if (dictionary.containsKey(word.toLowerCase())) {
				userData = dictionary.get(word.toLowerCase());
				if (userData.getLemma().equals("None")) {
					userData.setLemma(lemma);
				}
				userData.update(article, click);
				userData.scoreWord(click, true);
				word = word.toLowerCase();
			}else {
				userData = new Word(word, lemma);
				userData.update(article, click);
				userData.scoreWord(click, false);
			}
		}
		else if(Character.isLowerCase(word.charAt(0)))
		{
			Character first = Character.toUpperCase(word.charAt(0));
			String WordCheck = first+word.substring(1);
			if (dictionary.containsKey(WordCheck)) {
				userData = dictionary.get(WordCheck);
				if (userData.getLemma().equals("None")) {
					userData.setLemma(lemma);
				}
				userData.setWord(word.toLowerCase());
				userData.update(article, click);
				userData.scoreWord(click, true);
				dictionary.remove(WordCheck);
			}else {
				userData = new Word(word, lemma);
				userData.update(article, click);
				userData.scoreWord(click, false);
			}
		}
        dictionary.put(word, userData);
        Gson gson = new Gson();
        String json = gson.toJson(dictionary);
        OutputStreamWriter out=

                new OutputStreamWriter(context.openFileOutput(file, 0));

        out.write(json);
        Log.d("File", "Saved " + word);

        out.close();
    }

	/*
     *this method delete a specific word from the dictionary
     *used when user clicks UNDO button
     */
    public void deleteFromWordFile(String word, String lemma, String file, String article, boolean click) throws JSONException, IOException {
        HashMap<String, Word> dictionary = loadWordsFile(file);
		Word userData;
        userData = dictionary.get(word);
        if(userData == null){
            if(Character.isUpperCase(word.charAt(0))){
                word = word.toLowerCase();
                userData = dictionary.get(word);
            }
        }

        boolean KeepInDictionary = userData != null && userData.RemoveUpdate(article, click); //update the word clicks (decrement)
		Log.d("KeepInDictionary", String.valueOf(KeepInDictionary));
        if(KeepInDictionary){
            dictionary.put(word, userData); //update the word if clicked or passed
        }
        else {
            dictionary.remove(word); // remove from dictionary if not clicked or passed
		}
        Gson gson = new Gson();
        String json = gson.toJson(dictionary);
        OutputStreamWriter out= new OutputStreamWriter(context.openFileOutput(file, 0));
        out.write(json);
        out.close();
    }

}

