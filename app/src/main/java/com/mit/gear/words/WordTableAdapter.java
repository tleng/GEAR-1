package com.mit.gear.words;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mattmellor.gear.R;
import java.util.ArrayList;
import de.codecrafters.tableview.TableDataAdapter;

/**
 * Created by NuhaKhayat on 7/8/2016 AD.
 * This class is used to set the data of each cell in the table
 */
public class WordTableAdapter extends TableDataAdapter<Word> {

    public WordTableAdapter(Context context, ArrayList<Word> data) {
        super(context, data);
    }

    /*
     * This method take each data in the array list and render the view accordingly
     */
    public View getCellView(final int rowIndex, final int columnIndex, final ViewGroup parentView) {
        final Word word = getRowData(rowIndex);
        View renderedView = null;
        switch (columnIndex) {
            case 0:
                renderedView = renderString(word.getWord());
                break;
            case 1:
                if(word.getLemma().equals("None")){
                    renderedView = renderString("");
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
        textView.setTextSize(17);
        return textView;
    }
}
