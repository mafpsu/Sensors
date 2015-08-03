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

public class RawDataFile {

	private static final String MODULE_TAG = "RawDataFile";

	private final String fileName;
	private final String dataDir;

	protected BufferedWriter file;
	protected final StringBuilder sb = new StringBuilder();
	protected boolean exceptionOccurred = false;
	protected boolean firstLine = true;
	protected final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * Constructor for the raw data file
	 * @param name The name of the file
	 * @param tripId The trip number being recorded (which is appended to file name)
	 * @param dataDir The data directory where raw data files are placed
	 */
	public RawDataFile(String name, long tripId, String dataDir) {
		this.dataDir = dataDir;
		this.fileName = name + " " + String.valueOf(tripId);
	}
	
	/**
	 * Opens this file and sets firstLine to true
	 */
	public void open(Context context) {
		try {
			file = new BufferedWriter(new FileWriter(new File(dataDir, this.fileName)));
			firstLine = true;
		} catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	/**
	 * Closes this file
	 */
	public void close() {
		try {
			if (null != file) {
				file.close();
			}
		} catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
}
