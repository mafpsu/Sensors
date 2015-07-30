package edu.pdx.cecs.orcyclesensors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Email {

	private static final String MODULE_TAG = "Email";

	private final String subject;
	private final String text;
	private final ArrayList<Uri> attachments = new ArrayList<Uri>();

	/**
	 * Instantiates an object containing attachments to be e-mailed to user
	 * 
	 * @param dataFileInfos
	 */
	public Email(ArrayList<DataFileInfo> dataFileInfos) {

		StringBuilder sbText = new StringBuilder();
		long totalAttachmentSize = 0;
		long fileLength;
		String fileName;
		String filePath;

		subject = "Bsensor data files";

		// Generate note text
		sbText.append("Please find attached, the following data files:\n");

		// convert from paths to Android friendly Parcelable Uri's
		for (DataFileInfo file : dataFileInfos) {
			fileName = file.getName();
			filePath = file.getPath();
			fileLength = file.getLength();
			totalAttachmentSize += fileLength;

			sbText.append(fileName);
			sbText.append(" (");
			sbText.append(fileLength);
			sbText.append(" bytes)\n");

			//File fileIn = new File(filePath);
			//fileIn.setReadable(true, false);
			//Uri u = Uri.fromFile(fileIn);
			//attachments.add(u);
			
			File inFile = new File(filePath);
			File outFile = null;
	        try {
	            outFile = new File(Environment.getExternalStorageDirectory() + File.separator + fileName + ".txt");
	            outFile.createNewFile();
	            copyFile(inFile, outFile);
	            Uri u = Uri.fromFile(outFile);
				attachments.add(u);
	        } catch (IOException ex) {
				Log.e(MODULE_TAG, ex.getMessage());
	        }

			
			
			
		}

		sbText.append("\nTotal attachment size: ");
		sbText.append(totalAttachmentSize);
		sbText.append(" bytes\n");

		text = sbText.toString();
	}

	/**
	 * Copies file from external storage to internal storage
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	void copyFile(File src, File dst) throws IOException {
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

	/**
	 * Return e-mail subject line
	 * 
	 * @return
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Return e-mail text
	 * 
	 * @return
	 */
	public String getText() {
		return text.toString();
	}

	/**
	 * Returns the list of e-mail attachments in URI form
	 * 
	 * @return
	 */
	public ArrayList<Uri> getAttachments() {
		return attachments;
	}
}
