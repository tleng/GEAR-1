package com.mit.gear.activities;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mattmellor.gear.R;
import com.mit.gear.NavDrawer.NavDrawerItem;
import com.mit.gear.NavDrawer.NavDrawerListAdapter;
import com.mit.gear.ShareActionProvider.SmartShareActionProvider;
import com.mit.gear.data.DataStorage;
import com.mit.gear.data.UserDataCollection;
import com.mit.gear.words.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Activity that represents the starting screen of the app
 */

public class MainActivity extends AppCompatActivity {
	private String TAG = "MainActivity";
	private DrawerLayout mDrawerLayout;
	public static ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
    public static Context context;
	private MenuItem clearItem;
	private MenuItem shareItem;
	public static String ScreenSize;


	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	public static CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	public static ArrayList<NavDrawerItem> navDrawerItems;
	public static NavDrawerListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Get screen size to change font and size accordingly
		int screenSize = getResources().getConfiguration().screenLayout &
				Configuration.SCREENLAYOUT_SIZE_MASK;
		switch(screenSize) {
			case Configuration.SCREENLAYOUT_SIZE_LARGE:
				ScreenSize = "Large";
				break;
			case Configuration.SCREENLAYOUT_SIZE_NORMAL:
				ScreenSize = "Normal";
				break;
		}
        context = this;
        // login with dummy user
        String defaultUserName = "defaultUser";
        UserDataCollection.login(defaultUserName);
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// news
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		//Lite Stories
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
		// stories
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
		// vocab
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		//starred
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));


		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);


		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.addDrawerListener(mDrawerToggle);

		// enabling action bar app icon and behaving it as toggle button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		if (savedInstanceState == null) {
            mDrawerLayout.openDrawer(mDrawerList);
		}
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_start, menu);
		final SmartShareActionProvider provider =
				new SmartShareActionProvider(this, SmartShareActionProvider.ShareIcon.White);		//Create new share action object and set the icon
		provider.setShareIntent(createShareIntent());
		final MenuItem item = menu.findItem(R.id.action_share);
		MenuItemCompat.setActionProvider(item, provider);
		provider.setOnShareTargetSelectedListener
				(new SmartShareActionProvider.OnShareTargetSelectedListener() {
					@Override
					public boolean onShareTargetSelected(SmartShareActionProvider source, Intent intent){
						final String app = intent.getComponent().getPackageName();
						Log.d("Chosen app",app);
						customizeIntent(app,intent);
						return false;
					}
				});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(context, UserSettingsActivity.class));
				return true;
			case R.id.action_clear:
				try {
                    //resetting the openedArticles set
                    SharedPreferences sharedPreferences;
                    sharedPreferences = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor =  sharedPreferences.edit();
                    editor.putStringSet("openedArticles", new HashSet<String>());
                    editor.commit();
					editor.putString("SortState","None");
					editor.commit();
                    StoriesSelectionActivity.needsToScore=true;
					LiteNewsFragment.needsToScore=true;
					DataStorage dataStorage = new DataStorage(context);
					dataStorage.clearUserDictionary();
					displayView(3);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		clearItem = menu.findItem(R.id.action_clear);
		shareItem = menu.findItem(R.id.action_share);
		if (mTitle.equals("Vocabulary")){
			clearItem.setVisible(true);
			shareItem.setVisible(true);
		}else{
			clearItem.setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Displaying fragment view for selected nav drawer list item
	 * */

	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			Log.d(TAG,"News Tab Opened");
			fragment = new StoriesSelectionActivity();
			break;
		case 1:
			Log.d(TAG,"Lite News Tab Opened");
			fragment = new LiteNewsFragment();
				break;
		case 2:
			Log.d(TAG,"Stories Tab Opened");
			fragment = new SuggestedStoriesActivity();
			break;
		case 3:
			Log.d(TAG,"Vocabulary Tab Opened");
			fragment = new DisplayVocabularyActivity();
			break;

		case 4:
			Log.d(TAG,"Starred Tab Opened");
			fragment = new StarredNewsFragment();
			break;


		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/*
	 * Method to create intent to share with other app
	 * by setting the intent action to send
	 */
	private Intent createShareIntent(){
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TITLE,"Vocabulary List");
		shareIntent.setType("text/plain");
		return shareIntent;
	}

	/*
	 * Method to customize intent text depending on the selected app
	 */
	private void customizeIntent(String app, Intent intent){
		switch (app){
			case"com.google.android.gm":
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_SUBJECT, "GEAR vocabulary list");
				intent.putExtra(Intent.EXTRA_TEXT,
						"Check out my vocabulary list from the GEAR app\n"+getVocabularyString());
				break;
			case "com.google.android.keep":
				intent.putExtra(Intent.EXTRA_SUBJECT, "GEAR vocabulary list");
				intent.putExtra(Intent.EXTRA_TEXT,
						"Check out my vocabulary list from the GEAR app\n"+getVocabularyString());
				break;
			case "com.google.android.apps.docs":
				intent.putExtra(Intent.EXTRA_TEXT,
						"My vocabulary list copied from the GEAR app\n"+getVocabularyString());
				break;
			default:
				intent.putExtra(Intent.EXTRA_TEXT,
						"Check out my vocabulary list from the GEAR app\n"+getVocabularyString());
		}
	}

	/*
	 * Method to generate vocabulary string by reading user dictionary
	 */
	public static String getVocabularyString() {
		DataStorage dataStorage = new DataStorage(context);
		HashMap<String, Word> vocabulary = dataStorage.loadUserDictionary();
		String vocabString ="";
		if (vocabulary.isEmpty()) {
			vocabString = " ";
		}else{
			vocabString = "Word\t\tDefinition\n";
			//Loop through user dictionary and list vocabulary words
			for (Map.Entry<String, Word> entry : vocabulary.entrySet()) {
				String key = entry.getKey();
				Word word = entry.getValue();
				if(word.getLemma().equals("None"))
					vocabString += key +",\t\t"+ "\n";
				else
					vocabString += key + ",\t\t"+word.getLemma()+ "\n";
			}
		}
		return vocabString;
	}

}
