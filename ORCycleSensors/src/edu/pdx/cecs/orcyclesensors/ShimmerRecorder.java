package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer2;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer2.ObjectClusterSensorName;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ObjectCluster;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.FormatCluster;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer3;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ShimmerRecorder {

	private static final String MODULE_TAG = "ShimmerRecorder";

	// The Handler that gets information back from the BluetoothChatService
    private static Handler shimmerMessageHandler = new ShimmerMessageHandler();

    private static Map<String, ShimmerRecorder> shimmerRecorders = new HashMap<String, ShimmerRecorder>();
	

	public enum State { IDLE, CONNECTING, RUNNING, PAUSED, FAILED };

	private ShimmerService mService = null;
	private State state = State.IDLE;
	private int shimmerVersion = -1;
	public boolean isEXGUsingDefaultECGConfiguration;
	public boolean isEXGUsingDefaultEMGConfiguration;
	private boolean recordRawData;
	private long tripId;
	private String dataDir;
	private final String bluetoothAddress;
	private RawDataFile_Shimmer summaryDataFile;
	private Context context;
	private final HashMap<String, RawDataFile_ShimmerSensor> sensorDataFiles = new  HashMap<String, RawDataFile_ShimmerSensor>();
	private List<String> enabledSensors = null;
	private String[] enabledSignals = null;
    
	HashMap<String, ArrayList<Double>> signalReadings = new HashMap<String, ArrayList<Double>>();
	
    // ---------------------------------------------------------
	
	private ShimmerRecorder(Context context, String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {
		this.context = context;
		this.bluetoothAddress = bluetoothAddress;
		this.recordRawData = recordRawData;
		this.tripId = tripId;
		this.dataDir = dataDir;
	}

	// **********************************
	// * static interface
	// **********************************
	
	public static ShimmerRecorder create(Context context, String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {
		
		ShimmerRecorder shimmerRecorder = null;
		
		if (shimmerRecorders.containsKey(bluetoothAddress)) {
			shimmerRecorders.remove(bluetoothAddress);
		}

		shimmerRecorders.put(bluetoothAddress, shimmerRecorder = new ShimmerRecorder(context, bluetoothAddress, recordRawData, tripId, dataDir));

		return shimmerRecorder;
	}

	// **********************************
	// * public interface
	// **********************************
	
	synchronized public void start(Context context) {

        mService = MyApplication.getInstance().getShimmerService();
    	mService.setMessageHandler(shimmerMessageHandler);
		mService.enableGraphingHandler(true);
		mService.setEnableLogging(true);
		mService.connectShimmer(bluetoothAddress, "Device");
    	state = State.CONNECTING;
	}
	
	synchronized public void pause() {
		if (state == State.RUNNING) {
			state = State.PAUSED;
		}
		else {
			Log.e(MODULE_TAG, "Invalid state");
		}
	}

	synchronized public void resume() {
		if (state == State.PAUSED) {
			state = State.RUNNING;
		}
		else {
			Log.e(MODULE_TAG, "Invalid state");
		}
	}

	synchronized private void setState(State state) {
		this.state = state;
	}
	
	synchronized public State getState() {
		return state;
	}
	
	protected void closeRawDataFile() {
		if (null != summaryDataFile) {
			try {
				summaryDataFile.close();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			for (String key: sensorDataFiles.keySet()) {
				try {
					sensorDataFiles.get(key).close();
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	}

	synchronized public void unregister() {
		mService.stopStreaming(bluetoothAddress);
		mService.enableGraphingHandler(false);
		mService.disconnectShimmer(bluetoothAddress);
        closeRawDataFile();
        signalReadings.clear();
        shimmerRecorders.remove(bluetoothAddress);
	}

	synchronized private void init(Message msg) {
		
    	if (state == State.RUNNING) 
    		return;
    	
    	try {
			state = State.RUNNING;
	        shimmerVersion = mService.getShimmerVersion(bluetoothAddress);
			Shimmer shimmer = mService.getShimmer(bluetoothAddress);
			isEXGUsingDefaultECGConfiguration = shimmer.isEXGUsingDefaultECGConfiguration();
			isEXGUsingDefaultEMGConfiguration = shimmer.isEXGUsingDefaultEMGConfiguration();
			String filenameRoot = "Shimmer" + "(" + String.valueOf(bluetoothAddress) + ") " + String.valueOf(tripId) + " ";

			// Get list of enabled sensors
			enabledSignals = shimmer.getListofEnabledSensorSignals();
			ArrayList<String> signalNames = new ArrayList<String>(); 

			// Create arrays to hold sensor reading for each signal of each enabled sensor
			for (String signalName: enabledSignals) {
				if (!signalName.equals("Timestamp")) {
					signalReadings.put(signalName, new ArrayList<Double>());
					signalNames.add(signalName);
				}
			}
			
			// If flag is set, Create data files for writing the raw data
			ShimmerSignalGroup signalGroup;
			
			if (recordRawData) {
				enabledSensors = shimmer.getListofEnabledSensors();
				for (String sensorName: enabledSensors) {
					if (!sensorName.equals("Timestamp")) {
    					// Get the readings for the group  of signals specified
						signalGroup = ShimmerSignalGroup.create(sensorName, shimmerVersion,
								isEXGUsingDefaultECGConfiguration, isEXGUsingDefaultEMGConfiguration);
    					RawDataFile_ShimmerSensor rawDataFile = 
    							new RawDataFile_ShimmerSensor(filenameRoot + sensorName, tripId, dataDir, signalGroup.signalNames, shimmerVersion);
    					rawDataFile.open(context);
    					sensorDataFiles.put(sensorName, rawDataFile);
    				}
				}
				// If flag is set, Create a data summary file
				if (signalReadings.size() > 0) {
					String[] arraySignalNames = new String[0];
					arraySignalNames = signalNames.toArray(arraySignalNames);
					summaryDataFile = new RawDataFile_Shimmer(filenameRoot + "Upload", tripId, dataDir, arraySignalNames, shimmerVersion);
					summaryDataFile.open(context);
				}
			}
			
			// tell the shimmer device to start sending data
			mService.startStreaming(bluetoothAddress);
    	}
    	catch(Exception ex) {
    		Log.e(MODULE_TAG, ex.getMessage());
			state = State.FAILED;
    	}
	}
	
	
	/**
	 * Write the result for each enabled sensor/group of enabled sensor signals
	 * @param tripData
	 * @param currentTimeMillis
	 * @param location
	 */
 	synchronized public void writeResult(TripData tripData, long currentTimeMillis, Location location) {

		HashMap<String, CalcReading> results = new HashMap<String, CalcReading>();
		RawDataFile_ShimmerSensor sensorDataFile;
		ArrayList<Double> signalReading = null;
		ArrayList<Double> signal0 = null;
		ArrayList<Double> signal1 = null;
		ArrayList<Double> signal2 = null;

		CalcReading result;
		try {
			// Cycle thru each enabled signals
			for (String signalName: enabledSignals) {
				
				// For now, we are not going to record the sensor's timestamp
				if (signalName.equalsIgnoreCase("Timestamp")) {
					continue;
				}
				
				signalReading = signalReadings.get(signalName);
				if (null != (result = calculateResult(signalName, signalReading))) {
					results.put(signalName, result);
				}
			}

			// Cycle thru enabled sensors
			for (String sensorName: enabledSensors) {

				if (sensorName.equals("Timestamp")) {
					continue;
				}

				// Get the readings for the group of signals specified
				ShimmerSignalGroup signalGroup = ShimmerSignalGroup.create(sensorName, shimmerVersion,
						isEXGUsingDefaultECGConfiguration, isEXGUsingDefaultEMGConfiguration);
				// Get calculated values
				double[] standardDeviations = new double[signalGroup.signalNames.length];
				double[] averageValues = new double[signalGroup.signalNames.length];
				int numSamples = 0;
				
				// Move results into arrays
				for (int i = 0; i < signalGroup.signalNames.length; ++i) {
					if (null != (result = results.get(signalGroup.signalNames[i]))) {
						averageValues[i] = result.avg;
						standardDeviations[i] = result.std;
						numSamples = result.size;
					}
					else {
						Log.i(MODULE_TAG, "Null sample: " + sensorName);
					}
				}
				
				tripData.addShimmerReading(currentTimeMillis, bluetoothAddress,
						ShimmerFormat.getSensorType(sensorName,
								shimmerVersion,
								isEXGUsingDefaultECGConfiguration,
								isEXGUsingDefaultEMGConfiguration),
						numSamples, averageValues, standardDeviations);
			}

			// Copy the readings to the sensor data file
			if (recordRawData) {
				
				ShimmerSignalGroup signalGroup;
				
				for (String sensorName: enabledSensors) {
					
					// For now, we are not going to record the sensor's timestamp
					if (sensorName.equalsIgnoreCase("timestamp")) {
						continue;
					}
	
					if (null != (sensorDataFile = sensorDataFiles.get(sensorName))) {
						// Get the group of signals for the specified sensor
						if (null != (signalGroup = ShimmerSignalGroup.create(sensorName, shimmerVersion,
								isEXGUsingDefaultECGConfiguration, isEXGUsingDefaultEMGConfiguration))) {
						
							if ((signalGroup.signalNames.length > 0) && (signalGroup.signalNames[0] != null)) {
								
								signal0 = signalReadings.get(signalGroup.signalNames[0]);
								
								if ((signalGroup.signalNames.length > 1) && (signalGroup.signalNames[1] != null)) {
									
									signal1 = signalReadings.get(signalGroup.signalNames[1]);
									
									if ((signalGroup.signalNames.length > 2) && (signalGroup.signalNames[2] != null)) {
										
										signal2 = signalReadings.get(signalGroup.signalNames[2]);
									}
									else {
										signal2 = null;
									}
								}
								else {
									signal1 = null;
								}
							}
							else {
								signal0 = null;
							}
							sensorDataFile.write(currentTimeMillis, location, signal0, signal1, signal2);
						}
					}
				}
				
				// Copy the result readings (average and variance) to the summary data file
				if (null != summaryDataFile) {
					summaryDataFile.write(currentTimeMillis, location, results);
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			reset();
		}
	}

	private CalcReading calculateResult(String signalName, ArrayList<Double> readings) {
		double avg_readings;
		double ssd_readings;
		CalcReading calcReading = null;
		if (readings.size() > 0) {
			avg_readings = MyMath.getAverageValueD(readings);
			ssd_readings = MyMath.getStandardDeviationD(readings, avg_readings);
			calcReading = new CalcReading(signalName, readings.size(), avg_readings, ssd_readings);
		}
		return calcReading;
	}
	
	private void reset() {
		for (String key: signalReadings.keySet()) {
			signalReadings.get(key).clear();
		}
	}
	
	synchronized private void writeData(ObjectCluster objectCluster){
		
		ArrayList<Double> readings;
		Collection<FormatCluster> formats;
		FormatCluster formatCluster;

		// For each signal that we are recording
		for(String signalName : signalReadings.keySet()) {
			// get the readings data
			if (null != (readings = signalReadings.get(signalName))) {
				// Retrieve the object cluster containing new reading data
				if (null != (formats = objectCluster.mPropertyCluster.get(correctedSignalName(signalName)))) {
					// Retrieve the calibrated reading value
					if (null != (formatCluster = ((FormatCluster)ObjectCluster.returnFormatCluster(formats, calFormat(signalName))))) {
						readings.add(formatCluster.mData);
					}
	 	    	}
			}
		}
	}

	/**
	 * for some sensors, the calibrated value sensor name is the Legacy name
	 * @param signalName
	 * @return
	 */
	private String correctedSignalName(String signalName) {
		
		if (signalName.equals(Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180)) {
			return "Pressure";
		}
		else if (signalName.equals(Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180)) {
			return "Temperature";
		}
		else {
			return signalName;
		}
	}

	/**
	 * for some sensors, there is only a RAW value
	 * @param signalName
	 * @return
	 */
	private String calFormat(String signalName) {
		
		if (signalName.equals(Shimmer3.ObjectClusterSensorName.EXG1_STATUS)) {
			return "RAW";
		}
		else {
			return "CAL";
		}
	}

	// *********************************************************************************
	// *                          Shimmer Message Handler
	// *********************************************************************************

	private static final class ShimmerMessageHandler extends Handler {
		
		public void handleMessage(Message msg) {

        	ShimmerRecorder recorder = null;
			ObjectCluster objectCluster = null;
			
			try {
				switch (msg.what) {
	            
	            case Shimmer.MESSAGE_STATE_CHANGE:

    		    	switch (msg.arg1) {
    		    	
	                case Shimmer.STATE_CONNECTED: //this has been deprecated
	                    break;
	                    
	                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
	                	
	    		    	if ((msg.obj instanceof ObjectCluster)) { 
	    					objectCluster = (ObjectCluster) msg.obj;
	    					if (null != (recorder = shimmerRecorders.get(objectCluster.mBluetoothAddress))) {
		    					recorder.init(msg);
	    					}
	    		    	}
	                    break;
	
	                case Shimmer.STATE_CONNECTING:
	                    break;
	                    
	                case Shimmer.STATE_NONE:
	                	
	    		    	if ((msg.obj instanceof ObjectCluster)) { 
	    					objectCluster = (ObjectCluster) msg.obj;
	    					if (null != (recorder = shimmerRecorders.get(objectCluster.mBluetoothAddress))) {
		    					recorder.setState(State.FAILED);
	    					}
	    		    	}
	                    break;
    		    	}
	            
	            case Shimmer.MESSAGE_READ:

					if ((msg.obj instanceof ObjectCluster)) {
    					objectCluster = (ObjectCluster) msg.obj;
    					if (null != (recorder = shimmerRecorders.get(objectCluster.mBluetoothAddress))) {
	    					recorder.writeData((ObjectCluster)msg.obj);
    					}
					}
	                break;
	
	            case Shimmer.MESSAGE_ACK_RECEIVED:
	            	break;
	
	            case Shimmer.MESSAGE_DEVICE_NAME:
	                break;
	
	            case Shimmer.MESSAGE_TOAST:
	                break;
	            }
	        }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                                CalcReading
	// *********************************************************************************

	public class CalcReading {
		public String signalName;
		public int size;
		public double avg;
		public double std;
		
		public CalcReading(String signalName, int size, double avg, double std) {
			this.signalName = signalName;
			this.size = size;
			this.avg = avg;
			this.std = std;
		}
	}
}
