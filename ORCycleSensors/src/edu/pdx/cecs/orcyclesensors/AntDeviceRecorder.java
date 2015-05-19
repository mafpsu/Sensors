package edu.pdx.cecs.orcyclesensors;

import java.math.BigDecimal;
import java.util.EnumSet;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class AntDeviceRecorder {

	private static final String MODULE_TAG = "AntDeviceRecorder";
	
	private static void showResultStatus(Context context, AntPlusHeartRatePcc result, 
			RequestAccessResult resultCode, DeviceState initialDeviceState) {
		switch(resultCode)
	    {
	        case SUCCESS:
	            Log.i(MODULE_TAG, result.getDeviceName() + ": " + initialDeviceState);
	            if(!result.supportsRssi()) {
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

	private final class HeartRateDataEvent implements IHeartRateDataReceiver {
		@Override
		synchronized public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
		    final int computedHeartRate, final long heartBeatCount,
		    final BigDecimal heartBeatEventTime, final DataState dataState)
		{
			if (DataState.UNRECOGNIZED != dataState) {
				addSample(computedHeartRate);
			}
		}
	}

	private final class DeviceStateChangeReceiver implements
			IDeviceStateChangeReceiver {
		@Override
		synchronized public void onDeviceStateChange(final DeviceState newDeviceState) {
			
			// TODO: this should be synchronized
			AntDeviceRecorder.this.deviceState = newDeviceState;
		}
	}

	private final class AccessResultReceiver implements
			IPluginAccessResultReceiver<AntPlusHeartRatePcc> {
		//Handle the result, connecting to events on success or reporting failure to user.
		@Override
		synchronized public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
		    DeviceState initialDeviceState)
		{
			showResultStatus(context, result, resultCode, initialDeviceState);
			
			if (RequestAccessResult.SUCCESS == resultCode) {
	            hrPcc = result;
	            state = State.RUNNING;
	            subscribeToHrEvents();
			}
			else {
				hrPcc = null;
	            state = State.FAILED;
			}
		}
	}

	private Context context;
	private int deviceNumber;
	private int deviceType;
	private DeviceState deviceState;
    private float sumHeartRate = 0; 
    private int numSamples = 0; 

	public enum State { IDLE, CONNECTING, RUNNING, PAUSED, FAILED };
	private State state = State.IDLE;
	
	private PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;
    private AntPlusHeartRatePcc hrPcc = null;

    //Receives device's connection result
    private IPluginAccessResultReceiver<AntPlusHeartRatePcc> accessResultReciever =
            new AccessResultReceiver();

	//Receives state changes
	private  IDeviceStateChangeReceiver deviceStateChangeReceiver =
	    new DeviceStateChangeReceiver();
            
	public static AntDeviceRecorder create(int deviceNumber, int type) {
		return new AntDeviceRecorder(deviceNumber, type);
	}
	
	private AntDeviceRecorder(int deviceNumber, int deviceType) {
		this.deviceNumber = deviceNumber;
		this.deviceType = deviceType;
	}
	
	private float[] averageValues() {
		
		float[] averageValues;
		
		switch(deviceType) {
		
		case AntDeviceInfo.HEART_RATE_DEVICE:
			averageValues = new float[1];
			averageValues[0] = (numSamples == 0 ? 0 : sumHeartRate/(float)numSamples);
			break;
		
		default:
			averageValues = null;
			break;
		}

		return averageValues;
	}
	
	synchronized private void addSample(int computedHeartRate) {
		if (State.RUNNING == state) {
			sumHeartRate += computedHeartRate;
			++numSamples;		
		}
	}
	
	synchronized public AntDeviceRecorderResult getResult() {
		AntDeviceRecorderResult result = new AntDeviceRecorderResult(deviceNumber, deviceType, numSamples, averageValues());
		reset();
		return result;
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

	synchronized public void unregister() {
        if(releaseHandle != null)
        	
        {
            releaseHandle.close();
            releaseHandle = null;
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

    private void requestAccessToPcc()
    {
    	state = State.CONNECTING;

	    // starts the plugins UI search
	    releaseHandle = AntPlusHeartRatePcc.requestAccess(this.context,
	        deviceNumber, 0, accessResultReciever, deviceStateChangeReceiver);
    }
    
    private void subscribeToHrEvents()
    {
        hrPcc.subscribeHeartRateDataEvent(new HeartRateDataEvent());
    }
    
	private void reset() {
		sumHeartRate = 0.0f;
		numSamples = 0;
	}
}
