package edu.pdx.cecs.orcyclesensors;

import java.io.IOException;
import java.util.List;

import android.location.Location;
import android.util.Log;

public class RawDataFile_ScalarSensor extends RawDataFile{

	private static final String MODULE_TAG = "RawDataFile_ScalarSensor";

	public RawDataFile_ScalarSensor(String name, long tripId, String dataDir) {
		super(name, tripId, dataDir);
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
}
