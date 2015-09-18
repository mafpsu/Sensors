/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  ORcycle 2.2.0 has introduced new app features: safety focus with new buttons
 *  to report safety issues and crashes (new questionnaires), expanded trip
 *  questionnaire (adding questions besides trip purpose), app utilization
 *  reminders, app tutorial, and updated font and color schemes.
 *
 *  @author Bryan.Blanc <bryanpblanc@gmail.com>    (code)
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
 *************************************************************************************
 *
 *  Cycle Atlanta, Copyright 2014 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong@gatech.edu>
 *
 *   Updated/Modified for Atlanta's app deployment. Based on the
 *   CycleTracks codebase for SFCTA.
 *
 *   CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 *   @author Billy Charlton <billy.charlton@sfcta.org>
 *
 *   This file is part of CycleTracks.
 *
 *   CycleTracks is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */
//
package edu.pdx.cecs.orcyclesensors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import edu.pdx.cecs.orcyclesensors.Fragment_MainRecord.Result;

public class Activity_TripMap extends Activity {

	private Controller_TripMap controller = null;

	public static final int RESULT_DONE = 0;
	public static final int RESULT_BACK = 1;

	private enum TripMapView { info, map };

	private static final String MODULE_TAG = "Activity_TripMap";

	private static final double NOTE_MIN_DISTANCE_FROM_TRIP = 45.7247; // meters is approximate 150 feet;
	private static final float METERS_PER_SECOND_TO_MILES_PER_HOUR = 2.2369f;

	GoogleMap mapView;
	ArrayList<CyclePoint> gpspoints;
	ArrayList<LatLng> mapPoints;
	Polyline polyline;
	Polyline segmentPolyline = null;

	private LatLngBounds.Builder bounds;
	private boolean initialPositionSet = false;
	private Button buttonRateStart = null;
	private Button buttonRateFinish = null;
	private boolean crosshairInRangeOfTrip = false;
	private LatLng crosshairLocation = null;
	private int indexOfClosestPoint = 0;
	private int segmentStartIndex = -1;
	private int segmentEndIndex = -1;
	private long tripId = -1;
	private boolean isNewTrip = false;
	private int tripSource = Controller.EXTRA_TRIP_SOURCE_UNDEFINED;
	private boolean selectingSegment = false;
	private com.google.android.gms.maps.model.Marker segmentStartMarker = null;
	private View questionsView;
	private View llTmButtons;

	private MenuItem mnuInfo;
	private MenuItem mnuMap;
	private TripMapView currentView = TripMapView.map;

	//private TextView tvAtmMoveCloser;

	// *********************************************************************************
	// *
	// *********************************************************************************

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			controller = MyApplication.getInstance().getCtrlTripMap();

			initialPositionSet = false;
			crosshairInRangeOfTrip = false;
			selectingSegment = false;

			// getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_trip_map);

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Set zoom controls
			mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.tripMap)).getMap();
			llTmButtons = findViewById(R.id.llTmButtons);
			llTmButtons.setVisibility(View.VISIBLE);

			questionsView = findViewById(R.id.tripQuestionsRootView);
			questionsView.setVisibility(View.INVISIBLE);

			//tvAtmMoveCloser = (TextView) findViewById(R.id.tvAtmMoveCloser);


			Bundle extras = getIntent().getExtras();
			isNewTrip = extras.getBoolean(Controller.EXTRA_IS_NEW_TRIP, false);

			tripId = extras.getLong(Controller.EXTRA_TRIP_ID, Controller.EXTRA_TRIP_ID_UNDEFINED);
			if (Controller.EXTRA_TRIP_ID_UNDEFINED == tripId) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_ID");
			}

			tripSource = extras.getInt(Controller.EXTRA_TRIP_SOURCE, Controller.EXTRA_TRIP_SOURCE_UNDEFINED);
			if (Controller.EXTRA_TRIP_SOURCE_UNDEFINED == tripSource) {
				throw new IllegalArgumentException(MODULE_TAG + ": invalid extra - EXTRA_TRIP_SOURCE");
			}

			TripData trip = TripData.fetchTrip(this, tripId);

			// Show trip details
			TextView tvAtmMapPurpose  = (TextView) findViewById(R.id.tvAtmMapPurpose);
			TextView tvAtmStartTime   = (TextView) findViewById(R.id.tvAtmStartTime);
			TextView tvAtmElapsedTime = (TextView) findViewById(R.id.tvAtmElapsedTime);
			TextView tvAtmDistance    = (TextView) findViewById(R.id.tvAtmDistance);
			TextView tvAtmAvgSpeed    = (TextView) findViewById(R.id.tvAtmAvgSpeed);

			tvAtmMapPurpose.setText(trip.getPurpose());
			tvAtmStartTime.setText("Start Time: " + getFormattedStartTime(trip.getStartTime()));
			tvAtmElapsedTime.setText("Elapsed Time: " + getFormattedDuration(trip.getStartTime(), trip.getEndTime()));
			tvAtmDistance.setText("Distance: " + getFormattedDistance(trip.getDistance()));
			tvAtmAvgSpeed.setText("Avg. Speed: " + getFormattedSpeed(trip.getAvgSpeedMps(false)));

			buttonRateStart = (Button) findViewById(R.id.btn_atm_rate_start);
			buttonRateStart.setOnClickListener(new ButtonRateStart_OnClickListener());

			// the next two lines will temporarily disable the RateSegment functionality
			//buttonRateStart.setOnClickListener(new ButtonRateStart_OnClickListener());
			buttonRateStart.setVisibility(View.GONE);

			buttonRateFinish = (Button) findViewById(R.id.btn_atm_rate_finish);
			buttonRateFinish.setOnClickListener(new ButtonRateFinish_OnClickListener());
			buttonRateFinish.setVisibility(View.GONE);

			gpspoints = trip.getPoints();
			mapPoints = new ArrayList<LatLng>();

			LatLng point;
			bounds = new LatLngBounds.Builder();
			for (int i = 0; i < gpspoints.size(); i++) {
				mapPoints.add(point = new LatLng(gpspoints.get(i).latitude * 1E-6, gpspoints.get(i).longitude * 1E-6));
				bounds.include(point);
			}

			if (trip.startpoint != null) {
				addMarker(trip.startpoint, R.drawable.trip_start);
			}

			if (trip.endpoint != null) {
				addMarker(trip.endpoint, R.drawable.trip_end);
			}

			polyline = drawMap(0, mapPoints.size() - 1, Color.BLUE);

			mapView.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition cameraPosition) {

					Projection p;
					VisibleRegion vr;

					if (!initialPositionSet) {
						// Move camera.
						mapView.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
						// Remove listener to prevent position reset on camera move.
						initialPositionSet = true;
					}

					if (null != (p = mapView.getProjection())) {
						if (null != (vr = p.getVisibleRegion())) {

							crosshairLocation = new LatLng((vr.latLngBounds.northeast.latitude + vr.latLngBounds.southwest.latitude)/2.0,
													   (vr.latLngBounds.northeast.longitude + vr.latLngBounds.southwest.longitude)/2.0);

							double crosshairDistanceFromTrip = getCrosshairDistanceFromTrip(crosshairLocation);

							// textCrosshair.setText(String.valueOf(crosshairDistanceFromTrip)); // Keep for debugging

							if (crosshairInRangeOfTrip) {
								buttonRateStart.setTextColor(getResources().getColor(R.color.user_button_text));
								buttonRateStart.setBackgroundColor(getResources().getColor(R.color.user_button_background));
								buttonRateFinish.setTextColor(getResources().getColor(R.color.user_button_text));
								buttonRateFinish.setBackgroundColor(getResources().getColor(R.color.user_button_background));
								//tvAtmMoveCloser.setVisibility(View.GONE);


								if ((segmentStartIndex != -1) && (segmentEndIndex == -1)) {
									// Remove previously drawn line
									if (null != segmentPolyline)
										segmentPolyline.remove();
									// draw the new line
									segmentPolyline = drawMap(segmentStartIndex, indexOfClosestPoint, Color.MAGENTA);
								}

							}
							else {
								buttonRateStart.setTextColor(Color.WHITE);
								buttonRateStart.setBackgroundColor(Color.RED);
								buttonRateFinish.setTextColor(Color.WHITE);
								buttonRateFinish.setBackgroundColor(Color.RED);
								//tvAtmMoveCloser.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			});

			// ----------------
			// Upload trip data
			// ----------------

			if ((trip.getStatus() < TripData.STATUS_SENT) && (extras != null)
					&& isNewTrip) {
				// And upload to the cloud database, too! W00t W00t!
				TripUploader uploader = new TripUploader(Activity_TripMap.this, MyApplication.getInstance().getUserId());
				uploader.execute(trip.tripid);
			}

		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
		}
		currentView = TripMapView.map;
	}

	@SuppressLint("SimpleDateFormat")
	private String getFormattedDuration(Double startTime, Double endTime) {

		SimpleDateFormat sdfDuration = new SimpleDateFormat("HH:mm:ss");
		sdfDuration.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdfDuration.format(endTime - startTime);
	}

	@SuppressLint("SimpleDateFormat")
	private String getFormattedStartTime(Double startTime) {
		SimpleDateFormat sdfStart = new SimpleDateFormat("MMM d, y  h:mm a", Locale.US);
		return sdfStart.format(startTime);
	}

	/**
	 * Returns distance formatted in miles
	 * @param distance in meters
	 * @return distance formatted in miles
	 */
	private String getFormattedDistance(float distanceMeters) {
		float miles = (0.0006212f * distanceMeters);
		return String.format(Locale.US, "%1.1f miles", miles);
	}

	/**
	 * Returns speed formatted in miles per hour
	 * @param speedMPS in meters per second
	 * @return speed formatted in miles per hour
	 */
	private String getFormattedSpeed(float speedMPS) {
		float speedMPH = speedMPS * METERS_PER_SECOND_TO_MILES_PER_HOUR;
		return String.format(Locale.US, "%1.1f mph", speedMPH);
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			setCurrentView(currentView);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void setSelectingSegment(boolean value) {
		if (true == (selectingSegment = value)) {
			buttonRateStart.setVisibility(View.GONE);
			buttonRateFinish.setVisibility(View.VISIBLE);
		}
		else {
			// this line will temporarily disable the RateSegment functionality
			//buttonRateStart.setVisibility(View.VISIBLE);
			buttonRateStart.setVisibility(View.GONE);
			buttonRateFinish.setVisibility(View.GONE);

			if (null != segmentPolyline)
				segmentPolyline.remove();

			if (null != segmentStartMarker) {
				segmentStartMarker.remove();
			}
			segmentStartIndex = -1;
			segmentEndIndex = -1;
		}
	}

	private boolean getSelectingSegment() {
		return selectingSegment;
	}

	private com.google.android.gms.maps.model.Marker
	addMarker(CyclePoint cyclePoint, int resourceId) {

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(BitmapDescriptorFactory.fromResource(resourceId));
		markerOptions.anchor(0.0f, 1.0f); // Anchors the marker on the bottom left
		markerOptions.position(new LatLng(cyclePoint.latitude * 1E-6, cyclePoint.longitude * 1E-6));

		return mapView.addMarker(markerOptions);
	}

	private Polyline drawMap(int start, int end, int color) {

		// swap order if out of order
		if (start > end) {
			int tmp = start;
			start = end;
			end = tmp;
		}

		PolylineOptions polylineOptions = new PolylineOptions();
		polylineOptions.geodesic(true).color(color);

		for (int i = start; i <= end; i++) {
			polylineOptions.add(mapPoints.get(i));
		}

		return mapView.addPolyline(polylineOptions);
	}

	/**
	 * Get shortest distance from trip to crosshairs, and mark index of that point
	 * @param crosshairs
	 * @return
	 */
	private double getCrosshairDistanceFromTrip(LatLng crosshairs) {

		float distance[] = new float[1];

		// Get the initial point
		LatLng tripPoint = mapPoints.get(0);
		// get distance to initial point
		Location.distanceBetween(crosshairs.latitude, crosshairs.longitude, tripPoint.latitude, tripPoint.longitude, distance);
		// set index of closest point to initial point
		indexOfClosestPoint = 0;

		// Set minimum distance = distance to initial point
		double minDistance = distance[0];

		// now cycle through remaining points
		for (int i = 1; i < mapPoints.size(); i++) {
			tripPoint = mapPoints.get(i);
			Location.distanceBetween(crosshairs.latitude, crosshairs.longitude, tripPoint.latitude, tripPoint.longitude, distance);
			if (distance[0] < minDistance) {
				minDistance = distance[0];
				indexOfClosestPoint = i;
			}
		}

		crosshairInRangeOfTrip = (minDistance <= NOTE_MIN_DISTANCE_FROM_TRIP);

		return minDistance;
	}

	@Override
	public void onBackPressed() {
		try {

			if(getSelectingSegment()) {
				setSelectingSegment(false);
			}
			else {
				// Remove polylines if they exist
				if ((mapView != null) && (polyline != null)) {
					polyline.remove();
				}

				controller.finish(this, RESULT_BACK, tripSource);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			getMenuInflater().inflate(R.menu.trip_map, menu);

			mnuInfo = menu.getItem(0);
			mnuMap = menu.getItem(1);
			setCurrentView(currentView);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_trip_map_view_info:
				setCurrentView(TripMapView.info);
				return true;

			case R.id.action_trip_map_view_map:
				setCurrentView(TripMapView.map);
				return true;

			case R.id.action_trip_map_close:

				// close -> go back to FragmentMainInput
				if ((mapView != null) && (polyline != null)) {
					polyline.remove();
				}

				controller.finish(this, RESULT_DONE, tripSource);
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	private void setCurrentView(TripMapView tripMapView) {

		switch (tripMapView) {

		case info:

			questionsView.setVisibility(View.VISIBLE);
			llTmButtons.setVisibility(View.INVISIBLE);
			//tvAtmMoveCloser.setVisibility(View.INVISIBLE);
			if ((null != mnuInfo) && (null != mnuMap)) {
				mnuInfo.setVisible(false);
				mnuMap.setVisible(true);
			}
			break;

		case map:

			questionsView.setVisibility(View.INVISIBLE);
			llTmButtons.setVisibility(View.VISIBLE);
			if (!crosshairInRangeOfTrip) {
				//tvAtmMoveCloser.setVisibility(View.VISIBLE);
			}
			if ((null != mnuInfo) && (null != mnuMap)) {
				mnuInfo.setVisible(true);
				mnuMap.setVisible(false);
			}
			break;
		}
		currentView = tripMapView;
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

    /**
     * Class: ButtonRate_OnClickListener
     *
     * Description: Callback to be invoked when buttonRateSegment button is clicked
     */
	private final class ButtonRateStart_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				if (!crosshairInRangeOfTrip) {
					Toast.makeText(Activity_TripMap.this,
							getResources().getString((R.string.atm_crosshair_distance_message)),
							Toast.LENGTH_SHORT).show();
				}
				else {
					setSelectingSegment(true);
					segmentStartIndex = indexOfClosestPoint;
					segmentStartMarker = addMarker(gpspoints.get(segmentStartIndex), R.drawable.trip_start);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

    /**
     * Class: ButtonRateFinish_OnClickListener
     *
     * Description: Callback to be invoked when buttonRateFinish button is clicked
     */
	private final class ButtonRateFinish_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {

			try {
				if (!crosshairInRangeOfTrip) {
					Toast.makeText(Activity_TripMap.this,
							getResources().getString((R.string.atm_crosshair_distance_message)),
							Toast.LENGTH_SHORT).show();
				}
				else if (indexOfClosestPoint == segmentStartIndex) {
					Toast.makeText(Activity_TripMap.this,
							"Ending position must be different than starting position.",
							Toast.LENGTH_SHORT).show();
				}
				else {
					segmentEndIndex = indexOfClosestPoint;
					addMarker(gpspoints.get(segmentEndIndex), R.drawable.trip_end);

					// The user may have selected the start and beginning indexes
					// in reverse order, so check and swap if necessary
					if (segmentStartIndex > segmentEndIndex) {
						int tmp = segmentStartIndex;
						segmentStartIndex = segmentEndIndex;
						segmentEndIndex = tmp;
					}

					buttonRateStart.setVisibility(View.GONE);
					buttonRateFinish.setVisibility(View.VISIBLE);

					//transitionToRateSegmentActivity();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}
}
