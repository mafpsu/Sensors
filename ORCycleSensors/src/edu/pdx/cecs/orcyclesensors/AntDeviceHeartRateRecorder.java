package edu.pdx.cecs.orcyclesensors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;

import android.location.Location;
import android.util.Log;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;

public class AntDeviceHeartRateRecorder extends AntDeviceRecorder implements 
	IPluginAccessResultReceiver<AntPlusHeartRatePcc> , 
	IHeartRateDataReceiver {

	private static final String MODULE_TAG = "AntDeviceHeartRateRecorder";

    private final ArrayList<Integer>  heartRates = new ArrayList<Integer>(1024); 
	private final RawDataFile_HeartRate rawDataFile;

	// **************************************************************
    // *                        Constructors
    // **************************************************************

    public AntDeviceHeartRateRecorder(int deviceNumber, RawDataFile_HeartRate rawDataFile) {
		super(deviceNumber, rawDataFile);
		this.rawDataFile = rawDataFile;
	}
    
	// **************************************************************
    // *             Connection variables and methods
    // **************************************************************

    private AntPlusHeartRatePcc hrPcc = null;

	private PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;
	
	/**
	 * Starts the plugins UI search
	 */
    public void requestAccessToPcc()
    {
    	state = State.CONNECTING;
	    releaseHandle = AntPlusHeartRatePcc.requestAccess(this.context, deviceNumber, 0, this, this);
    }
    
	/**
	 * Handle the result, connecting to events on success or reporting failure to user.
	 */
	@Override
	synchronized public void onResultReceived(AntPlusHeartRatePcc result,
			RequestAccessResult resultCode, DeviceState initialDeviceState) {

		try {
			if ((null != result) && (RequestAccessResult.SUCCESS == resultCode)) {
				showResultStatus(context, result.getDeviceName(), result.supportsRssi(), resultCode, initialDeviceState);
				hrPcc = result;
				state = State.RUNNING;
				subscribeToEvents();
			} else {
				hrPcc = null;
				state = State.FAILED;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	synchronized public void unregister() {
        if(releaseHandle != null)
        	
        {
            releaseHandle.close();
            releaseHandle = null;
        }
        closeRawDataFile();
	}

    // **************************************************************
    // *                     Data Event Handlers
    // **************************************************************

    public void subscribeToEvents()
    {
        hrPcc.subscribeHeartRateDataEvent(this);
    }

	@Override
	synchronized public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
	    final int computedHeartRate, final long heartBeatCount,
	    final BigDecimal heartBeatEventTime, final DataState dataState)
	{
		try {
			if (DataState.UNRECOGNIZED != dataState) {
				if (State.RUNNING == state) {
					heartRates.add(computedHeartRate);
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	synchronized public void writeResult(TripData tripData, long currentTimeMillis, Location location) {
		
		float avgHeartRate = 0.0f;
		double stdHeartRate = 0.0f;
		int numSamples = heartRates.size();
		
		if (numSamples > 0) {
			avgHeartRate = MyMath.getAverageValueI(heartRates);
			stdHeartRate = MyMath.getStandardDeviationI(heartRates, avgHeartRate);
		}
		tripData.addHeartRateDeviceReading(currentTimeMillis, numSamples, avgHeartRate, stdHeartRate);

		if (null != rawDataFile) {
			rawDataFile.write(currentTimeMillis, location, heartRates);
		}

		heartRates.clear();
	}
}

