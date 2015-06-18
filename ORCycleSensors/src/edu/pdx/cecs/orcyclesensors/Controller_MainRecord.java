/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  @author Robin Murray <robin5@pdx.edu>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires and new ORcycle features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
 *
 *  Updated/modified for Oregon pilot study and app deployment.
 *
 *  ORcycle is free software: you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or any later version.
 *  ORcycle is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with
 *  ORcycle. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.pdx.cecs.orcyclesensors;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;

public class Controller_MainRecord extends Controller {

	private static final String MODULE_TAG = "Controller_MainRecord";

	private static final String COM_ANDROID_SETTINGS = "com.android.settings";
	private static final String COM_ANDROID_SETTINGS_SECURITY_SETTINGS = "com.android.settings.SecuritySettings";

	public Controller_MainRecord() {
	}

	// *********************************************************************************
	// *                    FragmentMainInput Transitions
	// *********************************************************************************

	public void finish(Fragment_MainRecord f) {
		finish(f, -1, -1);
	}

	public void finish(Fragment_MainRecord f, long tripId) {
		finish(f, tripId, -1);
	}

	public void finish(Fragment_MainRecord f, long tripId, long noteId) {

		switch(f.getResult()) {

		case NO_GPS:
			transitionToLocationServices(f);
			break;
			
		case SAVE_TRIP:
			transitionToTripMapActivity(f, tripId);
			break;
			
		default:
			Log.e(MODULE_TAG, "Fragment result value not set");
			break;
		}
	}

	private void transitionToLocationServices(Fragment f) {
		
		final ComponentName toLaunch = new ComponentName(
				COM_ANDROID_SETTINGS,
				COM_ANDROID_SETTINGS_SECURITY_SETTINGS);

		final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(toLaunch);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		f.startActivityForResult(intent, 0);
	}

	private void transitionToTripMapActivity(Fragment f, long tripId) {
		
		Activity activity = f.getActivity();
		
		Intent intent = new Intent(activity, Activity_TripMap.class);
		intent.putExtra(EXTRA_TRIP_ID, tripId);
		intent.putExtra(EXTRA_IS_NEW_TRIP, true);
		intent.putExtra(EXTRA_TRIP_SOURCE, EXTRA_TRIP_SOURCE_MAIN_RECORD);
		
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		activity.finish();
	}
}
