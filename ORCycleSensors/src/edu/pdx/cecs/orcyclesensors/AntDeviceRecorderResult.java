package edu.pdx.cecs.orcyclesensors;

public class AntDeviceRecorderResult {

	private int deviceNumber;
	private int deviceType;
	private int numSamples;
	private float[] values;

	public AntDeviceRecorderResult(int deviceNumber, int deviceType, int numSamples, float[] values) {
		this.deviceNumber = deviceNumber;
		this.deviceType = deviceType;
		this.numSamples = numSamples;
		this.values = values;
	}
	
	public int getDeviceNumber() {
		return deviceNumber;
	}

	public int getDeviceType() {
		return deviceType;
	}

	public int getNumSamples() {
		return numSamples;
	}

	public float[] getValues() {
		return values;
	}
}
