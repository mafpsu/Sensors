package edu.pdx.cecs.orcyclesensors;

import java.io.File;
import java.util.ArrayList;
import android.util.Log;

public class DataFileInfoManager {

	private static final String MODULE_TAG = "DataFileInfoManager";
	private static File dataDir = null;
	
	/**
	 * Sets the path to the raw data file's directory.  If
	 * the directory does not exist, it is created.
	 * @param dirPath
	 * @return
	 */
	public static boolean setDirPath(String appPath, String dataDirName) {
		String dirPath = appPath + File.separator + dataDirName;
		dataDir = new File(dirPath);
		if (!dataDir.exists()) {
			return dataDir.mkdirs();
		}
		return true;
	}
	
	/**
	 * Returns the absolute path to the raw data's directory
	 * @return
	 */
	public static String getDirPath() {
		return dataDir.getAbsolutePath();
	}
	
	/**
	 * Returns a listing of files in the data directory
	 * @return
	 */
	public static ArrayList<DataFileInfo> getAppDataFiles() {
		
		final ArrayList<DataFileInfo> appDataFiles = new ArrayList<DataFileInfo>();
		File dataFiles[];

		if (null != (dataFiles = dataDir.listFiles())) {
			for (File dataFile: dataFiles) {
				appDataFiles.add(new DataFileInfo(dataFile.getName(), dataFile.getAbsolutePath(), dataFile.length()));
			}
		}
		return appDataFiles;
	}

	/**
	 * Deletes each of the files in the array specified by dataFileInfos
	 * @param dataFileInfos the list of files to delete
	 */
	public static void deleteAppDataFiles(ArrayList<DataFileInfo> dataFileInfos) {
		for (DataFileInfo dataFileInfo : dataFileInfos) {
			deleteFile(dataFileInfo);
		}
	}

	/**
	 * Deletes the files specified by dataFileInfo
	 * @param dataFileInfo file to delete
	 */
	public static void deleteFile(DataFileInfo dataFileInfo) {
		File file = new File(dataFileInfo.getPath());
		if (file.exists()) {
			boolean success = file.delete();
			if (!success) {
				Log.e(MODULE_TAG, "Could not delete file: " + dataFileInfo.getPath());
			}
		}
	}
	
}
