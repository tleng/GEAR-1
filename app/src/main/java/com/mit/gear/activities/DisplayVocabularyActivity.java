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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.listeners.TableHeaderClickListener;
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
	//Counter to count the number of header clicks for each column
	private int WordColClicksNum = 0;
	private int LemmaColClicksNum = 0;
	private int ClicksColClicksNum = 0;
	private int PassedColClicksNum = 0;
	private int TimeColClicksNum = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_display_user_vocabulary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		//Access sharedPreferences to get th user setting on what type of word to display
        sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        ShowClicked = sharedPreferences.getBoolean("showClicked",true);
        ShowSeen = sharedPreferences.getBoolean("showSeen",true);
        View v = getView();
        TableView = (SortableTableView)v.findViewById(R.id.tableView);
        SetTableView(TableView);        //Set table view to display current user vocabulary
		/*
		 * Set header click listener to get the sort state and save it
		 * so when vocabulary list opened again list will be sorted accordingly
		 */
		TableView.addHeaderClickListener(new TableHeaderClickListener() {
			@Override
			public void onHeaderClicked(int columnIndex) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("SortState", SortState(columnIndex));
				editor.commit();
			}
		});
    }

    @Override
    public void onResume() {
		TableView = (SortableTableView)getView().findViewById(R.id.tableView);
        ShowClicked = sharedPreferences.getBoolean("showClicked",true);
        ShowSeen = sharedPreferences.getBoolean("showSeen",true);
        SetTableView(TableView);        //Set table view to display current user vocabulary
        super.onResume();
    }

	@Override
	public void onPause() {
		//Reset all column click counter
		WordColClicksNum = 0;
		LemmaColClicksNum = 0;
		ClicksColClicksNum = 0;
		PassedColClicksNum = 0;
		TimeColClicksNum = 0;
		TableView = null;
		super.onPause();
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
			if(MainActivity.ScreenSize.equals("Normal")){
				simpleTableHeaderAdapter.setTextSize(10);
				simpleTableHeaderAdapter.setPaddings(10, 10, 0, 10);
			}else{
				simpleTableHeaderAdapter.setTextSize(15);
				simpleTableHeaderAdapter.setPaddings(10, 15, 0, 15);
			}
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
			if(MainActivity.ScreenSize.equals("Normal")){
				simpleTableHeaderAdapter.setTextSize(10);
				simpleTableHeaderAdapter.setPaddings(10, 10, 0, 10);
			}else{
				simpleTableHeaderAdapter.setTextSize(15);
				simpleTableHeaderAdapter.setPaddings(10, 15, 0, 15);
			}
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
			if(MainActivity.ScreenSize.equals("Normal")){
				simpleTableHeaderAdapter.setTextSize(10);
				simpleTableHeaderAdapter.setPaddings(10, 10, 0, 10);
			}else{
				simpleTableHeaderAdapter.setTextSize(15);
				simpleTableHeaderAdapter.setPaddings(10, 15, 0, 15);
			}
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
            tableView.setHeaderAdapter(simpleTableHeaderAdapter);

            tableView.setColumnCount(0);                                                                          //Set the number of column
            tableView.setClickable(false);

            tableView.setDataAdapter(new WordTableAdapter(getActivity().getApplicationContext(),VocabularyList));         //World table adapter to set the table data to the words in the array list
        }
    }

    /*
     * This method is used to add the words is the user dictionary to the array list
     * The type of word added depends on the settings
     * `calls method SortDate to sort data according to the saved sort state
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
		if(SortDate(VocList) != null){
			VocList = SortDate(VocList);
		}
        return VocList;
    }

	/*
	 * Method to keep track of columns clicks
	 * return the sort state to be saved
	 */
	private String SortState(int columnIndex){
		switch (columnIndex){
			case 0:
				WordColClicksNum++;
				if(WordColClicksNum%2 == 0){
					return "WordDesc";
				}else{
					return "WordAsc";
				}
			case 1:
				if(ShowClicked&&ShowSeen || ShowClicked&&!ShowSeen){
					LemmaColClicksNum++;
					if(LemmaColClicksNum%2 == 0){
						return "LemmaDesc";
					}else{
						return "LemmaAsc";
					}
				}else if(ShowSeen&&!ShowClicked){
					PassedColClicksNum++;
					if(PassedColClicksNum%2 == 0){
						return "PassedDesc";
					}else{
						return "PassedAsc";
					}
				}
				break;
			case 2:
				if(ShowClicked&&ShowSeen || ShowClicked&&!ShowSeen){
					ClicksColClicksNum++;
					if(ClicksColClicksNum%2 == 0){
						return "ClicksDesc";
					}else{
						return "ClicksAsc";
					}
				}else if(ShowSeen&&!ShowClicked){
					TimeColClicksNum++;
					if(TimeColClicksNum%2 == 0){
						return "TimeDesc";
					}else{
						return "TimeAsc";
					}
				}
				break;
			case 3:
				if(ShowClicked&&ShowSeen){
					PassedColClicksNum++;
					if(PassedColClicksNum%2 == 0){
						return "PassedDesc";
					}else{
						return "PassedAsc";
					}
				}else if(ShowClicked&&!ShowSeen){
					TimeColClicksNum++;
					if(TimeColClicksNum%2 == 0){
						return  "TimeDesc";
					}else{
						return "TimeAsc";
					}
				}
				break;
			case 4:
				TimeColClicksNum++;
				if(TimeColClicksNum%2 == 0){
					return "TimeDesc";
				}else{
					return "TimeAsc";
				}
		}
		return "";
	}

	/*
	 * Method to sort data acording to the saved sort state
	 */
	private ArrayList<Word> SortDate(ArrayList<Word> vocList){
		String SavedState = sharedPreferences.getString("SortState", "None");
		if(!SavedState.equals("None")){
			switch (SavedState){
				case "WordDesc":
					Collections.sort(vocList, Comparators.SetWordComparatorReverse());
					break;
				case "WordAsc":
					Collections.sort(vocList, Comparators.SetWordComparator());
					break;
				case "LemmaDesc":
					Collections.sort(vocList, Comparators.SetLemmaComparatorReverse());
					break;
				case "LemmaAsc":
					Collections.sort(vocList, Comparators.SetLemmaComparator());
					break;
				case "ClicksDesc":
					Collections.sort(vocList, Comparators.SetClicksComparatorReverse());
					break;
				case "ClicksAsc":
					Collections.sort(vocList, Comparators.SetClicksComparator());
					break;
				case "PassedDesc":
					Collections.sort(vocList, Comparators.SetPassesComparatorReverse());
					break;
				case "PassedAsc":
					Collections.sort(vocList, Comparators.SetPassesComparator());
					break;
				case "TimeDesc":
					Collections.sort(vocList, Comparators.SetTimesComparatorReverse());
					break;
				case "TimeAsc":
					Collections.sort(vocList, Comparators.SetTimesComparator());
					break;
			}
		}else {
			vocList = null;
		}
		return vocList;
	}
}
