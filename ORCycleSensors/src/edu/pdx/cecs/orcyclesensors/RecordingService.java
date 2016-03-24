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
 *	 Cycle Altanta, Copyright 2012 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong15@gmail.com>
 *
 *   Updated/Modified for Atlanta's app deployment. Based on the
 *   CycleTracks codebase for SFCTA.
 *
 *   CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 * 	 @author Billy Charlton <billy.charlton@sfcta.org>
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

package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class RecordingService extends Service 
	implements IRecordService, LocationListener  {

	public final static String MODULE_TAG = "RecordingService";

	IRecordServiceListener recordServiceListener;

	// Aspects of the currently recording trip
	Location lastLocation;
	float distanceMeters;   // The distance traveled in meters
	private TripData trip = null;

	private final static long MIN_TIME_BETWEEN_READINGS_MILLISECONDS = 1000;
	private final static float MIN_DISTANCE_BETWEEN_READINGS_METERS = 0.0f;
	private final static int MIN_DESIRED_ACCURACY = 19;

	private int state = STATE_IDLE;

	private SpeedMonitor speedMonitor;
	private Sound startSound = null;
	private int pauseId = -1;
	
	// list of Ant+ devices to record
	private ArrayList<AntDeviceInfo> antDeviceInfos;
	@SuppressLint("UseSparseArrays")
	private Map<Integer, AntDeviceRecorder> antDeviceRecorders = new HashMap<Integer, AntDeviceRecorder>();
	
	// list of phone sensors to record
    private ArrayList<SensorItem> sensors;
	private Map<String, SensorRecorder> sensorRecorders = new HashMap<String, SensorRecorder>();

	// list of Shimmer devices to record
	private ArrayList<ShimmerDeviceInfo> shimmerDeviceInfos;
	private Map<String, ShimmerRecorder> shimmerRecorders = new HashMap<String, ShimmerRecorder>();
	
	// list of Shimmer devices to record
	private ArrayList<EpocDeviceInfo> epocDeviceInfos;
	private Map<String, EpocRecorder> epocRecorders = new HashMap<String, EpocRecorder>();
	
	private long minTimeBetweenReadings;
	
	private final MyServiceBinder myServiceBinder = new MyServiceBinder();

	// *********************************************************************************
	// *                            Service Implementation
	// *********************************************************************************

	@Override
	public IBinder onBind(Intent arg0) {
		return myServiceBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class MyServiceBinder extends Binder {
		

        /**
         * This function returns a reference to the bound service
         *
         * @return Reference to the bound service
         */
        public RecordingService getService() {
            return RecordingService.this;
        }
	}

	// *********************************************************************************
	// *                       RecordingService Implementation
	// *********************************************************************************

	public int getState() {
		
		if (STATE_WAITING_FOR_DEVICE_CONNECT == state) {
			
			switch(getAsyncDeviceConnectState()) {
			
			case DEVICES_STATE_ALL_CONNECTED:
				state = STATE_RECORDING;
				break;
				
			case DEVICES_STATE_ATLEAST_ONE_FAILED_CONNECT:
				state = STATE_DEVICE_CONNECT_FAILED;
				break;
				
			default /* DEVICES_STATE_NOT_ALL_CONNECTED */:
				//state = STATE_WAITING_FOR_DEVICE_CONNECT;
				break;
			}
		}

		return state;
	}

	public long getCurrentTripID() {
		if (RecordingService.this.trip != null) {
			return RecordingService.this.trip.tripid;
		}
		return -1;
	}

	public TripData getCurrentTripData() {
		return trip;
	}

	public int pauseId() {
		return pauseId;
	}

	public void setListener(IRecordServiceListener listener) {
		RecordingService.this.recordServiceListener = listener;
		//notifyListeners();
	}

	public void reset() {
		RecordingService.this.state = STATE_IDLE;
		sensorRecorders.clear();
	}

	/**
	 * Start the recording process:
	 *  - reset trip variables
	 *  - enable location manager updates
	 *  - enable bike bell timer
	 * @throws Exception 
	 */
	public void startRecording(TripData trip, 
			ArrayList<AntDeviceInfo> antDeviceInfos, 
			ArrayList<SensorItem> sensorItems,
			ArrayList<ShimmerDeviceInfo> shimmerDeviceInfos,
			ArrayList<EpocDeviceInfo> epocDeviceInfos,
			long minTimeBetweenReadings,
			boolean recordRawData, String dataFileDir) throws Exception {
		
		boolean hasSensorData = false;
		boolean hasAntDeviceData = false;
		boolean hasShimmerData = false;
		boolean hasEpocData = false;

		this.trip = trip;
		this.pauseId = -1;
		this.distanceMeters = 0.0f;
		this.lastLocation = null;
		this.sensors = sensorItems;
		this.sensorRecorders.clear();
		this.antDeviceInfos = antDeviceInfos;
		this.antDeviceRecorders.clear();
		this.shimmerDeviceInfos = shimmerDeviceInfos;
		this.shimmerRecorders.clear();
		this.epocDeviceInfos = epocDeviceInfos;
		this.epocRecorders.clear();
		this.minTimeBetweenReadings = minTimeBetweenReadings;
		
		// Create a recorder for each sensor
		for (SensorItem sensorItem: this.sensors) {
			if (sensorItem.getRate() <= SensorManager.SENSOR_DELAY_NORMAL) {
				sensorRecorders.put(sensorItem.getName(), 
						SensorRecorder.create(sensorItem.getName(), 
								sensorItem.getType(), sensorItem.getRate(), recordRawData, trip.tripid, dataFileDir));
				hasSensorData = true;
			}
		}

		// Create a recorder for each Ant+ device
		for (AntDeviceInfo antDeviceInfo: this.antDeviceInfos) {
			antDeviceRecorders.put(antDeviceInfo.getNumber(), 
					AntDeviceRecorder.create(antDeviceInfo.getNumber(),
							antDeviceInfo.getDeviceType(), recordRawData, trip.tripid, dataFileDir));
			hasAntDeviceData = true;
		}

		// Create a recorder for each Shimmer device
		for (ShimmerDeviceInfo shimmerDeviceInfo: this.shimmerDeviceInfos) {
			shimmerRecorders.put(shimmerDeviceInfo.getAddress(), 
					ShimmerRecorder.create(this, shimmerDeviceInfo.getAddress(), recordRawData, trip.tripid, dataFileDir));
			hasShimmerData = true;
		}

		// Create a recorder for each Epoc device
		for (EpocDeviceInfo epocDeviceInfo: this.epocDeviceInfos) {
			epocRecorders.put(epocDeviceInfo.getAddress(), 
					EpocRecorder.create(this, epocDeviceInfo.getAddress(), recordRawData, trip.tripid, dataFileDir));
			hasEpocData = true;
		}

		trip.updateTrip(hasSensorData, hasAntDeviceData, hasShimmerData, hasEpocData);
		
		
		// Start listening for GPS updates!
		// registerLocationUpdates(minTimeBetweenReadings);

		// Start listening for device updates!
		startDeviceRecorders();
		
		// Start listening for sensor updates!
		startSensorRecorders();

		// Start listening for shimmer updates!
		startShimmerRecorders();

		// Start listening for shimmer updates!
		startEpocRecorders();

		if (null == speedMonitor) {
			speedMonitor = new SpeedMonitor(this);
		}
		
		if (null == startSound) {
			startSound = new Sound(this, R.raw.startbeep);
		}

		this.state = STATE_WAITING_FOR_DEVICE_CONNECT;
	}

	/**
	 * Pause the recording process:
	 *  - disable location manager updates
	 *  - start recording paused time
	 */
	public void pauseRecording(int pauseId) {
		this.pauseId = pauseId;
		this.state = STATE_PAUSED;
		trip.startPause();
		pauseDeviceRecorders();
		pauseSensorRecorders();
		pauseShimmerRecorders();
		pauseEpocRecorders();
		if (null != speedMonitor)
			speedMonitor.cancel();
	}

	/**
	 * Resume recording process:
	 *  - enable location manager updates
	 *  - calculate time paused and save in trip data
	 */
	public void resumeRecording() {
		this.state = STATE_RECORDING;
		this.pauseId = -1;
		trip.finishPause();
		resumeDeviceRecorders();
		resumeSensorRecorders();
		resumeShimmerRecorders();
		resumeEpocRecorders();

		if (null != speedMonitor)
			speedMonitor.start();
	}

	/**
	 * End the recording process:
	 *  - disable location manager updates
	 *  - clear notifications
	 *  - if trip has any points, finalize data collection and push to
	 *    database, otherwise cancel trip and don't save any data
	 */
	public long finishRecording() {
		this.state = STATE_FULL;

		if (null != speedMonitor)
			speedMonitor.cancel();

		// Disable location manager updates
		unregisterLocationUpdates();
		// Disable sensor and device recorders
		unregisterDeviceRecorders();
		unregisterSensorRecorders();
		unregisterShimmerRecorders();
		unregisterEpocRecorders();

		//
		if (trip.getNumPoints() > 0) {
			trip.finish(); // makes some final calculations and pushed trip to the database
		}
		else {
			cancelRecording(); // TODO: isn't the tripid invalid at this point? Verify.
		}

		return trip.tripid;
	}

	public void cancelRecording() {

		if (null != speedMonitor)
			speedMonitor.cancel();

		if (trip != null) {
			trip.dropTrip();
		}

		unregisterLocationUpdates();
		unregisterDeviceRecorders();
		unregisterSensorRecorders();
		unregisterShimmerRecorders();
		unregisterEpocRecorders();

		this.state = STATE_IDLE;
	}
	
	// *********************************************************************************
	// *                     LocationListener Implementation
	// *********************************************************************************

	private void startLocationUpdates() {

		LocationManager lm;
		if (null != (lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE))) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					minTimeBetweenReadings,
					MIN_DISTANCE_BETWEEN_READINGS_METERS, this);
		}
	}

	private void unregisterLocationUpdates() {
		LocationManager lm;
		if (null != (lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE))) {
			lm.removeUpdates(this);
		}
	}

    /**
     * Called when the location has changed.
     *
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
	@Override
	public void onLocationChanged(Location location) {

		try {
			if ((state == STATE_WAITING_FOR_DEVICE_CONNECT) || 
				(state == STATE_DEVICE_CONNECT_FAILED)) {
				return;
			}

			if (location != null) {

				if ((null != speedMonitor) && (location.hasSpeed()))
					speedMonitor.recordSpeed(System.currentTimeMillis(), location.getSpeed());

				if (lastLocation != null) {
					distanceMeters += lastLocation.distanceTo(location);
				}
				else {
					startSound.play();
				}

				long currentTimeMillis = System.currentTimeMillis();
				
				trip.addPointNow(location, currentTimeMillis, distanceMeters);

				// record sensor values
				for (SensorRecorder sensorRecorder: sensorRecorders.values()) {
					sensorRecorder.writeResult(trip, currentTimeMillis, location);
				}

				// record device values
				for (AntDeviceRecorder deviceRecorder: antDeviceRecorders.values()) {
					deviceRecorder.writeResult(trip, currentTimeMillis, location);
				}
				
				// record shimmer values
				for (ShimmerRecorder shimmerRecorder: shimmerRecorders.values()) {
					shimmerRecorder.writeResult(trip, currentTimeMillis, location);
				}
				
				// record epoc values
				for (EpocRecorder epocRecorder: epocRecorders.values()) {
					epocRecorder.writeResult(trip, currentTimeMillis, location);
				}
				
				// record location for distance measurement
				lastLocation = location;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
	@Override
	public void onProviderDisabled(String arg0) {
	}

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	// *********************************************************************************
	// *                     Sensor Implementation
	// *********************************************************************************

	private void startSensorRecorders() {
		
		SensorRecorder recorder;

		for (String key : sensorRecorders.keySet()) {
			recorder = sensorRecorders.get(key);
			recorder.start(this);
		}
	}

	private void pauseSensorRecorders() {
		
		SensorRecorder recorder;

		for (String key : sensorRecorders.keySet()) {
			recorder = sensorRecorders.get(key);
			recorder.pause();
		}
	}

	private void resumeSensorRecorders() {
		
		SensorRecorder recorder;

		for (String key : sensorRecorders.keySet()) {
			recorder = sensorRecorders.get(key);
			recorder.resume();
		}
	}

	private void unregisterSensorRecorders() {
		
		SensorRecorder recorder;

		for (String key : sensorRecorders.keySet()) {
			recorder = sensorRecorders.get(key);
			recorder.unregister(this);
		}
	}

	// *********************************************************************************
	// *                     AntDevice Implementation
	// *********************************************************************************

	private void startDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : antDeviceRecorders.keySet()) {
			recorder = antDeviceRecorders.get(key);
			recorder.start(this);
		}
	}
	
	private void resumeDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : antDeviceRecorders.keySet()) {
			recorder = antDeviceRecorders.get(key);
			recorder.resume();
		}
	}
	
	private void pauseDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : antDeviceRecorders.keySet()) {
			recorder = antDeviceRecorders.get(key);
			recorder.pause();
		}
	}
	
	private void unregisterDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : antDeviceRecorders.keySet()) {
			recorder = antDeviceRecorders.get(key);
			recorder.unregister();
		}
	}

	// *********************************************************************************
	// *                     Shimmer Implementation
	// *********************************************************************************

	private void startShimmerRecorders() {
		
		ShimmerRecorder recorder;

		for (String key : shimmerRecorders.keySet()) {
			recorder = shimmerRecorders.get(key);
			recorder.start(this);
		}
	}
	
	private void resumeShimmerRecorders() {
		
		ShimmerRecorder recorder;

		for (String key : shimmerRecorders.keySet()) {
			recorder = shimmerRecorders.get(key);
			recorder.resume();
		}
	}
	
	private void pauseShimmerRecorders() {
		
		ShimmerRecorder recorder;

		for (String key : shimmerRecorders.keySet()) {
			recorder = shimmerRecorders.get(key);
			recorder.pause();
		}
	}
	
	private void unregisterShimmerRecorders() {
		
		ShimmerRecorder recorder;

		for (String key : shimmerRecorders.keySet()) {
			recorder = shimmerRecorders.get(key);
			recorder.unregister();
		}
	}

	// *********************************************************************************
	// *                     Epoc Implementation
	// *********************************************************************************

	private void startEpocRecorders() {
		
		EpocRecorder recorder;

		for (String key : epocRecorders.keySet()) {
			recorder = epocRecorders.get(key);
			recorder.start(this);
		}
	}
	
	private void resumeEpocRecorders() {
		
		EpocRecorder recorder;

		for (String key : epocRecorders.keySet()) {
			recorder = epocRecorders.get(key);
			recorder.resume();
		}
	}
	
	private void pauseEpocRecorders() {
		
		EpocRecorder recorder;

		for (String key : epocRecorders.keySet()) {
			recorder = epocRecorders.get(key);
			recorder.pause();
		}
	}
	
	private void unregisterEpocRecorders() {
		
		EpocRecorder recorder;

		for (String key : epocRecorders.keySet()) {
			recorder = epocRecorders.get(key);
			recorder.unregister();
		}
	}

	// *********************************************************************************
	// *                     Ant+ and Shimmer connect state
	// *********************************************************************************

	private int getAsyncDeviceConnectState() {
		
		AntDeviceRecorder antDeviceRecorder;
		ShimmerRecorder shimmerRecorder;
		EpocRecorder epocRecorder;
		boolean oneFailed = false;
		boolean connectDone = true;

		// Check all Ant+ device recorders for state
		for (Integer key : antDeviceRecorders.keySet()) {
			antDeviceRecorder = antDeviceRecorders.get(key);
			switch(antDeviceRecorder.getState()) {
			case IDLE:
				connectDone = false;
				break;
			case CONNECTING:
				connectDone = false;
				break;
			case RUNNING:
				break;
			case PAUSED:
				break;
			case FAILED:
				oneFailed = true;
				break;
			}
		}
		
		// Check all Shimmer device recorders for state
		for (String key : shimmerRecorders.keySet()) {
			shimmerRecorder = shimmerRecorders.get(key);
			switch(shimmerRecorder.getState()) {
			case IDLE:
				connectDone = false;
				break;
			case CONNECTING:
				connectDone = false;
				break;
			case RUNNING:
				break;
			case PAUSED:
				break;
			case FAILED:
				oneFailed = true;
				break;
			}
		}
		
		// Check all Shimmer device recorders for state
		for (String key : epocRecorders.keySet()) {
			epocRecorder = epocRecorders.get(key);
			switch(epocRecorder.getState()) {
			case IDLE:
				connectDone = false;
				break;
			case CONNECTING:
				connectDone = false;
				break;
			case RUNNING:
				break;
			case PAUSED:
				break;
			case FAILED:
				oneFailed = true;
				break;
			}
		}
		
		// Summarize connect state
		if (!connectDone) {
			return DEVICES_STATE_NOT_ALL_CONNECTED;
		}
		else if (oneFailed) {
			return DEVICES_STATE_ATLEAST_ONE_FAILED_CONNECT;
		} 
		else {
			// Turn on speed monitor
			speedMonitor.start();
			// Start listening for GPS updates!
			startLocationUpdates();
			return DEVICES_STATE_ALL_CONNECTED;
		}
	}
}
