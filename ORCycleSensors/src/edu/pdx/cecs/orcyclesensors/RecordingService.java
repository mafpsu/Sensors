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
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
	private int pauseId = -1;
	
	private ArrayList<AntDeviceInfo> devices;
	private Map<Integer, AntDeviceRecorder> deviceRecorders = new HashMap<Integer, AntDeviceRecorder>();
	
    private ArrayList<SensorItem> sensors;
	private Map<String, SensorRecorder> sensorRecorders = new HashMap<String, SensorRecorder>();

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
			
			switch(getDeviceConnectState()) {
			
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
			long minTimeBetweenReadings,
			boolean recordRawData, String dataFileDir) throws Exception {
		
		this.trip = trip;
		this.pauseId = -1;
		this.distanceMeters = 0.0f;
		this.lastLocation = null;
		this.devices = antDeviceInfos;
		this.deviceRecorders.clear();
		this.sensors = sensorItems;
		this.sensorRecorders.clear();
		
		// Create a recorder for each sensor
		for (SensorItem sensorItem: this.sensors) {
			if (sensorItem.getRate() <= SensorManager.SENSOR_DELAY_NORMAL) {
				sensorRecorders.put(sensorItem.getName(), 
						SensorRecorder.create(sensorItem.getName(), 
								sensorItem.getType(), sensorItem.getRate(), recordRawData, trip.tripid, dataFileDir));
			}
		}

		// Create a recorder for each Ant+ device
		for (AntDeviceInfo antDeviceInfo: this.devices) {
			deviceRecorders.put(antDeviceInfo.getNumber(), 
					AntDeviceRecorder.create(antDeviceInfo.getNumber(),
							antDeviceInfo.getDeviceType(), recordRawData, trip.tripid, dataFileDir));
		}

		// Start listening for GPS updates!
		registerLocationUpdates(minTimeBetweenReadings);

		// Start listening for device updates!
		startDeviceRecorders();
		
		// Start listening for sensor updates!
		startSensorRecorders();

		if (null == speedMonitor) {
			speedMonitor = new SpeedMonitor(this);
		}
		speedMonitor.start();

		// Initialize recording state
		if (this.devices.size() == 0) {
			this.state = STATE_RECORDING;
		}
		else {
			this.state = STATE_WAITING_FOR_DEVICE_CONNECT;
		}
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
		unregisterDeviceRecorders();
		unregisterSensorRecorders();

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

		this.state = STATE_IDLE;
	}
	
	// *********************************************************************************
	// *                     LocationListener Implementation
	// *********************************************************************************

	private void registerLocationUpdates(long minTimeBetweenReadings) {

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
			if (state == STATE_WAITING_FOR_DEVICE_CONNECT) {
				return;
			}

			if (location != null) {

				if ((null != speedMonitor) && (location.hasSpeed()))
					speedMonitor.recordSpeed(System.currentTimeMillis(), location.getSpeed());

				if (lastLocation != null) {
					distanceMeters += lastLocation.distanceTo(location);
				}

				long currentTimeMillis = System.currentTimeMillis();
				
				trip.addPointNow(location, currentTimeMillis, distanceMeters);

				// record sensor values
				for (SensorRecorder sensorRecorder: sensorRecorders.values()) {
					sensorRecorder.writeResult(trip, currentTimeMillis, location);
				}

				// record device values
				for (AntDeviceRecorder deviceRecorder: deviceRecorders.values()) {
					deviceRecorder.writeResult(trip, currentTimeMillis, location);
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
	// *                     SensorEventListener Implementation
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
	// *                     AntDeviceEventListener Implementation
	// *********************************************************************************

	private void startDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : deviceRecorders.keySet()) {
			recorder = deviceRecorders.get(key);
			recorder.start(this);
		}
	}
	
	private void resumeDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : deviceRecorders.keySet()) {
			recorder = deviceRecorders.get(key);
			recorder.resume();
		}
	}
	
	private int getDeviceConnectState() {
		
		AntDeviceRecorder recorder;
		boolean oneFailed = false;
		boolean notAllConnected = false;

		for (Integer key : deviceRecorders.keySet()) {
			recorder = deviceRecorders.get(key);
			switch(recorder.getState()) {
			case IDLE:
				notAllConnected = true;
				break;
			case CONNECTING:
				notAllConnected = true;
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
		
		if (oneFailed) {
			return DEVICES_STATE_ATLEAST_ONE_FAILED_CONNECT;
		}
		else if (notAllConnected) {
			return DEVICES_STATE_NOT_ALL_CONNECTED;
		}
		else {
			return DEVICES_STATE_ALL_CONNECTED;
		}
	}

	private void pauseDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : deviceRecorders.keySet()) {
			recorder = deviceRecorders.get(key);
			recorder.pause();
		}
	}
	
	private void unregisterDeviceRecorders() {
		
		AntDeviceRecorder recorder;

		for (Integer key : deviceRecorders.keySet()) {
			recorder = deviceRecorders.get(key);
			recorder.unregister();
		}
	}
}
