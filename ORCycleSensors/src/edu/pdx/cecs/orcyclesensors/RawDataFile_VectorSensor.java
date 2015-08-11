package edu.pdx.cecs.orcyclesensors;

import java.io.IOException;
import java.util.List;
import android.location.Location;
import android.util.Log;

public class RawDataFile_VectorSensor extends RawDataFile {

	private static final String MODULE_TAG = "RawDataFile_VectorSensor";

	private static final String FILE_HEADER = "Time,Latitude,Longitude,X,Y,Z";

	public RawDataFile_VectorSensor(String name, long tripId, String dataDir) {
		super(name, tripId, dataDir);
	}

	public String getHeader() {
		return FILE_HEADER;
	}
	
	public void write(long currentTimeMillis, Location location, 
			List<Float> readings0, 
			List<Float> readings1,
			List<Float> readings2) {
		
		if (exceptionOccurred) {
			return;
		}

		long lat = (int) (location.getLatitude() * 1E6);
		long lgt = (int) (location.getLongitude() * 1E6);

		// Clear previous string output
		sb.setLength(0);
		
		// for each reading
		for (int i = 0; i < readings0.size(); ++i) {
			sb.append(df.format(currentTimeMillis));
			sb.append(", ");
			sb.append(((double)lat) / 1E6);
			sb.append(", ");
			sb.append(((double)lgt) / 1E6);
			sb.append(", ");
			sb.append(readings0.get(i));
			sb.append(", ");
			sb.append(readings1.get(i));
			sb.append(", ");
			sb.append(readings2.get(i));
			sb.append("\r\n");
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
