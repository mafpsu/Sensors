package edu.pdx.cecs.orcyclesensors;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public abstract class AntDeviceRecorder implements IDeviceStateChangeReceiver {

	private static final String MODULE_TAG = "AntDeviceRecorder";

	public enum State { IDLE, CONNECTING, RUNNING, PAUSED, FAILED };

	// **********************************
	// * Abstract methods
	// **********************************
	
    protected abstract void requestAccessToPcc();
    
    protected abstract void unregister();
    
    protected abstract void writeResult(TripData tripData, long currentTimeMillis);
    
	// ************************************************
	// * Protected section classes, methods & variables
	// ************************************************

	protected final int deviceNumber;
	
	protected Context context;
	
	protected State state = State.IDLE;
    
	protected AntDeviceRecorder(int deviceNumber) {
		this.deviceNumber = deviceNumber;
	}

	protected static void showResultStatus(Context context, String deviceName, boolean supportsRssi,
			RequestAccessResult resultCode, DeviceState initialDeviceState) {
		switch(resultCode)
	    {
	        case SUCCESS:
	            Log.i(MODULE_TAG, deviceName + ": " + initialDeviceState);
	            if(!supportsRssi) {
	            	Log.i(MODULE_TAG,"tv_rssi: N/A");
	            	}
	            break;

	        case CHANNEL_NOT_AVAILABLE:
	            Toast.makeText(context, "Channel Not Available", Toast.LENGTH_SHORT).show();
	            break;
	            
	        case ADAPTER_NOT_DETECTED:
	            Toast.makeText(context, "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", Toast.LENGTH_SHORT).show();
	            break;
	            
	        case BAD_PARAMS:
	            //Note: Since we compose all the params ourself, we should never see this result
	            Toast.makeText(context, "Bad request parameters.", Toast.LENGTH_SHORT).show();
	            break;
	            
	        case OTHER_FAILURE:
	            Toast.makeText(context, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
	            break;
	            
	        case DEPENDENCY_NOT_INSTALLED:
	            Toast.makeText(context, "Ant+ libray not installed.", Toast.LENGTH_SHORT).show();
	            break;
	            
	        case USER_CANCELLED:
	            Toast.makeText(context, "User cancelled!", Toast.LENGTH_SHORT).show();
	            break;

	        case UNRECOGNIZED:
	            Toast.makeText(context, "Failed: UNRECOGNIZED. PluginLib Upgrade Required?", Toast.LENGTH_SHORT).show();
	            break;
	            
	        case SEARCH_TIMEOUT:
	            Toast.makeText(context, "Ant+ deviced timed out.", Toast.LENGTH_SHORT).show();
	            break;
	            
	        default:
	            Toast.makeText(context, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
	            break;
	    }
	}

	// **********************************
	// * public interface
	// **********************************
	
	public static AntDeviceRecorder create(int deviceNumber, DeviceType deviceType) throws Exception {
		
		if (deviceType == DeviceType.HEARTRATE) {
			return new AntDeviceHeartRateRecorder(deviceNumber);
		}
		else if (deviceType == DeviceType.BIKE_POWER) {
			return new AntDeviceBikePowerRecorder(deviceNumber);
		}

		throw new Exception("Attempt to create unknown DeviceRecorder type.");
	}
	
	synchronized public void start(Context context) {

		this.context = context;
		handleReset();
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
	
	private void handleReset()
    {
        //Release the old access if it exists
		unregister();
        requestAccessToPcc();
    }

	@Override
	synchronized public void onDeviceStateChange(
			final DeviceState newDeviceState) {
	}
}
