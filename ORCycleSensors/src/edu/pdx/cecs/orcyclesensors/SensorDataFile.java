package edu.pdx.cecs.orcyclesensors;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class SensorDataFile {

	private static final String MODULE_TAG = "RawDataFile";

	private final String fileName;
	private BufferedWriter file;
	private StringBuilder sb = new StringBuilder();
	private final String dataDir;
	private boolean exceptionOccurred = false;
	private boolean firstLine = true;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	
	public SensorDataFile(String name, long tripId, String dataDir) {
		this.dataDir = dataDir;
		this.fileName = name + " " + String.valueOf(tripId);
	}
	
	public void open(Context context) {
		try {
			file = new BufferedWriter(new FileWriter(new File(dataDir, this.fileName)));
			firstLine = true;
		} catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	public void close() {
		try {
			if (null != file) {
				file.close();
			}
		} catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	public void write(long currentTimeMillis, Location location, List<Float> readings0) {
		
		if (exceptionOccurred) {
			return;
		}

		long lat = (int) (location.getLatitude() * 1E6);
		long lgt = (int) (location.getLongitude() * 1E6);

		// Clear previous string output
		sb.setLength(0);
		
		// for each reading
		for (float reading: readings0) {
			if (!firstLine) {
				sb.append("\r\n");
			}
			else {
				firstLine = false;
			}
			sb.append(df.format(currentTimeMillis));
			sb.append(", ");
			sb.append(((double)lat) / 1E6);
			sb.append(", ");
			sb.append(((double)lgt) / 1E6);
			sb.append(", ");
			sb.append(reading);
		}
		
		// If there is data, write it to buffer
		if (sb.length() > 0) {
			try {
				file.write(sb.toString(), 0, sb.length());
			}
			catch(IOException ex) {
				if (!exceptionOccurred) {
					exceptionOccurred = true;
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	}
	
	public void write(long currentTimeMillis, Location location, 
			List<Float> readings0, 
			List<Float> readings1,
			List<Float> readings2) {
		
		if (exceptionOccurred) {
			return;
		}

		long lat = (int) (location.getLatitude() * 1E6);
		long lgt = (int) (location.getLongitude() * 1E6);

		// Clear previous string output
		sb.setLength(0);
		
		// for each reading
		for (int i = 0; i < readings0.size(); ++i) {
			if (!firstLine) {
				sb.append("\r\n");
			}
			else {
				firstLine = false;
			}
			sb.append(df.format(currentTimeMillis));
			sb.append(", ");
			sb.append(((double)lat) / 1E6);
			sb.append(", ");
			sb.append(((double)lgt) / 1E6);
			sb.append(", ");
			sb.append(readings0.get(i));
			sb.append(", ");
			sb.append(readings1.get(i));
			sb.append(", ");
			sb.append(readings2.get(i));
		}
		
		// If there is data, write it to buffer
		if (sb.length() > 0) {
			try {
				file.write(sb.toString(), 0, sb.length());
			}
			catch(IOException ex) {
				if (!exceptionOccurred) {
					exceptionOccurred = true;
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	}
}
