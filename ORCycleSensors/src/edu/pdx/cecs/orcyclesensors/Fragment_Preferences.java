package edu.pdx.cecs.orcyclesensors;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Fragment_Preferences extends PreferenceFragment{
	
	private static final String MODULE_TAG = "Fragment_Preferences";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			addPreferencesFromResource(R.xml.user_preferences_fragment);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onStop() {
        super.onStop();
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	        MyApplication.getInstance().loadSharedPreferences(prefs);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
    }
}
