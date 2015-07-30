/**
 *  ORcycleSensors, Copyright 2015, PSU Transportation, Technology, and People Lab.
 *
 *  @author Robin Murray <robin5@pdx.edu>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires, and features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
 *
 *  ORcycle is free software: you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or any later version.
 *  ORcycle is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with
 *  ORcycle. If not, see <http://www.gnu.org/licenses/>.
 *
 */
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

	private static final String subject = "Bsensor data files";
	private static final String dataFileExtension = ".csv";
	private static final StringBuilder sbText = new StringBuilder();
	private final ArrayList<Uri> attachments = new ArrayList<Uri>();

	/**
	 * Instantiates an object containing attachments to be e-mailed to user
	 * 
	 * @param dataFileInfos
	 */
	public Email(ArrayList<DataFileInfo> dataFileInfos) {

		long totalAttachmentSize = 0;
		long fileLength;
		String fileName;
		String filePath;

		// Generate note text
		sbText.append("Please find attached, the following data files:\n");

		for (DataFileInfo file : dataFileInfos) {
			
			// Get file info
			fileName = file.getName();
			filePath = file.getPath();
			fileLength = file.getLength();
			totalAttachmentSize += fileLength;

			// Put some information about file into message.
			sbText.append(fileName);
			sbText.append(" (");
			sbText.append(fileLength);
			sbText.append(" bytes)\n");

			// Copy the file from internal to external storage 
			// where it can be seen by other applications
			File inFile = new File(filePath);
			File outFile = null;
	        try {
	            outFile = new File(Environment.getExternalStorageDirectory() + File.separator + fileName + dataFileExtension);
	            outFile.createNewFile();
	            copyFile(inFile, outFile);
	    		// Convert from paths to Android friendly Parcelable Uri's
	            Uri uri = Uri.fromFile(outFile);
				attachments.add(uri);
	        } 
	        catch (IOException ex) {
				Log.e(MODULE_TAG, ex.getMessage());
	        }
		}

		sbText.append("\nTotal attachment size: ");
		sbText.append(totalAttachmentSize);
		sbText.append(" bytes\n");
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
		return sbText.toString();
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
