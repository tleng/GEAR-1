package com.mattmellor.gear;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.backendgear_1121.gear.Gear;
import com.appspot.backendgear_1121.gear.model.GearBackendDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.mattmellor.gear.R.id.app_article_bar;

public class  MainActivity extends AppCompatActivity {
    private static String LOG_APP_TAG = "tag";
    private android.support.v7.widget.Toolbar toolbar;
    private UserData currentUserData;
    private String currentDefinition = "No definition";
    private Integer currentPosition = 0;

    private UserDataCollection allUserData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        User user = new User();
//        currentUser = user;

        //Setting a custom action bar


        setContentView(R.layout.activity_main);
        toolbar= (android.support.v7.widget.Toolbar) findViewById(app_article_bar);
        setSupportActionBar(toolbar);

        //Getting rid of title for the action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        allUserData = new UserDataCollection(getApplicationContext());
        UserData userData = new UserData(start.currentUser.id(),story);
        this.currentUserData = userData;
        Long time = System.currentTimeMillis();
        this.currentUserData.setStartTime(time);
        allUserData.addUserDataToAllUserData(userData);

        txtContent.setMovementMethod(LinkMovementMethod.getInstance());
        txtContent.setText(text, TextView.BufferType.SPANNABLE);
        Spannable spans = (Spannable) txtContent.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(text);

        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = text.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(possibleWord);
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

    }

    private void displayDefinition(GearBackendDefinition... definitions) {
        String msg;
        if (definitions==null || definitions.length < 1) {
            msg = "Definition was not present";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } else {
            Log.d("Display", "Displaying " + definitions.length + " definition.");
            List<GearBackendDefinition> definitionsList = Arrays.asList(definitions);
            Toast.makeText(this, definitionsList.get(0).toString(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            public void onClick(View widget) {
                Log.d("tapped on:", mWord);
                Context context = getApplicationContext();
                CharSequence message = mWord + " ausgewählt.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, message, duration);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();

                //Mike's server stuff
                // Use of an anonymous class is done for sample code simplicity. {@code AsyncTasks} should be
                // static-inner or top-level classes to prevent memory leak issues.
                // @see http://goo.gl/fN1fuE @26:00 for a great explanation.
                AsyncTask<String, Void, GearBackendDefinition> getAndDisplayDefinition =
                        new AsyncTask<String, Void, GearBackendDefinition> () {
                            @Override
                            protected GearBackendDefinition doInBackground(String... words) {
                                // Retrieve service handle.
                                Gear apiServiceHandle = AppConstants.getApiServiceHandle();
                                GearBackendDefinition definition = new GearBackendDefinition().setMessage(words[0]);

                                try {
                                    Gear.Gearapi.Define getDefinition = apiServiceHandle.gearapi().define(definition);
                                    GearBackendDefinition gearDefinition = getDefinition.execute();
                                    return gearDefinition;
                                } catch (IOException e) {
                                    Log.e("Uh Oh", "Exception during API call", e);
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(GearBackendDefinition definition) {
                                final TextView readingDictionary = (TextView) findViewById(R.id.definition_box);
                                if (definition!=null) {
                                    currentDefinition = definition.getMessage();
                                    readingDictionary.setText(currentDefinition);
                                } else {
                                    readingDictionary.setText("");
                                    Log.e("Uh Oh", "No definitions were returned by the API.");
                                }
                            }
                        };

                getAndDisplayDefinition.execute(mWord);

                overallUserVocab.addWordToUserDictionary(mWord);
                start.currentUser.addToDictionary(mWord);
                currentUserData.addWord(mWord);
//
//                final TextView definition = (TextView) findViewById(R.id.definition_box);
//                definition.setText(currentDefinition);

            }

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
    }

    public String dictionaryOutput(String word) {
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
        Intent intent = new Intent(MainActivity.this, popUpRateArticle.class);
        startActivity(intent);
        intent.putExtra("currentArticle", (String) currentUserData.getArticle());
        intent.putExtra("currentUserId", (String) currentUserData.getUserId());
    }
    

    @Override
    protected void onPause () {
        super.onPause();
        ScrollView articleView = (ScrollView) findViewById(R.id.SCROLLER_ID);
        int position = articleView.getBottom() - (articleView.getHeight() + articleView.getScrollY());
        int percentage = (int)((articleView.getHeight()+articleView.getScrollY())/articleView.getBottom());
        Log.d("Position", Integer.toString(position));
        Log.d("Bottom", Integer.toString(articleView.getBottom()));
        Log.d("Height", Integer.toString(articleView.getHeight()));
        Log.d("Scroll Y", Integer.toString(articleView.getScrollY()));
        currentPosition = articleView.getScrollY();

        Long time = System.currentTimeMillis();
        this.currentUserData.setExitTime(time);
        try {
            allUserData.writeToFile("testing.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void OnResume() {
        super.onResume();
        ScrollView articleView = (ScrollView) findViewById(R.id.SCROLLER_ID);
        articleView.scrollTo(0,currentPosition);
    }
}
