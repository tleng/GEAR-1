package com.mit.gear.words;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mattmellor.gear.R;
import com.mit.gear.activities.DisplayVocabularyActivity;
import com.mit.gear.activities.MainActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.codecrafters.tableview.TableDataAdapter;

/**
 * Created by NuhaKhayat on 7/8/2016 AD.
 * This class is used to set the data of each cell in the table
 */
public class WordTableAdapter extends TableDataAdapter<Word> {
    private boolean ShowClicked;
    private boolean ShowSeen;
	private String TodaysDate;

    public WordTableAdapter(Context context, ArrayList<Word> data) {
		super(context, data);
		ShowClicked = DisplayVocabularyActivity.ShowClicked;
		ShowSeen = DisplayVocabularyActivity.ShowSeen;
		TodaysDate = getTodayDate();
    }

    /*
     * This method take each data in the array list and render the view accordingly
     */
    public View getCellView(final int rowIndex, final int columnIndex, final ViewGroup parentView) {
		final Word word = getRowData(rowIndex);
		View renderedView = null;
		if(ShowClicked&&ShowSeen){
			switch (columnIndex) {
				case 0:
					renderedView = renderString(word.getWord(),false);
					break;
				case 1:
					if(word.getLemma().equals("None")){
						break;
					}else{
						renderedView = renderString(word.getLemma(),false);
						break;
					}
				case 2:
					renderedView = renderString(String.valueOf(word.totalWordClicks()),false);
					break;
				case 3:
					renderedView = renderString(String.valueOf(word.totalWordPasses()),false);
					break;
				case 4:
					DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					Date date = new Date(word.getClickTime());
					String WordDate = formatter.format(date);
					if(TodaysDate.equals(WordDate)){
						SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
						Date resultdate = new Date(word.getClickTime());
						renderedView = renderString(sdf.format(resultdate),true);
						break;
					}else{
						SimpleDateFormat sdf = new SimpleDateFormat("d MMM h:mm a");
						Date resultdate = new Date(word.getClickTime());
						renderedView = renderString(sdf.format(resultdate),true);
						break;
					}
			}
		}else if(ShowClicked&&!ShowSeen){
			switch (columnIndex) {
				case 0:
					renderedView = renderString(word.getWord(),false);
					break;
				case 1:
					renderedView = renderString(word.getLemma(),false);
					break;
				case 2:
					renderedView = renderString(String.valueOf(word.totalWordClicks()),false);
					break;
				case 3:
					DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					Date date = new Date(word.getClickTime());
					String WordDate = formatter.format(date);
					if(TodaysDate.equals(WordDate)){
						SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
						Date resultdate = new Date(word.getClickTime());
						renderedView = renderString(sdf.format(resultdate),true);
						break;
					}else{
						SimpleDateFormat sdf = new SimpleDateFormat("d MMM h:mm a");
						Date resultdate = new Date(word.getClickTime());
						renderedView = renderString(sdf.format(resultdate),true);
						break;
					}
			}
		}else if(ShowSeen&&!ShowClicked){
			switch (columnIndex) {
				case 0:
					renderedView = renderString(word.getWord(),false);
					break;
				case 1:
					renderedView = renderString(String.valueOf(word.totalWordPasses()),false);
					break;
				case 2:
					DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					Date date = new Date(word.getClickTime());
					String WordDate = formatter.format(date);
					if(TodaysDate.equals(WordDate)){
						SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
						Date resultdate = new Date(word.getClickTime());
						renderedView = renderString(sdf.format(resultdate),true);
						break;
					}else{
						SimpleDateFormat sdf = new SimpleDateFormat("d MMM h:mm a");
						Date resultdate = new Date(word.getClickTime());
						renderedView = renderString(sdf.format(resultdate),true);
						break;
					}
			}
		}else{
			renderedView = renderString("",false);
		}
		return renderedView;
	}


    /*
     * Set the text view style and data for table cell
     */
    private View renderString(final String value, boolean Smaller) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setGravity(Gravity.LEFT);
        textView.setTextColor(getResources().getColor(R.color.default_word));
        textView.setPadding(20, 10, 20, 10);
		if(MainActivity.ScreenSize.equals("Normal")){
			textView.setTextSize(11);
		}else{
			textView.setTextSize(15);
		}
		if(Smaller && !MainActivity.ScreenSize.equals("Normal")){
			textView.setTextSize(13);
		}
        return textView;
    }

	/*
     * Method to get today's date without time
     */
	private String getTodayDate(){
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date TodayDate = new Date(System.currentTimeMillis());
		return formatter.format(TodayDate);
	}
}
