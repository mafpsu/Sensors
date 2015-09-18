package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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

	public SavedDevicesAdapter savedDevicesAdapter;
	private ListView lvSavedDevices;
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
	
				lvSavedDevices = (ListView) rootView.findViewById(R.id.list_shimmer_sensors);
				lvSavedDevices.setOnItemClickListener(new SavedDevices_OnItemClickListener());
	
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
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                String address = data.getExtras().getString(Activity_ShimmerDeviceList.EXTRA_DEVICE_ADDRESS);
	                Log.d("ShimmerActivity",address);
	          		/*mService.connectShimmer(address, "Device");
	          		mBluetoothAddress = address;
	          		mService.setGraphHandler(mHandler);*/
	            }
	            break;
	    	case REQUEST_COMMAND_SHIMMER:
	    		/*
	    		if (resultCode == Activity.RESULT_OK) {
		    		if(data.getExtras().getBoolean("ToggleLED",false) == true)
		    		{
		    			mService.toggleAllLEDS();
		    		}
		    		
		    		if(data.getExtras().getDouble("SamplingRate",-1) != -1)
		    		{
		    			mService.writeSamplingRate(mBluetoothAddress, data.getExtras().getDouble("SamplingRate",-1));
		    			Log.d("ShimmerActivity",Double.toString(data.getExtras().getDouble("SamplingRate",-1)));
		    			mGraphSubSamplingCount=0;
		    		}
		    		
		    		if(data.getExtras().getInt("AccelRange",-1) != -1)
		    		{
		    			mService.writeAccelRange(mBluetoothAddress, data.getExtras().getInt("AccelRange",-1));
		    		}
		    		
		    		if(data.getExtras().getInt("GyroRange",-1) != -1)
		    		{
		    			mService.writeGyroRange(mBluetoothAddress, data.getExtras().getInt("GyroRange",-1));
		    		}
		    		
		    		if(data.getExtras().getInt("PressureResolution",-1) != -1)
		    		{
		    			mService.writePressureResolution(mBluetoothAddress, data.getExtras().getInt("PressureResolution",-1));
		    		}
		    		
		    		if(data.getExtras().getInt("MagRange",-1) != -1)
		    		{
		    			mService.writeMagRange(mBluetoothAddress, data.getExtras().getInt("MagRange",-1));
		    		}
		    		
		    		if(data.getExtras().getInt("GSRRange",-1) != -1)
		    		{
		    			mService.writeGSRRange(mBluetoothAddress,data.getExtras().getInt("GSRRange",-1));
		    		}
		    		if(data.getExtras().getDouble("BatteryLimit",-1) != -1)
		    		{
		    			mService.setBattLimitWarning(mBluetoothAddress, data.getExtras().getDouble("BatteryLimit",-1));
		    		}
		    		
	    		}*/
	    		break;
	    	case REQUEST_LOGFILE_SHIMMER:
	    		/*if (resultCode == Activity.RESULT_OK) {
	    			mEnableLogging = data.getExtras().getBoolean("LogFileEnableLogging");
	    			if (mEnableLogging==true){
	    				mService.setEnableLogging(mEnableLogging);
	    			}
	    			//set the filename in the LogFile
	    			mFileName=data.getExtras().getString("LogFileName");
	    			mService.setLoggingName(mFileName);
	    			
	    			if (mEnableLogging==false){
	    	        	mTitleLogging.setText("Logging Disabled");
	    	        } else if (mEnableLogging==true){
	    	        	mTitleLogging.setText("Logging Enabled");
	    	        }
	    			
	    		}*/
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
			ArrayList<AntDeviceInfo> antDeviceInfos = MyApplication.getInstance().getAppDevices();

			savedDevicesAdapter = new SavedDevicesAdapter(getActivity().getLayoutInflater(), antDeviceInfos,
					getResources().getColor(R.color.default_color), 
					getResources().getColor(R.color.pressed_color));

			lvSavedDevices.setAdapter(savedDevicesAdapter);
			lvSavedDevices.invalidate();
		} catch(Exception ex) {
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
				if (editMode != null) {
					
					// toggle selection
					savedDevicesAdapter.toggleSelection(antDeviceNumber);
					
					// set selection background color
					if (savedDevicesAdapter.isSelected(antDeviceNumber)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					// If there are devices to delete, enable delete menu item
					menuDelete.setEnabled(savedDevicesAdapter.numSelectedItems() > 0);
					editMode.setTitle(savedDevicesAdapter.numSelectedItems() + " Selected");
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
				int numSelectedItems = savedDevicesAdapter.getSelectedItems().size();
				
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
			
			ArrayList<Long> antDeviceNumbers = savedDevicesAdapter.getSelectedItems();

			try {
				// delete selected trips
				for (long antDeviceNumber: antDeviceNumbers) {
					try {
						MyApplication.getInstance().deleteAppDevice((int)antDeviceNumber);
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
}
