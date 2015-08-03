package edu.pdx.cecs.orcyclesensors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class EmailManager {

	private static final String MODULE_TAG = "EmailManager";
	private static String attachmentPath;

	/**
	 * Sets the path to the raw data file's directory.  If
	 * the directory does not exist, it is created.
	 * @param dirPath
	 * @return
	 */
	public static boolean setAttachmentPath(String appName, String dirName) {
		
		attachmentPath = Environment.getExternalStorageDirectory() + File.separator + appName + File.separator + dirName; 
		File dir = new File(attachmentPath);
		if (!dir.exists()) {
			return dir.mkdirs();
		}
		return true;
	}
	
	public static void cleanAttachmentDirectory() {
		File attachmentDir = new File(attachmentPath);
		String absDirPath = attachmentDir.getAbsolutePath();
		String filePath;
		Log.i(MODULE_TAG, absDirPath);

		File attachments[];

		if (null != (attachments = attachmentDir.listFiles())) {
			for (File attachment: attachments) {
				filePath = attachment.getAbsolutePath();
				Log.i(MODULE_TAG, "delete file: " + filePath);
			}
		}
	}
	
	/**
	 * Copy the file from internal storage directory to external storage directory
	 * @param filePath path to file being copied
	 * @return URI to the file in external storage directory
	 */
	public static Uri CreateAttachment(String filePath, String fileName, String outFileExtension) {
		
		File inFile = new File(filePath);
		String outFileName;
		File outFile = null;
		Uri uri = null;
		
		outFileName = attachmentPath + File.separator + fileName;
		
		if (null != outFileExtension) {
			outFileName += outFileExtension;
		}

		try {
            outFile = new File(outFileName);
            outFile.createNewFile();
            copyFile(inFile, outFile);
    		// Convert from paths to Android friendly Parcelable Uri's
            uri = Uri.fromFile(outFile);
        } 
        catch (IOException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
        }
        return uri;
	}

	/**
	 * Copies file from external storage to internal storage
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	private static void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			inChannel = new FileInputStream(src).getChannel();
			outChannel = new FileOutputStream(dst).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

}
