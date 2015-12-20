package com.mattmellor.gear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;

import static com.mattmellor.gear.R.id.app_article_bar;

/**
 * Activity where user can browse and select which story to read
 */
public class StoriesSelectionActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories_selection);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(app_article_bar);
        setSupportActionBar(toolbar);
        try {
            generateRecommendationButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Make this dynamically read articles in folder and
        // create buttons, like in stories selection, instead of hardcoding
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_stories_suggestion_and_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    /**
//     * Starts the ReadArticleActivity for the selected story
//     * @param view
//     */
//    public void openStory(View view){
//        Intent intent = new Intent(StoriesSelectionActivity.this, ReadArticleActivity.class);
//        intent.putExtra("story", (String) view.getTag());
//        startActivity(intent);
//        finish();
//    }

    View.OnClickListener getOnClickSetStory(final Button button)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(StoriesSelectionActivity.this, ReadArticleActivity.class);
                intent.putExtra("story",button.getContentDescription());
                startActivity(intent);
                finish();
            }
        };
    }

    private void generateRecommendationButtons() throws IOException {
        String [] articles = getAssets().list("");

        // adjust linear layout to display articles in
        LinearLayout ll = (LinearLayout) findViewById(R.id.allStoriesLinearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setOrientation(LinearLayout.VERTICAL);


        // StackOverflow suggested code to dynamically create buttons for the articles
        for (String article:articles) {
            if (article.equals("images") || article.equals("sounds") || article.equals("webkit")|| article.equals("pskc_schema.xsd")) {
                continue;
            }
            Button myButton = new Button(this);
            myButton.setText(Html.fromHtml(article));
            myButton.setContentDescription(article);
            myButton.setHeight(30);
            myButton.setTransformationMethod(null); // ensures text is lower case

            Log.d("button added:", myButton.toString());
            myButton.setOnClickListener(getOnClickSetStory(myButton));

            ll.addView(myButton, lp);
            Log.d("number of buttons:", Integer.toString(ll.getChildCount()));
        }
    }
}
