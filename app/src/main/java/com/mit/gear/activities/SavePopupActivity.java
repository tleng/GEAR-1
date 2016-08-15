package com.mit.gear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.reading.GEARClickableSpan;
import com.mit.gear.reading.PageFragment;
import com.mit.gear.reading.ReadArticleActivity;
import com.mit.gear.words.GEARGlobal;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.shape.CircleShape;

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
		savePopupActivity.setFinishOnTouchOutside(false);    //Make the activity not cancelable
        setContentView(R.layout.popup_save);
        TextView saveProgressQuery = (TextView)findViewById(R.id.saveProgressQuery);
        Intent i = getIntent();
        saveProgressQuery.setText( i.getStringExtra("saveProgressQuery"));
        isLastPage = i.getBooleanExtra("isLastPage",false);
        numberOfPages = i.getIntExtra("numberOfPages", 0);
		//Sets this activity to be a pop up
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        final int width = dm.widthPixels;
        final int height = dm.heightPixels;

        if(ReadArticleActivity.needsUserManual) {   //this shows a user manual hint to the user for save progress button
			//Set tutorial content text according to the screen size
			String ContentText;
			if(MainActivity.ScreenSize.equals("Normal")){
				ContentText = getResources().getString(R.string.UserManualSavePgContentSmall);
			}else{
				ContentText = getResources().getString(R.string.UserManualSavePgContent);
			}
			new MaterialShowcaseView.Builder(this)
                    .setTarget(findViewById(R.id.saveProgressQuery))
					.setShape(new CircleShape((int) (height* 0.1)))
                    .setTitleText(getResources().getString(R.string.UserManualTitle))
                    .setDismissText(getResources().getString(R.string.UserManualDismissText))
                    .setContentText(ContentText)
                    .singleUse("saveProgressButton")
                    .setMaskColour(getResources().getColor(R.color.manualBackground))
                    .setFadeDuration(300)
                    .setListener(new IShowcaseListener() {
                        @Override
                        public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {

                        }

                        @Override
                        public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
							//Change the size of the popup according to the screen size
							if(MainActivity.ScreenSize.equals("Normal")){
								getWindow().setLayout((int) (width*0.7) , (int) (height* 0.2));			//Set the size of the activity
							}else{
								getWindow().setLayout((int) (width*0.6) , (int) (height* 0.2));			//Set the size of the activity
							}
                        }
                    })
                    .show();
            ReadArticleActivity.needsUserManual = false;
            getSharedPreferences("Settings", MODE_PRIVATE) 								//Set first run preferences to false
                    .edit()
                    .putBoolean("manual", false)
                    .apply();
        }else{
			//Change the size of the popup according to the screen size
			if(MainActivity.ScreenSize.equals("Normal")){
				getWindow().setLayout((int) (width*0.7) , (int) (height* 0.2));			//Set the size of the activity
			}else{
				getWindow().setLayout((int) (width*0.6) , (int) (height* 0.2));			//Set the size of the activity
			}
		}
    }

	/*
	 * This method will be excused when don't save button clicked
	 * Method will dismiss the savePopupActivity and ReadArticleActivity
	 */
    public void dontSave(View view){
        savePopupActivity.finish();
		ReadArticleActivity.getReadArticleActivityInstance().finish();
    }

	/*
	 * This method will be excused when save button clicked
	 * Method will call save progress in ReadArticleActivity and will dismiss both activty
	 * If the last page has been reached valus are set accordingly
	 */
    public void saveProgress(View view) {
        if (isLastPage){														//Checks last page has been reached
        	GEARGlobal.setLastWordClicked("None");
        	Integer LastWordIndex= PageFragment.wordIndexing.get(numberOfPages); 	//Sets the last clicked word index to the end of the article
        	GEARGlobal.setLastWordClickedIndex(LastWordIndex);
		}
        ReadArticleActivity.getReadArticleActivityInstance().saveProgress(view);
    }

	@Override
	protected void onPause() {
		savePopupActivity = null;
		super.onPause();
	}

}