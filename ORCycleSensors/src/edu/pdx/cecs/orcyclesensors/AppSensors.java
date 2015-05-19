package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.util.Log;

public class AppSensors {
	
	private static final String MODULE_TAG = "AppSensors";
	
	private static final String SETTING_FIFO_MAX_EVENT_COUNT = "fifoMaxEventCount";
	private static final String SETTING_FIFO_RESERVED_EVENT_COUNT = "fifoReservedEventCount";
	private static final String SETTING_MAX_DELAY = "maxDelay";
	private static final String SETTING_MAXIMUM_RANGE = "maximumRange";
	private static final String SETTING_MIN_DELAY = "minDelay";
	private static final String SETTING_NAME = "name";
	private static final String SETTING_POWER = "power";
	private static final String SETTING_REPORTING_MODE = "reportingMode";
	private static final String SETTING_RESOLUTION = "resolution";
	private static final String SETTING_TYPE = "type";
	private static final String SETTING_VENDOR = "vendor";
	private static final String SETTING_VERSION = "version";
	private static final String SETTING_IS_WAKE_UP_SENSOR = "isWakeUpSensor";
	private static final String SETTING_RATE = "rate";

	/**
     * Reference to single instance
     */
    private static AppSensors appSensors = null;

    /**
     * Returns the class instance of the MyApplication object
     */
    public static AppSensors getInstance() {
    	
    	if (null == appSensors) {
    		appSensors = new AppSensors();
    	}
        return appSensors;
    }

	private AppSensors() {
        // Set reference to this instance
		appSensors = this;
	}
	
	private ArrayList<SensorItem> sensors = new ArrayList<SensorItem>();

	public void setSensors(ArrayList<SensorItem> items) {
		this.sensors.clear();
		for (SensorItem item: items) {
			this.sensors.add(new SensorItem(item));
		}
	}

	public ArrayList<SensorItem> getSensors() {
		ArrayList<SensorItem> sensorItems = new ArrayList<SensorItem>();
		for (SensorItem sensor: sensors) {
			sensorItems.add(new SensorItem(sensor));
		}
		return sensorItems;
	}

	public SensorItem getSensor(String name) {
		
		for (SensorItem sensor: sensors) {
			if (name.equals(sensor.getName()))
				return new SensorItem(sensor);
		}
		return null;
	}

	public void saveTo(SharedPreferences settings,  String key) {
		
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putString(key, getJson(sensors));
		editor.apply();
	}
	
	private static String getJson(ArrayList<SensorItem> sensors) {
		
		JSONArray array = new JSONArray();
		
		for (SensorItem sensor: sensors) {
			try {
				JSONObject o = new JSONObject();
				o.put(SETTING_FIFO_MAX_EVENT_COUNT, sensor.getFifoMaxEventCount());
				o.put(SETTING_FIFO_RESERVED_EVENT_COUNT, sensor.getFifoReservedEventCount());
				o.put(SETTING_MAX_DELAY, sensor.getMaxDelay());
				o.put(SETTING_MAXIMUM_RANGE, sensor.getMaximumRange());
				o.put(SETTING_MIN_DELAY, sensor.getMinDelay());
				o.put(SETTING_NAME, sensor.getName());
				o.put(SETTING_POWER, sensor.getPower());
				o.put(SETTING_REPORTING_MODE, sensor.getReportingMode());
				o.put(SETTING_RESOLUTION, sensor.getResolution());
				o.put(SETTING_TYPE, sensor.getType());
				o.put(SETTING_VENDOR, sensor.getVendor());
				o.put(SETTING_VERSION, sensor.getVersion());
				o.put(SETTING_IS_WAKE_UP_SENSOR, sensor.isWakeUpSensor());
				o.put(SETTING_RATE, sensor.getRate());
				array.put(o);
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
		return array.toString();
	}

	public void loadFrom(SharedPreferences settings, String key) {
		// Load the sensor information
		sensors.clear();
		String setting = settings.getString(key, null);
		if ((null != setting) && (!setting.equals(""))) {
			try {
				JSONArray a = new JSONArray(setting);
				JSONObject o;
		
				for (int i = 0; i < a.length(); ++i) {
					try {
						o = a.getJSONObject(i);
						sensors.add(new SensorItem(
								o.getInt(SETTING_FIFO_MAX_EVENT_COUNT),
								o.getInt(SETTING_FIFO_RESERVED_EVENT_COUNT),
								o.getInt(SETTING_MAX_DELAY) -1,
								(float) o.getDouble(SETTING_MAXIMUM_RANGE),
								o.getInt(SETTING_MIN_DELAY),
								o.getString(SETTING_NAME),
								(float) o.getDouble(SETTING_POWER),
								o.getInt(SETTING_REPORTING_MODE),
								(float) o.getDouble(SETTING_RESOLUTION),
								o.getInt(SETTING_TYPE),
								o.getString(SETTING_VENDOR),
								o.getInt(SETTING_VERSION),
								o.getBoolean(SETTING_IS_WAKE_UP_SENSOR),
								o.getInt(SETTING_RATE)));
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
	}


	public void updateSensor(SensorItem newSensor) {
		
		String name = newSensor.getName();
		
		for (SensorItem sensor: sensors) {
			if (name.equals(sensor.getName())) {
				sensor.copy(newSensor);
				return;
			}
		}
	}	
}
