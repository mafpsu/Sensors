package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

public class EcgGenerator {

	private String bluetoothAddress;
	private double timestamp;
	private ArrayList<Double> timestamps = new ArrayList<Double>();
	private ArrayList<Double> ecg1Status = new ArrayList<Double>();
	private ArrayList<Double> ecg1Ch1Readings = new ArrayList<Double>();
	private ArrayList<Double> ecg1Ch2Readings = new ArrayList<Double>();
	private ArrayList<Double> ecg2Status = new ArrayList<Double>();
	private ArrayList<Double> ecg2Ch1Readings = new ArrayList<Double>();
	private ArrayList<Double> ecg2Ch2Readings = new ArrayList<Double>();
	private int numSamples = 50;
	
	public EcgGenerator(String bluetoothAddress) {
		
		double value = 0.0;
		this.bluetoothAddress = bluetoothAddress;

		for (int i = 0; i < numSamples; ++i) {
			ecg1Status.add(128.0);
			ecg1Ch1Readings.add(value);
			ecg1Ch2Readings.add(value + 1.0);
			ecg2Status.add(128.0);
			ecg2Ch1Readings.add(value + 2.0);
			ecg2Ch2Readings.add(value + 3.0);
			
			if (i < 25) {
				value += .04;
			}
			else {
				value -= .04;
			}
			
			timestamps.add(getNextTimestamp());
		}
	}
	
	public String getBluetoothAddress() {
		return bluetoothAddress;
	}

	public ArrayList<Double> getTimestamps() {
		return timestamps;
	}

	public ArrayList<Double> getEcg1Ch1Readings() {
		return ecg1Ch1Readings;
	}

	public ArrayList<Double> getEcg1Ch2Readings() {
		return ecg1Ch2Readings;
	}

	public ArrayList<Double> getEcg2Ch1Readings() {
		return ecg2Ch1Readings;
	}

	public ArrayList<Double> getEcg2Ch2Readings() {
		return ecg2Ch2Readings;
	}
	
	public ArrayList<Double> getEcg1Status() {
		return ecg2Ch2Readings;
	}
	
	public ArrayList<Double> getEcg2Status() {
		return ecg2Ch2Readings;
	}
	
	public void moveNext() {
		timestamps.clear();
		for (int i = 0; i < 50; ++i) {
			timestamps.add(getNextTimestamp());
		}
	}

	private double getNextTimestamp() {
		timestamp += 0.02;
		return timestamp;
	}
	
	public int getNumSamples() {
		return numSamples;
	}

}
