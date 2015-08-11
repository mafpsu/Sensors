package edu.pdx.cecs.orcyclesensors;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

public abstract class SensorRecorder implements SensorEventListener {
	
	private static final String MODULE_TAG = "AntDeviceRecorder";
	
	protected enum State { IDLE, RUNNING, PAUSED, FAILED };
	protected final String sensorName;
	protected final int type;
	protected State state;

	private final RawDataFile rawDataFile;
	private final int rate;
	
	public static SensorRecorder create(String sensorName, int type, int rate, boolean recordRawData, long tripId, String dataDir) {
		
		switch(type) {
		case Sensor.TYPE_ACCELEROMETER: // 1
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);

		case Sensor.TYPE_AMBIENT_TEMPERATURE: // 13
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_GAME_ROTATION_VECTOR: // 15
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null); // guess
			
		case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR: // 20
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null); // guess
			
		case Sensor.TYPE_GRAVITY: // 9
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_GYROSCOPE: // 4
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_GYROSCOPE_UNCALIBRATED: // 16
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_HEART_RATE: // 21
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null); // guess
			
		case Sensor.TYPE_LIGHT: // 5
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_LINEAR_ACCELERATION: // 10
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_MAGNETIC_FIELD: // 2
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED: // 14
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);
			
		// This constant was deprecated in API level 8.
		// use SensorManager.getOrientation() instead
		// case Sensor.TYPE_ORIENTATION: // 3
			//return new SensorRecorder(name, type);
			
		case Sensor.TYPE_PRESSURE: // 6
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_PROXIMITY: // 8
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_RELATIVE_HUMIDITY: // 12
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_SIGNIFICANT_MOTION: // 17
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_STEP_COUNTER: // 19
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		case Sensor.TYPE_STEP_DETECTOR: // 18
			return new ScalarSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_ScalarSensor(sensorName, tripId, dataDir) : null);
			
		// This constant was deprecated in API level 14. use 
		// Sensor.TYPE_AMBIENT_TEMPERATURE instead
		// case Sensor.TYPE_TEMPERATURE: // 7 - 
			//return new SensorRecorder(name, type);
			
		case Sensor.TYPE_ROTATION_VECTOR:
			return new VectorSensorRecorder(sensorName, type, rate, recordRawData ? new RawDataFile_VectorSensor(sensorName, tripId, dataDir) : null);
			
		default: return null;
		}
	}
	
	public SensorRecorder(String name, int type, int rate, RawDataFile rawDataFile) {
		this.sensorName = name;
		this.type = type;
		this.rate = rate;
		this.rawDataFile = rawDataFile;
		this.state = State.IDLE;
	}

	public String getSensorName() {
		return this.sensorName;
	}

	synchronized public void start(Context context) {
		
		SensorManager sensorManager;
		
		// Get reference to sensor manager
    	if (null != (sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE))) {
    		List<Sensor> hardwareSensors;
    		// Get list of hardware sensors
	    	if (null != (hardwareSensors = sensorManager.getSensorList(Sensor.TYPE_ALL))) {    	
		    	for (Sensor hardwareSensor : hardwareSensors) {
		    		// Search for the first matching sensor name and register to receive events
	    			if (sensorName.equals(hardwareSensor.getName())) {
	    				sensorManager.registerListener(this, hardwareSensor, rate);
	    				
	    				if (null != rawDataFile) {
	    					rawDataFile.open(context);
	    				}
	    				
	    				this.state = State.RUNNING;
	    				return;
	    			}
		    	}
	    	}
    	}
		this.state = State.FAILED;
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

	synchronized public void unregister(Context context) {
		
		SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(this);
		
		if (null != rawDataFile) {
			rawDataFile.close();
		}
		
		this.state = State.IDLE;
	}

	abstract void addSample(float[] values);
	
	abstract void writeResult(TripData tripData, long currentTimeMillis, Location location);
	
	@Override
	synchronized public void onSensorChanged(SensorEvent event) {
		addSample(event.values);
	}

	@Override
	synchronized public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
