package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

public class EcgGenerator {

	private final ArrayList<Double> timestamps = new ArrayList<Double>();
	private final ArrayList<Double> ecg1Status = new ArrayList<Double>();
	private final ArrayList<Double> ecg1Ch1Readings = new ArrayList<Double>();
	private final ArrayList<Double> ecg1Ch2Readings = new ArrayList<Double>();
	private final ArrayList<Double> ecg2Status = new ArrayList<Double>();
	private final ArrayList<Double> ecg2Ch1Readings = new ArrayList<Double>();
	private final ArrayList<Double> ecg2Ch2Readings = new ArrayList<Double>();
	private final int numSamples = 50;

	private double timestamp;
	private double avgEcg1Status;
	private double ssdEcg1Status;
	private double avgEcg1Ch1Readings;
	private double ssdEcg1Ch1Readings;
	private double avgEcg1Ch2Readings;
	private double ssdEcg1Ch2Readings;
	private double avgEcg2Status;
	private double ssdEcg2Status;
	private double avgEcg2Ch1Readings;
	private double ssdEcg2Ch1Readings;
	private double avgEcg2Ch2Readings;
	private double ssdEcg2Ch2Readings;
	
	/**
	 * Instantiate EcgGenerator class with initial ECG readings and calculated averages 
	 * and sum square differences
	 */
	public EcgGenerator() {
		
		initEcgReadings();
		calcAveragesAndSumSquareDifferences();
	}
	
	/**
	 * Initialize ECG readings (to a simple sawtooth pattern)
	 */
	private void initEcgReadings() {

		double reading = 0.0;
		
		for (int i = 0; i < numSamples; ++i) {
			ecg1Status.add(128.0);
			ecg1Ch1Readings.add(reading);
			ecg1Ch2Readings.add(reading + 1.0);
			ecg2Status.add(128.0);
			ecg2Ch1Readings.add(reading + 2.0);
			ecg2Ch2Readings.add(reading + 3.0);
			
			if (i < 25) {
				reading += .04;
			}
			else {
				reading -= .04;
			}
			
			timestamps.add(getNextTimestamp());
		}
	}

	/**
	 * Calculate reading averages and sum square differences
	 */
	private void calcAveragesAndSumSquareDifferences() {
		
		avgEcg1Status = MyMath.getAverageValueD(getEcg1Status());
		ssdEcg1Status = MyMath.getStandardDeviationD(ecg1Status, avgEcg1Status);
		avgEcg1Ch1Readings = MyMath.getAverageValueD(ecg1Ch1Readings);
		ssdEcg1Ch1Readings = MyMath.getStandardDeviationD(ecg1Ch1Readings, avgEcg1Ch1Readings);
		avgEcg1Ch2Readings = MyMath.getAverageValueD(ecg1Ch2Readings);
		ssdEcg1Ch2Readings = MyMath.getStandardDeviationD(ecg1Ch2Readings, avgEcg1Ch2Readings);
		
		avgEcg2Status = MyMath.getAverageValueD(ecg2Status);
		ssdEcg2Status = MyMath.getStandardDeviationD(ecg2Status, avgEcg2Status);
		avgEcg2Ch1Readings = MyMath.getAverageValueD(ecg2Ch1Readings);
		ssdEcg2Ch1Readings = MyMath.getStandardDeviationD(ecg2Ch1Readings, avgEcg2Ch1Readings);
		avgEcg2Ch2Readings = MyMath.getAverageValueD(ecg2Ch2Readings);
		ssdEcg2Ch2Readings = MyMath.getStandardDeviationD(ecg2Ch2Readings, avgEcg2Ch2Readings);
	}
	
	public ArrayList<Double> getTimestamps() {
		return timestamps;
	}

	public ArrayList<Double> getEcg1Status() {
		return ecg1Status;
	}
	
	public ArrayList<Double> getEcg1Ch1Readings() {
		return ecg1Ch1Readings;
	}

	public ArrayList<Double> getEcg1Ch2Readings() {
		return ecg1Ch2Readings;
	}

	public ArrayList<Double> getEcg2Status() {
		return ecg2Status;
	}
	
	public ArrayList<Double> getEcg2Ch1Readings() {
		return ecg2Ch1Readings;
	}

	public ArrayList<Double> getEcg2Ch2Readings() {
		return ecg2Ch2Readings;
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

	public double getAvgEcg1Status() {
		return avgEcg1Status;
	}
	public double getSsdEcg1Status() {
		return ssdEcg1Status;
	}

	public double getAvgEcg1Ch1Readings() {
		return avgEcg1Ch1Readings;
	}

	public double getSsdEcg1Ch1Readings() {
		return ssdEcg1Ch1Readings;
	}

	public double getAvgEcg1Ch2Readings() {
		return avgEcg1Ch2Readings;
	}

	public double getSsdEcg1Ch2Readings() {
		return ssdEcg1Ch2Readings;
	}
	
	public double getAvgEcg2Status() {
		return avgEcg2Status;
	}
	public double getSsdEcg2Status() {
		return ssdEcg2Status;
	}

	public double getAvgEcg2Ch1Readings() {
		return avgEcg2Ch1Readings;
	}

	public double getSsdEcg2Ch1Readings() {
		return ssdEcg2Ch1Readings;
	}

	public double getAvgEcg2Ch2Readings() {
		return avgEcg2Ch2Readings;
	}

	public double getSsdEcg2Ch2Readings() {
		return ssdEcg2Ch2Readings;
	}

}
