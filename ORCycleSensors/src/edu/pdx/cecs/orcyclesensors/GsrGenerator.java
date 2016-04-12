package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

public class GsrGenerator {

	private final ArrayList<Double> timestamps = new ArrayList<Double>();
	private final ArrayList<Double> readings = new ArrayList<Double>();
	private final int numSamples = 50;

	private double timestamp;
	private double avgReading;
	private double ssdReading;
	
	/**
	 * Instantiate GsrGenerator class with initial GSR readings and calculated averages 
	 * and sum square differences
	 */
	public GsrGenerator() {
		
		initReadings();
		calcAveragesAndSumSquareDifferences();
	}
	
	/**
	 * Initialize GSR readings (to a simple sawtooth pattern)
	 */
	private void initReadings() {

		double reading = 2000.0;
		
		for (int i = 0; i < numSamples; ++i) {
			readings.add(reading);
			
			if (i < 25) {
				reading += 40;
			}
			else {
				reading -= 40;
			}
			
			timestamps.add(getNextTimestamp());
		}
	}

	/**
	 * Calculate reading averages and sum square differences
	 */
	private void calcAveragesAndSumSquareDifferences() {
		
		avgReading = MyMath.getAverageValueD(getReadings());
		ssdReading = MyMath.getStandardDeviationD(readings, avgReading);
	}
	
	public ArrayList<Double> getTimestamps() {
		return timestamps;
	}

	public ArrayList<Double> getReadings() {
		return readings;
	}
	
	public void moveNext() {
		timestamps.clear();
		for (int i = 0; i < 50; ++i) {
			timestamps.add(getNextTimestamp());
		}
		calcAveragesAndSumSquareDifferences();
	}

	private double getNextTimestamp() {
		timestamp += 0.02;
		return timestamp;
	}
	
	public int getNumSamples() {
		return numSamples;
	}

	public double getAvgReadings() {
		return avgReading;
	}
	
	public double getSsdReadings() {
		return ssdReading;
	}
}
