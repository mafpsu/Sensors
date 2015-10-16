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
import java.util.HashMap;
import java.util.List;
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

	public enum State { IDLE, CONNECTING, RUNNING, PAUSED, FAILED };

	private ShimmerService mService = null;
	private State state = State.IDLE;
	private int shimmerVersion = -1;
	private boolean recordRawData;
	private long tripId;
	private String dataDir;
	private final String bluetoothAddress;
	private RawDataFile_Shimmer summaryDataFile;
	private Context context;
	private final HashMap<String, RawDataFile_ShimmerSensor> sensorDataFiles = new  HashMap<String, RawDataFile_ShimmerSensor>();
	private String[] enabledSensors;
	// The Handler that gets information back from the BluetoothChatService
    private Handler shimmerMessageHandler = new ShimmerMessageHandler();
    
	HashMap<String, ArrayList<Double>> signalReadings = new HashMap<String, ArrayList<Double>>();
	
	public class CalcReading {
		public String signalName;
		public int size;
		public double avg;
		public double ssd;
		
		public CalcReading(String signalName, int size, double avg, double ssd) {
			this.signalName = signalName;
			this.size = size;
			this.avg = avg;
			this.ssd = ssd;
		}
	}

    // ---------------------------------------------------------
	
	public ShimmerRecorder(Context context, String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {
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
		return new ShimmerRecorder(context, bluetoothAddress, recordRawData, tripId, dataDir);
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
	}

	public class SignalGroup {
		public String sensorName;
		private String[] signalNames = new String[0];
		
		public SignalGroup(String sensorName, String signalName0, String signalName1, String signalName2) {
			this.sensorName = sensorName;
			signalNames = new String[3];
			this.signalNames[0] = signalName0;
			this.signalNames[1] = signalName1;
			this.signalNames[2] = signalName2;
		}
		public SignalGroup(String sensorName, String signalName0, String signalName1) {
			this.sensorName = sensorName;
			signalNames = new String[2];
			this.signalNames[0] = signalName0;
			this.signalNames[1] = signalName1;
		}
		public SignalGroup(String sensorName, String signalName0) {
			this.sensorName = sensorName;
			signalNames = new String[1];
			this.signalNames[0] = signalName0;
		}
	}
	
	/**
	 * Return a SignalGroup object which specifies what group of signals specify a sensor
	 * @param sensorName
	 * @return a SignalGroup object which specifies what group of signals specify a sensor
	 */
	private SignalGroup getSignalGroup(String sensorName) {
		
		SignalGroup signalGroup = null;

		if (sensorName.equals("Accelerometer")) {
			
			signalGroup = new SignalGroup(sensorName, 
					Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_X,
					Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Y,
					Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Z);
			
		} else if (sensorName.equals("Low Noise Accelerometer")) {
			
			signalGroup = new SignalGroup(sensorName, 
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
					Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);
			
		} else if (sensorName.equals("Wide Range Accelerometer")) {
			
			signalGroup = new SignalGroup(sensorName, 
					"Wide Range Accelerometer X",
					"Wide Range Accelerometer Y",
					"Wide Range Accelerometer Z");
			
		} else if (sensorName.equals("Gyroscope")) {
			
			signalGroup = new SignalGroup(sensorName,
					"Gyroscope X",
					"Gyroscope Y",
					"Gyroscope Z");
			
		} else if (sensorName.equals("Magnetometer")) {
			
			signalGroup = new SignalGroup(sensorName,
					"Magnetometer X",
					"Magnetometer Y",
					"Magnetometer Z");
			
		} else if (sensorName.equals("GSR")) {
			
			signalGroup = new SignalGroup(sensorName,"GSR");
			
		} else if (sensorName.equals("EMG")) {
			
			signalGroup = new SignalGroup(sensorName,"EMG");
			
		} else if (sensorName.equals("ECG")) {
			
			if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R) {
				signalGroup = new SignalGroup(sensorName, "ECG RA-LL", "ECG LA-LL");
			}
			
		} else if (sensorName.equals("EXG1") || sensorName.equals("EXG2") || sensorName.equals("EXG1 16Bit") || sensorName.equals("EXG2 16Bit")) {

				if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3) {
					if (mService.getShimmer(bluetoothAddress).isEXGUsingECG24Configuration() ||
						mService.getShimmer(bluetoothAddress).isEXGUsingECG16Configuration()) {

						// same name for both 16 and 24 bit
						signalGroup = new SignalGroup(sensorName, 
								Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
								Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT,
								Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT);
						
					} else if (mService.getShimmer(bluetoothAddress).isEXGUsingEMG24Configuration() ||
							mService.getShimmer(bluetoothAddress).isEXGUsingEMG16Configuration()) {
						
						// same name for both 16 and 24 bit
						signalGroup = new SignalGroup(sensorName, 
								Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
								Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT);

					} else if (mService.getShimmer(bluetoothAddress).isEXGUsingTestSignal24Configuration()) {

						signalGroup = new SignalGroup(sensorName, 
								Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
								Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
								Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);

					} else if (mService.getShimmer(bluetoothAddress).isEXGUsingTestSignal16Configuration()) {

						signalGroup = new SignalGroup(sensorName, 
								Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
								Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
								Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);

					} else {

						if (sensorName.equals("EXG1 16Bit") || sensorName.equals("EXG2 16Bit")) {
							
							signalGroup = new SignalGroup(sensorName, 
								Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
								Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT,
								Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT);
							
						} else {
							
							signalGroup = new SignalGroup(sensorName, 
									Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
									Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT,
									Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT);
						}
					}
				}

		} else if (sensorName.equals("Bridge Amplifier")) {

			signalGroup = new SignalGroup(sensorName, 
					"Bridge Amplifier High",
					"Bridge Amplifier Low");

		} else if (sensorName.equals("Heart Rate")) {

			signalGroup = new SignalGroup(sensorName, "Heart Rate");

		} else if (sensorName.equals("ExpBoard A0")) {

			signalGroup = new SignalGroup(sensorName, Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0);

		} else if (sensorName.equals("ExpBoard A7")) {

			signalGroup = new SignalGroup(sensorName, Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7);

		} else if (sensorName.equals("Battery Voltage")) {

			signalGroup = new SignalGroup(sensorName, "VSenseReg", "VSenseBatt");

		} else if (sensorName.equals("Timestamp")) {

			signalGroup = new SignalGroup(sensorName, "Timestamp");

		} else if (sensorName.equals("External ADC A7")) {

			if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3) {
				signalGroup = new SignalGroup(sensorName, Shimmer3.ObjectClusterSensorName.EXT_EXP_A7);
			} else {
				signalGroup = new SignalGroup(sensorName, Shimmer2.ObjectClusterSensorName.EXT_EXP_A7);
			}

		} else if (sensorName.equals("External ADC A6")) {

			Shimmer shmr = mService.getShimmer(bluetoothAddress);
			if (shmr != null) {
				if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3) {
					signalGroup = new SignalGroup(sensorName, Shimmer3.ObjectClusterSensorName.EXT_EXP_A6);
				} else {
					signalGroup = new SignalGroup(sensorName, Shimmer2.ObjectClusterSensorName.EXT_EXP_A6);
				}
			}

		} else if (sensorName.equals("External ADC A15")) {

			signalGroup = new SignalGroup(sensorName, "External ADC A15");

		} else if (sensorName.equals("Internal ADC A1")) {

			signalGroup = new SignalGroup(sensorName, "Internal ADC A1");

		} else if (sensorName.equals("Internal ADC A12")) {

			signalGroup = new SignalGroup(sensorName, "Internal ADC A12");

		} else if (sensorName.equals("Internal ADC A13")) {

			signalGroup = new SignalGroup(sensorName, "Internal ADC A13");

		} else if (sensorName.equals("Internal ADC A14")) {

			signalGroup = new SignalGroup(sensorName, "Internal ADC A14");

		} else if (sensorName.equals("Pressure")) {

			signalGroup = new SignalGroup(sensorName, "Pressure", "Temperature");

		}
		
		return signalGroup;
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
		ArrayList<Double> signal0 = null;
		ArrayList<Double> signal1 = null;
		ArrayList<Double> signal2 = null;


		try {
			for (String sensorName: enabledSensors) {
				
				// For now, we are not going to record the sensor's timestamp
				if (sensorName.equalsIgnoreCase("timestamp")) {
					continue;
				}
				
				// Get the group of signals for the specified sensor
				SignalGroup signalGroup = getSignalGroup(sensorName);

				// Calculate the average and standard deviation results for each signal
				if ((signalGroup.signalNames.length > 0) && (signalGroup.signalNames[0] != null)) {
					signal0 = signalReadings.get(signalGroup.signalNames[0]);
					results.put(signalGroup.signalNames[0], calculateResult(signalGroup.signalNames[0], signal0));
				}
				else {
					signal0 = null;
				}
				
				if ((signalGroup.signalNames.length > 1) && (signalGroup.signalNames[1] != null)) {
					signal1 = signalReadings.get(signalGroup.signalNames[1]);
					results.put(signalGroup.signalNames[1], calculateResult(signalGroup.signalNames[1], signal1));
				}
				else {
					signal1 = null;
				}
				
				if ((signalGroup.signalNames.length > 2) && (signalGroup.signalNames[2] != null)) {
					signal2 = signalReadings.get(signalGroup.signalNames[2]);
					results.put(signalGroup.signalNames[2], calculateResult(signalGroup.signalNames[2], signal2));
				}
				else {
					signal2 = null;
				}

				// Copy the readings to the sensor data file
				if (recordRawData) {
					if (null != (sensorDataFile = sensorDataFiles.get(sensorName))) {
						sensorDataFile.write(currentTimeMillis, location, signal0, signal1, signal2);
					}
				}
			}

			tripData.addShimmerReading(currentTimeMillis, results);

			// Copy the result readings (average and variance) to the summary data file
			if (null != summaryDataFile) {
				summaryDataFile.write(currentTimeMillis, location, results);
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
			ssd_readings = MyMath.getSumSquareDifferenceD(readings, avg_readings);
			calcReading = new CalcReading(signalName, readings.size(), avg_readings, ssd_readings);
		}
		return calcReading;
	}
	
	private void reset() {
		for (String key: signalReadings.keySet()) {
			signalReadings.get(key).clear();
		}
	}
	// *********************************************************************************
	// *                          Shimmer Message Handler
	// *********************************************************************************

	private final class ShimmerMessageHandler extends Handler {
		
		public void handleMessage(Message msg) {
			
			String filename;

			try {
				switch (msg.what) {
	            
	            case Shimmer.MESSAGE_STATE_CHANGE:
	            	
	                switch (msg.arg1) {
	
	                case Shimmer.STATE_CONNECTED: //this has been deprecated
	                    break;
	                    
	                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
	                	
	                	if (state == State.RUNNING) 
	                		break;
	                	
	                	try {
		    				state = State.RUNNING;
		    		        shimmerVersion = mService.getShimmerVersion(bluetoothAddress);
		    				Shimmer shimmer = mService.getShimmer(bluetoothAddress);
		    				String filenameRoot = "Shimmer" + "(" + String.valueOf(bluetoothAddress) + ") " + String.valueOf(tripId) + " ";
	
		    				// Get list of enabled sensors
		    				List<String> tmp = shimmer.getListofEnabledSensors();
		    				enabledSensors = new String[0];
		    				enabledSensors = tmp.toArray(enabledSensors);
		    				ArrayList<String> signalNames = new ArrayList<String>(); 

		    				// Create arrays to hold sensor reading for each signal of each enabled sensor
		    				for (String sensorName: enabledSensors) {
		    					
		    					// Get the readings for the group  of signals specified
		    					SignalGroup signalGroup = getSignalGroup(sensorName);
	
		    					for (int i = 0; i < signalGroup.signalNames.length; ++i) {
			    					signalReadings.put(signalGroup.signalNames[i], new ArrayList<Double>());
			    					signalNames.add(signalGroup.signalNames[i]);
		    					}
	
			    				// If flag is set, Create data files for writing the data
		    					if (recordRawData) {
			    					RawDataFile_ShimmerSensor rawDataFile = new RawDataFile_ShimmerSensor(filenameRoot + sensorName, tripId, dataDir, signalGroup.signalNames);
			    					rawDataFile.open(context);
			    					sensorDataFiles.put(sensorName, rawDataFile);
			    				}
		    				}
		    				
		    				// If flag is set, Create a data summary file
		    				if ((recordRawData) && (signalReadings.size() > 0)) {
		    					String[] arraySignalNames = new String[0];
		    					arraySignalNames = signalNames.toArray(arraySignalNames);
		    					summaryDataFile = new RawDataFile_Shimmer(filenameRoot + "Upload", tripId, dataDir, arraySignalNames);
		    					summaryDataFile.open(context);
		    				}
	
		    				mService.startStreaming(bluetoothAddress);
	                	}
	                	catch(Exception ex) {
	                		Log.e(MODULE_TAG, ex.getMessage());
		    				state = State.FAILED;
	                	}
	                    break;
	
	                case Shimmer.STATE_CONNECTING:
	                    break;
	                    
	                case Shimmer.STATE_NONE:
	    				state = State.FAILED;
	                    break;
	                }
	                break;
	            
	            case Shimmer.MESSAGE_READ:

					if ((msg.obj instanceof ObjectCluster)) {
		            	writeData((ObjectCluster)msg.obj);
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

	private void writeData(ObjectCluster objectCluster){
		
		Multimap<String, FormatCluster> propertyCluster = objectCluster.mPropertyCluster;
		ArrayList<Double> readings;

		for(String key : propertyCluster.keys()) {
			for(FormatCluster f : propertyCluster.get(key)) {
				if (f.mFormat.equalsIgnoreCase("CAL")) {
					if (null != (readings = signalReadings.get(key))) {
						readings.add(f.mData);
					}
				}
			}
		}
	}
}
