package edu.pdx.cecs.orcyclesensors;

import java.util.UUID;

import android.content.SharedPreferences;

public class UserId {

	private static final String ANDROID_USER = "android";
	
	private String id;
	
	public static UserId Create() {
		return new UserId(generateUserId());
	}
	
	public static UserId loadSetting(SharedPreferences settings, String key) {
		
		String setting = settings.getString(key, "");
		if (setting.equals("")) {
			return null;
		}
		
		return new UserId(setting);
	}
	
	public static void SaveTo(SharedPreferences settings, String key, UserId userid) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, userid.id);
		editor.apply();
	}

	private static String generateUserId() {
		String value = ANDROID_USER + UUID.randomUUID().toString();
		return value;
	}
	
	private UserId(String value) {
		id = value;
	}
	
	@Override
	public String toString() {
		return id;
	}
}
