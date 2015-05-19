package edu.pdx.cecs.orcyclesensors;

public class SensorRecorderResult {

	private String sensorName;
	private int sensorType;
	private int numSamples;
	private float[] averageValues;
	private float[] sumSquareDifferences;

	public SensorRecorderResult(String sensorName, int sensorType, int numSamples, float[] averageValues, float[] sumSquareDifferences) {
		this.sensorName = sensorName;
		this.sensorType = sensorType;
		this.numSamples = numSamples;
		this.averageValues = averageValues;
		this.sumSquareDifferences = sumSquareDifferences;
	}
	
	public String getSensorName() {
		return sensorName;
	}

	public int getSensorType() {
		return sensorType;
	}

	public int getNumSamples() {
		return numSamples;
	}

	public float[] getValues() {
		return averageValues;
	}

	public float[] getSumSquareDifferences() {
		return sumSquareDifferences;
	}
}
