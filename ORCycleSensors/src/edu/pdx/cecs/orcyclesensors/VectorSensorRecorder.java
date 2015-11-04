package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.location.Location;

public class VectorSensorRecorder extends SensorRecorder {

	private ArrayList<Float> readings0;
	private ArrayList<Float> readings1;
	private ArrayList<Float> readings2;
	private int numSamples;
	private final RawDataFile_VectorSensor rawDataFile;

	public VectorSensorRecorder(String name, int type, int rate, RawDataFile_VectorSensor rawDataFile) {
		super(name, type, rate, rawDataFile);
		this.rawDataFile = rawDataFile;
		this.readings0 = new ArrayList<Float>(1024);
		this.readings0.ensureCapacity(1024);
		this.readings1 = new ArrayList<Float>(1024);
		this.readings1.ensureCapacity(1024);
		this.readings2 = new ArrayList<Float>(1024);
		this.readings2.ensureCapacity(1024);
		reset();
	}
	
	private void reset() {
		numSamples = 0;
		readings0.clear();
		readings1.clear();
		readings2.clear();
	}
	
	synchronized public void addSample(float[] values) {
		if (State.RUNNING == state) {
			readings0.add(values[0]);
			readings1.add(values[1]);
			readings2.add(values[2]);
			++numSamples;
		}
	}

	synchronized public void writeResult(TripData tripData, long currentTimeMillis, Location location) {
		
		float[] averageValues;
		double[] standardDeviations;

		averageValues = new float[3];
		standardDeviations = new double[3];
		
		if (readings0.size() == 0) {
			averageValues[0] = 0;
			standardDeviations[0] = 0;
		}
		else {
			averageValues[0] = MyMath.getAverageValueF(readings0);
			standardDeviations[0] = MyMath.getStandardDeviationF(readings0, averageValues[0]);
		}
		
		if (readings1.size() == 0) {
			averageValues[1] = 0;
			standardDeviations[1] = 0;
		}
		else {
			averageValues[1] = MyMath.getAverageValueF(readings1);
			standardDeviations[1] = MyMath.getStandardDeviationF(readings1, averageValues[1]);
		}
		
		if (readings2.size() == 0) {
			averageValues[2] = 0;
			standardDeviations[2] = 0;
		}
		else {
			averageValues[2] = MyMath.getAverageValueF(readings2);
			standardDeviations[2] = MyMath.getStandardDeviationF(readings2, averageValues[2]);
		}
		
		tripData.addSensorReadings(currentTimeMillis, sensorName, type, numSamples,
				averageValues, standardDeviations);

		if (null != rawDataFile) {
			rawDataFile.write(currentTimeMillis, location, readings0, readings1, readings2);
		}

		reset();
	}
}
