package edu.pdx.cecs.orcyclesensors;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class SensorDataFile {

	private static final String MODULE_TAG = "RawDataFile";

	private String fileName;
	private BufferedWriter file;
	private StringBuilder sb = new StringBuilder();
	private boolean exceptionOccurred = false;
	
	public SensorDataFile(String name, long tripId) {
		this.fileName = name + String.valueOf(tripId);
	}
	
	public void open(Context context) {
		try {
			file = new BufferedWriter(new FileWriter(new File(context.getFilesDir(), this.fileName)));
			String tmp = context.getFilesDir().toString();
			Log.i(MODULE_TAG, "file name: " + this.fileName);
		} catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	public void close() {
		try {
			file.close();
		} catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	public void write(List<Float> readings0) {
		
		if (exceptionOccurred) {
			return;
		}

		// Clear previous string output
		sb.setLength(0);
		
		// for each reading
		for (float reading: readings0) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
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
	
	public void write(List<Float> readings0, 
			List<Float> readings1,
			List<Float> readings2) {
		
		if (exceptionOccurred) {
			return;
		}

		// Clear previous string output
		sb.setLength(0);
		
		// for each reading
		for (int i = 0; i < readings0.size(); ++i) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
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
