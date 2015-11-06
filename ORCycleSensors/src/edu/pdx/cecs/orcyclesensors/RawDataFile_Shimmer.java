package edu.pdx.cecs.orcyclesensors;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import edu.pdx.cecs.orcyclesensors.ShimmerRecorder.CalcReading;
import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;

public class RawDataFile_Shimmer extends RawDataFile {

	private static final String MODULE_TAG = "RawDataFile_Shimmer";
	
	private static final String COMMA = ",";
	private static final String NEWLINE = "\r\n";
	private static final String NULL_ENTRY = ",0,NULL,NULL";
	private final String[] signalNames;
	private final int[] decimalPlaces;
	private final String header;
	private boolean exceptionOccurred;
	
	@SuppressLint("SimpleDateFormat")
	private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public RawDataFile_Shimmer(String name, long tripId, String dataDir, String[] signalNames, int shimmerVersion) {
		super(name, tripId, dataDir);
		this.signalNames = signalNames;
		this.decimalPlaces = new int[signalNames.length];

		final StringBuilder sbHeader = new StringBuilder();
		sbHeader.append("Time,Latitude,Longitude");
		for (int i = 0; i < signalNames.length; i++) {
			sbHeader.append(COMMA);
			sbHeader.append("Size " + signalNames[i]);
			sbHeader.append(COMMA);
			sbHeader.append("Avg " + signalNames[i]);
			sbHeader.append(COMMA);
			sbHeader.append("STD Dev " + signalNames[i]);
			decimalPlaces[i] = ShimmerFormat.getSignalDecimalPlaces(signalNames[i], shimmerVersion);
		}
		sbHeader.append(NEWLINE);
		sbHeader.append(",,");
		for (int i = 0; i < signalNames.length; i++) {
			sbHeader.append(COMMA);
			sbHeader.append("Units " );
			sbHeader.append(COMMA);
			sbHeader.append(ShimmerFormat.getSignalUnits(signalNames[i], shimmerVersion));
			sbHeader.append(COMMA);
			sbHeader.append(ShimmerFormat.getSignalUnits(signalNames[i], shimmerVersion));
		}
		sbHeader.append(NEWLINE);

		header = sbHeader.toString();
	}
	
	public String getHeader() {
		return header;
	}
	
	public void write(long currentTimeMillis, Location location, HashMap<String, CalcReading> results) {
		
		final StringBuilder row = new StringBuilder();
		CalcReading calcReading;

		if (exceptionOccurred) {
			return;
		}

		try {
			final long lat = (int) (location.getLatitude() * 1E6);
			final long lgt = (int) (location.getLongitude() * 1E6);
	
			// Clear previous string output
			row.append(df.format(currentTimeMillis));
			row.append(COMMA);
			row.append(((double)lat) / 1E6);
			row.append(COMMA);
			row.append(((double)lgt) / 1E6);
	
			for (int i = 0; i < signalNames.length; i++) {
				if (null == (calcReading = results.get(signalNames[i]))) {
					row.append(NULL_ENTRY);
				}
				else {
					row.append(COMMA);
					row.append(calcReading.size);
					row.append(COMMA);
					row.append(calcReading.avg);
					row.append(COMMA);
					row.append(MyMath.rnd(calcReading.std, decimalPlaces[i]));
				}
			}
			row.append(NEWLINE);

			// If there is data, write it to buffer
			if (row.length() > 0) {
				file.write(row.toString(), 0, row.length());
			}
		}
		catch(IOException ex) {
			if (!exceptionOccurred) {
				exceptionOccurred = true;
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}
}
