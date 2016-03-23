package edu.pdx.cecs.orcyclesensors;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class Fragment_MainRecord extends Fragment implements 
	ConnectionCallbacks,
	OnConnectionFailedListener,
	LocationListener, 
	IRecordServiceListener,
	OnMyLocationButtonClickListener {

	private static final String MODULE_TAG = "Fragment_MainRecord";
	private MyApplication myApp;
	private Controller_MainRecord controller = null;

	// Reference to recording service;
	private IRecordService recordingService;

	private static final float CF_METERS_TO_MILES = 0.00062137f;  	// Conversion factor meters to miles
	private static final float METERS_PER_SECOND_TO_MILES_PER_HOUR = 2.2369f;

	private static final int FMI_USER_PAUSED = 1;
	private static final int FMI_NOTE_PAUSED = 2;
	private static final int FMI_FINISH_PAUSED = 3;

	public enum Result { SAVE_TRIP, NO_GPS };

	private Result result;

	// UI Elements
	private Button buttonStart = null;
	private Button buttonPause = null;
	private Button buttonResume = null;
	private Button buttonFinish = null;
	private TextView txtDuration = null;
	private TextView txtDistance = null;
	private TextView txtAvgSpeed = null;
	private TextView txtWaitingDeviceConnect = null;
	
	private AlertDialog dlgFailedDeviceConnection = null;
	
	private Timer statusUpdateTimer;
	
	final Handler serviceConnectionHandler = new Handler();
	private Timer serviceConnectionTimer;

	final Handler deviceConnectionHandler = new Handler();
	private Timer deviceConnect;

	final Handler taskHandler = new Handler();
	
	private Location currentLocation = null;

	// Format used to show elapsed time to user when recording trips
	private final SimpleDateFormat tripDurationFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

	// *********************************************************************************
	// *
	// *********************************************************************************

	private MyMap myMap = null;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	// *********************************************************************************
	// *                                Constructor
	// *********************************************************************************

	public Fragment_MainRecord() {
		tripDurationFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	// *********************************************************************************
	// *                              Fragment Handlers
	// *********************************************************************************

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = null;

		try {
			myApp = MyApplication.getInstance();
			controller = myApp.getCtrlMainRecord();

			rootView = inflater.inflate(R.layout.fragment_main_record, (ViewGroup) null);

			Log.v(MODULE_TAG, "Cycle: Fragment_MainRecord onCreateView");

			// Convenient pointer to global application object

			// Setup the button to start recording
			buttonStart = (Button) rootView.findViewById(R.id.buttonStart);
			buttonStart.setVisibility(View.GONE);
			buttonStart.setOnClickListener(new ButtonStart_OnClickListener());

			// Setup the button to pause recording
			buttonPause = (Button) rootView.findViewById(R.id.buttonPause);
			buttonPause.setVisibility(View.GONE);
			buttonPause.setOnClickListener(new ButtonPause_OnClickListener());

			// Setup the button to resume recording
			buttonResume = (Button) rootView.findViewById(R.id.buttonResume);
			buttonResume.setVisibility(View.GONE);
			buttonResume.setOnClickListener(new ButtonResume_OnClickListener());

			// Setup the button to finish recording
			buttonFinish = (Button) rootView.findViewById(R.id.buttonFinish);
			buttonFinish.setVisibility(View.GONE);
			buttonFinish.setOnClickListener(new ButtonFinish_OnClickListener());

			// Copy from Recording Activity
			txtDuration = (TextView) rootView.findViewById(R.id.textViewElapsedTime);
			txtDistance = (TextView) rootView.findViewById(R.id.textViewDistance);
			txtAvgSpeed = (TextView) rootView.findViewById(R.id.textViewAvgSpeed);
			txtWaitingDeviceConnect = (TextView) rootView.findViewById(R.id.tv_waiting_device_connect);

			setHasOptionsMenu(false);
			
			GoogleMap gmap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map)).getMap();
			
			myMap = new MyMap(gmap, this);
			
			mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
	        .addConnectionCallbacks(this)
	        .addOnConnectionFailedListener(this)
	        .addApi(LocationServices.API)
	        .build();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainRecord onResume");
			
			myMap.setUpMapIfNeeded();

			if (null == recordingService) {
				scheduleServiceConnect();
			}
			else {
				syncDisplayToRecordingState();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onStart() {
        super.onStart();
        try {
        // Connect the client.
        	mGoogleApiClient.connect();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
    }

	@Override
	public void onPause() {
		super.onPause();

		try {
			Log.v(MODULE_TAG, "Cycle: onPause()");

			if (statusUpdateTimer != null)
				statusUpdateTimer.cancel();

			if (serviceConnectionTimer != null)
				serviceConnectionTimer.cancel();

			/*if (mLocationClient != null) {
				mLocationClient.disconnect();
			}*/

			if (recordingService != null) {
				recordingService.setListener(null);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onStop() {

		try {
			// Disconnecting the client invalidates it.
	        mGoogleApiClient.disconnect();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
        super.onStop();
    }

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			Log.v(MODULE_TAG, "Cycle: Fragment_MainRecord onDestroyView");
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *
	// *********************************************************************************
	
	/**
	 * Set up buttons, and enable status updates
	 */
	private void syncDisplayToRecordingState() {

		switch(recordingService.getState()) {

		case RecordingService.STATE_IDLE:
			setupButtons();
			break;

		case RecordingService.STATE_WAITING_FOR_DEVICE_CONNECT:
			setupButtons();
			break;

		case RecordingService.STATE_RECORDING:
			setupButtons();
			break;

		case RecordingService.STATE_PAUSED:
			if (recordingService.pauseId() == FMI_NOTE_PAUSED) {
				recordingService.resumeRecording();
			}
			setupButtons();
			break;

		case RecordingService.STATE_FULL:
			setupButtons();
			dialogTripFinish(true);
			break;

		case RecordingService.STATE_DEVICE_CONNECT_FAILED:
			setupButtons();
			showFailedDeviceConnectionDialog();
			break;

		default:
			Log.e(MODULE_TAG, "Undefined recording state encountered");
			break;
		}

		scheduleStatusUpdates(); // every second
	}

	/**
	 * Creates and schedules a timer to update the trip duration.
	 */
	private void scheduleStatusUpdates() {
		statusUpdateTimer = new Timer();
		statusUpdateTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					tripStatusHandler.post(tripStatusUpdateTask);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}, 0, 1000); // every second
	}

	/**
	 * Creates and schedules a timer to connect to the recording service.
	 */
	private void scheduleServiceConnect() {
		serviceConnectionTimer = new Timer();
		serviceConnectionTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					serviceConnectionHandler.post(doServiceConnection);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}, 2000, 2000); // 2 second delay, at 2 second intervals
	}

	/**
	 * Creates and schedules a timer to connect to the recording service.
	 */
	private void scheduleDeviceConnect() {
		deviceConnect = new Timer();
		deviceConnect.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					deviceConnectionHandler.post(doWaitDeviceConnection);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}, 2000, 2000); // 2 second delay, at 2 second intervals
	}

	// *********************************************************************************
	// *                   Trip Status Update Timers and Runnables
	// *********************************************************************************

	/**
	 *  Handler for callbacks to the UI thread
	 */
	final Handler tripStatusHandler = new Handler();
	
	final Runnable tripStatusUpdateTask = new Runnable() {
		public void run() {
			try {
				updateTripStatus();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	};

	/**
	 * Update the duration label
	 */
	private void updateTripStatus() {

		try {
			if (null != recordingService) {

				ApplicationStatus appStatus = myApp.getStatus();

				boolean isRecording =
						((RecordingService.STATE_RECORDING == recordingService.getState()) ||
						(RecordingService.STATE_PAUSED == recordingService.getState()));

				if (isRecording) {

					TripData tripData = appStatus.getTripData();

					if (null != tripData) {
						txtDuration.setText(tripDurationFormat.format(tripData.getDuration(true)));
						this.updateStatus(tripData.getDistance(), tripData.getAvgSpeedMps(true));
					}
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                     WaitForDeviceConnection Tasking
	// *********************************************************************************

	/**
	 * Class: doServiceConnection
	 * This task is to be executed after onResume has occurred to assure we still
	 * have a reference to the recording service.  This would happen if the OS
	 * kicked the service out of memory while the owning activity was dormant.
	 */
	final Runnable doWaitDeviceConnection = new Runnable() {
		public void run() {
			try {
				int state = recordingService.getState();

				if (IRecordService.STATE_DEVICE_CONNECT_FAILED == state) {
					
					Log.v(MODULE_TAG, "doWaitDeviceConnection(): device timed-out!");

					// The connection failed so cancel the timer
					deviceConnect.cancel();
					syncDisplayToRecordingState();
				}
				else if (IRecordService.STATE_RECORDING == state) {
					
					Log.v(MODULE_TAG, "doWaitDeviceConnection(): got connection!");

					// We now have connection to the device so cancel the timer
					deviceConnect.cancel();
					syncDisplayToRecordingState();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		} // end of run
	};

	// *********************************************************************************
	// *                     WaitForServiceConnection Tasking
	// *********************************************************************************

	/**
	 * Class: doServiceConnection
	 * This task is to be executed after onResume has occurred to assure we still
	 * have a reference to the recording service.  This would happen if the OS
	 * kicked the service out of memory while the owning activity was dormant.
	 */
	final Runnable doServiceConnection = new Runnable() {
		public void run() {

			try {

				if (null == recordingService) {
					Log.v(MODULE_TAG, "doServiceConnection(): Service not yet connected.");

					// See if a service connection has been established
					if (null != (recordingService = myApp.getRecordingService())) {
						Log.v(MODULE_TAG, "doServiceConnection(): got connection!");

						// We now have connection to the service so cancel the timer
						serviceConnectionTimer.cancel();

						recordingService.setListener(Fragment_MainRecord.this);
						//scheduleTask(TASK_SERVICE_CONNECT_COMPLETE);
						syncDisplayToRecordingState();
					}
				}
				else {
					Log.v(MODULE_TAG, "doServiceConnection(): Service already connected.");
					serviceConnectionTimer.cancel();
					syncDisplayToRecordingState();
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		} // end of run
	};

	/**
	 * Creates the menu items
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		try {
			// Inflate the menu items for use in the action bar
			// inflater.inflate(R.menu.saved_devices, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handles item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}

	/**
	 * Set buttons according to application state
	 * @param appState
	 */
	private void setupButtons() {

		if ((null == buttonStart)||
			(null == buttonPause)||
			(null == buttonResume)||
			(null == buttonFinish)) {
			return;
		}
		
		switch(recordingService.getState()) {

		case IRecordService.STATE_IDLE:

			txtWaitingDeviceConnect.setVisibility(View.GONE);
			buttonStart.setVisibility(View.VISIBLE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.GONE);
			break;

		case IRecordService.STATE_WAITING_FOR_DEVICE_CONNECT:

			txtWaitingDeviceConnect.setVisibility(View.VISIBLE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.GONE);
			break;

		case IRecordService.STATE_RECORDING:

			txtWaitingDeviceConnect.setVisibility(View.GONE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.VISIBLE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.VISIBLE);
			break;

		case IRecordService.STATE_PAUSED:

			txtWaitingDeviceConnect.setVisibility(View.GONE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.VISIBLE);
			buttonFinish.setVisibility(View.VISIBLE);
			break;

		case IRecordService.STATE_FULL:

			txtWaitingDeviceConnect.setVisibility(View.GONE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.GONE);
			break;

		case IRecordService.STATE_DEVICE_CONNECT_FAILED:

			txtWaitingDeviceConnect.setVisibility(View.GONE);
			buttonStart.setVisibility(View.GONE);
			buttonPause.setVisibility(View.GONE);
			buttonResume.setVisibility(View.GONE);
			buttonFinish.setVisibility(View.GONE);
			break;
		}
	}

	// *********************************************************************************
	// *                              Button Handlers
	// *********************************************************************************

	/**
     * Class: ButtonStart_OnClickListener
     *
     * Description: Callback to be invoked when startButton button is clicked
     */
	private final class ButtonStart_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				startRecording();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				setupButtons();
			}
		}
	}
	
	
	private void startRecording() throws Exception {
		// Before we go to record, check GPS status
		if (!myApp.getStatus().isProviderEnabled()) {
			// Alert user GPS not available
			dialogNoGps();
		}
		else if (myApp.startRecording(getActivity())) {
			scheduleDeviceConnect();
		}
	}
	

    /**
     * Class: ButtonPause_OnClickListener
     *
     * Description: Callback to be invoked when pauseButton button is clicked
     */
	private final class ButtonPause_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				recordingService.pauseRecording(FMI_USER_PAUSED);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				setupButtons();
			}
		}
	}

    /**
     * Class: ButtonResume_OnClickListener
     *
     * Description: Callback to be invoked when resumeButton button is clicked
     */
	private final class ButtonResume_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				recordingService.resumeRecording();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				setupButtons();
			}
		}
	}

    /**
     * Class: ButtonFinish_OnClickListener
     *
     * Description: Callback to be invoked when ButtonFinish button is clicked
     */
	private final class ButtonFinish_OnClickListener implements View.OnClickListener {

		/**
		 * Description: Handles onClick for view
		 */
		public void onClick(View v) {
			try {
				recordingService.pauseRecording(FMI_FINISH_PAUSED);
				setupButtons();

				TripData tripData = myApp.getStatus().getTripData();
				boolean allowSave = (tripData.getNumPoints() > 0);
				dialogTripFinish(allowSave);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	private Fragment_MainRecord setResult(Result result) {
		this.result = result;
		return this;
	}

	public Result getResult() {
		return this.result;
	}

	// *********************************************************************************
	// *
	// *********************************************************************************

	/**
	 * Updates the status of a trip being recorded.
	 * @param distanceMeters Distance traveled in meters
	 * @param avgSpeedMps Average speed in meters per second
	 */
	public void updateStatus(float distanceMeters, float avgSpeedMps) {

		float distanceMiles = distanceMeters * CF_METERS_TO_MILES;
		txtDistance.setText(String.format("%1.1f miles", distanceMiles));

		float avgSpeedMph = avgSpeedMps * METERS_PER_SECOND_TO_MILES_PER_HOUR;
		txtAvgSpeed.setText(String.format("%1.1f mph", avgSpeedMph));
	}

	/**
	 * Cancels recording
	 */
	private void cancelRecording() {

		try {
			myApp.cancelRecording();

			txtDuration = (TextView) getActivity().findViewById(R.id.textViewElapsedTime);
			txtDuration.setText(getResources().getString(R.string.fmi_reset_duration));

			txtDistance = (TextView) getActivity().findViewById(R.id.textViewDistance);
			txtDistance.setText(getResources().getString(R.string.fmi_reset_distance));

			txtAvgSpeed = (TextView) getActivity().findViewById(R.id.textViewAvgSpeed);
			txtAvgSpeed.setText(getResources().getString(R.string.fmi_reset_avg_speed));

			setupButtons();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                         Connection Failed Dialog
	// *********************************************************************************

	/**
	 * Build dialog telling user that a device has failed to connect
	 */
	private void showFailedDeviceConnectionDialog() {
		
		if (null == dlgFailedDeviceConnection) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.fmr_dnc_title);
			builder.setMessage(R.string.fmr_dnc_message);
			builder.setPositiveButton(R.string.fmr_dnc_button_ok, new FailedDeviceConnectionDialog_OkListener());
			dlgFailedDeviceConnection = builder.create();
			dlgFailedDeviceConnection.show();
		}
	}

	/**
	 * This class handles the dialogFailedDeviceConnection dialog's OK button event
	 */
	private final class FailedDeviceConnectionDialog_OkListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				dialog.cancel();
				cancelRecording();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			finally {
				dlgFailedDeviceConnection = null;
			}
		}
	}

	// *********************************************************************************
	// *                            No GPS Dialog
	// *********************************************************************************

	/**
	 * Build dialog telling user that the GPS is not available
	 */
	private void dialogNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getResources().getString(R.string.fmi_no_gps));
		builder.setCancelable(false);
		builder.setPositiveButton(getResources().getString(R.string.fmi_no_gps_dialog_ok),
				new DialogNoGps_OkListener());
		builder.setNegativeButton(getResources().getString(R.string.fmi_no_gps_dialog_cancel),
				new DialogNoGps_CancelListener());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogNoGps_OkListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				controller.finish(setResult(Result.NO_GPS));
				//setResult(Result.NO_GPS);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogNoGps_CancelListener implements DialogInterface.OnClickListener {
		public void onClick(final DialogInterface dialog, final int id) {
			try {
				dialog.cancel();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                            Trip Finished Dialogs
	// *********************************************************************************

	/**
	 * Build dialog telling user to save this trip
	 */
	private void dialogTripFinish(boolean allowSave) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getResources().getString(R.string.fmi_dtf_save_trip));
		if (allowSave) {
			builder.setMessage(getResources().getString(R.string.fmi_dtf_query_save));
			builder.setNegativeButton(getResources().getString(R.string.fmi_dtf_save),
					new DialogTripFinish_OnSaveTripClicked());
		}
		else {
			builder.setMessage(getResources().getString(R.string.fmi_no_gps_data));
		}
		builder.setNeutralButton(getResources().getString(R.string.fmi_dtf_discard),
				new DialogTripFinish_OnDiscardTripClicked());
		builder.setPositiveButton(getResources().getString(R.string.fmi_dtf_resume),
				new DialogTripFinish_OnContinueTripClicked());
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private final class DialogTripFinish_OnSaveTripClicked implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {

			try {
				dialog.cancel();

				myApp.finishRecording();

				TripData tripData = myApp.getStatus().getTripData();

				if (tripData.getNumPoints() > 0) {
					// Save the trip details to the phone database. W00t!
					tripData.updateTrip(tripData.getStartTime(), tripData.getEndTime(),
							tripData.getDistance(), "");
					tripData.updateTripStatus(TripData.STATUS_COMPLETE);

					controller.finish(setResult(Result.SAVE_TRIP), tripData.tripid);
				}
				// Otherwise, cancel and go back to main screen
				else {
					alertUserNoGPSData();
					cancelRecording();
				}
				setupButtons();
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogTripFinish_OnDiscardTripClicked implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				cancelRecording();
			}
			catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			} finally {
				setupButtons();
			}
		}
	}

	private final class DialogTripFinish_OnContinueTripClicked implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				recordingService.resumeRecording();
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			} finally {
				setupButtons();
			}
		}
	}

	// *********************************************************************************
	// *                            Map Location Tracking
	// *********************************************************************************

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		try {
			currentLocation = new Location(location);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		try {
			// mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	        mLocationRequest.setInterval(1000); // Update location every second

	        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
	    }
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		try {
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		try {
			Log.i(MODULE_TAG, "Connection failed: result = " + String.valueOf(result));
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                            Misc & Helper Functions
	// *********************************************************************************

	private void alertUserNoGPSData() {
		Toast.makeText(getActivity(),
			getResources().getString(R.string.fmi_no_gps_data),
			Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onMyLocationButtonClick() {
		try {
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}
}
