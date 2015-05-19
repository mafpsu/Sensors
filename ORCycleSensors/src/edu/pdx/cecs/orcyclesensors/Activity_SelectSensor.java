package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class Activity_SelectSensor extends ListActivity {

	private static final String MODULE_TAG = "Fragment_MainSensors";
	
	// UI Elements
	private Button btnOk = null;
	private Button btnCancel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	try {
			setContentView(R.layout.activity_select_sensor);
			ListView listView = (ListView) findViewById(android.R.id.list);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	
	        SensorListAdapter adapter = new SensorListAdapter((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE));
			LoadSensorItems(MyApplication.getInstance().getAppSensors(), adapter);
	        setListAdapter(adapter);
	        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        
	        btnOk = (Button) findViewById(R.id.btn_ass_ok);
	        btnOk.setOnClickListener(new ButtonOk_OnClickListener());
	        
	        btnCancel = (Button) findViewById(R.id.btn_ass_cancel);
	        btnCancel.setOnClickListener(new ButtonCancel_OnClickListener());
	        
	        setTitle("Choose sensors");
    	}
    	catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
    	}
    }

	/**
	 * Scans hardware for sensor info and loads it into the adapter
	 * @param adapter
	 */
    private void LoadSensorItems(ArrayList<SensorItem> savedSensors, SensorListAdapter adapter) {
    	SensorManager sensorManager;
		boolean checked;
		int rate;
		
		// Get reference to sensor manager
    	if (null != (sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE))) {
    		List<Sensor> availableSensors;
    		// Get list of available sensors
	    	if (null != (availableSensors = sensorManager.getSensorList(Sensor.TYPE_ALL))) {    	
		    	for (Sensor availableSensor : availableSensors) {
		    		checked = false;
		    		rate = SensorManager.SENSOR_DELAY_NORMAL;
		    		// Search for matching sensor name and check it if found
		    		for (SensorItem savedSensor : savedSensors) {
		    			if (savedSensor.getName().equals(availableSensor.getName())) {
		    				checked = true;
		    				rate = savedSensor.getRate();
		    				break;
		    			}
		    		}
		    		// Add found sensor to adapter
		    		adapter.addItem(new SensorListItem(availableSensor, rate, checked));
		    	}
	    	}
    	}
    }    

	@Override
	public void onSaveInstanceState (Bundle outState) {
		try {
			Log.v(MODULE_TAG, "Cycle: Dialog_SelectSensor onSaveInstanceState");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(outState);
	}

	// *********************************************************************************
	// *                              Button Handlers
	// *********************************************************************************

	/**
     * Class: ButtonOk_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class ButtonOk_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				SensorListAdapter adapter = (SensorListAdapter) getListAdapter();
				MyApplication.getInstance().setAppSensors(adapter.getSelectedSensors());
				Activity_SelectSensor.this.finish();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	/**
     * Class: ButtonCancel_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class ButtonCancel_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				Activity_SelectSensor.this.finish();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

}
