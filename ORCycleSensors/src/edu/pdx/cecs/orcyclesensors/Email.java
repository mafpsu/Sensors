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
	private static final StringBuilder sbText = new StringBuilder();
	private static final String dataFileExtension = ".csv";

	private final String[] addresses = new String[1];
	private final ArrayList<Uri> attachments = new ArrayList<Uri>();

	/**
	 * Instantiates an object containing attachments to be e-mailed to user
	 * 
	 * @param dataFileInfos
	 */
	public Email(String address, ArrayList<DataFileInfo> dataFileInfos) {

		long totalAttachmentSize = 0;
		long fileLength;
		String fileName;
		String filePath;
		Uri uri;

		addresses[0] = new String(address);

		// Generate note text
		sbText.append("Please find attached, the following data files:\n");

		for (DataFileInfo file : dataFileInfos) {

			// Get file info
			fileName = file.getName();
			filePath = file.getPath();
			fileLength = file.getLength();
			totalAttachmentSize += fileLength;

			// Put file name in the message.
			sbText.append(fileName);
			sbText.append(" (");

			// Put file length in the message.
			sbText.append(fileLength);
			sbText.append(" bytes)\n");

			// Create attachment for email application
			if (null != (uri = EmailManager.CreateAttachment(filePath, fileName, dataFileExtension))) {
				attachments.add(uri);
			}
		}

		sbText.append("\nTotal attachment size: ");
		sbText.append(totalAttachmentSize);
		sbText.append(" bytes\n");
	}

	/**
	 * Return e-mail addresses
	 * @return
	 */
	public String[] getAddresses() {
		return addresses;
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
