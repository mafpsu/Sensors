package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer3;

public class ShimmerFormat {

	private static final String UNITS_MPS2 = "m/(sec^2)";
	private static final String UNITS_DPS = "deg/sec";
	private static final String UNITS_LOCAL = "local";
	private static final String UNITS_KOHMS = "kOhms";
	private static final String UNITS_MVOLTS = "mVolts";
	private static final String UNITS_BPM = "BPM";
	private static final String UNITS_KPA = "kPa";
	private static final String UNITS_CELSIUS = "Celsius";
	private static final String UNITS_NO_UNITS = "No Units";
	
	public static final int SHIMMER2_ACCELEROMETER = 1;
	public static final int SHIMMER2_GYROSCOPE = 2;
	public static final int SHIMMER2_MAGNETOMETER = 3;
	public static final int SHIMMER2_GSR = 4;
	public static final int SHIMMER2_ECG = 5;
	public static final int SHIMMER2_EMG = 6;
	public static final int SHIMMER2_BRIDGE_AMPLIFIER = 7;
	public static final int SHIMMER2_HEART_RATE = 8;
	public static final int SHIMMER2_EXP_BOARD_A0 = 9;
	public static final int SHIMMER2_EXP_BOARD_A7 = 10;
	public static final int SHIMMER2_BATTERY = 11;

	public static final int SHIMMER3_LOW_NOISE_ACCELEROMETER = 20;
	public static final int SHIMMER3_WIDE_RANGE_ACCELEROMETER = 21;
	public static final int SHIMMER3_GYROSCOPE = 22;
	public static final int SHIMMER3_MAGNETOMETER = 23;
	public static final int SHIMMER3_BATTERY = 24;
	public static final int SHIMMER3_EXT_ADC_A6 = 25;
	public static final int SHIMMER3_EXT_ADC_A7 = 26;
	public static final int SHIMMER3_EXT_ADC_A15 = 27;
	public static final int SHIMMER3_INT_ADC_1 = 28;
	public static final int SHIMMER3_INT_ADC_12 = 29;
	public static final int SHIMMER3_INT_ADC_13 = 30;
	public static final int SHIMMER3_INT_ADC_14 = 31;
	public static final int SHIMMER3_PRESSURE = 32;
	public static final int SHIMMER3_GSR = 33;
	public static final int SHIMMER3_EXG1_24 = 34;
	public static final int SHIMMER3_EXG1_ECG_24 = 35;
	public static final int SHIMMER3_EXG1_EMG_24 = 36;
	public static final int SHIMMER3_EXG2_24 = 37;
	public static final int SHIMMER3_EXG2_ECG_24 = 38;
	public static final int SHIMMER3_EXG2_EMG_24 = 39;
	public static final int SHIMMER3_EXG1_16 = 40;
	public static final int SHIMMER3_EXG1_ECG_16 = 41;
	public static final int SHIMMER3_EXG1_EMG_16 = 42;
	public static final int SHIMMER3_EXG2_16 = 43;
	public static final int SHIMMER3_EXG2_ECG_16 = 44;
	public static final int SHIMMER3_EXG2_EMG_16 = 45;
	public static final int SHIMMER3_BRIDGE_AMPLIFIER = 46;

	public static String getSignalUnits(String signalName, int shimmerVersion) {

		if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R) {

			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_X)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Y)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Z)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.GYRO_X)) return UNITS_DPS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.GYRO_Y)) return UNITS_DPS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.GYRO_Z)) return UNITS_DPS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.MAG_X)) return UNITS_LOCAL;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.MAG_Y)) return UNITS_LOCAL;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.MAG_Z)) return UNITS_LOCAL;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.GSR)) return UNITS_KOHMS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.ECG_RA_LL)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.ECG_LA_LL)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.EMG)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.HEART_RATE)) return UNITS_BPM;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.BATTERY)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer2.ObjectClusterSensorName.REG)) return UNITS_MVOLTS;
		}	// Shimmer 3
		else if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3) { 

			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z)) return UNITS_MPS2;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X)) return UNITS_DPS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y)) return UNITS_DPS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z)) return UNITS_DPS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.MAG_X)) return UNITS_LOCAL;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y)) return UNITS_LOCAL;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z)) return UNITS_LOCAL;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.BATTERY)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXT_EXP_A15)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXT_EXP_A7)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXT_EXP_A6)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A1)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A12)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A13)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.INT_EXP_A14)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180)) return UNITS_KPA; 
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180)) return UNITS_CELSIUS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.GSR)) return UNITS_KOHMS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_STATUS)) return UNITS_NO_UNITS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_STATUS)) return UNITS_NO_UNITS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT)) return UNITS_MVOLTS;
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH)) return UNITS_MVOLTS; 
			if (signalName.equals(Configuration.Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW)) return UNITS_MVOLTS;
		}
	return "Undefined";
	}

	public static int getSensorType(String sensorName, int shimmerVersion, 
			boolean isEXGUsingDefaultECGConfiguration,
			boolean isEXGUsingDefaultEMGConfiguration) throws Exception {
		
		// --------------------------------------------------------------------
		//                         Shimmer 2 & 2R
		// --------------------------------------------------------------------
		
		if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || 
				shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R) {

			if (sensorName.equals("Accelerometer")){
				return SHIMMER2_ACCELEROMETER;
			}
			else if (sensorName.equals("Gyroscope")){
				return SHIMMER2_GYROSCOPE;
			}
			else if (sensorName.equals("Magnetometer")){
				return SHIMMER2_MAGNETOMETER;
			}
			else if (sensorName.equals("GSR")){
				return SHIMMER2_GSR;
			}
			else if (sensorName.equals("ECG")){
				return SHIMMER2_ECG;
			}
			else if (sensorName.equals("EMG")){
				return SHIMMER2_EMG;
			}
			else if (sensorName.equals("Bridge Amplifier")){
				return SHIMMER2_BRIDGE_AMPLIFIER;
			}
			else if (sensorName.equals("Heart Rate")){
				return SHIMMER2_HEART_RATE;
			}
			else if (sensorName.equals("Exp Board A0")){ // TODO: Verify Exp Board A0
				return SHIMMER2_EXP_BOARD_A0;
			}
			else if (sensorName.equals("Exp Board A7")){ // TODO: Verify Exp Board A7
				return SHIMMER2_EXP_BOARD_A7;
			}
			else if (sensorName.equals("Battery Voltage")){
				return SHIMMER2_BATTERY;
			}
		}
		
		// --------------------------------------------------------------------
		//                              Shimmer 3
		// --------------------------------------------------------------------

		else if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3) {
			
			if (sensorName.equals("Low Noise Accelerometer")){
				return SHIMMER3_LOW_NOISE_ACCELEROMETER;
			}
			else if (sensorName.equals("Wide Range Accelerometer")){
				return SHIMMER3_WIDE_RANGE_ACCELEROMETER;
			}
			else if (sensorName.equals("Gyroscope")){
				return SHIMMER3_GYROSCOPE;
			}
			else if (sensorName.equals("Magnetometer")){
				return SHIMMER3_MAGNETOMETER;
			}
			else if (sensorName.equals("Battery Voltage")){
				return SHIMMER3_BATTERY;
			}
			else if (sensorName.equals("External ADC A15")){
				return SHIMMER3_EXT_ADC_A15;
			}
			else if (sensorName.equals("External ADC A7")){
				return SHIMMER3_EXT_ADC_A7;
			}
			else if (sensorName.equals("External ADC A6")){
				return SHIMMER3_EXT_ADC_A6;
			}
			else if (sensorName.equals("Internal ADC A1")){
				return SHIMMER3_INT_ADC_1;
			}
			else if (sensorName.equals("Internal ADC A12")){
				return SHIMMER3_INT_ADC_12;
			}
			else if (sensorName.equals("Internal ADC A13")){
				return SHIMMER3_INT_ADC_13;
			}
			else if (sensorName.equals("Internal ADC A14")){
				return SHIMMER3_INT_ADC_14;
			}
			else if (sensorName.equals("Pressure")){ // TODO: Verify Pressure
				return SHIMMER3_PRESSURE;
			}
			else if (sensorName.equals("GSR")){
				return SHIMMER3_GSR;
			}
			else if (sensorName.equals("EXG1")){
				if (isEXGUsingDefaultECGConfiguration){
					return SHIMMER3_EXG1_ECG_24;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return SHIMMER3_EXG1_EMG_24;
				} 
				else {
					return SHIMMER3_EXG1_24;
				}
			}
			else if (sensorName.equals("EXG2")){
				if (isEXGUsingDefaultECGConfiguration){
					return SHIMMER3_EXG2_ECG_24;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return SHIMMER3_EXG2_EMG_24;
				} 
				else {
					return SHIMMER3_EXG2_24;
				}
			}
			else if (sensorName.equals("EXG1 16Bit")){
				if (isEXGUsingDefaultECGConfiguration){
					return SHIMMER3_EXG1_ECG_16;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return SHIMMER3_EXG1_EMG_16;
				} else {
					return SHIMMER3_EXG1_16;
				}
			}
			else if (sensorName.equals("EXG2 16 Bit")){
				if (isEXGUsingDefaultECGConfiguration){
					return SHIMMER3_EXG2_ECG_16;
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					return SHIMMER3_EXG2_EMG_16;
				} else {
					return SHIMMER3_EXG2_16;
				}
			}
			else if (sensorName.equals("Bridge Amplifier")){
				return SHIMMER3_BRIDGE_AMPLIFIER;
			}
		}
		throw new Exception("Unknown sensor configuration encountered: " + sensorName);
	}
}