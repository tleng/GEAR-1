package com.mit.gear.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mattmellor.gear.R;
import com.mit.gear.activities.MainActivity;
import com.mit.gear.words.Word;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

	public HashMap<String, Boolean> loadColorFile() {
		WordToColor = loadWordsColor(COLORFILE);
		return WordToColor;
	}

	/*
	 * Method to load file that contains the words that are not in dictionary
	 * and needs to be colored, different case of the word is in dictionary
	 */
	private HashMap<String, Boolean> loadWordsColor(String file) {
		HashMap<String, Boolean> loadingFile = new HashMap<>();
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
				loadingFile = new Gson().fromJson(buf.toString(), new TypeToken<HashMap<String, Boolean>>() {
				}.getType());
			}
		}catch (java.io.FileNotFoundException e) {
			Log.d("Dictionary","No dictionary file found.");
		}catch (Throwable t) {

		}finally {
			return loadingFile;
		}
	}


	/*
	 * Method to add word to coloring file
	 * different case of the word is clicked
	 */
	public void addToColorFile(String word, boolean click) throws JSONException, IOException {
		HashMap<String, Boolean> dictionary = loadColorFile();
		Log.d("WordToColor", dictionary.toString());
		dictionary.put(word,click);
		Gson gson = new Gson();
		String json = gson.toJson(dictionary);
		OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(COLORFILE, 0));
		out.write(json);
		Log.d("Saved WordToColor",word +","+click);
		out.close();
	}

	/*
	 * Method to delete word to coloring file
	 * different case of the word is clicked
	 */
	public void deleteFromColorFile(String word) throws JSONException, IOException {
		HashMap<String, Boolean> dictionary = loadColorFile();
		dictionary.remove(word);
		Gson gson = new Gson();
		String json = gson.toJson(dictionary);
		OutputStreamWriter out= new OutputStreamWriter(context.openFileOutput(COLORFILE, 0));
		out.write(json);
		out.close();
	}

	/*
	 * Method to clear all words in coloring file
	 */
	public void clearColorFile() throws IOException {
		HashMap<String,Boolean> userDictionary = new HashMap<>();
		Gson gson = new Gson();
		String json = gson.toJson(userDictionary);
		OutputStreamWriter out= new OutputStreamWriter(context.openFileOutput(COLORFILE, 0));
		out.write(json);
		out.close();
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
                //Log.d(file,loadingFile.toString());
            }
            //Log.d(file,"null");

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
            userData = null;
            for (int i = 0; i < (Integer) lemmaArticleClick.get(3); i++) {
                if (dictionary.containsKey(word)) {
                    userData = dictionary.get(word);
                    userData.update(article, click);
                    userData.scoreWord(click, true);
                } else {
                    userData = new Word(word, lemma);
                    userData.update(article, click);
                    userData.scoreWord(click, false);
                }
            }
            dictionary.put(word, userData);
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
        Log.d("1 dictionaryAdd", dictionary.toString());
        Word userData = null;
        if (dictionary.containsKey(word))
		{
            userData = dictionary.get(word);
			if (userData.getLemma().equals("None")){
				userData.setLemma(lemma);
			}
            if(!wordSmall.equals("None")){
				Log.d("addToWordsFile","before Change to small = "+userData.getWord());
				userData.setWord(wordSmall);
				Log.d("addToWordsFile","Change to small = "+userData.getWord());
				dictionary.remove(word);
				Log.d("dictionaryAdd", dictionary.toString());
				word = wordSmall;
			}
            userData.update(article, click);
            userData.scoreWord(click, true);
			Log.d("userDataAdd", "1 Word="+userData.getWord());
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
				Log.d("userDataAdd", "1 Word=" + userData.getWord());
				word = word.toLowerCase();
			}else {
				userData = new Word(word, lemma);
				userData.update(article, click);
				userData.scoreWord(click, false);
				Log.d("userDataAdd", "2 Word="+userData.getWord());
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
				Log.d("userDataAdd", "1 Word=" + userData.getWord());
			}else {
				userData = new Word(word, lemma);
				userData.update(article, click);
				userData.scoreWord(click, false);
				Log.d("userDataAdd", "2 Word="+userData.getWord());
			}
		}
		Log.d("userDataAdd", "Word Last=" + userData.getWord());
		Log.d("Word", "Last=" + word);
        dictionary.put(word, userData);
		Log.d("2 dictionaryAdd", dictionary.toString());
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
		Log.d("1 dictionaryDelete", dictionary.toString());
        Log.d("userDataWord 1 ", word);
        userData = dictionary.get(word);
        if(userData == null){
			Log.d("userData", "Null");
            if(Character.isUpperCase(word.charAt(0))){
				Log.d("Word", "UpperCase");
                word = word.toLowerCase();
                userData = dictionary.get(word);
				Log.d("userData", "get small Word = "+userData.getWord());
            }
        }
		Log.d("userData", "NotNull Word = "+userData.getWord());
        Log.d("userDataWord 2 ", word);
        boolean KeepInDictionary = userData.RemoveUpdate(article, click); //update the word clicks (decrement)
		Log.d("KeepInDictionary", String.valueOf(KeepInDictionary));
        if(KeepInDictionary){
            dictionary.put(word, userData); //update the word if clicked or passed
        }
        else {
            dictionary.remove(word); // remove from dictionary if not clikced or passed
			if(Character.isUpperCase(word.charAt(0))){
				//MainActivity.WordToColor.remove(word.toLowerCase());
				deleteFromColorFile(word.toLowerCase());
			}else {
				Character first = Character.toUpperCase(word.charAt(0));
				word = first + word.substring(1);
				//MainActivity.WordToColor.remove(word);
				deleteFromColorFile(word);
			}
		}
		Log.d("2 dictionaryDelete", dictionary.toString());
        Gson gson = new Gson();
        String json = gson.toJson(dictionary);
        OutputStreamWriter out= new OutputStreamWriter(context.openFileOutput(file, 0));
        out.write(json);
        out.close();
    }

}

