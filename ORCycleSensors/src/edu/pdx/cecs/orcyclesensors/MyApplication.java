/**
 *  ORcycleSensors, Copyright 2015, PSU Transportation, Technology, and People Lab.
 *
 *  @author Robin Murray <robin5@pdx.edu>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires, and features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
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

import java.util.ArrayList;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * This class extends the <code>Application<code/> class, and implements it as a singleton.
 * This class is used to maintain global application state.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>Application<code/> class.
 * created 5/22/2014
 */
public class MyApplication extends android.app.Application {

	private final String MODULE_TAG = "MyApplication";
	
	private static final String PREFS_APPLICATION = "PREFS_APPLICATION";
	private static final String SETTING_USER_ID = "SETTING_USER_ID";
	private static final String SETTING_SENSORS = "SETTING_SENSORS";
	private static final String SETTING_DEVICES = "SETTING_DEVICES";
	private static final String SETTING_SHIMMERS = "SETTING_SHIMMERS";
	private static final String SETTING_GPS_FREQUENCY = "PREF_GPS_FREQUENCY";
	private static final String SETTING_DEFAULT_FREQUENCY = "1.0";
	private static final long DEFAULT_MIN_RECORDING_DELAY = 1000;

	private static final String RAW_DATA_FILES_DIR_NAME = "data";

	private static final String PREF_RECORD_RAW_DATA = "PREF_RECORD_RAW_DATA";
	private static final boolean DEFAULT_VALUE_RECORD_RAW_DATA = false;

	private static final String PREF_RAW_DATA_EMAIL_ADDRESS = "PREF_RAW_DATA_EMAIL_ADDRESS";
	private static final String DEFAULT_RAW_DATA_EMAIL_ADDRESS = "";

	private UserId userId = null;
	private AppDevices appDevices = null;
	private AppSensors appSensors = null;
	private AppShimmers appShimmers = null;
	private AppInfo appInfo = null;
	private RecordingService recordingService = null;
	private long minTimeBetweenReadings = 1000; // milliseconds
	private boolean recordRawData = false;
	private String rawDataEmailAddress = "";
	private TripData trip;
    private static final Controller_MainRecord ctrlMainRecord = new Controller_MainRecord();
    private static final Controller_TripMap ctrlTripMap = new Controller_TripMap();

	
	public MyApplication() {
		super();
        // Set reference to this instance
        myApp = this;
	}
	
	/**
     * Reference to class instance
     */
    private static MyApplication myApp = null;

    /**
     * Returns the class instance of the MyApplication object
     */
    public static MyApplication getInstance() {
        return myApp;
    }

	public Controller_MainRecord getCtrlMainRecord() {
		return ctrlMainRecord;
	}

	public Controller_TripMap getCtrlTripMap() {
		return ctrlTripMap;
	}

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * @throws java.lang.OutOfMemoryError
     */
    @Override
    public final void onCreate() {
        super.onCreate();
        try {
    		if (!DataFileInfoManager.setDirPath(getFilesDir().getAbsolutePath(), RAW_DATA_FILES_DIR_NAME)) {
    			Log.e(MODULE_TAG, "Could not create raw data files directory");
    		}
    		
			if (!EmailManager.setAttachmentPath("edu.pdx.cecs.orcyclesensors", "email_attachments")) {
    			Log.e(MODULE_TAG, "Could not create attachment directory.  No external drive present");
			}
			//EmailManager.cleanAttachmentDirectory(); // TODO:  Don't do this here because mailer might not have mailed files yet

			ConnectRecordingService();
			loadApplicationSettings();
			
        }
        catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
        }
    }

	private void loadApplicationSettings() {

		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);

		// Load the User ID.  Create it if it doesn't exist
		if (null == (userId = UserId.loadSetting(settings, SETTING_USER_ID))) {
			userId = UserId.Create();
			UserId.SaveTo(settings, SETTING_USER_ID, userId);
		}

		(appDevices = AppDevices.getInstance()).loadFrom(settings, SETTING_DEVICES);
		
		(appSensors = AppSensors.getInstance()).loadFrom(settings, SETTING_SENSORS);
		
		(appShimmers = AppShimmers.getInstance()).loadFrom(settings, SETTING_SHIMMERS);
		
		// setDefaultApplicationSettings();

		appInfo = new AppInfo(this.getBaseContext());
		
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		loadSharedPreferences(prefs);
	}
	
	public void loadSharedPreferences(SharedPreferences prefs) {
		
		loadMinTimeBetweenReadings(prefs);
		recordRawData = prefs.getBoolean(PREF_RECORD_RAW_DATA, DEFAULT_VALUE_RECORD_RAW_DATA);
		rawDataEmailAddress = prefs.getString(PREF_RAW_DATA_EMAIL_ADDRESS, DEFAULT_RAW_DATA_EMAIL_ADDRESS);
	}
	
	private void loadMinTimeBetweenReadings(SharedPreferences prefs) {
		
		String prefsFrequency = prefs.getString(SETTING_GPS_FREQUENCY, SETTING_DEFAULT_FREQUENCY);
		boolean isBadValue = false;

		try {
			float frequency = Float.parseFloat(prefsFrequency);
			if ((frequency <= 0) || Float.isNaN(frequency) || Float.isInfinite(frequency))
				isBadValue = true;
			else
				minTimeBetweenReadings = (long)(1000.0f / frequency);
		}
		catch(Exception ex) {
			isBadValue = true;
		}

		if (isBadValue) {
			Editor editor = prefs.edit();
			editor.putString(SETTING_GPS_FREQUENCY, SETTING_DEFAULT_FREQUENCY);
			editor.apply();
			minTimeBetweenReadings = DEFAULT_MIN_RECORDING_DELAY;
		}
	}

	// *********************************
	// * General application information
	// *********************************
	
	public String getVersionName() {
		return appInfo.getVersionName();
	}

	public int getVersionCode() {
		return appInfo.getVersionCode();
	}

	public String getAppVersion() {
		return appInfo.getAppVersion();
	}

	public String getDeviceModel() {
		return appInfo.getDeviceModel();
	}

	public String getRawDataEmailAddress() {
		return rawDataEmailAddress;
	}

	// **********************************
	// * Interface to application sensors
	// **********************************
	
	public void updateSensor(SensorItem sensorItem) {
		
		appSensors.updateSensor(sensorItem);

		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		
		appSensors.saveTo(settings, SETTING_SENSORS);
	}
	
	public void setAppSensors(ArrayList<SensorItem> sensors) {

		appSensors.setSensors(sensors);
		
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		
		appSensors.saveTo(settings, SETTING_SENSORS);
	}

	public ArrayList<SensorItem> getAppSensors() {
		return appSensors.getSensors();
	}

	public SensorItem getAppSensor(String name) {
		return appSensors.getSensor(name);
	}

	// **********************************
	// * Interface to application devices
	// **********************************
	
	public void addAppDevice(int number, String name, DeviceType deviceType) {
		
		appDevices.addDevice(number, name, deviceType);
		
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		
		appDevices.saveTo(settings, SETTING_DEVICES);
	}
	
	public void deleteAppDevice(int deviceNumber) {
		
		appDevices.deleteDevice(deviceNumber);
		
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		
		appDevices.saveTo(settings, SETTING_DEVICES);
	}

	public ArrayList<AntDeviceInfo> getAppDevices() {
		return appDevices.getAntDeviceInfos();
	}

	// **********************************
	// * Interface to Shimmer devices
	// **********************************
	
	public void addShimmerDevice(String address, String name) {
		
		appShimmers.addDevice(address, name);
		
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		
		appShimmers.saveTo(settings, SETTING_SHIMMERS);
	}
	
	public void deleteShimmerDevice(String address) {
		
		appShimmers.deleteDevice(address);
		
		SharedPreferences settings = getSharedPreferences(PREFS_APPLICATION, MODE_PRIVATE);
		
		appShimmers.saveTo(settings, SETTING_SHIMMERS);
	}

	public ArrayList<ShimmerDeviceInfo> getAppShimmers() {
		return appShimmers.getShimmerDeviceInfos();
	}

	// *************************************
	// * Interface to application data Files
	// *************************************
	
	public ArrayList<DataFileInfo> getAppDataFiles(Context context) {
		return DataFileInfoManager.getAppDataFiles();
	}

	public void deleteDataFiles(ArrayList<DataFileInfo> dataFileInfos) {
		DataFileInfoManager.deleteAppDataFiles(dataFileInfos);
	}

	// *********************************************************************************
	// *              RecordingService ServiceConnection Interface
	// *********************************************************************************

    /**
     * Connects the recording service to the Application object
     */
    private void ConnectRecordingService() {

    	try {
        Intent intent = new Intent(this, RecordingService.class);
        bindService(intent, recordingServiceServiceConnection, Context.BIND_AUTO_CREATE);
    	}
        catch(SecurityException ex) {
			Log.d(MODULE_TAG, ex.getMessage());
        }
    }

    /**
     * Connection to the RecordingService
     */
    public final ServiceConnection recordingServiceServiceConnection = new ServiceConnection() {

        /**
         * Called when a connection to the Service has been established. 
         * With the {@link android.os.IBinder} of the communication channel
         * to the Service.
         *
         * @param name The concrete component name of the service that has
         * been connected.
         *
         * @param service The IBinder of the Service's communication channel,
         * which you can now make calls on.
         */
        public void onServiceConnected(ComponentName name, IBinder service) {

        	Log.v(MODULE_TAG, "Connecting to RecordingService");

        	if (null == recordingService) {
        		recordingService = ((RecordingService.MyServiceBinder)service).getService();
        	}
        }

        /**
         * Called when a connection to the Service has been lost. 
         * This typically happens when the process hosting the service has crashed 
         * or been killed.  This does <em>not</em> remove the ServiceConnection 
         * itself -- this binding to the service will remain active, and you will 
         * receive a call to {@link #onServiceConnected} when the Service is 
         * next running.
         * 
         * @param name The concrete component name of the service whose
         * connection has been lost.
         */
        public void onServiceDisconnected(ComponentName name) {

        	Log.v(MODULE_TAG, "Disconnecting from RecordingService");

        	recordingService = null;
        }
    };

    public IRecordService getRecordingService() {
    	return recordingService;
    }

    /**
     * startRecording
     * @throws Exception 
     */
    public void startRecording(FragmentActivity activity) throws Exception {
		switch (recordingService.getState()) {

		case RecordingService.STATE_IDLE:
			trip = TripData.createTrip(activity);
			recordingService.startRecording(trip, appDevices.getAntDeviceInfos(), 
					appSensors.getSensors(), minTimeBetweenReadings, recordRawData, DataFileInfoManager.getDirPath());
			break;

		case RecordingService.STATE_RECORDING:
			long id = recordingService.getCurrentTripID();
			trip = TripData.fetchTrip(activity, id);
			break;
		}

		startRecordingNotification(lastTripStartTime = trip.getStartTime());
    }

    /**
     * finishRecording
     */
    public void finishRecording() {
    	recordingService.finishRecording();
    	// this call must be made to clear the recording full state
    	recordingService.reset();
		clearRecordingNotifications();
		lastTripStartTime = RESET_START_TIME;
   }

    /**
     * cancelRecording
     */
    public void cancelRecording() {
    	recordingService.cancelRecording();
		clearRecordingNotifications();
		lastTripStartTime = RESET_START_TIME;
    }

    public boolean isRecording() {
    	if (recordingService == null) {
    		return false;
    	}
    	else {
    		return ((RecordingService.STATE_RECORDING == recordingService.getState()) ||
					(RecordingService.STATE_PAUSED == recordingService.getState()));
    	}
    }

	// *********************************************************************************
	// * Recording
	// *********************************************************************************

	private static final double RESET_START_TIME = 0.0;

	private double lastTripStartTime = RESET_START_TIME;

	public void ResumeNotification() {
		if (isRecording()) {
			startRecordingNotification(lastTripStartTime);
		}
	}

	private void startRecordingNotification(double startTime) {
		// Add the notify bar and blinking light
		MyNotifiers.setRecordingNotification(this);
	}

	private void clearRecordingNotifications() {
		MyNotifiers.cancelRecordingNotification(this.getBaseContext());
	}

	private void clearReminderNotifications() {
		MyNotifiers.cancelReminderNotification(this.getBaseContext());
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

    private boolean isProviderEnabled() {
		final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public ApplicationStatus getStatus() {

    	ApplicationStatus applicationStatus = new ApplicationStatus(
				this.isProviderEnabled(),
				this.trip);

    	return applicationStatus;
    }

	// *********************************************************************************
	// *
	// *********************************************************************************

	public String getUserId() {
		return userId.toString();
	}

}
