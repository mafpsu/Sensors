package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

	private static final String EXTRA_ACTION_MODE_EDIT = "EXTRA_ACTION_MODE_EDIT";
	private static final String EXTRA_ACTION_MODE_SELECTED_ITEMS = "EXTRA_ACTION_MODE_SELECTED_ITEMS";

	private DataFilesAdapter dataFilesAdapter;
	private ListView lvDataFiles;
	private MenuItem menuDelete;
	private MenuItem menuEmail;
	private boolean resumeActionModeEdit;
	private long[] savedActionModeItems;

	private ActionMode editMode;
	private final ActionMode.Callback editModeCallback = new EditModeCallback();
	
	// *********************************************************************************
	// *                                Fragment
	// *********************************************************************************

	public Fragment_MainDataFiles() {
	}

	/**
	 * Called to do the initial creation of the fragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			Log.v(MODULE_TAG, "Cycle: onCreate()");
	
			if (null != savedInstanceState) {
				resumeActionModeEdit = savedInstanceState.getBoolean(EXTRA_ACTION_MODE_EDIT, false);
				if (null == (savedActionModeItems = savedInstanceState.getLongArray(EXTRA_ACTION_MODE_SELECTED_ITEMS))) {
					savedActionModeItems = new long[0];
				}
			}
			else {
				resumeActionModeEdit = false;
				savedActionModeItems = new long[0];
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Called once the fragment has been created in order for it
	 * to create it's user interface.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(MODULE_TAG, "Cycle: onCreateView");

		View rootView = null;

		try {
			if ( null != (rootView = inflater.inflate(R.layout.fragment_main_data_files, (ViewGroup) null))) {

				lvDataFiles = (ListView) rootView.findViewById(R.id.lv_data_files);
				lvDataFiles.setOnItemClickListener(new DataFiles_OnItemClickListener());

				setHasOptionsMenu(true);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onResume");
			populateFileList();
			if (resumeActionModeEdit) {
				startActionModeEdit();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Save UI state changes to the savedInstanceState variable.
	 * This bundle will be passed to onCreate, onCreateView, and
	 * onCreateView if the parent activity is killed and restarted
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			if (editMode != null) {
				// record action mode state
				savedInstanceState.putBoolean(EXTRA_ACTION_MODE_EDIT, true);
				if (null != dataFilesAdapter) {
					long[] selectedItems = dataFilesAdapter.getSelectedItemsArray();
					if(selectedItems.length > 0) {
						savedInstanceState.putLongArray(EXTRA_ACTION_MODE_SELECTED_ITEMS, selectedItems);
					}
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(savedInstanceState);
	}
	
	/**
	 * Creates menu items
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		try {
			// Inflate the menu items for use in the action bar
			inflater.inflate(R.menu.edit, menu);
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
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_edit:
				return startActionModeEdit();
				
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	// *********************************************************************************
	// *                             Fragment Actions
	// *********************************************************************************

	private void populateFileList() {
		try {
			// Get data source
			ArrayList<DataFileInfo> dataFileInfos = MyApplication.getInstance().getAppDataFiles(getActivity());

			dataFilesAdapter = new DataFilesAdapter(getActivity().getLayoutInflater(),
					dataFileInfos,
					getResources().getColor(R.color.default_color),
					getResources().getColor(R.color.pressed_color));

			lvDataFiles.setAdapter(dataFilesAdapter);
			lvDataFiles.invalidate();
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void clearSelections() {
		
		int numListViewItems = lvDataFiles.getChildCount();
		
		dataFilesAdapter.clearSelectedItems();

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
			
			try {
				if (editMode != null) {
					
					// toggle selection
					dataFilesAdapter.toggleSelection(id);
					
					// set selection background color
					if (dataFilesAdapter.isSelected(id)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					// If there are devices to delete, enable delete and email menu items
					menuDelete.setEnabled(dataFilesAdapter.numSelectedItems() > 0);
					menuEmail.setEnabled(dataFilesAdapter.numSelectedItems() > 0);

					editMode.setTitle(dataFilesAdapter.numSelectedItems() + " Selected");
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
	// *                              Edit Action Mode
	// *********************************************************************************

	/**
	 * Starts the edit action mode.
	 * @return true if new action mode was started, false otherwise.
	 */
	private boolean startActionModeEdit() {
		if (editMode != null) {
			return false;
		}
		// Start the CAB using the ActionMode.Callback defined above
		editMode = getActivity().startActionMode(editModeCallback);
		return true;
	}
	
	private final class EditModeCallback implements ActionMode.Callback {

		/**
		 * Called when the action mode is created; startActionMode() was called
		 */
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
				// Inflate a menu resource providing context menu items
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.data_files_context_menu, menu);
				dataFilesAdapter.setSelectedItems(savedActionModeItems);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return true;
		}

		/**
		 * Called each time the action mode is shown. Always
		 * called after onCreateActionMode, but may be called
		 * multiple times if the mode is invalidated.
		 */
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			try {
				int numSelectedItems = dataFilesAdapter.getSelectedItems().size();

				menuDelete = menu.findItem(R.id.action_delete_data_files);
				menuDelete.setEnabled(numSelectedItems > 0);
				
				menuEmail = menu.findItem(R.id.action_email_data_files);
				menuEmail.setEnabled(numSelectedItems > 0);
				
				mode.setTitle(numSelectedItems + " Selected");
				return true;
				}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false; // Return false if nothing is done
		}

		/**
		 * Called when the user selects a contextual menu item
		 */
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			try {
				switch (item.getItemId()) {

				case R.id.action_delete_data_files:
					actionDeleteSelectedFiles();
					mode.finish(); // Action picked, so close the CAB
					return true;
					
				case R.id.action_email_data_files:
					if (MyApplication.getInstance().getRawDataEmailAddress().equals("")) {
						dialogEmailAddressNotSet();
					}
					else {
						actionEmailSelectedDataFiles();
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

		/**
		 * Called when the user exits the action mode
		 */
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			try {
				editMode = null;
				clearSelections();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}

		/**
		 * Delete selected data files
		 */
		private void actionDeleteSelectedFiles() {
			try {
				MyApplication.getInstance().deleteDataFiles(dataFilesAdapter.getSelectedDataFileInfos());
				populateFileList();
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
			Email email = new Email(MyApplication.getInstance().getRawDataEmailAddress(), 
					dataFilesAdapter.getSelectedDataFileInfos());
			
			// Launch the email activity to send the email
			transitionToEmailActivity(email);
		}
	}

	// *********************************************************************************
	// *                            Dialog E-mail address not set
	// *********************************************************************************

	/**
	 * Build dialog telling user that the GPS is not available
	 */
	private void dialogEmailAddressNotSet() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.fmdf_deans_title);
		builder.setMessage(R.string.fmdf_deans_message);
		builder.setPositiveButton(R.string.fmdf_deans_button_ok, new DialogEmailAddressNotSet_ButtonOk());
		builder.setNegativeButton(R.string.fmdf_deans_button_cancel, new DialogEmailAddressNotSet_ButtonCancel());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogEmailAddressNotSet_ButtonOk implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				dialog.cancel();
				transitionToSettingsActivity();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogEmailAddressNotSet_ButtonCancel implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				dialog.cancel();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                                       Transitions
	// *********************************************************************************

	private void transitionToEmailActivity(Email email) {
		try {
			
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_EMAIL, email.getAddresses());
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

	private void transitionToSettingsActivity() {
		//Intent intent = new Intent(this, Activity_About.class);
		Intent intent = new Intent(getActivity(), Activity_UserPreferences.class);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
