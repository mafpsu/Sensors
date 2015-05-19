package edu.pdx.cecs.orcyclesensors;

import java.util.List;

import android.preference.PreferenceActivity;
import android.util.Log;

public class Activity_UserPreferences extends PreferenceActivity{

	private static final String MODULE_TAG = "Activity_UserPreferences";

	@Override
	public void onBuildHeaders(List<Header> target) {
		try {
			loadHeadersFromResource(R.xml.user_preferences_header, target);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
    /**
     * Subclasses should override this method and verify that the given fragment is a valid type
     * to be attached to this activity. The default implementation returns <code>true</code> for
     * apps built for <code>android:targetSdkVersion</code> older than
     * {@link android.os.Build.VERSION_CODES#KITKAT}. For later versions, it will throw an exception.
     * @param fragmentName the class name of the Fragment about to be attached to this activity.
     * @return true if the fragment class name is valid for this Activity and false otherwise.
     */
	@Override
    public boolean isValidFragment(String fragmentName) {
		
		if (fragmentName.equals(Fragment_Preferences.class.getName())) {
			return true;
		}
		
		return false;
    }

	
}
