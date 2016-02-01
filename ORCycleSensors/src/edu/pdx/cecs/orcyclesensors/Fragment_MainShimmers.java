package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.Toast;

public class Fragment_MainShimmers extends Fragment {

	private static final String MODULE_TAG = "Fragment_MainShimmers";

	private static final String EXTRA_ACTION_MODE_EDIT = "EXTRA_ACTION_MODE_EDIT";
	private static final String EXTRA_ACTION_MODE_SELECTED_ITEMS = "EXTRA_ACTION_MODE_SELECTED_ITEMS";

	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_CONFIGURE_SHIMMER = 3;
	static final int REQUEST_CONFIGURE_VIEW_SENSOR = 4;
	static final int REQUEST_COMMAND_SHIMMER = 5;
	static final int REQUEST_LOGFILE_SHIMMER = 6;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
    private static String mBluetoothAddress = null;

	public SavedShimmersAdapter savedShimmersAdapter;
	private ListView lvSavedShimmers;
	private MenuItem menuDelete;

	private boolean resumeActionModeEdit;
	private long[] savedActionModeItems;

	private ActionMode editMode;
	private final ActionMode.Callback editModeCallback = new EditModeCallback();

	// *********************************************************************************
	// *                          Fragment Life Cycle
	// *********************************************************************************

	public Fragment_MainShimmers() {
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
			if (null != (rootView = inflater.inflate(R.layout.fragment_main_shimmers, (ViewGroup) null))) {
	
				lvSavedShimmers = (ListView) rootView.findViewById(R.id.list_shimmer_sensors);
				lvSavedShimmers.setOnItemClickListener(new SavedShimmers_OnItemClickListener());
	
				setHasOptionsMenu(true);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
        	Toast.makeText(getActivity(), "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
        	//finish();
        }
        
		return rootView;
	}

    @Override
    public void onStart() {
    	super.onStart();

    	if (null != mBluetoothAdapter) {
	    	if(!mBluetoothAdapter.isEnabled()) {     	
	        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	    	}
    	}
    }
	
	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: onResume");
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
			if (editMode != null) {
				// record action mode state
				savedInstanceState.putBoolean(EXTRA_ACTION_MODE_EDIT, true);
				if (null != savedShimmersAdapter) {
					long[] selectedItems = savedShimmersAdapter.getSelectedItemsArray();
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

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		try {
	    	switch (requestCode) {
	    	
	    	case REQUEST_ENABLE_BT:
	    		
	            // When the request to enable Bluetooth returns
	            if (resultCode == Activity.RESULT_OK) {
	            	
	                //setMessage("\nBluetooth is now enabled");
	                Toast.makeText(getActivity(), "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
	            } else {
	                // User did not enable Bluetooth or an error occured
	            	Toast.makeText(getActivity(), "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
	                //finish();       
	            }
	            break;
	            
	    	case REQUEST_CONNECT_SHIMMER:
	    		
	            // DeviceListActivity returns with a shimmer device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                String bluetoothAddress = data.getExtras().getString(Activity_ShimmerDeviceList.EXTRA_BLUETOOTH_ADDRESS);
	                MyApplication.getInstance().addShimmerDevice(bluetoothAddress, "Shimmer");
	            }
	            break;
	            
	    	case REQUEST_CONFIGURE_VIEW_SENSOR:
	    		
	            if (resultCode == Activity.RESULT_OK) {
	                String bluetoothAddress = data.getExtras().getString(Activity_ShimmerSensorList.EXTRA_BLUETOOTH_ADDRESS);
	                MyApplication.getInstance().addShimmerDevice(bluetoothAddress, "Shimmer");
	            }
	    		break;
	    	}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                             Fragment Actions
	// *********************************************************************************

	private void populateDeviceList() {
		try {
			// Get data source
			ArrayList<ShimmerDeviceInfo> shimmerDeviceInfos = MyApplication.getInstance().getAppShimmers();

			savedShimmersAdapter = new SavedShimmersAdapter(getActivity().getLayoutInflater(), shimmerDeviceInfos,
					getResources().getColor(R.color.default_color), 
					getResources().getColor(R.color.pressed_color));

			lvSavedShimmers.setAdapter(savedShimmersAdapter);
			lvSavedShimmers.invalidate();
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void clearSelections() {
		
		int numListViewItems = lvSavedShimmers.getChildCount();
		
		savedShimmersAdapter.clearSelectedItems();

		// Reset all list items to their normal color
		for (int i = 0; i < numListViewItems; i++) {
			lvSavedShimmers.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.default_color));
		}
	}
	
	// *********************************************************************************
	// *                           Item Click Listener
	// *********************************************************************************

	private final class SavedShimmers_OnItemClickListener implements AdapterView.OnItemClickListener {
		
		public void onItemClick(AdapterView<?> parent, View v, int pos, long antDeviceNumber) {
			
			Log.v(MODULE_TAG, "onItemClick (id = " + String.valueOf(antDeviceNumber) + ", pos = " + String.valueOf(pos) + ")");

			try {
				if (editMode != null) {
					
					// toggle selection
					savedShimmersAdapter.toggleSelection(antDeviceNumber);
					
					// set selection background color
					if (savedShimmersAdapter.isSelected(antDeviceNumber)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					// If there are devices to delete, enable delete menu item
					menuDelete.setEnabled(savedShimmersAdapter.numSelectedItems() > 0);
					editMode.setTitle(savedShimmersAdapter.numSelectedItems() + " Selected");
				}
				else {
					
					String bluetoothAddress = savedShimmersAdapter.getItem(pos).getAddress();
					
					 // dialogSelectShimmerFunction(bluetoothAddress);
					transitionToShimmerConfigureActivity(bluetoothAddress);
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
				inflater.inflate(R.menu.saved_devices_context_menu, menu);
				savedShimmersAdapter.setSelectedItems(savedActionModeItems);
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
				int numSelectedItems = savedShimmersAdapter.getSelectedItems().size();
				
				menuDelete = menu.findItem(R.id.action_delete_saved_devices);
				menuDelete.setEnabled(numSelectedItems > 0);

				mode.setTitle(numSelectedItems + " Selected");
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
					actionDeleteSelectedDevices();
					mode.finish(); // Action picked, so close the CAB
					return true;
					
				case R.id.action_add_devices:
					if (null != mBluetoothAdapter) {
						transitionToAddDeviceActivity();
					}
					else {
			        	Toast.makeText(getActivity(), "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
					}
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
		private void actionDeleteSelectedDevices() {
			
			ArrayList<Long> indexes = savedShimmersAdapter.getSelectedItems();

			try {
				// delete selected trips
				for (long index: indexes) {
					try {
						ShimmerDeviceInfo info = savedShimmersAdapter.getItem((int)index);
						MyApplication.getInstance().deleteShimmerDevice(info.getAddress());
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				populateDeviceList();
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
		Intent intent = new Intent(getActivity(), Activity_ShimmerDeviceList.class);
		startActivityForResult(intent, REQUEST_CONNECT_SHIMMER);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		//getActivity().finish();
	}

	private void transitionToShimmerConfigureActivity(String bluetoothAddress) {
		Intent intent = new Intent(getActivity(), Activity_ShimmerConfig.class);
		intent.putExtra(Activity_ShimmerConfig.EXTRA_BLUETOOTH_ADDRESS, bluetoothAddress);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
