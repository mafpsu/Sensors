package edu.pdx.cecs.orcyclesensors;

import java.util.HashMap;

import edu.pdx.cecs.orcyclesensors.ShimmerRecorder.CalcReading;
import android.location.Location;

public class RawDataFile_Epoc extends RawDataFile {

	private static final String eeg_header = "COUNTER_MEMS,GYROX,GYROY,GYROZ,ACCX,ACCY,ACCZ,MAGX,MAGY,MAGZ,TimeStamp";
	
	public RawDataFile_Epoc(String fileName, long tripId, String dataDir) {
		super(fileName, tripId, dataDir);
		// TODO Auto-generated constructor stub
	}

	@Override
	String getHeader() {
		return eeg_header;
	}

	public void write(long currentTimeMillis, Location location, HashMap<String, CalcReading> eegReadings) {
		
		if (exceptionOccurred) {
			return;
		}
	}
}
