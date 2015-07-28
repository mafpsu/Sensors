package edu.pdx.cecs.orcyclesensors;

import java.io.File;
import java.util.ArrayList;

import android.util.Log;

public class DataFileInfo {

	private static final String MODULE_TAG = "DataFileInfo";
	private static String dir = null;
	private static File dataDir = null;

	private String name;
	private String path;
	private long length;

	public DataFileInfo(String name, String path, long length) {
		this.name = name;
		this.path = path;
		this.length = length;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public long getLength() {
		return length;
	}
	
	public static void deleteFile(DataFileInfo dataFileInfo) {
		File file = new File(dataFileInfo.getPath());
		boolean success = file.delete();
		if (!success) {
			Log.e(MODULE_TAG, "Could not delete file: " + dataFileInfo.getPath());
		}
	}
	
	public static boolean setPath(String dirPath) {
		dataDir = new File(dirPath);
		if (!dataDir.exists()) {
			return dataDir.mkdirs();
		}
		return true;
	}
	
	public static String getDirPath() {
		return dataDir.getAbsolutePath();
	}
	
	public static ArrayList<DataFileInfo> getDataFiles(ArrayList<DataFileInfo> appDataFiles) {
		appDataFiles.clear();
		File dataFiles[] = dataDir.listFiles();
		for (File dataFile: dataFiles) {
			appDataFiles.add(new DataFileInfo(dataFile.getName(), dataFile.getAbsolutePath(), dataFile.length()));
		}
		return appDataFiles;
	}
}
