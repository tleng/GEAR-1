package com.mattmellor.gear;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.basistech.rosette.api.RosetteAPI;
import com.basistech.rosette.api.RosetteAPIException;
import com.basistech.rosette.apimodel.Lemma;
import com.basistech.rosette.apimodel.MorphologyResponse;
import com.paragon.open.dictionary.api.Dictionary;
import com.paragon.open.dictionary.api.Direction;
import com.paragon.open.dictionary.api.Language;
import com.paragon.open.dictionary.api.OpenDictionaryAPI;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

import static com.mattmellor.gear.R.id.app_article_bar;

public class  MainActivity extends AppCompatActivity {
    private static String LOG_APP_TAG = "tag";
    private android.support.v7.widget.Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar= (android.support.v7.widget.Toolbar) findViewById(app_article_bar);
        setSupportActionBar(toolbar);

        //TextView txtContent = (TextView) findViewById(R.id.articleView);
        final TextView txtContent = (TextView) findViewById(R.id.articleView);
        final TextView definition = (TextView) findViewById(R.id.definition_box);

        AssetManager assetManager = getAssets();

        String story = getIntent().getExtras().getString("story");
        // To load text file
        InputStream input;
        String text = story;
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

        txtContent.setMovementMethod(LinkMovementMethod.getInstance());
        txtContent.setText(text, TextView.BufferType.SPANNABLE);
//        Spannable spans = (Spannable) txtContent.getText();
//        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
//        iterator.setText(text);
//        int start = iterator.first();
//        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
//                .next()) {
//            String possibleWord = text.substring(start, end);
//            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
//                ClickableSpan clickSpan = getClickableSpan(possibleWord);
//                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    //Mike's extra stuff to be tested
//    private ClickableSpan getClickableSpan(final String word) {
//        return new ClickableSpan() {
//            final String mWord;
//            {
//                mWord = word;
//            }
//
//            @Override
//            public void onClick(View widget) {
//                Log.d("tapped on:", mWord);
//                Context context = getApplicationContext();
//                CharSequence message = mWord + " ausgewählt.";
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, message, duration);
//                toast.setGravity(Gravity.TOP, 0, 0);
//                toast.show();
//
//                overallUserVocab.addWordToUserDictionary(mWord);
//
//                final TextView definition = (TextView) findViewById(R.id.definition_box);
//                definition.setText(dictionaryOutput(mWord));
//            }
//
//            public void updateDrawState(TextPaint ds) {
//                ds.setUnderlineText(false);
//            }
//        };
//    }
    //end Mike's extra stuff to be tested

    public void toggleDictionary(View view){
        TextView dictionaryText = (TextView) findViewById(R.id.definition_box);
        dictionaryText.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
        RadioButton radioButton = (RadioButton) findViewById(R.id.show_dictionary);
        radioButton.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
    }

    public void showDictionary(View view) {
        TextView dictionaryText = (TextView) findViewById(R.id.definition_box);
        dictionaryText.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
        RadioButton radioButton = (RadioButton) findViewById(R.id.show_dictionary);
        radioButton.setVisibility(dictionaryText.isShown() ? View.GONE : View.VISIBLE);
    }

    public String dictionaryOutput(String word) {
        //Dictionary dictionary = getDictionary();
        //dictionary.showTranslation(word);

//        RosetteAPI rosetteApi = null;
//        try {
//            rosetteApi = new RosetteAPI("982437a36146ddbfe79ccac334eb92a8");
//            Log.d("got rosetteAPI:", rosetteApi.toString());
//            MorphologyResponse response = rosetteApi.getMorphology(RosetteAPI.MorphologicalFeature.LEMMAS,
//                    word, null, null);
//            Log.d("got response:", response.toString());
//             word = response.getLemmas().get(0).toString();
//            Log.d("got lemma:",word);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (RosetteAPIException e) {
//            e.printStackTrace();
//        }

        return word;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onClickUpPopWindow(View view){
        startActivity(new Intent(MainActivity.this, popUpRateArticle.class));
    }



    private Dictionary getDictionary() {
        OpenDictionaryAPI openDictionaryAPI = new OpenDictionaryAPI(getApplicationContext());
        Log.d("got dictionaryAPI", openDictionaryAPI.toString());
        Dictionary dictionary = openDictionaryAPI.getDictionaries(new Direction(Language.German, Language.English)).iterator().next();
        Log.d("got dictionary", dictionary.toString());
        return dictionary;
    }
}
