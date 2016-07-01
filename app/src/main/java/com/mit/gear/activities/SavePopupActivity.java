package com.mit.gear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.reading.GEARClickableSpan;
import com.mit.gear.reading.PageFragment;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.words.GEARGlobal;

/**
 *
 */
public class SavePopupActivity extends Activity {
    //creating savePopupActivity instance
    public static SavePopupActivity savePopupActivity;
    public boolean isLastPage = false;
    public Integer numberOfPages;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        savePopupActivity = this;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_save);
        TextView saveProgressQuery = (TextView)findViewById(R.id.saveProgressQuery);
        Intent i = getIntent();
        saveProgressQuery.setText( i.getStringExtra("saveProgressQuery"));
        isLastPage = i.getBooleanExtra("isLastPage",false);
        numberOfPages = i.getIntExtra("numberOfPages", 0);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //sets this activity to be a pop up
        //Following lines set the size of the activity
        getWindow().setLayout((int) (width*0.5) , (int) (height* 0.2));

    }

    public void dontSave(View view){
        //dismissing the savePopupActivity
        savePopupActivity.finish();
    }

    public void saveProgress(View view) {
    //checks if the user in the last page sets the last clicked word index to the end of the article
        if (isLastPage)
    {
        GEARGlobal.setLastWordClicked("None");
        Integer LastWordIndex= PageFragment.wordIndexing.get(numberOfPages);
        GEARGlobal.setLastWordClickedIndex(LastWordIndex);
    }
        ReadArticleActivity.getReadArticleActivityInstance().saveProgress(view);
    }

}