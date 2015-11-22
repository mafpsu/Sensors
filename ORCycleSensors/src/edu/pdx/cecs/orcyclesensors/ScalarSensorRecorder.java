package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.location.Location;

public class ScalarSensorRecorder extends SensorRecorder {
	
	private ArrayList<Float> readings0;
	private int numSamples;
	private RawDataFile_ScalarSensor rawDataFile;

	public ScalarSensorRecorder(String name, int type, int rate, RawDataFile_ScalarSensor rawDataFile) {
		super(name, type, rate, rawDataFile);
		this.rawDataFile = rawDataFile;
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
	
	synchronized public void writeResult(TripData tripData, long currentTimeMillis, Location location) {
		
		float[] averageValues;
		double[] standardDeviation;

		averageValues = new float[1];
		standardDeviation = new double[1];
		
		if (readings0.size() == 0) {
			averageValues[0] = 0;
			standardDeviation[0] = 0;
		}
		else {
			averageValues[0] = MyMath.getAverageValueF(readings0);
			standardDeviation[0] = MyMath.getStandardDeviationF(readings0, averageValues[0]);
		}
		
		tripData.addSensorReadings(currentTimeMillis, sensorName, type, numSamples,
				averageValues, standardDeviation);
		
		if (null != rawDataFile) {
			rawDataFile.write(currentTimeMillis, location, readings0);
		}

		reset();
	}
}
