package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer3;

public class ShimmerSignalGroup {
	
	private static String[] sh2Accelerometer = { 
			Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_X,
			Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Y,
			Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Z };
	
	private static String[] sh2Gyroscope = {
			Configuration.Shimmer2.ObjectClusterSensorName.GYRO_X,
			Configuration.Shimmer2.ObjectClusterSensorName.GYRO_Y,
			Configuration.Shimmer2.ObjectClusterSensorName.GYRO_Z };
	
	private static String[] sh2Magnetometer = {
			Configuration.Shimmer2.ObjectClusterSensorName.MAG_X,
			Configuration.Shimmer2.ObjectClusterSensorName.MAG_Y,
			Configuration.Shimmer2.ObjectClusterSensorName.MAG_Z };
	
	private static String[] sh2GSR = {
			Configuration.Shimmer2.ObjectClusterSensorName.GSR };
	
	private static String[] sh2ECG = {
			Configuration.Shimmer2.ObjectClusterSensorName.ECG_RA_LL,
			Configuration.Shimmer2.ObjectClusterSensorName.ECG_LA_LL };
	
	private static String[] sh2EMG = {
			Configuration.Shimmer2.ObjectClusterSensorName.EMG };
	
	private static String[] sh2BridgeAmplifier = {
			Configuration.Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,
			Configuration.Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW };
	
	private static String[] sh2HeartRate = {
			Configuration.Shimmer2.ObjectClusterSensorName.HEART_RATE };

	private static String[] sh2ExpBoardA0 = {
			Configuration.Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0 };

	private static String[] sh2ExpBoardA7 = {
			Configuration.Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7 };

	private static String[] sh2BatteryVoltage = {
			Configuration.Shimmer2.ObjectClusterSensorName.BATTERY,
			Configuration.Shimmer2.ObjectClusterSensorName.REG };

	private static String[] sh3LowNoiseAccelerometer = {
			Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
			Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
			Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z };

	private static String[] sh3WideRangeAccelerometer = {
			Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
			Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
			Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z };

	private static String[] sh3Gyroscope = {
			Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
			Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
			Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z };

	private static String[] sh3Magnetometer = {
			Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
			Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
			Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z };

	private static String[] sh3BatteryVoltage = {
			Shimmer3.ObjectClusterSensorName.BATTERY };

	private static String[] sh3ExtExpA15 = {
			Shimmer3.ObjectClusterSensorName.EXT_EXP_A15 };

	private static String[] sh3ExtExpA7 = {
			Shimmer3.ObjectClusterSensorName.EXT_EXP_A7 };

	private static String[] sh3ExtExpA6 = {
			Shimmer3.ObjectClusterSensorName.EXT_EXP_A6 };

	private static String[] sh3IntExpA1 = {
			Shimmer3.ObjectClusterSensorName.INT_EXP_A1 };

	private static String[] sh3IntExpA12 = {
			Shimmer3.ObjectClusterSensorName.INT_EXP_A12 };

	private static String[] sh3IntExpA13 = {
			Shimmer3.ObjectClusterSensorName.INT_EXP_A13 };

	private static String[] sh3IntExpA14 = {
			Shimmer3.ObjectClusterSensorName.INT_EXP_A14 };

	private static String[] sh3Pressure = {
			Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180, 
			Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180 };

	private static String[] sh3GSR = {
			Configuration.Shimmer3.ObjectClusterSensorName.GSR };

	private static String[] sh3DefaultECG1_24 = {
			Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
			Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
			Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT };

	private static String[] sh3DefaultEMG1_24 = {
			Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
			Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
			Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT };

	private static String[] sh3EXG1_24 = {
			Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
			Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT };

	private static String[] sh3DefaultECG2_24 = {
			Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
			Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT };

	private static String[] sh3DefaultEMG2_24 = {
			Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
			Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT };

	private static String[] sh3EMG2_24 = {
			Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
			Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT };
	
	private static String[] sh3DefaultECG1_16 = {
			Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
			Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
			Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT };

	private static String[] sh3DefaultEMG1_16 = {
			Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
			Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
			Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT };

	private static String[] sh3EXG1_16 = {
			Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
			Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT };

	private static String[] sh3DefaultECG2_16 = {
			Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
			Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT };

	private static String[] sh3DefaultEMG2_16 = {
			Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
			Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT };

	private static String[] sh3EXG2_16 = {
			Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
			Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
			Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT };

	private static String[] sh3BridgeAmplifier = {
			Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH, 
			Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW };

	public static String[] get(String sensorName, int shimmerVersion, 
			boolean isEXGUsingDefaultECGConfiguration, boolean isEXGUsingDefaultEMGConfiguration) {
		
		// --------------------------------------------------------------------
		//                         Shimmer 2 & 2R
		// --------------------------------------------------------------------
		if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R) {

			if (sensorName.equals("Accelerometer")){
				return sh2Accelerometer;
			}
			else if (sensorName.equals("Gyroscope")){
				return sh2Gyroscope;
			}
			else if (sensorName.equals("Magnetometer")){
				return sh2Magnetometer;
			}
			else if (sensorName.equals("GSR")){
				return sh2GSR;
			}
			else if (sensorName.equals("ECG")){
				return sh2ECG;
			}
			else if (sensorName.equals("EMG")){
				return sh2EMG;
			}
			else if (sensorName.equals("Bridge Amplifier")){
				return sh2BridgeAmplifier;
			}
			else if (sensorName.equals("Heart Rate")){
				return sh2HeartRate;
			}
			else if (sensorName.equals("Exp Board A0")){ // TODO: Verify Exp Board A0
				return sh2ExpBoardA0;
			}
			else if (sensorName.equals("Exp Board A7")){ // TODO: Verify Exp Board A7
				return sh2ExpBoardA7;
			}
			else if (sensorName.equals("Battery Voltage")){
				return sh2BatteryVoltage;
			}
		}
		// --------------------------------------------------------------------
		//                              Shimmer 3
		// --------------------------------------------------------------------
		
		else if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3) { 
			if (sensorName.equals("Low Noise Accelerometer")){
				return sh3LowNoiseAccelerometer;
			}
			else if (sensorName.equals("Wide Range Accelerometer")){
				return sh3WideRangeAccelerometer;
			}
			else if (sensorName.equals("Gyroscope")){
				return sh3Gyroscope;
			}
			else if (sensorName.equals("Magnetometer")){
				return sh3Magnetometer;
			}
			else if (sensorName.equals("Battery Voltage")){
				return sh3BatteryVoltage;
			}
			else if (sensorName.equals("External ADC A15")){
				return sh3ExtExpA15;
			}
			else if (sensorName.equals("External ADC A7")){
				return sh3ExtExpA7;
			}
			else if (sensorName.equals("External ADC A6")){
				return sh3ExtExpA6;
			}
			else if (sensorName.equals("Internal ADC A1")){
				return sh3IntExpA1;
			}
			else if (sensorName.equals("Internal ADC A12")){
				return sh3IntExpA12;
			}
			else if (sensorName.equals("Internal ADC A13")){
				return sh3IntExpA13;
			}
			else if (sensorName.equals("Internal ADC A14")){
				return sh3IntExpA14;
			}
			else if (sensorName.equals("Pressure")){ // TODO: Verify Pressure
				return sh3Pressure;
			}
			else if (sensorName.equals("GSR")){
				return sh3GSR;
			}
			else if (sensorName.equals("EXG1")){
				if (isEXGUsingDefaultECGConfiguration){
					return sh3DefaultECG1_24;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return sh3DefaultEMG1_24;
				} else {
					return sh3EXG1_24;
				}
			}
			else if (sensorName.equals("EXG2")){
				if (isEXGUsingDefaultECGConfiguration){
					return sh3DefaultECG2_24;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return sh3DefaultEMG2_24;
				} else {
					return sh3EMG2_24;
				}
			}
			else if (sensorName.equals("EXG1 16Bit")){
				if (isEXGUsingDefaultECGConfiguration){
					return sh3DefaultECG1_16;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return sh3DefaultEMG1_16;
				} else {
					return sh3EXG1_16;
				}
			}
			else if (sensorName.equals("EXG2 16 Bit")){
				if (isEXGUsingDefaultECGConfiguration){
					return sh3DefaultECG2_16;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return sh3DefaultEMG2_16;
				} else {
					return sh3EXG2_16;
				}
			}
			else if (sensorName.equals("Bridge Amplifier")){
				return sh3BridgeAmplifier;
			}
		}
		return null;
	}
}
