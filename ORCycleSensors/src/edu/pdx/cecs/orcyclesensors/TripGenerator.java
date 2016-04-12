package edu.pdx.cecs.orcyclesensors;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;

public class TripGenerator {

	private static final String MODULE_TAG = "TripGenerator";
	private static final String MOCK_ECG_BLUETOOTH_ADDRESS = "00:00:00:00:00:00";
	private static final String MOCK_GSR_BLUETOOTH_ADDRESS = "00:00:00:00:00:01";
	private static final int NUM_TRIP_POINTS_60_MIN = 3600; // 1 HOUR
	private static final int NUM_TRIP_POINTS_45_MIN = 2700; // 1 HOUR
	private static final int NUM_TRIP_POINTS_30_MIN = 1800; // 1 HOUR
	private static final int NUM_TRIP_POINTS_15_MIN = 900; // 1 HOUR
	private static final int NUM_TRIP_POINTS_1_MIN = 60; // 1 HOUR

	public static void generateFakeTrip(Context context) {

		TripData trip;
		boolean hasSensorData = false;
		boolean hasAntDeviceData = false;
		boolean hasShimmerData = true;
		boolean hasEpocData = false;
		double latitude = 45.508936;		// PSU
		double longitude = -122.681718;	// PSU
		long currentTimeMillis = System.currentTimeMillis();
		
		Location location = new Location("null");;
		location.setTime(currentTimeMillis);
		location.setAccuracy(1.0f);
		location.setAltitude(1.0);
		location.setSpeed(1.0f);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		
		EcgGenerator ecgGen = new EcgGenerator();
		GsrGenerator gsrGen = new GsrGenerator();
		
		MockShimmer ecgShimmer = new MockShimmer(MockShimmer.SensorType.ECG , MOCK_ECG_BLUETOOTH_ADDRESS);
		MockShimmer gsrShimmer = new MockShimmer(MockShimmer.SensorType.GSR , MOCK_GSR_BLUETOOTH_ADDRESS);
		
		ShimmerConfig shimmerEcgConfig = new ShimmerConfig(ecgShimmer, ecgShimmer.getBluetoothAddress(),
				ShimmerVerDetails.HW_ID.SHIMMER_3, true, false);

		ShimmerConfig shimmerGsrConfig = new ShimmerConfig(gsrShimmer, gsrShimmer.getBluetoothAddress(),
				ShimmerVerDetails.HW_ID.SHIMMER_3, true, false);

		try {
			trip = TripData.createTrip(context);
			trip.updateTrip(hasSensorData, hasAntDeviceData, hasShimmerData, hasEpocData);
			trip.updateTrip(shimmerEcgConfig);
			trip.updateTrip(shimmerGsrConfig);
			
			double[] avgEcgVals = new double[3];
			double[] ssdEcgVals = new double[3];
			double[] avgGsrVals = new double[1];
			double[] ssdGsrVals = new double[1];

			for (int i = 0; i < NUM_TRIP_POINTS_60_MIN; ++i) {
				
				// Add point data
				trip.addPointNow(location, (double)currentTimeMillis, 0.00001f);
				
				avgEcgVals[0] = ecgGen.getAvgEcg1Status();
				avgEcgVals[1] = ecgGen.getAvgEcg1Ch1Readings();
				avgEcgVals[2] = ecgGen.getAvgEcg1Ch2Readings();
				
				ssdEcgVals[0] = ecgGen.getSsdEcg1Status();
				ssdEcgVals[1] = ecgGen.getSsdEcg1Ch1Readings();
				ssdEcgVals[2] = ecgGen.getSsdEcg1Ch2Readings();
				
				trip.addShimmerReadings(currentTimeMillis, ecgShimmer.getBluetoothAddress(), ShimmerFormat.SHIMMER3_EXG1_ECG_24,
						ecgGen.getNumSamples(), avgEcgVals, ssdEcgVals);

				avgEcgVals[0] = ecgGen.getAvgEcg2Status();
				avgEcgVals[1] = ecgGen.getAvgEcg2Ch1Readings();
				avgEcgVals[2] = ecgGen.getAvgEcg2Ch2Readings();
				
				ssdEcgVals[0] = ecgGen.getSsdEcg2Status();
				ssdEcgVals[1] = ecgGen.getSsdEcg2Ch1Readings();
				ssdEcgVals[2] = ecgGen.getSsdEcg2Ch2Readings();
				
				trip.addShimmerReadings(currentTimeMillis, ecgShimmer.getBluetoothAddress(), ShimmerFormat.SHIMMER3_EXG2_ECG_24,
						ecgGen.getNumSamples(), avgEcgVals, ssdEcgVals);

				trip.addShimmerReadingsECGData(currentTimeMillis, ecgShimmer.getBluetoothAddress(),
						ecgGen.getTimestamps(), 
						ecgGen.getEcg1Ch1Readings(),
						ecgGen.getEcg1Ch2Readings(), 
						ecgGen.getEcg2Ch1Readings(), 
						ecgGen.getEcg2Ch2Readings());

				ecgGen.moveNext();

				avgGsrVals[0] = gsrGen.getAvgReadings();
				ssdGsrVals[0] = gsrGen.getSsdReadings();
				trip.addShimmerReadings(currentTimeMillis, gsrShimmer.getBluetoothAddress(), ShimmerFormat.SHIMMER3_GSR,
						gsrGen.getNumSamples(), avgGsrVals, ssdGsrVals);


				
				// Set next position and time
				currentTimeMillis += 1000;
				location.setTime(currentTimeMillis);
				location.setLatitude(location.getLatitude() + 0.00001);
				location.setLongitude(location.getLongitude() + 0.00001);
			}
			trip.finish();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
		}
	}
}
