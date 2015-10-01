package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

import android.content.SharedPreferences;
import android.util.Log;

public class AppShimmers {

	private static final String MODULE_TAG = "AppShimmers";

	/**
     * Reference to single instance
     */
    private static AppShimmers appShimmers = null;

    /**
     * Returns the class instance of the MyApplication object
     */
    public static AppShimmers getInstance() {
    	
    	if (null == appShimmers) {
    		appShimmers = new AppShimmers();
    	}
        return appShimmers;
    }

	private ArrayList<ShimmerDeviceInfo> devices = new ArrayList<ShimmerDeviceInfo>();

	private AppShimmers() {
        // Set reference to this instance
		appShimmers = this;
	}
	
	public void addDevice(String address, String name) {
		
		for (ShimmerDeviceInfo deviceInfo: devices) {
			if (deviceInfo.getAddress().equalsIgnoreCase(address)) {
				devices.remove(deviceInfo);
				break;
			}
		}
		devices.add(new ShimmerDeviceInfo(address, name));
	}
	
	public void deleteDevice(String address) {
		
		for (ShimmerDeviceInfo info: devices) {
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

	private static String getJson(ArrayList<ShimmerDeviceInfo> pairedDevices) {
		
		JSONArray list = new JSONArray();
		
		for (ShimmerDeviceInfo deviceInfo: pairedDevices) {
			try {
				JSONObject o = new JSONObject();
				o.put("name", deviceInfo.getName());
				o.put("address", deviceInfo.getAddress());
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
		
				for (int i = 0; i < a.length(); ++i) {
					try {
						o = a.getJSONObject(i);
						address = o.getString("address");
						name = o.getString("name");
						devices.add(new ShimmerDeviceInfo(address, name));
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

	public ArrayList<ShimmerDeviceInfo> getShimmerDeviceInfos() {
		
		ArrayList<ShimmerDeviceInfo> newList = new ArrayList<ShimmerDeviceInfo>();
		for (ShimmerDeviceInfo info: devices) {
			newList.add(new ShimmerDeviceInfo(info.getAddress(), info.getName()));
		}
		return newList;
	}
}
