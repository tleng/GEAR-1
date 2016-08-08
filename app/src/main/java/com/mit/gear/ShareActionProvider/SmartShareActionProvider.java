/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mit.gear.ShareActionProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.mattmellor.gear.R;
import com.mit.gear.activities.MainActivity;

/**
 * This is a provider for a share action. It is responsible for creating views
 * that enable data sharing and also to show a sub menu with sharing activities
 * if the hosting item is placed on the overflow menu.
 */
public class SmartShareActionProvider extends ActionProvider {
	/**
	 * Listener for the event of selecting a share target.
	 */
	public static interface OnShareTargetSelectedListener{
		/**
		 * Called when a share target has been selected. The client can
		 * decide whether to perform some action before the sharing is
		 * actually performed.
		 * @param source The source of the notification.
		 * @param intent The intent for launching the chosen share target.
		 * @return The return result is ignored. Always return false for consistency.
		 */
		public boolean onShareTargetSelected(SmartShareActionProvider source, Intent intent);

	}

	public static enum ShareIcon{
		White, Black;
	}

	private static final int DEFAULT_INITIAL_ACTIVITY_COUNT = 4;  				 		//The default for the maximal number of activities shown in the sub-menu.
	private int mMaxShownActivityCount = DEFAULT_INITIAL_ACTIVITY_COUNT;		 		//The the maximum number activities shown in the sub-menu.
	private final ShareMenuItemOnMenuItemClickListener mOnMenuItemClickListener 		//Listener for handling menu item clicks.
			= new ShareMenuItemOnMenuItemClickListener();
	public static final String DEFAULT_SHARE_HISTORY_FILE_NAME = "share_history.xml"; 	//The default name for storing share history.
	private final Context mContext;														//Context for accessing resources.
	private final int mDrawableResId;
	private final Drawable mDrawable;
	private String mShareHistoryFileName = DEFAULT_SHARE_HISTORY_FILE_NAME;				//The name of the file with share history data.
	private OnShareTargetSelectedListener mOnShareTargetSelectedListener;
	private SmartActivityChooserModel.OnChooseActivityListener mOnChooseActivityListener;

	/**
	 * Creates a new instance.
	 * @param context Context for accessing resources.
	 * @param icon    ShareIcon for stylizing the icon.
	 */
	public SmartShareActionProvider(Context context, ShareIcon icon){
		super(context);
		mContext = context;
		mDrawable = null;
		mDrawableResId = icon == ShareIcon.White ? R.drawable.ic_action_share
				: R.drawable.ic_action_share_black;
	}

	/**
	 * Creates a new instance.
	 * @param context  Context for accessing resources.
	 * @param drawable drawable for stylizing the icon.
	 */
	public SmartShareActionProvider(Context context, Drawable drawable){
		super(context);
		mContext = context;
		mDrawable = drawable;
		mDrawableResId = -1;
	}

	/**
	 * Sets a listener to be notified when a share target has been selected.
	 * The listener can optionally decide to handle the selection and
	 * not rely on the default behavior which is to launch the activity.
	 * @param listener The listener.
	 */
	public void setOnShareTargetSelectedListener(OnShareTargetSelectedListener listener){
		mOnShareTargetSelectedListener = listener;
		setActivityChooserPolicyIfNeeded();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View onCreateActionView(){
		// Create the view and set its data model.
		SmartActivityChooserModel dataModel = SmartActivityChooserModel.get(mContext, mShareHistoryFileName);
		SmartActivityChooserView activityChooserView = new SmartActivityChooserView(mContext);
		activityChooserView.setActivityChooserModel(dataModel);

		final Drawable drawable = mDrawable == null ? mContext.getResources().getDrawable(mDrawableResId) : mDrawable;
		activityChooserView.setExpandActivityOverflowButtonDrawable(drawable);
		activityChooserView.setProvider(this);

		// Set content description.
		activityChooserView.setDefaultActionButtonContentDescription
				(R.string.abc_shareactionprovider_share_with_application);
		activityChooserView.setExpandActivityOverflowButtonContentDescription
				(R.string.abc_shareactionprovider_share_with);

		return activityChooserView;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasSubMenu()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPrepareSubMenu(SubMenu subMenu){
		// Clear since the order of items may change.
		subMenu.clear();
		SmartActivityChooserModel dataModel =
				SmartActivityChooserModel.get(mContext, mShareHistoryFileName);
		PackageManager packageManager = mContext.getPackageManager();

		final int expandedActivityCount = dataModel.getActivityCount();
		final int collapsedActivityCount = Math.min(expandedActivityCount, mMaxShownActivityCount);

		// Populate the sub-menu with a sub set of the activities.
		for (int i = 0; i < collapsedActivityCount; i++){
			ResolveInfo activity = dataModel.getActivity(i);
			subMenu.add(0, i, i, activity.loadLabel(packageManager))
					.setIcon(activity.loadIcon(packageManager))
					.setOnMenuItemClickListener(mOnMenuItemClickListener);
		}

		if (collapsedActivityCount < expandedActivityCount){
			// Add a sub-menu for showing all activities as a list item.
			SubMenu expandedSubMenu = subMenu.addSubMenu
					(Menu.NONE, collapsedActivityCount, collapsedActivityCount,
							mContext.getString(R.string.abc_activity_chooser_view_see_all));
			for (int i = 0; i < expandedActivityCount; i++){
				ResolveInfo activity = dataModel.getActivity(i);
				expandedSubMenu.add(0, i, i, activity.loadLabel(packageManager))
						.setIcon(activity.loadIcon(packageManager))
						.setOnMenuItemClickListener(mOnMenuItemClickListener);
			}
		}
	}

	/**
	 * Sets the file name of a file for persisting the share history which
	 * history will be used for ordering share targets.
	 * @param shareHistoryFile The share history file name.
	 */
	public void setShareHistoryFileName(String shareHistoryFile){
		mShareHistoryFileName = shareHistoryFile;
		setActivityChooserPolicyIfNeeded();
	}

	/**
	 * Sets an intent with information about the share action.
	 * @param shareIntent The share intent.
	 * @see Intent#ACTION_SEND
	 * @see Intent#ACTION_SEND_MULTIPLE
	 */
	public void setShareIntent(Intent shareIntent){
		SmartActivityChooserModel dataModel = SmartActivityChooserModel.get(mContext, mShareHistoryFileName);
		dataModel.setIntent(shareIntent);
	}

	/**
	 * Reusable listener for handling share item clicks.
	 */
	private class ShareMenuItemOnMenuItemClickListener implements OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			SmartActivityChooserModel dataModel = SmartActivityChooserModel.get(mContext, mShareHistoryFileName);
			final int itemId = item.getItemId();
			Intent launchIntent = dataModel.chooseActivity(itemId);
			if (launchIntent != null) {
				launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				mContext.startActivity(launchIntent);
			}
			return true;
		}
	}

	/**
	 * Set the activity chooser policy of the model backed by the current
	 * share history file if needed which is if there is a registered callback.
	 */
	private void setActivityChooserPolicyIfNeeded(){
		if (mOnShareTargetSelectedListener == null){
			return;
		}
		if (mOnChooseActivityListener == null){
			mOnChooseActivityListener = new ShareActivityChooserModelPolicy();
		}
		SmartActivityChooserModel dataModel = SmartActivityChooserModel.get(mContext, mShareHistoryFileName);
		dataModel.setOnChooseActivityListener(mOnChooseActivityListener);
	}

	/**
	 * Policy that delegates to the {@link OnShareTargetSelectedListener}, if such.
	 */
	private class ShareActivityChooserModelPolicy
			implements SmartActivityChooserModel.OnChooseActivityListener{

		@Override
		public boolean onChooseActivity(SmartActivityChooserModel host, Intent intent){
			//If vocabulary list is empty do not open the chosen share app
			if(MainActivity.getVocabularyString().equals(" ")&&
					MainActivity.mTitle.equals("Vocabulary")){
				Toast.makeText(MainActivity.context, "No data to share\nVocabulary list is empty",
						Toast.LENGTH_LONG).show();
				return true;
			}else {
				if (mOnShareTargetSelectedListener != null) {
					mOnShareTargetSelectedListener.onShareTargetSelected(SmartShareActionProvider.this, intent);
				}
			}
			return false;
		}
	}
}