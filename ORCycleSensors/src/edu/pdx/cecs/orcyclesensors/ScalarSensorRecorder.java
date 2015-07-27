package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

public class ScalarSensorRecorder extends SensorRecorder {
	
	private ArrayList<Float> readings0;
	private int numSamples;

	public ScalarSensorRecorder(String name, int type, int rate) {
		super(name, type, rate);
		this.readings0 = new ArrayList<Float>(1024);
		this.readings0.ensureCapacity(1024);
		reset();
	}
	
	private void reset() {
		numSamples = 0;
		readings0.clear();
	}
	
	synchronized public void addSample(float[] values) {
		if (State.RUNNING == state) {
			readings0.add(values[0]);
			++numSamples;
		}
	}
	
	synchronized public void writeResult(TripData tripData, long currentTimeMillis) {
		
		float[] averageValues;
		float[] sumSquareDifferences;

		averageValues = new float[1];
		sumSquareDifferences = new float[1];
		
		if (readings0.size() == 0) {
			averageValues[0] = 0;
			sumSquareDifferences[0] = 0;
		}
		else {
			averageValues[0] = MyMath.getAverageValueF(readings0);
			sumSquareDifferences[0] = MyMath.getSumSquareDifferenceF(readings0, averageValues[0]);
		}
		
		tripData.addSensorReadings(currentTimeMillis, sensorName, type, numSamples,
				averageValues, sumSquareDifferences);

		reset();
	}
}
