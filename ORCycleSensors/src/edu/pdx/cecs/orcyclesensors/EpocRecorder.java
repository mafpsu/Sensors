package edu.pdx.cecs.orcyclesensors;

import java.util.HashMap;
import java.util.Map;

import com.emotiv.insight.IEdk;

import edu.pdx.cecs.orcyclesensors.ShimmerRecorder.CalcReading;
import android.content.Context;
import android.location.Location;
import android.util.Log;

public class EpocRecorder {

	private static final String MODULE_TAG = "EpocRecorder";

	private IEdk.IEE_MotionDataChannel_t[] Channel_list = {
			IEdk.IEE_MotionDataChannel_t.IMD_COUNTER, IEdk.IEE_MotionDataChannel_t.IMD_GYROX,IEdk.IEE_MotionDataChannel_t.IMD_GYROY,
			IEdk.IEE_MotionDataChannel_t.IMD_GYROZ,IEdk.IEE_MotionDataChannel_t.IMD_ACCX,IEdk.IEE_MotionDataChannel_t.IMD_ACCY,IEdk.IEE_MotionDataChannel_t.IMD_ACCZ,
			IEdk.IEE_MotionDataChannel_t.IMD_MAGX,IEdk.IEE_MotionDataChannel_t.IMD_MAGY,IEdk.IEE_MotionDataChannel_t.IMD_MAGZ,IEdk.IEE_MotionDataChannel_t.IMD_TIMESTAMP};
	
	HashMap<String, CalcReading> eegReadings = new HashMap<String, CalcReading>();

	private Context context;
	private final String bluetoothAddress;
	private long tripId;
	private String dataDir;
    private static Map<String, EpocRecorder> epocRecorders = new HashMap<String, EpocRecorder>();
    private RawDataFile_Epoc rawDataFile = null;

    public enum State { IDLE, CONNECTING, RUNNING, PAUSED, FAILED };
	
    private State state = State.IDLE;

	private EpocRecorder(Context context, String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {
		this.context = context;
		this.bluetoothAddress = bluetoothAddress;
		this.tripId = tripId;
		this.dataDir = dataDir;
		if (recordRawData) {
			this.rawDataFile = new RawDataFile_Epoc("Epoc_" + bluetoothAddress + ".csv" , tripId, dataDir);
		}
	}

	public static EpocRecorder create(Context context, String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {
		
		EpocRecorder epocRecorder = null;
		
		if (epocRecorders.containsKey(bluetoothAddress)) {
			epocRecorders.remove(bluetoothAddress);
		}

		epocRecorders.put(bluetoothAddress, epocRecorder = new EpocRecorder(context, bluetoothAddress, recordRawData, tripId, dataDir));

		return epocRecorder;
	}

	synchronized public void start(Context context) {
    	// state = State.CONNECTING; TODO: change back
    	state = State.RUNNING;
		if (null != rawDataFile) {
			rawDataFile.open(context);
		}
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
        closeRawDataFile();
        //signalReadings.clear();
        epocRecorders.remove(bluetoothAddress);
	}

	/**
	 * Write the result for each enabled sensor/group of enabled sensor signals
	 * @param tripData
	 * @param currentTimeMillis
	 * @param location
	 */
 	synchronized public void writeResult(TripData tripData, long currentTimeMillis, Location location) {
		if (null != rawDataFile) {
			rawDataFile.write(currentTimeMillis, location, eegReadings);
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
}
