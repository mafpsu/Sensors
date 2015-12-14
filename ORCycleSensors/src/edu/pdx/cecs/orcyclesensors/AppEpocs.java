package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.util.Log;

public class AppEpocs {

	private static final String MODULE_TAG = "AppEpocs";
	private static final String SHIMMER_SETTING_NAME = "name";
	private static final String SHIMMER_SETTING_ADDRESS = "address";

	/**
     * Reference to single instance
     */
    private static AppEpocs AppEpocs = null;

    /**
     * Returns the class instance of the MyApplication object
     */
    public static AppEpocs getInstance() {
    	
    	if (null == AppEpocs) {
    		AppEpocs = new AppEpocs();
    	}
        return AppEpocs;
    }

	private ArrayList<EpocDeviceInfo> devices = new ArrayList<EpocDeviceInfo>();

	private AppEpocs() {
        // Set reference to this instance
		AppEpocs = this;
	}
	
	public void addDevice(String address, String name) {
		
		for (EpocDeviceInfo deviceInfo: devices) {
			if (deviceInfo.getAddress().equalsIgnoreCase(address)) {
				devices.remove(deviceInfo);
				break;
			}
		}
		devices.add(new EpocDeviceInfo(address, name));
	}
	
	public void deleteDevice(String address) {
		
		for (EpocDeviceInfo info: devices) {
			if (address == info.getAddress()) {
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

	private static String getJson(ArrayList<EpocDeviceInfo> pairedDevices) {
		
		JSONArray list = new JSONArray();
		
		for (EpocDeviceInfo deviceInfo: pairedDevices) {
			try {
				JSONObject o = new JSONObject();
				o.put(SHIMMER_SETTING_NAME, deviceInfo.getName());
				o.put(SHIMMER_SETTING_ADDRESS, deviceInfo.getAddress());
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
				String address;
				String name;
				int shimmerVersion;
				long enabledSensors;
		
				for (int i = 0; i < a.length(); ++i) {
					try {
						o = a.getJSONObject(i);
						address = o.getString(SHIMMER_SETTING_ADDRESS);
						name = o.getString(SHIMMER_SETTING_NAME);
						devices.add(new EpocDeviceInfo(address, name));
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

	public ArrayList<EpocDeviceInfo> getEpocDeviceInfos() {
		
		ArrayList<EpocDeviceInfo> newList = new ArrayList<EpocDeviceInfo>();
		for (EpocDeviceInfo info: devices) {
			newList.add(new EpocDeviceInfo(info.getAddress(), info.getName()));
		}
		return newList;
	}
}
