package com.mit.gear.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattmellor.gear.R;
import com.mit.gear.data.DataStorage;
import com.mit.gear.words.Comparators;
import com.mit.gear.words.Word;
import com.mit.gear.words.WordTableAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

/**
 * Class that displays user vocabulary
 */
public class DisplayVocabularyActivity extends Fragment {
    ArrayList<Word> VocList = new ArrayList<>();     //Array list of Word to save words that are in user dictionary
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_display_user_vocabulary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        SortableTableView TableView = (SortableTableView)v.findViewById(R.id.tableView);
        SetTableView(TableView);        //Set table view to display current user vocabulary
    }
    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_user_vocabulary);

        // display current user vocabulary
        TextView vocabView = (TextView) findViewById(R.id.vocabularyData);
        vocabView.setText(getVocabularyString());
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/


    /**
     * TODO: Determine meaningful way to display vocabulary – what does user want?
     * Turns user vocabulary into string representation for display
     * @return string to display

    private String getVocabularyString() {
        DataStorage dataStorage = new DataStorage(getActivity().getApplicationContext());
        HashMap<String, Word> vocabulary = dataStorage.loadUserDictionary();

        String vocabString = "";
        if (vocabulary.isEmpty()) {
            vocabString = "No vocabulary words looked up yet.\n";
        }

        // list vocabulary words
        for (Map.Entry<String, Word> entry : vocabulary.entrySet()) {
            String key = entry.getKey();
            Word word = entry.getValue();
            if(word.getLemma().equals("None"))
                vocabString += key +"\nClicked: " + Integer.toString(word.totalWordClicks()) + "\t\t\tPassed: " + Integer.toString(word.totalWordPasses()) + "\n\n";
            else
                vocabString += key + " , "+word.getLemma()+"\nClicked: " + Integer.toString(word.totalWordClicks()) + "\t\t\tPassed: " + Integer.toString(word.totalWordPasses()) + "\n\n";
        }

        return vocabString;
    }*/

    /*
     * This method is used to set vocabulary table view
     * set header and table text and cell color, set column comparator
     */
    private void SetTableView(SortableTableView tableView){
        SetData();
        final int rowEvenColor = getResources().getColor(R.color.table_data_row_even);
        final int rowOddColor = getResources().getColor(R.color.table_data_row_odd);
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter
                (getActivity().getApplicationContext(), "Word","Definition","Clicks","Seen","Time");             //Set table header column
        simpleTableHeaderAdapter.setTextColor(getResources().getColor(R.color.table_header_text));           //Set table header column text color
        simpleTableHeaderAdapter.setPaddings(15, 20, 0, 20);

        tableView.setHeaderBackgroundColor(getResources().getColor(R.color.table_header));           //Set table header background color
        tableView.setHeaderAdapter(simpleTableHeaderAdapter);
        tableView.setColumnCount(5);                                                                          //Set the number of column
        tableView.setDataRowBackgroundProvider
                (TableDataRowBackgroundProviders.alternatingRowColors(rowEvenColor, rowOddColor));            //Set row color alternating even odd color
        tableView.setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());                      //Set header sorting arrows style
        tableView.setColumnWeight(0,2);                                                                       //Set the width of the word column to be twice the size
		tableView.setColumnWeight(1,2);                                                                       //Set the width of the word column to be twice the size
		tableView.setClickable(false);

        //Set each column comparator
        tableView.setColumnComparator(0, Comparators.SetWordComparator());
        tableView.setColumnComparator(1, Comparators.SetLemmaComparator());
        tableView.setColumnComparator(2, Comparators.SetClicksComparator());
        tableView.setColumnComparator(3, Comparators.SetPassesComparator());
        tableView.setColumnComparator(4, Comparators.SetTimesComparator());
        tableView.setDataAdapter(new WordTableAdapter(getActivity().getApplicationContext(),VocList));         //World table adapter to set the table data to the words in the array list
    }

    /*
     * This method is used to add the words is the user dictionary to the array list
     */
    private void SetData(){
        DataStorage dataStorage = new DataStorage(getActivity().getApplicationContext());
        HashMap<String, Word> UserDictionary = dataStorage.loadUserDictionary();
        //Loop through user dictionary and add word to array list
        for (Map.Entry<String, Word> entry : UserDictionary.entrySet()) {
            Word word = entry.getValue();
            VocList.add(word);
        }
    }
}
