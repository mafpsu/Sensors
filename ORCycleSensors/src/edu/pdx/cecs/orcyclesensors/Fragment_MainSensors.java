package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class Fragment_MainSensors extends Fragment {

	private static final String MODULE_TAG = "Fragment_MainSensors";

	private ListView lvSensorsList;
	private ArrayList<SensorItem> sensors;

	private View rootView = null;

	private final class AdapterView_OnItemClickListener implements AdapterView.OnItemClickListener {
		
		public void onItemClick(AdapterView<?> parent, View v, int pos, long antDeviceNumber) {
			
			Log.v(MODULE_TAG, "onItemClick (id = " + String.valueOf(antDeviceNumber) + ", pos = " + String.valueOf(pos) + ")");

			try {
				SensorItem sensor = sensors.get(pos);
				transitionToSensorDetailActivity(sensor.getName());
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	public Fragment_MainSensors() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainDevices onCreateView");

			rootView = inflater.inflate(R.layout.fragment_main_sensors, (ViewGroup) null);
			lvSensorsList = (ListView) rootView.findViewById(R.id.list_sensors);
			lvSensorsList.setOnItemClickListener(new AdapterView_OnItemClickListener());
			
			setHasOptionsMenu(true);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

    private void LoadSensorItems(SensorRecorderListAdapter adapter) {
    	
    	sensors = MyApplication.getInstance().getAppSensors();
    	adapter.setSensors(sensors);
    }    

	// show edit button and hidden delete button
	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: SavedNotes onResume");
			PopulateSensorList();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	private void PopulateSensorList() {
		lvSensorsList.invalidate();
		LayoutInflater lf = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SensorRecorderListAdapter adapter = new SensorRecorderListAdapter(lf);
		LoadSensorItems(adapter);
		lvSensorsList.setAdapter(adapter);
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainSensors onSaveInstanceState");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainSensors onStart");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	@Override
	public void onStop() {
		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainSensors onStop");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onStop();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainSensors onPause");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainSensors onDestroyView");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/* Creates the menu items */
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

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_edit_sensors:
				transitionToSelectSensorActivity();
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

	private void transitionToSelectSensorActivity() {
		Intent intent = new Intent(getActivity(), Activity_SelectSensor.class);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void transitionToSensorDetailActivity(String sensorName) {
		Intent intent = new Intent(getActivity(), Activity_SensorDetail.class);
		intent.putExtra(Activity_SensorDetail.EXTRA_SENSOR_NAME, sensorName);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
