package edu.pdx.cecs.orcyclesensors;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

public class ZipFileManager {

	private static final String MODULE_TAG = "ZipFileManager";
	private static final int OUTPUT_BUFFER_SIZE = 8192;
	private static File dataDir = null;
	private static String zipFileName = null;
	
	/**
	 * Sets the path to the raw data file's directory.  If
	 * the directory does not exist, it is created.
	 * @param dirPath
	 * @return
	 */
	public static boolean setDirPath(String appPath, String dataDirName) {
		boolean dataDirExists;
		String dirPath = appPath + File.separator + dataDirName;
		dataDir = new File(dirPath);
		if (!(dataDirExists = dataDir.exists())) {
			dataDirExists = dataDir.mkdirs();
		}
		
		if (dataDirExists) {
			zipFileName = dirPath + File.separator + "upload.zip";
		}
		else {
			zipFileName = null;
		}
		return dataDirExists;
	}

	/**
	 * returns the filename of the temporary zip file
	 */
	public static String getZipFileName() {
		return zipFileName;
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
	public static ArrayList<DataFileInfo> getZipFiles() {
		
		final ArrayList<DataFileInfo> zipFiles = new ArrayList<DataFileInfo>();
		File dataFiles[];

		if (null != (dataFiles = dataDir.listFiles())) {
			for (File dataFile: dataFiles) {
				zipFiles.add(new DataFileInfo(dataFile.getName(), dataFile.getAbsolutePath(), dataFile.length()));
			}
		}
		return zipFiles;
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
	
	public static byte[] compress_v2(StringBuilder string) throws IOException {
		
		char[] buff = new char[OUTPUT_BUFFER_SIZE];
		byte[] compressedData = null;
		
		int start = 0;
		int end = string.length();
		int bytesLeft = end - start;
		int bytesToWrite;
		int startLength = end;
		File gzipFile;
		
		// This is the case of not being able to create the zip file directory
		if (null == zipFileName) {
			return null;
		}

		BufferedWriter writer = null;
		long totalBytesWritten = 0;
		
		try {
			gzipFile = new File(zipFileName);
			if (gzipFile.exists()) {
				gzipFile.delete();
			}
			GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(gzipFile));

			writer = new BufferedWriter(new OutputStreamWriter(zip));

			while (bytesLeft > 0) {
				bytesToWrite = bytesLeft > OUTPUT_BUFFER_SIZE ? OUTPUT_BUFFER_SIZE : bytesLeft;
				
				string.getChars(start, start + bytesToWrite, buff, 0);
				writer.write(buff, 0, bytesToWrite);
				
				totalBytesWritten += bytesToWrite;
				
				start += bytesToWrite;
				bytesLeft = end - start;
			}
			writer.close();
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return null;
	    } finally {         
	        if (writer != null)
	            writer.close();
	    }
		
		
		if (startLength != totalBytesWritten) {
			Log.e(MODULE_TAG, "Bytes not correct");
		}
		
		gzipFile = new File(zipFileName);
		if (gzipFile.length() > Integer.MAX_VALUE) {
			return null;
		}

		int size = (int) gzipFile.length();
		compressedData = new byte[size];
		FileInputStream zipIn = new FileInputStream(gzipFile);
		
		try {
			zipIn.read(compressedData);
			return compressedData;
		} catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			return null;
		}
		finally {
			if (null != zipIn)
				zipIn.close();
		}
	}

	public static byte[] compress_v1(String string) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
		GZIPOutputStream gos = new GZIPOutputStream(os);
		gos.write(string.getBytes());
		gos.close();
		byte[] compressed = os.toByteArray();
		os.close();
		return compressed;
	}

	public boolean isEqual(byte[] postBodyDataZipped1, byte[] postBodyDataZipped2) {
		
		int length1 = postBodyDataZipped1.length;
		int length2 = postBodyDataZipped2.length;
		if (length1 == length2) {
			for (int i = 0; i < length1; ++i) {
				if (postBodyDataZipped1[i] != postBodyDataZipped2[i]) {
					Log.e(MODULE_TAG, "Zipped bytes are *NOT* identical");
					return false;
				}
			}
			Log.e(MODULE_TAG, "Zipped bytes are identical");
			return true;
		}
		else {
			Log.e(MODULE_TAG, "Zipped bytes are *NOT* identical");
			return false;
		}
	}
}
