package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

import android.content.SharedPreferences;
import android.util.Log;

public class AppDevices {

	private static final String MODULE_TAG = "PairedDevices";

	/**
     * Reference to single instance
     */
    private static AppDevices appDevices = null;

    /**
     * Returns the class instance of the MyApplication object
     */
    public static AppDevices getInstance() {
    	
    	if (null == appDevices) {
    		appDevices = new AppDevices();
    	}
        return appDevices;
    }

	private ArrayList<AntDeviceInfo> devices = new ArrayList<AntDeviceInfo>();

	private AppDevices() {
        // Set reference to this instance
		appDevices = this;
	}
	
	public void addDevice(int number, String name, DeviceType deviceType) {
		
		// Note that only one device of a given type is allowed
		for (AntDeviceInfo info: devices) {
			if (deviceType == info.getDeviceType()) {
				devices.remove(info);
			}
		}

		devices.add(new AntDeviceInfo(number, name, deviceType));
	}
	
	public void deleteDevice(int number) {
		
		for (AntDeviceInfo info: devices) {
			if (number == info.getNumber()) {
				devices.remove(info);
				break;
			}
		}
	}

	public void saveTo(SharedPreferences settings, String key) {
		
		SharedPreferences.Editor editor = settings.edit();
		editor = settings.edit();
		editor.putString(key, getJson(devices));
		editor.apply();
	}

	private static String getJson(ArrayList<AntDeviceInfo> pairedDevices) {
		
		JSONArray list = new JSONArray();
		
		for (AntDeviceInfo deviceInfo: pairedDevices) {
			try {
				JSONObject o = new JSONObject();
				o.put("name", deviceInfo.getName());
				o.put("number", deviceInfo.getNumber());
				o.put("type", deviceInfo.getDeviceType().getIntValue());
				list.put(o);
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
		return list.toString();
	}
	
	public void loadFrom(SharedPreferences settings, String key) {
		devices.clear();
		String setting = settings.getString(key, null);
		if ((null != setting) && (!setting.equals(""))) {
			try {
				JSONArray a = new JSONArray(setting);
				JSONObject o;
				int number;
				String name;
				int type;
		
				for (int i = 0; i < a.length(); ++i) {
					try {
						o = a.getJSONObject(i);
						number = o.getInt("number");
						name = o.getString("name");
						type = o.getInt("type");
						devices.add(new AntDeviceInfo(number, name, DeviceType.getValueFromInt(type)));
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

	public ArrayList<AntDeviceInfo> getAntDeviceInfos() {
		
		ArrayList<AntDeviceInfo> newList = new ArrayList<AntDeviceInfo>();
		for (AntDeviceInfo info: devices) {
			newList.add(new AntDeviceInfo(info.getNumber(), info.getName(), info.getDeviceType()));
		}
		return newList;
	}
}
