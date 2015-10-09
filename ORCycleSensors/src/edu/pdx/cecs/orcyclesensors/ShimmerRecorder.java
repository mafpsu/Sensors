package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.AntDeviceRecorder.State;
import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ObjectCluster;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer2;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.FormatCluster;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer3;
import edu.pdx.cecs.orcyclesensors.shimmer.tools.Logging;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class ShimmerRecorder {

	private static final String MODULE_TAG = "ShimmerRecorder";

	public enum State { IDLE, CONNECTING, RUNNING, PAUSED, FAILED };

	private ShimmerService mService = null;
	private State state = State.IDLE;
	private int shimmerVersion = -1;
	private final String bluetoothAddress;
	private final RawDataFile rawDataFile;
	// The Handler that gets information back from the BluetoothChatService
    private Handler shimmerMessageHandler = new ShimmerMessageHandler();
    
    // Shimmer readings
    private ArrayList<Double[]> accelReadings = new ArrayList<Double[]>();
    private ArrayList<Double[]> accelLowNoiseReadings = new ArrayList<Double[]>();
    private ArrayList<Double[]> accelWideRangeReadings = new ArrayList<Double[]>();
    private ArrayList<Double[]> gyroscopeReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> magnetometerReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> gsrReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> emgReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> ecgReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> bridgeAmpReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> heartRateReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> expBoardA0Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> expBoardA7Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> batteryVoltageReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> timestampReadings = new ArrayList<Double[]>();
	private ArrayList<Double[]> adcA7Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> adcA6Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> adcA15Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> adcA1Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> adcA12Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> adcA13Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> adcA14Readings = new ArrayList<Double[]>();
	private ArrayList<Double[]> pressureReadings = new ArrayList<Double[]>();

    // ---------------------------------------------------------

	private static String mSensorView = ""; //The sensor device which should be viewed on the graph
    
    
    // ---------------------------------------------------------
	
	public ShimmerRecorder(String bluetoothAddress, RawDataFile rawDataFile) {
		this.bluetoothAddress = bluetoothAddress;
		this.rawDataFile = rawDataFile;
	}

	// **********************************
	// * static interface
	// **********************************
	
	public static ShimmerRecorder create(String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {

		String sensorName = "Shimmer(" + String.valueOf(bluetoothAddress) + ")";
		return new ShimmerRecorder(bluetoothAddress, recordRawData ? new RawDataFile_Shimmer(sensorName, tripId, dataDir) : null);
	}

	// **********************************
	// * public interface
	// **********************************
	
	synchronized public void start(Context context) {

		//handleReset();
        mService = MyApplication.getInstance().getShimmerService();
  		mService.setMessageHandler(shimmerMessageHandler);
		mService.enableGraphingHandler(true);
		mService.connectShimmer(bluetoothAddress, "Device");
  		
		if (null != rawDataFile) {
			rawDataFile.open(context);
		}
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
		if (null != rawDataFile) {
			rawDataFile.close();
		}
	}

	synchronized public void unregister() {
        /*if(releaseHandle != null) {
            releaseHandle.close();
            releaseHandle = null;
        }*/
		mService.stopStreaming(bluetoothAddress);
		mService.enableGraphingHandler(false);
		mService.disconnectShimmer(bluetoothAddress);
        closeRawDataFile();
	}

	synchronized public void writeResult(TripData tripData, long currentTimeMillis, Location location) {
		// TODO:
	}

	// *********************************************************************************
	// *                          Shimmer Message Handler
	// *********************************************************************************

	private final class ShimmerMessageHandler extends Handler {
		
		public void handleMessage(Message msg) {

			try {
				switch (msg.what) {
	            
	            case Shimmer.MESSAGE_STATE_CHANGE:
	            	
	                switch (msg.arg1) {
	
	                case Shimmer.STATE_CONNECTED: //this has been deprecated
	                    break;
	                    
	                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
	    				state = State.RUNNING;
	    		        shimmerVersion = mService.getShimmerVersion(bluetoothAddress);
	    				mService.startStreaming(bluetoothAddress);
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
		
		Collection<FormatCluster> formatClusters = objectCluster.mPropertyCluster.values();
		Iterator<FormatCluster> iterator = formatClusters.iterator();
		while (iterator.hasNext()) {
			FormatCluster f = (FormatCluster) iterator.next();
			Log.d(MODULE_TAG, "format = " + f.mFormat);
			Log.d(MODULE_TAG, "format = " + f.mUnits);
			if (f.mFormat.equals("")) {
				//accelReadings.add(object);
				
			} else if (f.mFormat.equals("")) {
			}
		}
		
		
		
		
		/*
		double[] calibratedDataArray = new double[0];
		String[] sensorName = new String[0];
		String calibratedUnits = "";
		String calibratedUnits2 = "";

		// mSensorView determines which sensor to graph
		// ---------------------------------------------------------------------------
		if (mSensorView.equals("Accelerometer")) {
			sensorName = new String[3]; // for x y and z axis
			calibratedDataArray = new double[3];
			sensorName[0] = Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_X;
			sensorName[1] = Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Y;
			sensorName[2] = Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Z;
		}
		// ---------------------------------------------------------------------------
		if (mSensorView.equals("Low Noise Accelerometer")) {
			sensorName = new String[3]; // for x y and z axis
			calibratedDataArray = new double[3];
			sensorName[0] = Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X;
			sensorName[1] = Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y;
			sensorName[2] = Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z;
		}
		// ---------------------------------------------------------------------------
		if (mSensorView.equals("Wide Range Accelerometer")) {
			sensorName = new String[3]; // for x y and z axis
			calibratedDataArray = new double[3];
			sensorName[0] = "Wide Range Accelerometer X";
			sensorName[1] = "Wide Range Accelerometer Y";
			sensorName[2] = "Wide Range Accelerometer Z";
		}
		// ---------------------------------------------------------------------------
		if (mSensorView.equals("Gyroscope")) {
			sensorName = new String[3]; // for x y and z axis
			calibratedDataArray = new double[3];
			sensorName[0] = "Gyroscope X";
			sensorName[1] = "Gyroscope Y";
			sensorName[2] = "Gyroscope Z";
		}
		if (mSensorView.equals("Magnetometer")) {
			sensorName = new String[3]; // for x y and z axis
			calibratedDataArray = new double[3];
			sensorName[0] = "Magnetometer X";
			sensorName[1] = "Magnetometer Y";
			sensorName[2] = "Magnetometer Z";
		}
		if (mSensorView.equals("GSR")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "GSR";
		}
		if (mSensorView.equals("EMG")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "EMG";
		}
		
		// =======================================================================================================================
		
		if (mSensorView.equals("ECG")) {

			calibratedDataArray = new double[2];
			Shimmer shmr = mService.getShimmer(bluetoothAddress);
			if (shmr != null) {
				if (mService.getShimmer(bluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2
						|| mService.getShimmer(bluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_2R) {
					sensorName = new String[2];
					sensorName[0] = "ECG RA-LL";
					sensorName[1] = "ECG LA-LL";
				}
			}
		}
		if (mSensorView.equals("EXG1") || mSensorView.equals("EXG2") || mSensorView.equals("EXG1 16Bit") || mSensorView.equals("EXG2 16Bit")) {
			Shimmer shmr = mService.getShimmer(bluetoothAddress);
			if (shmr != null) {
				if (mService.getShimmer(bluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3) {
					if (mService.getShimmer(bluetoothAddress).isEXGUsingECG24Configuration()
							|| mService.getShimmer(bluetoothAddress).isEXGUsingECG16Configuration()) {
						sensorName = new String[3];
						calibratedDataArray = new double[3];
						// same name for both 16 and 24 bit
						sensorName[0] = Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT;
						sensorName[1] = Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT;
						sensorName[2] = Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT;
					} else if (mService.getShimmer(bluetoothAddress).isEXGUsingEMG24Configuration()
							|| mService.getShimmer(bluetoothAddress).isEXGUsingEMG16Configuration()) {
						sensorName = new String[2];
						calibratedDataArray = new double[2];
						// same name for both 16 and 24 bit
						sensorName[0] = Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT;
						sensorName[1] = Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT;
					} else if (mService.getShimmer(bluetoothAddress).isEXGUsingTestSignal24Configuration()) {
						sensorName = new String[3];
						calibratedDataArray = new double[3];
						sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
						sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
						sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
					} else if (mService.getShimmer(bluetoothAddress).isEXGUsingTestSignal16Configuration()) {
						sensorName = new String[3];
						calibratedDataArray = new double[3];
						sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
						sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
						sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
					} else {
						sensorName = new String[3];
						calibratedDataArray = new double[3];
						if (mSensorView.equals("EXG1 16Bit") || mSensorView.equals("EXG2 16Bit")) {
							sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT;
							sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT;
							sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT;
						} else {
							sensorName[0] = Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT;
							sensorName[1] = Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT;
							sensorName[2] = Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT;
						}
					}
				}
			}
		}
		if (mSensorView.equals("Bridge Amplifier")) {
			sensorName = new String[2];
			calibratedDataArray = new double[2];
			sensorName[0] = "Bridge Amplifier High";
			sensorName[1] = "Bridge Amplifier Low";
		}
		if (mSensorView.equals("Heart Rate")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "Heart Rate";
		}
		if (mSensorView.equals("ExpBoard A0")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0;

		}
		if (mSensorView.equals("ExpBoard A7")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7;
		}
		if (mSensorView.equals("Battery Voltage")) {
			sensorName = new String[2];
			calibratedDataArray = new double[2];
			sensorName[0] = "VSenseReg";
			sensorName[1] = "VSenseBatt";
		}
		if (mSensorView.equals("Timestamp")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "Timestamp";
		}
		if (mSensorView.equals("External ADC A7")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			Shimmer shmr = mService.getShimmer(bluetoothAddress);
			if (shmr != null) {
				if (mService.getShimmer(bluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3) {
					sensorName[0] = Shimmer3.ObjectClusterSensorName.EXT_EXP_A7;
				} else {
					sensorName[0] = Shimmer2.ObjectClusterSensorName.EXT_EXP_A7;
				}
			}
		}
		if (mSensorView.equals("External ADC A6")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			Shimmer shmr = mService.getShimmer(bluetoothAddress);
			if (shmr != null) {
				if (mService.getShimmer(bluetoothAddress).getShimmerVersion() == ShimmerVerDetails.HW_ID.SHIMMER_3) {
					sensorName[0] = Shimmer3.ObjectClusterSensorName.EXT_EXP_A6;
				} else {
					sensorName[0] = Shimmer2.ObjectClusterSensorName.EXT_EXP_A6;
				}
			}
		}
		if (mSensorView.equals("External ADC A15")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "External ADC A15";
		}
		if (mSensorView.equals("Internal ADC A1")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "Internal ADC A1";
		}
		if (mSensorView.equals("Internal ADC A12")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "Internal ADC A12";
		}
		if (mSensorView.equals("Internal ADC A13")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "Internal ADC A13";
		}
		if (mSensorView.equals("Internal ADC A14")) {
			sensorName = new String[1];
			calibratedDataArray = new double[1];
			sensorName[0] = "Internal ADC A14";
		}
		if (mSensorView.equals("Pressure")) {
			sensorName = new String[2];
			calibratedDataArray = new double[2];
			sensorName[0] = "Pressure";
			sensorName[1] = "Temperature";
		}

		// ******************************************************************************************
		// *
		// ******************************************************************************************
		
		if (sensorName.length != 0) { // Device 1 is the assigned user id, see constructor of the Shimmer
			if (sensorName.length > 0) {
				//
				Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[0]); // first retrieve all the possible formats for the current sensor device
				FormatCluster formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(ofFormats, "CAL"));
				if (formatCluster != null) {
					// //Obtain data for text view
					calibratedDataArray[0] = formatCluster.mData;
					calibratedUnits = formatCluster.mUnits;
				}
			}
		}
		if (sensorName.length > 1) {
			Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[1]); // first retrieve all
			FormatCluster formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(ofFormats, "CAL"));
			if (formatCluster != null) {
				calibratedDataArray[1] = formatCluster.mData;
				calibratedUnits2 = formatCluster.mUnits;
			}
		}
		if (sensorName.length > 2) {

			Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(sensorName[2]); // first retrieve all
			FormatCluster formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(ofFormats, "CAL"));
			if (formatCluster != null) {
				calibratedDataArray[2] = formatCluster.mData;
				data.add(formatCluster.mData);
			}
		}
	*/}
}
