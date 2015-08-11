package edu.pdx.cecs.orcyclesensors;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class Activity_Main extends FragmentActivity implements
	ActionBar.TabListener {

	private static final String MODULE_TAG = "Activity_Main";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	private static final int FRAG_INDEX_RECORD = 0;
	private static final int FRAG_INDEX_TRIPS = 1;
	private static final int FRAG_INDEX_DEVICES = 2;
	private static final int FRAG_INDEX_SENSORS = 3;
	private static final int FRAG_INDEX_DATA_FILES = 4;

	private int fragmentToShow = FRAG_INDEX_RECORD;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private Fragment fragmentRecord;
	private Fragment fragmentTrips;
	private Fragment fragmentDevices;
	private Fragment fragmentSensors;
	private Fragment fragmentDataFiles;
	private int tabIndex = -1;

	// *********************************************************************************
	// *                             Activity Events
	// *********************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Instantiate fragments
		fragmentRecord = new Fragment_MainRecord();
		fragmentTrips = new Fragment_MainTrips();
		fragmentDevices = new Fragment_MainDevices();
		fragmentSensors = new Fragment_MainSensors();
		fragmentDataFiles = new Fragment_MainDataFiles();
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.psu_green));

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		int numFragments = mSectionsPagerAdapter.getCount();
		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < numFragments; i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.

			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		mViewPager.setOffscreenPageLimit(numFragments);

		Intent intent;
		Bundle bundle;
		fragmentToShow = FRAG_INDEX_RECORD;
		if (null != (intent = getIntent())) {
			if (null != (bundle = intent.getExtras())) {
				setFragmentToShow(bundle, Controller.EXTRA_SHOW_FRAGMENT_RECORD);
			}
		}
	}
	
	private void setFragmentToShow(Bundle bundle, int defaultFragment) {
		switch(bundle.getInt(Controller.EXTRA_SHOW_FRAGMENT, Controller.EXTRA_SHOW_FRAGMENT_RECORD)) {

		case Controller.EXTRA_SHOW_FRAGMENT_RECORD:
			fragmentToShow = FRAG_INDEX_RECORD;
			break;
		
		case Controller.EXTRA_SHOW_FRAGMENT_TRIPS:
			fragmentToShow = FRAG_INDEX_TRIPS;
			break;
		
		case Controller.EXTRA_SHOW_FRAGMENT_DEVICES:
			fragmentToShow = FRAG_INDEX_DEVICES;
			break;
		
		case Controller.EXTRA_SHOW_FRAGMENT_SENSORS:
			fragmentToShow = FRAG_INDEX_SENSORS;
			break;
		
		case Controller.EXTRA_SHOW_FRAGMENT_DATA_FILES:
			fragmentToShow = FRAG_INDEX_DATA_FILES;
			break;
		
		default:
			break;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();

		Log.v(MODULE_TAG, "Cycle: TabsConfig onResume");

		try {
			final ActionBar actionBar = getActionBar();
			actionBar.selectTab(actionBar.getTabAt(fragmentToShow));
			MyApplication.getInstance().ResumeNotification();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		Log.v(MODULE_TAG, "Cycle: TabsConfig onResume");

		try {
			outState.putInt(Controller.EXTRA_SHOW_FRAGMENT, tabIndex);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle outState) {

		Log.v(MODULE_TAG, "Cycle: TabsConfig onRestoreInstanceState");

		try {
			if (null != outState) {
				setFragmentToShow(outState, Controller.EXTRA_SHOW_FRAGMENT_UNDEFINED);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			transitionToSettingsActivity();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// *********************************************************************************
	// *                    ActionBar.TabListener Implementation
	// *********************************************************************************

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		try {
			// When the given tab is selected, switch to the corresponding page in
			// the ViewPager.
			// Toast.makeText(this, "TabSelected", Toast.LENGTH_LONG).show();
			mViewPager.setCurrentItem(tab.getPosition());
			final ActionBar actionBar = getActionBar();
			switch (tabIndex = tab.getPosition()) {
			case FRAG_INDEX_RECORD:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				break;
			case FRAG_INDEX_TRIPS:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setTitle(R.string.app_name);
				break;
			case FRAG_INDEX_DEVICES:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setTitle(R.string.app_name);
				break;
			case FRAG_INDEX_SENSORS:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setTitle(R.string.app_name);
				break;
			case FRAG_INDEX_DATA_FILES:
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(true);
				actionBar.setTitle(R.string.app_name);
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

		mViewPager.startActionMode(new ActionMode.Callback() {
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	// *********************************************************************************
	// *                                    Transitions
	// *********************************************************************************

	private void transitionToSettingsActivity() {
		//Intent intent = new Intent(this, Activity_About.class);
		Intent intent = new Intent(this, Activity_UserPreferences.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	// *********************************************************************************
	// *                               SectionsPagerAdapter
	// *********************************************************************************

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			switch (position) {
			case FRAG_INDEX_RECORD:
				return fragmentRecord;
			case FRAG_INDEX_TRIPS:
				return fragmentTrips;
			case FRAG_INDEX_DEVICES:
				return fragmentDevices;
			case FRAG_INDEX_SENSORS:
				return fragmentSensors;
			case FRAG_INDEX_DATA_FILES:
				return fragmentDataFiles;
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case FRAG_INDEX_RECORD:
				return getString(R.string.tab_title_record).toUpperCase(l);
			case FRAG_INDEX_TRIPS:
				return getString(R.string.tab_title_trips).toUpperCase(l);
			case FRAG_INDEX_DEVICES:
				return getString(R.string.tab_title_devices).toUpperCase(l);
			case FRAG_INDEX_SENSORS:
				return getString(R.string.tab_title_sensors).toUpperCase(l);
			case FRAG_INDEX_DATA_FILES:
				return getString(R.string.tab_title_data_files).toUpperCase(l);
			}
			return null;
		}
	}
}
