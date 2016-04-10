package edu.pdx.cecs.orcyclesensors;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;

public class TripGenerator {

	private static final String MODULE_TAG = "TripGenerator";
	private static final String MOCK_ECG_BLUETOOTH_ADDRESS = "00:00:00:00:00:00";
	private static final String MOCK_GSR_BLUETOOTH_ADDRESS = "00:00:00:00:00:01";

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
		
		EcgGenerator gen = new EcgGenerator();
		MockShimmer ecgShimmer = new MockShimmer(MockShimmer.SensorType.ECG , MOCK_ECG_BLUETOOTH_ADDRESS);
		MockShimmer gsrShimmer = new MockShimmer(MockShimmer.SensorType.GSR , MOCK_GSR_BLUETOOTH_ADDRESS);
		
		ShimmerConfig shimmerConfig = new ShimmerConfig(ecgShimmer, ecgShimmer.getBluetoothAddress(),
				ShimmerVerDetails.HW_ID.SHIMMER_3, true, false);

		try {
			trip = TripData.createTrip(context);
			trip.updateTrip(hasSensorData, hasAntDeviceData, hasShimmerData, hasEpocData);
			trip.updateTrip(shimmerConfig);
			
			double[] avgVals = new double[3];
			double[] ssdVals = new double[3];
			int numVals = gen.getNumSamples();


			for (int i = 0; i < 3600; ++i) {
				
				// Add point data
				trip.addPointNow(location, (double)currentTimeMillis, 0.00001f);
				
				avgVals[0] = gen.getAvgEcg1Status();
				avgVals[1] = gen.getAvgEcg1Ch1Readings();
				avgVals[2] = gen.getAvgEcg1Ch2Readings();
				
				ssdVals[0] = gen.getSsdEcg1Status();
				ssdVals[1] = gen.getSsdEcg1Ch1Readings();
				ssdVals[2] = gen.getSsdEcg1Ch2Readings();
				
				trip.addShimmerReadings(currentTimeMillis, ecgShimmer.getBluetoothAddress(), ShimmerFormat.SHIMMER3_EXG1_ECG_24,
						numVals, avgVals, ssdVals);

				avgVals[0] = gen.getAvgEcg2Status();
				avgVals[1] = gen.getAvgEcg2Ch1Readings();
				avgVals[2] = gen.getAvgEcg2Ch2Readings();
				
				ssdVals[0] = gen.getSsdEcg2Status();
				ssdVals[1] = gen.getSsdEcg2Ch1Readings();
				ssdVals[2] = gen.getSsdEcg2Ch2Readings();
				
				trip.addShimmerReadings(currentTimeMillis, ecgShimmer.getBluetoothAddress(), ShimmerFormat.SHIMMER3_EXG2_ECG_24,
						numVals, avgVals, ssdVals);

				trip.addShimmerReadingsECGData(currentTimeMillis, ecgShimmer.getBluetoothAddress(),
						gen.getTimestamps(), 
						gen.getEcg1Ch1Readings(),
						gen.getEcg1Ch2Readings(), 
						gen.getEcg2Ch1Readings(), 
						gen.getEcg2Ch2Readings());

				gen.moveNext();

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
