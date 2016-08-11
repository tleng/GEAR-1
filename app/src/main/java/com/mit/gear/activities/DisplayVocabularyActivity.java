package com.mit.gear.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
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
    ArrayList<Word> VocabularyList = new ArrayList<>();     //Array list of Word to save words that are in user dictionary
    public static boolean ShowClicked;
    public static boolean ShowSeen;
    private SharedPreferences sharedPreferences;
    private SortableTableView TableView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_display_user_vocabulary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        ShowClicked = sharedPreferences.getBoolean("showClicked",true);
        ShowSeen = sharedPreferences.getBoolean("showSeen",true);
        View v = getView();
        TableView = (SortableTableView)v.findViewById(R.id.tableView);
        SetTableView(TableView);        //Set table view to display current user vocabulary
    }

    @Override
    public void onResume() {
        ShowClicked = sharedPreferences.getBoolean("showClicked",true);
        ShowSeen = sharedPreferences.getBoolean("showSeen",true);
        SetTableView(TableView);        //Set table view to display current user vocabulary
        super.onResume();
    }

    /*
		 * This method is used to set vocabulary table view
		 * set header and table text and cell color, set column comparator
		 * column changes depending on the settings
		 */
    private void SetTableView(SortableTableView tableView){
        VocabularyList = SetData();
        final int rowEvenColor = getResources().getColor(R.color.table_data_row_even);
        final int rowOddColor = getResources().getColor(R.color.table_data_row_odd);
        tableView.setHeaderBackgroundColor(getResources().getColor(R.color.table_header));           //Set table header background color
        tableView.setDataRowBackgroundProvider
                (TableDataRowBackgroundProviders.alternatingRowColors(rowEvenColor, rowOddColor));   //Set row color alternating even odd color
        tableView.setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());             //Set header sorting arrows style
        if(ShowClicked&&ShowSeen){
            final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter
                    (getActivity().getApplicationContext(), "Word","Definition","Clicks","Seen","Time");             //Set table header column
            simpleTableHeaderAdapter.setTextColor(getResources().getColor(R.color.table_header_text));           //Set table header column text color
            simpleTableHeaderAdapter.setPaddings(15, 20, 0, 20);
            tableView.setHeaderAdapter(simpleTableHeaderAdapter);

            tableView.setColumnCount(5);                                                                          //Set the number of column
            tableView.setColumnWeight(0,2);                                                                       //Set the width of the word column to be twice the size
            tableView.setColumnWeight(1,2);                                                                       //Set the width of the word column to be twice the size
            tableView.setClickable(false);

            //Set each column comparator
            tableView.setColumnComparator(0, Comparators.SetWordComparator());
            tableView.setColumnComparator(1, Comparators.SetLemmaComparator());
            tableView.setColumnComparator(2, Comparators.SetClicksComparator());
            tableView.setColumnComparator(3, Comparators.SetPassesComparator());
            tableView.setColumnComparator(4, Comparators.SetTimesComparator());
            tableView.setDataAdapter(new WordTableAdapter(getActivity().getApplicationContext(),VocabularyList));         //World table adapter to set the table data to the words in the array list
        }else if(ShowClicked&&!ShowSeen){
            final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter
                    (getActivity().getApplicationContext(), "Word","Definition","Clicks","Time");             //Set table header column
            simpleTableHeaderAdapter.setTextColor(getResources().getColor(R.color.table_header_text));           //Set table header column text color
            simpleTableHeaderAdapter.setPaddings(15, 20, 0, 20);
            tableView.setHeaderAdapter(simpleTableHeaderAdapter);

            tableView.setColumnCount(4);                                                                          //Set the number of column
            tableView.setColumnWeight(0,2);                                                                       //Set the width of the word column to be twice the size
            tableView.setColumnWeight(1,2);                                                                       //Set the width of the word column to be twice the size
            tableView.setClickable(false);

            //Set each column comparator
            tableView.setColumnComparator(0, Comparators.SetWordComparator());
            tableView.setColumnComparator(1, Comparators.SetLemmaComparator());
            tableView.setColumnComparator(2, Comparators.SetClicksComparator());
            tableView.setColumnComparator(3, Comparators.SetTimesComparator());
            tableView.setDataAdapter(new WordTableAdapter(getActivity().getApplicationContext(),VocabularyList));         //World table adapter to set the table data to the words in the array list
        }else if(ShowSeen&&!ShowClicked){
            final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter
                    (getActivity().getApplicationContext(), "Word","Seen","Time");             //Set table header column
            simpleTableHeaderAdapter.setTextColor(getResources().getColor(R.color.table_header_text));           //Set table header column text color
            simpleTableHeaderAdapter.setPaddings(15, 20, 0, 20);
            tableView.setHeaderAdapter(simpleTableHeaderAdapter);

            tableView.setColumnCount(3);                                                                          //Set the number of column
            tableView.setClickable(false);

            //Set each column comparator
            tableView.setColumnComparator(0, Comparators.SetWordComparator());
            tableView.setColumnComparator(1, Comparators.SetPassesComparator());
            tableView.setColumnComparator(2, Comparators.SetTimesComparator());
            tableView.setDataAdapter(new WordTableAdapter(getActivity().getApplicationContext(),VocabularyList));         //World table adapter to set the table data to the words in the array list

        }else{
            final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter
                    (getActivity().getApplicationContext(), "");             //Set table header column
            simpleTableHeaderAdapter.setTextColor(getResources().getColor(R.color.table_header_text));           //Set table header column text color
            simpleTableHeaderAdapter.setPaddings(15, 20, 0, 20);
            tableView.setHeaderAdapter(simpleTableHeaderAdapter);

            tableView.setColumnCount(0);                                                                          //Set the number of column
            tableView.setClickable(false);

            tableView.setDataAdapter(new WordTableAdapter(getActivity().getApplicationContext(),VocabularyList));         //World table adapter to set the table data to the words in the array list
        }
    }

    /*
     * This method is used to add the words is the user dictionary to the array list
     * The type of word added depends on the settings
     */
    private ArrayList<Word> SetData(){
        DataStorage dataStorage = new DataStorage(getActivity().getApplicationContext());
        HashMap<String, Word> UserDictionary = dataStorage.loadUserDictionary();
        ArrayList<Word> VocList = new ArrayList<>();
        //Add all word in UserDictionary
        if(ShowClicked&&ShowSeen){
            //Loop through user dictionary and add word to array list
            for (Map.Entry<String, Word> entry : UserDictionary.entrySet()) {
                Word word = entry.getValue();
                VocList.add(word);
            }
        }
        //Add only clicked word in UserDictionary
        else if(ShowClicked&&!ShowSeen){
            //Loop through user dictionary and add word to array list
            for (Map.Entry<String, Word> entry : UserDictionary.entrySet()) {
                Word word = entry.getValue();
                if (word.clicked) {
                    VocList.add(word);
                }
            }
        }
        //Add only passed word in UserDictionary
        else if(ShowSeen&&!ShowClicked){
            //Loop through user dictionary and add word to array list
            for (Map.Entry<String, Word> entry : UserDictionary.entrySet()) {
                Word word = entry.getValue();
                if (!word.clicked) {
                    VocList.add(word);
                }
            }
        }
        return VocList;
    }
}
