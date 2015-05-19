package edu.pdx.cecs.orcyclesensors;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class AppInfo {

	private int versionCode;
	private String versionName;
	private String appVersion;
	private String deviceModel;

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public AppInfo(Context context) {
		
		// Determine application information
		versionName = "";
		versionCode = 0;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionName = pInfo.versionName;
			versionCode = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		appVersion = versionName + " (" + versionCode + ") on Android " + Build.VERSION.RELEASE;

		// Determine model information
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;

		if (model.startsWith(manufacturer)) {
			deviceModel = capitalize(model);
		} else {
			deviceModel = capitalize(manufacturer) + " " + model;
		}
	}

	public String getVersionName() {
		return versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public String getDeviceModel() {
		return deviceModel;
	}
}
