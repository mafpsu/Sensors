package edu.pdx.cecs.orcyclesensors;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import android.location.Location;
import android.util.Log;

public class RawDataFile_BikePower extends RawDataFile {

	private static final String MODULE_TAG = "RawDataFile_BikePower";

	public RawDataFile_BikePower(String name, long tripId, String dataDir) {
		super(name, tripId, dataDir);
	}

	public void write(long currentTimeMillis, Location location, 
		List<BigDecimal> calculatedPower, 
		List<BigDecimal> calculatedTorque, 
		List<BigDecimal> calculatedCrankCadence, 
		List<BigDecimal> calculatedWheelSpeed, 
		List<BigDecimal> calculatedWheelDistance ) {
	
		if (exceptionOccurred) {
			return;
		}
		
		int calculatedPowerSize = calculatedPower.size();
		int calculatedTorqueSize = calculatedPower.size();
		int calculatedCrankCadenceSize = calculatedPower.size();
		int calculatedWheelSpeedSize = calculatedPower.size();
		int calculatedWheelDistanceSize = calculatedPower.size();
		
		int maxReadings = calculatedPowerSize;
		if (calculatedTorqueSize > maxReadings) maxReadings = calculatedTorqueSize;
		if (calculatedCrankCadenceSize > maxReadings) maxReadings = calculatedCrankCadenceSize;
		if (calculatedWheelSpeedSize > maxReadings) maxReadings = calculatedWheelSpeedSize;
		if (calculatedWheelDistanceSize > maxReadings) maxReadings = calculatedWheelDistanceSize;

		long lat = (int) (location.getLatitude() * 1E6);
		long lgt = (int) (location.getLongitude() * 1E6);
	
		// Clear previous string output
		sb.setLength(0);
		
		// for each reading
		for (int i = 0; i < maxReadings; ++i) {
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

			// Calculated Power
			if (i < calculatedPowerSize)
				sb.append(calculatedPower.get(i));
			else
				sb.append("null");
			sb.append(", ");
			
			// Calculated Torque
			if (i < calculatedTorqueSize)
				sb.append(calculatedTorque.get(i));
			else
				sb.append("null");
			sb.append(", ");
			
			// Calculated Crank Cadence
			if (i < calculatedCrankCadenceSize)
				sb.append(calculatedCrankCadence.get(i));
			else
				sb.append("null");
			sb.append(", ");
			
			// Calculated Wheel Speed
			if (i < calculatedWheelSpeedSize)
				sb.append(calculatedWheelSpeed.get(i));
			else
				sb.append("null");
			sb.append(", ");
			
			// Calculated Wheel Distance
			if (i < calculatedWheelDistanceSize)
				sb.append(calculatedWheelDistance.get(i));
			else
				sb.append("null");
			sb.append(", ");
			
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
