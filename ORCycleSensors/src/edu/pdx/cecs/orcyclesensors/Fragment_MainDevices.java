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

	public static final String ARG_SECTION_NUMBER = "section_number";

	private ListView lvAntDevices;
	private ArrayList<Long> devicesToDelete = new ArrayList<Long>();
	private MenuItem menuDelete;
	private ArrayList<AntDeviceInfo> antDeviceInfos;

	private ActionMode actionModeEdit;
	private final ActionMode.Callback mActionModeCallback = new ActionModeEditCallback();

	private final class AdapterView_OnItemClickListener implements AdapterView.OnItemClickListener {
		
		public void onItemClick(AdapterView<?> parent, View v, int pos, long antDeviceNumber) {
			
			Log.v(MODULE_TAG, "onItemClick (id = " + String.valueOf(antDeviceNumber) + ", pos = " + String.valueOf(pos) + ")");

			try {
				if (actionModeEdit != null) {
					if (devicesToDelete.indexOf(antDeviceNumber) > -1) {
						// highlight list item and record device to delete
						devicesToDelete.remove(antDeviceNumber);
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					} else {
						// Remove highlight from list item and remove device to delete from list of devices to delete
						devicesToDelete.add(antDeviceNumber);
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					}

					// If there are devices to delete, enable delete menu item
					if (devicesToDelete.size() == 0) {
						menuDelete.setEnabled(false);
					} else {
						menuDelete.setEnabled(true);
					}

					actionModeEdit.setTitle(devicesToDelete.size() + " Selected");
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

	public SavedDevicesAdapter savedDevicesListAdapter;

	public Fragment_MainDevices() {
	}

	// *********************************************************************************
	// *                              Activity Lifecycle
	// *********************************************************************************

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = null;

		try {
			rootView = inflater.inflate(R.layout.fragment_main_devices, (ViewGroup) null);

			Log.v(MODULE_TAG, "Cycle: Fragment_MainDevices onCreateView");

			setHasOptionsMenu(true);

			lvAntDevices = (ListView) rootView.findViewById(R.id.listViewSavedDevices);
			
			antDeviceInfos = MyApplication.getInstance().getAppDevices();
			
			populateDeviceList(lvAntDevices);

			devicesToDelete.clear();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	void populateDeviceList(ListView lv) {
		try {
			antDeviceInfos = MyApplication.getInstance().getAppDevices();

			savedDevicesListAdapter = new SavedDevicesAdapter(getActivity().getLayoutInflater(), antDeviceInfos);

			lv.setAdapter(savedDevicesListAdapter);
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		lv.setOnItemClickListener(new AdapterView_OnItemClickListener());

		registerForContextMenu(lv);
	}

	private void deleteDevice(long deviceNumber) {
		try {
			MyApplication.getInstance().deleteAppDevice((int)deviceNumber);
			lvAntDevices.invalidate();
			populateDeviceList(lvAntDevices);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onResume");
			lvAntDevices.invalidate();
			populateDeviceList(lvAntDevices);
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
		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_devices_context_menu, menu);
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
				menuDelete = menu.getItem(0);
				menuDelete.setEnabled(false);

				mode.setTitle(devicesToDelete.size() + " Selected");
				return true;
				}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			try {
				switch (item.getItemId()) {

				case R.id.action_add_devices:
					transitionToAddDeviceActivity();
					return true;

				case R.id.action_delete_saved_devices:
					// delete selected notes
					for (int i = 0; i < devicesToDelete.size(); i++) {
						deleteDevice(devicesToDelete.get(i));
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
				int numListViewItems = lvAntDevices.getChildCount();
				actionModeEdit = null;
				devicesToDelete.clear();

				// Reset all list items to their normal color
				for (int i = 0; i < numListViewItems; i++) {
					lvAntDevices.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.pressed_color));
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
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
