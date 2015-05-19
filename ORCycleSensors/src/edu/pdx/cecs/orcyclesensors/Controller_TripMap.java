package edu.pdx.cecs.orcyclesensors;

import android.app.Activity;
import android.content.Intent;

public class Controller_TripMap extends Controller {

	private static final String MODULE_TAG = "Controller_TripMap";

	public void finish(Activity_TripMap activity, int result, int tripSource) {
		
		switch(result) {
		
		case Activity_TripMap.RESULT_DONE:
			transitionToMain(activity, tripSource);
			break;
		
		case Activity_TripMap.RESULT_BACK:
			transitionToMain(activity, tripSource);
			break;
		}
	}

	private void transitionToMain(Activity activity, int tripSource) {

		Intent intent = new Intent(activity, Activity_Main.class);

		if (tripSource == EXTRA_TRIP_SOURCE_MAIN_RECORD) {
			intent.putExtra(EXTRA_SHOW_FRAGMENT, EXTRA_SHOW_FRAGMENT_RECORD);
		}
		else if (tripSource == EXTRA_TRIP_SOURCE_MAIN_TRIPS) {
			intent.putExtra(EXTRA_SHOW_FRAGMENT, EXTRA_SHOW_FRAGMENT_TRIPS);
		}
		else {
			throw new IllegalArgumentException(MODULE_TAG + ": tripSource contains invalid value");
		}
		
		activity.startActivity(intent);
		activity.finish();
		activity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down);
	}
}

