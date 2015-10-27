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
		int numReadings = 0;

		if ((null != readings0) && (numReadings < readings0.size())) {
			numReadings = readings0.size();
		}
		if ((null != readings1) && (numReadings < readings1.size())) {
			numReadings = readings1.size();
		}
		if ((null != readings2) && (numReadings < readings2.size())) {
			numReadings = readings2.size();
		}
		
		long lat = (int) (location.getLatitude() * 1E6);
		long lgt = (int) (location.getLongitude() * 1E6);
		String timestamp = df.format(currentTimeMillis);

		// Write the data to file
		try {
			if (numReadings == 0) {
				row.append(timestamp);
				row.append(COMMA);
				row.append(((double)lat) / 1E6);
				row.append(COMMA);
				row.append(((double)lgt) / 1E6);
				row.append(NEWLINE);
				return;
			}
			else {
				for (int i = 0; i < numReadings; ++i) {
					// Clear previous string output
					row.append(timestamp);
					row.append(COMMA);
					row.append(((double)lat) / 1E6);
					row.append(COMMA);
					row.append(((double)lgt) / 1E6);
					if (null != readings0) {
						row.append(COMMA);
						row.append(readings0.get(i));
						if (null != readings1) {
							row.append(COMMA);
							row.append(readings1.get(i));
							if (null != readings2) {
								row.append(COMMA);
								row.append(readings2.get(i));
							}
						}
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
