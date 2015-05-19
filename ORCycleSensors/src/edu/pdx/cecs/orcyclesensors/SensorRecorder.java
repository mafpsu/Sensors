package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorRecorder implements SensorEventListener {
	
	private static final String MODULE_TAG = "AntDeviceRecorder";
	
	private enum State { IDLE, RUNNING, PAUSED, FAILED };
	private enum SensorRate { IDLE, RUNNING, PAUSED, FAILED };
	
	private State state;
	private String sensorName;
	private int type;
	private int rate;
	private int numValuesToRead;
	private int numSamples;
	
	private ArrayList<Float> readings0;
	private ArrayList<Float> readings1;
	private ArrayList<Float> readings2;
	private float[] averageValues;
	private float[] sumSquareDifferences;
	
	

	public static SensorRecorder create(String sensorName, int type, int rate) {
		
		switch(type) {
		case Sensor.TYPE_ACCELEROMETER: // 1
			return new SensorRecorder(sensorName, type, rate);

		case Sensor.TYPE_AMBIENT_TEMPERATURE: // 13
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_GAME_ROTATION_VECTOR: // 15
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR: // 20
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_GRAVITY: // 9
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_GYROSCOPE: // 4
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_GYROSCOPE_UNCALIBRATED: // 16
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_HEART_RATE: // 21
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_LIGHT: // 5
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_LINEAR_ACCELERATION: // 10
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_MAGNETIC_FIELD: // 2
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED: // 14
			return new SensorRecorder(sensorName, type, rate);
			
		// This constant was deprecated in API level 8.
		// use SensorManager.getOrientation() instead
		// case Sensor.TYPE_ORIENTATION: // 3
			//return new SensorRecorder(name, type);
			
		case Sensor.TYPE_PRESSURE: // 6
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_PROXIMITY: // 8
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_RELATIVE_HUMIDITY: // 12
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_SIGNIFICANT_MOTION: // 17
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_STEP_COUNTER: // 19
			return new SensorRecorder(sensorName, type, rate);
			
		case Sensor.TYPE_STEP_DETECTOR: // 18
			return new SensorRecorder(sensorName, type, rate);
			
		// This constant was deprecated in API level 14. use 
		// Sensor.TYPE_AMBIENT_TEMPERATURE instead
		// case Sensor.TYPE_TEMPERATURE: // 7 - 
			//return new SensorRecorder(name, type);
			
		case Sensor.TYPE_ROTATION_VECTOR:
			return new SensorRecorder(sensorName, type, rate);
			
		default: return null;
		}
	}
	
	private static int getNumValuesToRead(int type) {
		
		final int unknown = -1;
		final int guess_1 = 1;
		final int guess_3 = 3;
		
		switch(type) {
		case Sensor.TYPE_ACCELEROMETER: // 1
			return 3;

		case Sensor.TYPE_AMBIENT_TEMPERATURE: // 13
			return 1;
			
		case Sensor.TYPE_GAME_ROTATION_VECTOR: // 15
			return guess_3;
			
		case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR: // 20
			return guess_3;
			
		case Sensor.TYPE_GRAVITY: // 9
			return 3;
			
		case Sensor.TYPE_GYROSCOPE: // 4
			return 3;
			
		case Sensor.TYPE_GYROSCOPE_UNCALIBRATED: // 16
			return guess_3;
			
		case Sensor.TYPE_HEART_RATE: // 21
			return guess_1;
			
		case Sensor.TYPE_LIGHT: // 5
			return 1;
			
		case Sensor.TYPE_LINEAR_ACCELERATION: // 10
			return 3;
			
		case Sensor.TYPE_MAGNETIC_FIELD: // 2
			return 3;
			
		case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED: // 14
			return guess_3;
			
		// This constant was deprecated in API level 8.
		// use SensorManager.getOrientation() instead
		// case Sensor.TYPE_ORIENTATION: // 3
			//return unknown;
			
		case Sensor.TYPE_PRESSURE: // 6
			return 1;
			
		case Sensor.TYPE_PROXIMITY: // 8
			return 1;
			
		case Sensor.TYPE_RELATIVE_HUMIDITY: // 12
			return 1;
			
		case Sensor.TYPE_SIGNIFICANT_MOTION: // 17
			return guess_1;
			
		case Sensor.TYPE_STEP_COUNTER: // 19
			return guess_1;
			
		case Sensor.TYPE_STEP_DETECTOR: // 18
			return guess_1;
			
		// This constant was deprecated in API level 14. use 
		// Sensor.TYPE_AMBIENT_TEMPERATURE instead
		// case Sensor.TYPE_TEMPERATURE: // 7 - 
			//return unknown;
			
		case Sensor.TYPE_ROTATION_VECTOR:
			return 3;
			
		default: return unknown;
		}
	}

	private SensorRecorder(String name, int type, int rate) {
		this.sensorName = name;
		this.type = type;
		this.rate = rate;
		this.numValuesToRead = getNumValuesToRead(type);
		this.state = State.IDLE;
		this.readings0 = new ArrayList<Float>(1024);
		this.readings0.ensureCapacity(1024);
		this.readings1 = new ArrayList<Float>(1024);
		this.readings1.ensureCapacity(1024);
		this.readings2 = new ArrayList<Float>(1024);
		this.readings2.ensureCapacity(1024);
		this.reset();
	}
	
	synchronized public void start(Context context) {
		
		SensorManager sensorManager;
		
		// Get reference to sensor manager
    	if (null != (sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE))) {
    		List<Sensor> hardwareSensors;
    		// Get list of hardware sensors
	    	if (null != (hardwareSensors = sensorManager.getSensorList(Sensor.TYPE_ALL))) {    	
		    	for (Sensor hardwareSensor : hardwareSensors) {
		    		// Search for the first matching sensor name and return it
	    			if (sensorName.equals(hardwareSensor.getName())) {
	    				sensorManager.registerListener(this, hardwareSensor, rate);
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
		this.state = State.IDLE;
	}

	public String getSensorName() {
		return this.sensorName;
	}
	
	synchronized public void addSample(float[] values) {
		
		if (State.RUNNING == state) {
			
			switch(numValuesToRead) {
			
			case 1:
				readings0.add(values[0]);
				++numSamples;
				break;
			
			case 3:
				readings0.add(values[0]);
				readings1.add(values[1]);
				readings2.add(values[2]);
				++numSamples;
				break;
			
			default:
				break;
			}
		}
	}
	
	synchronized public SensorRecorderResult getResult() {
		calcValues();
		SensorRecorderResult result = new SensorRecorderResult(sensorName, type, numSamples, averageValues, sumSquareDifferences);
		reset();
		return result;
	}
	
	private void calcValues() {
		
		switch(numValuesToRead) {
		
		case 1:
			
			averageValues = new float[1];
			averageValues[0] = getAverageValue(readings0);
			sumSquareDifferences = new float[1];
			sumSquareDifferences[0] = getSumSquareDifference(readings0, averageValues[0]);
			break;
		
		case 3:
			
			averageValues = new float[3];
			averageValues[0] = getAverageValue(readings0);
			averageValues[1] = getAverageValue(readings1);
			averageValues[2] = getAverageValue(readings2);
			sumSquareDifferences = new float[3];
			sumSquareDifferences[0] = getSumSquareDifference(readings0, averageValues[0]);
			sumSquareDifferences[1] = getSumSquareDifference(readings1, averageValues[1]);
			sumSquareDifferences[2] = getSumSquareDifference(readings2, averageValues[2]);
			break;
		
		default:
			
			averageValues = null;
			Log.e(MODULE_TAG, "Invalid number of sensor values encountered");
			break;
		}
	}
	
	private float getAverageValue(ArrayList<Float> readings) {
		float sum = 0.0f;
		for (float reading : readings) {
			sum += reading;
		}
		return sum / (float) readings.size();
	}

	private float getSumSquareDifference(ArrayList<Float> readings, float average) {

		float sum = 0.0f;
		float value;

		for (float reading : readings) {
			value = reading - average;
			sum += (value * value);
		}
		return sum;
	}

	private void reset() {
		numSamples = 0;
		readings0.clear();
		readings1.clear();
		readings2.clear();
	}
	
	@Override
	synchronized public void onSensorChanged(SensorEvent event) {
		addSample(event.values);
	}

	@Override
	synchronized public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
