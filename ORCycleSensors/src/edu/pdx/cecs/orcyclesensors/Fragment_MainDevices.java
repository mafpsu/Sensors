package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

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

public class Fragment_MainDevices extends Fragment {

	private static final String MODULE_TAG = "Fragment_MainDevices";

	private static final String EXTRA_ACTION_MODE_EDIT = "EXTRA_ACTION_MODE_EDIT";
	private static final String EXTRA_ACTION_MODE_SELECTED_ITEMS = "EXTRA_ACTION_MODE_SELECTED_ITEMS";

	public SavedDevicesAdapter savedDevicesAdapter;
	private ListView lvSavedDevices;
	private MenuItem menuDelete;
	private ArrayList<AntDeviceInfo> antDeviceInfos;

	private boolean resumeActionModeEdit;
	private long[] savedActionModeItems;

	private ActionMode actionModeEdit;
	private final ActionMode.Callback mActionModeCallback = new ActionModeEditCallback();

	// *********************************************************************************
	// *                          Fragment Life Cycle
	// *********************************************************************************

	public Fragment_MainDevices() {
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
			rootView = inflater.inflate(R.layout.fragment_main_devices, (ViewGroup) null);

			lvSavedDevices = (ListView) rootView.findViewById(R.id.listViewSavedDevices);
			lvSavedDevices.setOnItemClickListener(new SavedDevices_OnItemClickListener());

			setHasOptionsMenu(true);
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
			Log.v(MODULE_TAG, "Cycle: onResume");
			// lvAntDevices.invalidate(); TODO: Remove if this proves not necessary
			populateDeviceList();
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
			if (actionModeEdit != null) {
				// record action mode state
				savedInstanceState.putBoolean(EXTRA_ACTION_MODE_EDIT, true);
				if (null != savedDevicesAdapter) {
					long[] selectedItems = savedDevicesAdapter.getSelectedItemsArray();
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

	private void populateDeviceList() {
		try {
			// Get data source
			antDeviceInfos = MyApplication.getInstance().getAppDevices();

			savedDevicesAdapter = new SavedDevicesAdapter(getActivity().getLayoutInflater(), antDeviceInfos,
					getResources().getColor(R.color.default_color), 
					getResources().getColor(R.color.pressed_color));

			lvSavedDevices.setAdapter(savedDevicesAdapter);
			lvSavedDevices.invalidate();
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		// registerForContextMenu(lvAntDevices); TODO: Verify this isn't needed
	}

	private void deleteDevice(long deviceNumber) {
		try {
			MyApplication.getInstance().deleteAppDevice((int)deviceNumber);
			populateDeviceList();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void clearSelections() {
		
		int numListViewItems = lvSavedDevices.getChildCount();
		
		savedDevicesAdapter.clearSelectedItems();

		// Reset all list items to their normal color
		for (int i = 0; i < numListViewItems; i++) {
			lvSavedDevices.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.default_color));
		}
	}
	
	// *********************************************************************************
	// *                           Item Click Listener
	// *********************************************************************************

	private final class SavedDevices_OnItemClickListener implements AdapterView.OnItemClickListener {
		
		public void onItemClick(AdapterView<?> parent, View v, int pos, long antDeviceNumber) {
			
			Log.v(MODULE_TAG, "onItemClick (id = " + String.valueOf(antDeviceNumber) + ", pos = " + String.valueOf(pos) + ")");

			try {
				if (actionModeEdit != null) {
					savedDevicesAdapter.toggleSelection(antDeviceNumber);
					if (savedDevicesAdapter.isSelected(antDeviceNumber)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					menuDelete.setEnabled(savedDevicesAdapter.numSelectedItems() > 0);
					actionModeEdit.setTitle(savedDevicesAdapter.numSelectedItems() + " Selected");
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
		if (actionModeEdit != null) {
			return false;
		}
		// Start the CAB using the ActionMode.Callback defined above
		actionModeEdit = getActivity().startActionMode(mActionModeCallback);
		return true;
	}

	private final class ActionModeEditCallback implements ActionMode.Callback {
		
		/**
		 * Called when the action mode is created; startActionMode() was called
		 */
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_devices_context_menu, menu);
			savedDevicesAdapter.setSelectedItems(savedActionModeItems);
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
				menuDelete = menu.findItem(R.id.action_delete_saved_devices);
				menuDelete.setEnabled(savedDevicesAdapter.getSelectedItems().size() > 0);

				mode.setTitle(savedDevicesAdapter.numSelectedItems() + " Selected");
				return true;
				}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		/**
		 * Called when the user selects a contextual menu item
		 */
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			try {
				switch (item.getItemId()) {

				case R.id.action_delete_saved_devices:
					// delete selected devices
					actionDeleteSelectedDevices(savedDevicesAdapter.getSelectedItems());
					mode.finish(); // Action picked, so close the CAB
					return true;
					
				case R.id.action_add_devices:
					transitionToAddDeviceActivity();
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
				actionModeEdit = null;
				clearSelections();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private void actionDeleteSelectedDevices(ArrayList<Long> antDeviceNumbers) {
		try {
			// delete selected trips
			for (long antDeviceNumber: antDeviceNumbers) {
				try {
					deleteDevice(antDeviceNumber);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	// *********************************************************************************
	// *                                       Transitions
	// *********************************************************************************

	/**
	 * Launches Activity_AddDevice
	 */
	private void transitionToAddDeviceActivity() {
		Intent intent = new Intent(getActivity(), Activity_AddDevice.class);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		//getActivity().finish();
	}
}
