package edu.pdx.cecs.orcyclesensors;

import android.hardware.Sensor;

public class SensorItem {

	public static final String STRING_TYPE_ACCELEROMETER = "Accelerometer";
	public static final String STRING_TYPE_AMBIENT_TEMPERATURE = "Ambient Temperature";
	public static final String STRING_TYPE_GAME_ROTATION_VECTOR = "Game rotation Vector";
	public static final String STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR = "Geomagnetic Rotation Vector";
	public static final String STRING_TYPE_GRAVITY = "Gravity";
	public static final String STRING_TYPE_GYROSCOPE = "Gyroscope";
	public static final String STRING_TYPE_GYROSCOPE_UNCALIBRATED = "Gyroscope Uncalibrated";
	public static final String STRING_TYPE_HEART_RATE = "Heart Rate";
	public static final String STRING_TYPE_LIGHT = "Light";
	public static final String STRING_TYPE_LINEAR_ACCELERATION = "Linear Acceleration";
	public static final String STRING_TYPE_MAGNETIC_FIELD = "Magnetic Field";
	public static final String STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED = "Magnetic Field Uncalibrated";
	public static final String STRING_TYPE_PRESSURE = "Pressure";
	public static final String STRING_TYPE_PROXIMITY = "Proximity";
	public static final String STRING_TYPE_RELATIVE_HUMIDITY = "Relative Humidity";
	public static final String STRING_TYPE_ROTATION_VECTOR = "Rotation Vector";
	public static final String STRING_TYPE_SIGNIFICANT_MOTION = "Significant Motion";
	public static final String STRING_TYPE_STEP_COUNTER = "Step Counter";
	public static final String STRING_TYPE_STEP_DETECTOR = "Step Detector";
	
	public static final String STRING_RATE_SENSOR_DELAY_FASTEST = "Fastest";
	public static final String STRING_RATE_SENSOR_DELAY_GAME = "Game";
	public static final String STRING_RATE_SENSOR_DELAY_UI = "UI";
	public static final String STRING_RATE_SENSOR_DELAY_NORMAL = "Normal";
	public static final String STRING_RATE_SENSOR_DELAY_OFF = "Off";
	
	protected int fifoMaxEventCount = 0;
	protected int fifoReservedEventCount = 0;
	protected int maxDelay = 0;
	protected float maximumRange = 0;
	protected int minDelay = 0;
	protected String name;
	protected float power = 0.0f;
	protected int reportingMode = 0;
	protected float resolution = 0.0f;
	protected int type = 0;
	protected String vendor = null;
	protected int version = 0;
	protected boolean isWakeUpSensor = false;
	protected int rate = 0;

	public static String stringType(int type) {
		
		switch(type) {
		case Sensor.TYPE_ACCELEROMETER: // 1
			return STRING_TYPE_ACCELEROMETER;

		case Sensor.TYPE_AMBIENT_TEMPERATURE: // 13
			return STRING_TYPE_AMBIENT_TEMPERATURE;
			
		case Sensor.TYPE_GAME_ROTATION_VECTOR: // 15
			return STRING_TYPE_GAME_ROTATION_VECTOR;
			
		case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR: // 20
			return STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR;
			
		case Sensor.TYPE_GRAVITY: // 9
			return STRING_TYPE_GRAVITY;
			
		case Sensor.TYPE_GYROSCOPE: // 4
			return STRING_TYPE_GYROSCOPE;
			
		case Sensor.TYPE_GYROSCOPE_UNCALIBRATED: // 16
			return STRING_TYPE_GYROSCOPE_UNCALIBRATED;
			
		case Sensor.TYPE_HEART_RATE: // 21
			return STRING_TYPE_HEART_RATE;
			
		case Sensor.TYPE_LIGHT: // 5
			return STRING_TYPE_LIGHT;
			
		case Sensor.TYPE_LINEAR_ACCELERATION: // 10
			return STRING_TYPE_LINEAR_ACCELERATION;
			
		case Sensor.TYPE_MAGNETIC_FIELD: // 2
			return STRING_TYPE_MAGNETIC_FIELD;
			
		case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED: // 14
			return STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED;
			
		// This constant was deprecated in API level 8.
		// use SensorManager.getOrientation() instead
		// case Sensor.TYPE_ORIENTATION: // 3
			//return Sensor.STRING_TYPE_ORIENTATION;
			
		case Sensor.TYPE_PRESSURE: // 6
			return STRING_TYPE_PRESSURE;
			
		case Sensor.TYPE_PROXIMITY: // 8
			return STRING_TYPE_PROXIMITY;
			
		case Sensor.TYPE_RELATIVE_HUMIDITY: // 12
			return STRING_TYPE_RELATIVE_HUMIDITY;
			
		case Sensor.TYPE_ROTATION_VECTOR: // 12
			return STRING_TYPE_ROTATION_VECTOR;
			
		case Sensor.TYPE_SIGNIFICANT_MOTION: // 17
			return STRING_TYPE_SIGNIFICANT_MOTION;
			
		case Sensor.TYPE_STEP_COUNTER: // 19
			return STRING_TYPE_STEP_COUNTER;
			
		case Sensor.TYPE_STEP_DETECTOR: // 18
			return STRING_TYPE_STEP_DETECTOR;
			
		// This constant was deprecated in API level 14. use 
		// Sensor.TYPE_AMBIENT_TEMPERATURE instead
		// case Sensor.TYPE_TEMPERATURE: // 7 - 
			//return Sensor.STRING_TYPE_;
			
		default: return null;
		}
	}
	
	public SensorItem() {
	}

	public SensorItem(SensorListItem sensorListItem) {
		this.fifoMaxEventCount = sensorListItem.getFifoMaxEventCount();
		this.fifoReservedEventCount = sensorListItem.getFifoReservedEventCount();
		this.maxDelay = -1;
		this.maximumRange = sensorListItem.getMaximumRange();
		this.minDelay = sensorListItem.getMinDelay();
		this.name = new String(sensorListItem.getName());
		this.power = sensorListItem.getPower();
		this.reportingMode = -1;
		this.resolution = sensorListItem.getResolution();
		this.type = sensorListItem.getType();
		this.vendor = sensorListItem.getVendor();
		this.version = sensorListItem.getVersion();
		this.isWakeUpSensor = false;
		this.rate = sensorListItem.getRate();
	}

	public SensorItem(SensorItem sensorItem) {
		this.fifoMaxEventCount = sensorItem.getFifoMaxEventCount();
		this.fifoReservedEventCount = sensorItem.getFifoReservedEventCount();
		this.maxDelay = -1;
		this.maximumRange = sensorItem.getMaximumRange();
		this.minDelay = sensorItem.getMinDelay();
		this.name = new String(sensorItem.getName());
		this.power = sensorItem.getPower();
		this.reportingMode = -1;
		this.resolution = sensorItem.getResolution();
		this.type = sensorItem.getType();
		this.vendor = sensorItem.getVendor();
		this.version = sensorItem.getVersion();
		this.isWakeUpSensor = false;
		this.rate = sensorItem.getRate();
	}

	public SensorItem(
			int fifoMaxEventCount,
			int fifoReservedEventCount,
			int maxDelay,
			float maximumRange,
			int minDelay,
			String name,
			float power,
			int reportingMode,
			float resolution,
			int type,
			String vendor,
			int version,
			boolean isWakeUpSensor,
			int rate) {
		this.fifoMaxEventCount = fifoMaxEventCount;
		this.fifoReservedEventCount = fifoReservedEventCount;
		this.maxDelay = maxDelay;
		this.maximumRange = maximumRange;
		this.minDelay = minDelay;
		this.name = new String(name);
		this.power = power;
		this.reportingMode = reportingMode;
		this.resolution = resolution;
		this.type = type;
		this.vendor = vendor;
		this.version = version;
		this.isWakeUpSensor = isWakeUpSensor;
		this.rate = rate;
	}

	public int getFifoMaxEventCount() {
		return fifoMaxEventCount;
	}

	public int getFifoReservedEventCount() {
		return fifoReservedEventCount;
	}

	public int getMaxDelay() {
		return maxDelay;
	}

	public float getMaximumRange() {
		return maximumRange;
	}

	public int getMinDelay() {
		return minDelay;
	}

	public String getName() {
		return name;
	}
	
	public float getPower() {
		return power;
	}

	public int getReportingMode() {
		return reportingMode;
	}

	public float getResolution() {
		return resolution;
	}

	public String getStringType() {
		return stringType(type);
	}

	public int getType() {
		return type;
	}

	public String getVendor() {
		return vendor;
	}
	
	public int getVersion() {
		return version;
	}

	public boolean isWakeUpSensor() {
		return this.isWakeUpSensor;
	}

	public int getRate() {
		return rate;
	}

	public SensorItem setRate(int rate) {
		this.rate = rate;
		return this;
	}

	public String getStringRate() {
		switch(rate) {
		case 0: return STRING_RATE_SENSOR_DELAY_FASTEST;
		case 1: return STRING_RATE_SENSOR_DELAY_GAME;
		case 2: return STRING_RATE_SENSOR_DELAY_UI;
		case 3: return STRING_RATE_SENSOR_DELAY_NORMAL;
		default: return STRING_RATE_SENSOR_DELAY_OFF;
		}
	}

	public void copy(SensorItem sensorItem) {
		this.fifoMaxEventCount = sensorItem.fifoMaxEventCount;
		this.fifoReservedEventCount = sensorItem.fifoReservedEventCount;
		this.maxDelay = sensorItem.maxDelay;
		this.maximumRange = sensorItem.maximumRange;
		this.minDelay = sensorItem.minDelay;
		this.name = new String(sensorItem.name);
		this.power = sensorItem.power;
		this.reportingMode = sensorItem.reportingMode;
		this.resolution = sensorItem.resolution;
		this.type = sensorItem.type;
		this.vendor = new String(sensorItem.getVendor());
		this.version = sensorItem.version;
		this.isWakeUpSensor = sensorItem.isWakeUpSensor;
		this.rate = sensorItem.rate;
	}
}
