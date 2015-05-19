package edu.pdx.cecs.orcyclesensors;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.LatLng;

public class MyMap {

	private GoogleMap map;
	private boolean initted = false;
	OnMyLocationButtonClickListener onMyLocationButtonClickListener;
	
	public MyMap(GoogleMap map, OnMyLocationButtonClickListener onMyLocationButtonClickListener) {
		this.map = map;
		this.onMyLocationButtonClickListener = onMyLocationButtonClickListener;
		setUpMapIfNeeded();
	}
	
	public void init() {
		// Check if we were successful in obtaining the map.
		if ((map != null) && !initted) {
			map.setMyLocationEnabled(true);
			map.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
			//moveCameraToMyLocation();
			moveCameraToOregon();
		}
	}

	/**
	 * setUpMapIfNeeded: Instantiate the map
	 */
	public void setUpMapIfNeeded() {

		if (map != null) {
			init();

			UiSettings mUiSettings = map.getUiSettings();
			// Keep the UI Settings state in sync with the checkboxes.
			mUiSettings.setZoomControlsEnabled(true);
			mUiSettings.setCompassEnabled(true);
			mUiSettings.setMyLocationButtonEnabled(true);
			map.setMyLocationEnabled(true);
			mUiSettings.setScrollGesturesEnabled(true);
			mUiSettings.setZoomGesturesEnabled(true);
			mUiSettings.setTiltGesturesEnabled(true);
			mUiSettings.setRotateGesturesEnabled(true);
		}
}

	@SuppressWarnings("unused")
	private void moveCameraToMyLocation(Location location) {
		LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
	}

	private void moveCameraToOregon() {
		LatLng myLocation = new LatLng(43.8041334 , -120.55420119999996 );
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 6));
	}
}