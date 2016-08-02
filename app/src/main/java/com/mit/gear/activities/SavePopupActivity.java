package com.mit.gear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.reading.GEARClickableSpan;
import com.mit.gear.reading.PageFragment;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.words.GEARGlobal;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

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

        final int width = dm.widthPixels;
        final int height = dm.heightPixels;

        //sets this activity to be a pop up

        if(ReadArticleActivity.needsUserManual) {   //this shows a user manual hint to the user for save progress button
            new MaterialShowcaseView.Builder(this)
                    .setTarget(findViewById(R.id.savePopup))
                    .setShapePadding(150)
                    .setTitleText(getResources().getString(R.string.UserManualTitle))
                    .setDismissText(getResources().getString(R.string.UserManualDismissText))
                    .setContentText(getResources().getString(R.string.UserManualSavePgContent))
                    .singleUse("saveProgressButton")
                    .setMaskColour(getResources().getColor(R.color.manualBackground))
                    .setFadeDuration(300)
                    .setListener(new IShowcaseListener() {
                        @Override
                        public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {

                        }

                        @Override
                        public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                            getWindow().setLayout((int) (width*0.5) , (int) (height* 0.2));
                        }
                    })
                    .show();
            ReadArticleActivity.needsUserManual = false;
            getSharedPreferences("Settings", MODE_PRIVATE) 								//Set first run preferences to false
                    .edit()
                    .putBoolean("manual", false)
                    .apply();
        }else{
            getWindow().setLayout((int) (width*0.5) , (int) (height* 0.2));
        }

        //Following lines set the size of the activity
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