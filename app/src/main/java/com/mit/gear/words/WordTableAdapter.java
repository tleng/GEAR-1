package com.mit.gear.words;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mattmellor.gear.R;
import com.mit.gear.activities.DisplayVocabularyActivity;

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

    public WordTableAdapter(Context context, ArrayList<Word> data) {
		super(context, data);
		ShowClicked = DisplayVocabularyActivity.ShowClicked;
		ShowSeen = DisplayVocabularyActivity.ShowSeen;
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
					renderedView = renderString(word.getWord());
					break;
				case 1:
					if(word.getLemma().equals("None")){
						break;
					}else{
						renderedView = renderString(word.getLemma());
						break;
					}
				case 2:
					renderedView = renderString(String.valueOf(word.totalWordClicks()));
					break;
				case 3:
					renderedView = renderString(String.valueOf(word.totalWordPasses()));
					break;
				case 4:
					SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss:SS a");
					Date resultdate = new Date(word.getClickTime());
					renderedView = renderString(sdf.format(resultdate));
					break;
			}
		}else if(ShowClicked&&!ShowSeen){
			switch (columnIndex) {
				case 0:
					renderedView = renderString(word.getWord());
					break;
				case 1:
					renderedView = renderString(word.getLemma());
					break;
				case 2:
					renderedView = renderString(String.valueOf(word.totalWordClicks()));
					break;
				case 3:
					SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss:SS a");
					Date resultdate = new Date(word.getClickTime());
					renderedView = renderString(sdf.format(resultdate));
					break;
			}
		}else if(ShowSeen&&!ShowClicked){
			switch (columnIndex) {
				case 0:
					renderedView = renderString(word.getWord());
					break;
				case 1:
					renderedView = renderString(String.valueOf(word.totalWordPasses()));
					break;
				case 2:
					SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss:SS a");
					Date resultdate = new Date(word.getClickTime());
					renderedView = renderString(sdf.format(resultdate));
					break;
			}
		}else{
			renderedView = renderString("");
		}
		return renderedView;
	}


    /*
     * Set the text view style and data for table cell
     */
    private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setGravity(Gravity.LEFT);
        textView.setTextColor(getResources().getColor(R.color.default_word));
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(15);
        return textView;
    }
}
