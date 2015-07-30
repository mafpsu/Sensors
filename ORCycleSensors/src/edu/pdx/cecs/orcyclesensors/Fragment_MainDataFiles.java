package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class Fragment_MainDataFiles extends Fragment {

	private static final String MODULE_TAG = "Fragment_MainDataFiles";
	private static final String BSENSOR_EMAIL_ADDRESS = "robin5@pdx.edu";

	private DataFilesListAdapter dataFilesListAdapter;
	private ListView lvDataFiles;
	private MenuItem deleteMenuItem;
	private MenuItem emailMenuItem;

	private ActionMode actionModeDelete;
	private final ActionMode.Callback actionModeDeleteCallback = new ActionModeDeleteCallback();
	
	private ActionMode actionModeEmail;
	private final ActionMode.Callback actionModeEmailCallback = new ActionModeEmailCallback();

	// *********************************************************************************
	// *                                Fragment
	// *********************************************************************************

	public Fragment_MainDataFiles() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = null;

		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainDevices onCreateView");

			if ( null != (rootView = inflater.inflate(R.layout.fragment_main_data_files, (ViewGroup) null))) {

				lvDataFiles = (ListView) rootView.findViewById(R.id.lv_data_files);
				
				//dataFileInfos = MyApplication.getInstance().getAppDataFiles(getActivity());
				
				// populateDeviceList();

				// selectedItems.clear();

				setHasOptionsMenu(true);
			}

		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	private void populateFileList() {
		try {
			ArrayList<DataFileInfo> dataFileInfos = MyApplication.getInstance().getAppDataFiles(getActivity());
			dataFilesListAdapter = new DataFilesListAdapter(getActivity().getLayoutInflater(),
											dataFileInfos,
											getResources().getColor(R.color.default_color),
											getResources().getColor(R.color.pressed_color));

			lvDataFiles.setAdapter(dataFilesListAdapter);
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		lvDataFiles.setOnItemClickListener(new DataFiles_OnItemClickListener());

		registerForContextMenu(lvDataFiles);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onResume");
			lvDataFiles.invalidate();
			populateFileList();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onPause");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Cleanup view resources 
	 */
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onDestroyView");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Configure menu items
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		try {
			// Inflate the menu items for use in the action bar
			// inflater.inflate(R.menu.saved_devices, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handles menu item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {

			case R.id.action_delete_data_files:
				// edit
				if (actionModeDelete != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined above
				actionModeDelete = getActivity().startActionMode(actionModeDeleteCallback);
				return true;

			case R.id.action_email_data_files:
				// edit
				if (actionModeEmail != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined above
				actionModeEmail = getActivity().startActionMode(actionModeEmailCallback);
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	private void clearSelections() {
		
		int numListViewItems = lvDataFiles.getChildCount();
		
		dataFilesListAdapter.clearSelectedItems();

		// Reset all list items to their normal color
		for (int i = 0; i < numListViewItems; i++) {
			lvDataFiles.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.default_color));
		}
	}
	
	// *********************************************************************************
	// *                           Item Click Listener
	// *********************************************************************************

	private final class DataFiles_OnItemClickListener implements AdapterView.OnItemClickListener {
		
		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			
			Log.v(MODULE_TAG, "onItemClick (id = " + String.valueOf(id) + ", pos = " + String.valueOf(pos) + ")");
			
			dataFilesListAdapter.getSelectedItems();

			try {
				if (actionModeDelete != null) {
					
					// toggle selection
					dataFilesListAdapter.toggleSelection(id);
					if (dataFilesListAdapter.isSelected(id)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					// If there are devices to delete, enable delete menu item
					deleteMenuItem.setEnabled(dataFilesListAdapter.numSelectedItems() > 0);

					actionModeDelete.setTitle(dataFilesListAdapter.numSelectedItems() + " Selected");
				}
				else if (actionModeEmail != null) {

					// toggle selection
					dataFilesListAdapter.toggleSelection(id);
					if (dataFilesListAdapter.isSelected(id)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					// If there are devices to delete, enable delete menu item
					emailMenuItem.setEnabled(dataFilesListAdapter.numSelectedItems() > 0);

					actionModeEmail.setTitle(dataFilesListAdapter.numSelectedItems() + " Selected");
				}
				else {
					//transitionToSensorDetailActivity(sensor.getName());
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                              Delete Action Mode
	// *********************************************************************************

	private final class ActionModeDeleteCallback implements ActionMode.Callback {
		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.data_files_delete_menu, menu);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			try {
				Log.v(MODULE_TAG, "onPrepareActionMode");
				deleteMenuItem = menu.getItem(0);
				deleteMenuItem.setEnabled(false);

				mode.setTitle(dataFilesListAdapter.getSelectedItems().size() + " Selected");
				}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			try {
				switch (item.getItemId()) {

				case R.id.action_delete_data_file:
					// delete selected notes
					for (int i = 0; i < dataFilesListAdapter.getSelectedItems().size(); i++) {
						actionDeleteFile(dataFilesListAdapter.getSelectedItems().get(i));
					}
					mode.finish(); // Action picked, so close the CAB
					return true;
					
				default:
					return false;
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			try {
				actionModeDelete = null;
				clearSelections();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

		private void actionDeleteFile(long position) { // TODO: change to delete by name
			try {
				MyApplication.getInstance().deleteDataFile((int)position);
				lvDataFiles.invalidate();
				populateFileList();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

	}

	// *********************************************************************************
	// *                              Email Action Mode
	// *********************************************************************************

	private final class ActionModeEmailCallback implements ActionMode.Callback {
		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.data_files_email_menu, menu);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			try {
				Log.v(MODULE_TAG, "onPrepareActionMode");
				emailMenuItem = menu.getItem(0);
				emailMenuItem.setEnabled(false);

				mode.setTitle(dataFilesListAdapter.getSelectedItems().size() + " Selected");
				}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			try {
				switch (item.getItemId()) {

				case R.id.action_email_data_file:
					actionEmailSelectedDataFiles();
					mode.finish(); // Action picked, so close the CAB
					return true;
					
				default:
					return false;
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			try {
				actionModeEmail = null;
				clearSelections();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
		
		/**
		 * Email selected data files
		 */
		private void actionEmailSelectedDataFiles() {

			// Create an email with the attached data files
			Email email = new Email(dataFilesListAdapter.getSelectedDataFileInfos());
			
			// Launch the email activity to send the email
			transitionToEmailActivity(email);
		}
	}

	// *********************************************************************************
	// *                                       Transitions
	// *********************************************************************************

	private void transitionToEmailActivity(Email email) {
		try {
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { BSENSOR_EMAIL_ADDRESS });
			intent.putExtra(Intent.EXTRA_SUBJECT, email.getSubject());
			intent.putExtra(Intent.EXTRA_TEXT, email.getText());
		    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, email.getAttachments());

		    // launch the email chooser activity and return the result to this activity
		    // startActivity(Intent.createChooser(intent, ""));

		    // For the checkbox "Use by default for this action" will appear in
		    // the application chooser dialog, thus user will be able to select the
		    // default application for sending the emails with multiple attachments
		    startActivity(intent);
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
}
