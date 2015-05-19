package edu.pdx.cecs.orcyclesensors;

public class SensorConfiguration {

	private static SensorConfiguration config = new SensorConfiguration();

	private boolean mHasAccelerometer;
	private boolean mHasGyroscope;
	private boolean mHasMagnetometer;
	private boolean mHasGravity;
	private boolean mHasLinearAcceleration;
	private boolean mHasRotation;
	private boolean mHasTemperature;
	private boolean mHasProximity;
	private boolean mHasLight;
	private boolean mHasPressure;
	private boolean mHasHumidity;
	
	public static SensorConfiguration getInstance() {
		return config;
	}

	public SensorConfiguration newInstance() {
		return new SensorConfiguration();
	}

	public void save() {
		
	}
	
	public boolean hasAccelerometer() {
		return mHasAccelerometer;
	}
	public boolean hasGyroscope() {
		return mHasGyroscope;
	}
	public boolean hasMagnetometer() {
		return mHasMagnetometer;
	}
	public boolean hasGravity() {
		return mHasGravity;
	}
	public boolean hasLinearAcceleration() {
		return mHasLinearAcceleration;
	}
	public boolean hasRotation() {
		return mHasRotation;
	}
	public boolean hasTemperature() {
		return mHasTemperature;
	}
	public boolean hasProximity() {
		return mHasProximity;
	}
	public boolean hasLight() {
		return mHasLight;
	}
	public boolean hasPressure() {
		return mHasPressure;
	}
	public boolean hasHumidity() {
		return mHasHumidity;
	}
	
	public void setHasAccelerometer(boolean value) {
		mHasAccelerometer = value;
	}
	public void setHasGyroscope(boolean value) {
		mHasGyroscope = value;
	}
	public void setHasMagnetometer(boolean value) {
		mHasMagnetometer = value;
	}
	public void setHasGravity(boolean value) {
		mHasGravity = value;
	}
	public void setHasLinearAcceleration(boolean value) {
		mHasLinearAcceleration = value;
	}
	public void setHasRotation(boolean value) {
		mHasRotation = value;
	}
	public void setHasTemperature(boolean value) {
		mHasTemperature = value;
	}
	public void setHasProximity(boolean value) {
		mHasProximity = value;
	}
	public void setHasLight(boolean value) {
		mHasLight = value;
	}
	public void setHasPressure(boolean value) {
		mHasPressure = value;
	}
	public void setHasHumidity(boolean value) {
		mHasHumidity = value;
	}
}
