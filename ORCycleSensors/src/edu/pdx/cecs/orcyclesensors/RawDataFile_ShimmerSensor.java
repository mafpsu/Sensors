package edu.pdx.cecs.orcyclesensors;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

public class RawDataFile_ShimmerSensor extends RawDataFile {

	private static final String MODULE_TAG = "RawDataFile_Shimmer";
	private static final String COMMA = ",";
	private static final String NEWLINE = "\r\n";
	
	private Context context = null;
	private boolean exceptionOccurred;
	private String header = null;
	
	@SuppressLint("SimpleDateFormat")
	protected final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	/**
	 * Constructor
	 * @param name
	 * @param tripId
	 * @param dataDir
	 * @param sensorName
	 */
	public RawDataFile_ShimmerSensor(String name, long tripId, String dataDir, String[] signalNames) {
		super(name, tripId, dataDir);
		
		StringBuilder sbHeader = new StringBuilder();
		sbHeader.append("Time,Latitude,Longitude");
		for (int i = 0; i < signalNames.length; ++i) {
			sbHeader.append(COMMA);
			sbHeader.append(signalNames[i]);
		}
		header = sbHeader.toString();
	}
	
	/**
	 * 
	 */
	public String getHeader() {
		return header;
	}
	
	/**
	 * 
	 * @param currentTimeMillis
	 * @param location
	 * @param readings
	 */
	public void write(long currentTimeMillis, Location location, 
			ArrayList<Double> readings0, ArrayList<Double> readings1, ArrayList<Double> readings2) {
		
		final StringBuilder row = new StringBuilder();

		if (exceptionOccurred) {
			return;
		}
		
		// Note: it is possible to not have an equal number of readings, so
		// in order to include only complete rows of values
		int maxNumReadings = 0;
		int numReadings0 = 0;
		int numReadings1 = 0;
		int numReadings2 = 0;

		if (null != readings0) {
			numReadings0 = readings0.size();
		}
		if (null != readings1) {
			numReadings1 = readings1.size();
		}
		if (null != readings2) {
			numReadings2 = readings2.size();
		}
		if (numReadings0 > maxNumReadings) maxNumReadings = numReadings0;
		if (numReadings1 > maxNumReadings) maxNumReadings = numReadings1;
		if (numReadings2 > maxNumReadings) maxNumReadings = numReadings2;
		
		long lat = (int) (location.getLatitude() * 1E6);
		long lgt = (int) (location.getLongitude() * 1E6);
		String timestamp = df.format(currentTimeMillis);

		// Write the data to file
		try {
			if (maxNumReadings == 0) {
				row.append(timestamp);
				row.append(COMMA);
				row.append(((double)lat) / 1E6);
				row.append(COMMA);
				row.append(((double)lgt) / 1E6);
				row.append(NEWLINE);
				if (null != readings0) {
					row.append(COMMA);
					row.append("null");
				}
				if (null != readings1) {
					row.append(COMMA);
					row.append("null");
				}
				if (null != readings2) {
					row.append(COMMA);
					row.append("null");
				}
				return;
			}
			else {
				for (int i = 0; i < maxNumReadings; ++i) {
					// Clear previous string output
					row.append(timestamp);
					row.append(COMMA);
					row.append(((double)lat) / 1E6);
					row.append(COMMA);
					row.append(((double)lgt) / 1E6);
					if (null != readings0) {
						row.append(COMMA);
						if (i < numReadings0)
							row.append(readings0.get(i));
						else
							row.append("null");
					}
					if (null != readings1) {
						row.append(COMMA);
						if (i < numReadings1)
							row.append(readings1.get(i));
						else
							row.append("null");
					}
					if (null != readings2) {
						row.append(COMMA);
						if (i < numReadings2)
							row.append(readings2.get(i));
						else
							row.append("null");
					}
					row.append(NEWLINE);
				}
			
				// If there is data, write it to buffer
				if (row.length() > 0) {
					file.write(row.toString(), 0, row.length());
				}
			}
		}
		catch(IOException ex) {
			if (!exceptionOccurred) {
				exceptionOccurred = true;
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}
}
