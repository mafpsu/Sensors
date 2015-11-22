package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration.Shimmer3;

public class ShimmerSignalGroup {
	
	final String[] signalNames;
	
	private ShimmerSignalGroup(String signalName0, String signalName1, String signalName2) {
		this.signalNames = new String[3];
		this.signalNames[0] = signalName0;
		this.signalNames[1] = signalName1;
		this.signalNames[2] = signalName2;
	}

	private ShimmerSignalGroup(String signalName0, String signalName1) {
		this.signalNames = new String[2];
		this.signalNames[0] = signalName0;
		this.signalNames[1] = signalName1;
	}
	
	private ShimmerSignalGroup(String signalName0) {
		this.signalNames = new String[1];
		this.signalNames[0] = signalName0;
	}

	public static ShimmerSignalGroup create(String sensorName, int shimmerVersion, 
			boolean isEXGUsingDefaultECGConfiguration, boolean isEXGUsingDefaultEMGConfiguration) {
		
		ShimmerSignalGroup signalGroup = null;

		// --------------------------------------------------------------------
		//                         Shimmer 2 & 2R
		// --------------------------------------------------------------------
		if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2 || shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_2R) {

			if (sensorName.equals("Accelerometer")){
				signalGroup = new ShimmerSignalGroup( 
						Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_X,
						Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Y,
						Configuration.Shimmer2.ObjectClusterSensorName.ACCEL_Z);
			}
			else if (sensorName.equals("Gyroscope")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer2.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer2.ObjectClusterSensorName.GYRO_Z);
			}
			else if (sensorName.equals("Magnetometer")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.MAG_X,
						Configuration.Shimmer2.ObjectClusterSensorName.MAG_Y,
						Configuration.Shimmer2.ObjectClusterSensorName.MAG_Z);
			}
			else if (sensorName.equals("GSR")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.GSR);
			}
			else if (sensorName.equals("ECG")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.ECG_RA_LL,
						Configuration.Shimmer2.ObjectClusterSensorName.ECG_LA_LL);
			}
			else if (sensorName.equals("EMG")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.EMG);
			}
			else if (sensorName.equals("Bridge Amplifier")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_HIGH,
						Configuration.Shimmer2.ObjectClusterSensorName.BRIDGE_AMP_LOW);
			}
			else if (sensorName.equals("Heart Rate")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.HEART_RATE);
			}
			else if (sensorName.equals("Exp Board A0")){ // TODO: Verify Exp Board A0
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.EXP_BOARD_A0);
			}
			else if (sensorName.equals("Exp Board A7")){ // TODO: Verify Exp Board A7
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.EXP_BOARD_A7);
			}
			else if (sensorName.equals("Battery Voltage")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer2.ObjectClusterSensorName.BATTERY,
						Configuration.Shimmer2.ObjectClusterSensorName.REG);
			}
		}
		// --------------------------------------------------------------------
		//                              Shimmer 3
		// --------------------------------------------------------------------
		else if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3) { 
			if (sensorName.equals("Low Noise Accelerometer")){
				signalGroup = new ShimmerSignalGroup( 
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_LN_Z);
			}
			else if (sensorName.equals("Wide Range Accelerometer")){
				signalGroup = new ShimmerSignalGroup( 
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_X,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.ACCEL_WR_Z);
			}
			else if (sensorName.equals("Gyroscope")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_X,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.GYRO_Z);
			}
			else if (sensorName.equals("Magnetometer")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_X,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_Y,
						Configuration.Shimmer3.ObjectClusterSensorName.MAG_Z);
			}
			else if (sensorName.equals("Battery Voltage")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.BATTERY);
			}
			else if (sensorName.equals("External ADC A15")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.EXT_EXP_A15);
			}
			else if (sensorName.equals("External ADC A7")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.EXT_EXP_A7);
			}
			else if (sensorName.equals("External ADC A6")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.EXT_EXP_A6);
			}
			else if (sensorName.equals("Internal ADC A1")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.INT_EXP_A1);
			}
			else if (sensorName.equals("Internal ADC A12")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.INT_EXP_A12);
			}
			else if (sensorName.equals("Internal ADC A13")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.INT_EXP_A13);
			}
			else if (sensorName.equals("Internal ADC A14")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.INT_EXP_A14);
			}
			else if (sensorName.equals("Pressure")){ // TODO: Verify Pressure
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.PRESSURE_BMP180, 
						Shimmer3.ObjectClusterSensorName.TEMPERATURE_BMP180);
			}
			else if (sensorName.equals("GSR")){
				signalGroup = new ShimmerSignalGroup(
						Configuration.Shimmer3.ObjectClusterSensorName.GSR);
			}
			else if (sensorName.equals("EXG1")){
				if (isEXGUsingDefaultECGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
							Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT,
							Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
							Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT,
							Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT);
				} else {
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG1_CH1_24BIT,
							Shimmer3.ObjectClusterSensorName.EXG1_CH2_24BIT);
				}
			}
			else if (sensorName.equals("EXG2")){
				if (isEXGUsingDefaultECGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
							Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
							Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT);
				} else {
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG2_CH1_24BIT,
							Shimmer3.ObjectClusterSensorName.EXG2_CH2_24BIT);
				}
			}
			else if (sensorName.equals("EXG1 16Bit")){
				if (isEXGUsingDefaultECGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
							Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT,
							Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
							Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT,
							Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT);
				} else {
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG1_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG1_CH1_16BIT,
							Shimmer3.ObjectClusterSensorName.EXG1_CH2_16BIT);
				}
			}
			else if (sensorName.equals("EXG2 16 Bit")){
				if (isEXGUsingDefaultECGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
							Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT);
				}
				else if (isEXGUsingDefaultEMGConfiguration){
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
							Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT);
				} else {
					signalGroup = new ShimmerSignalGroup(
							Shimmer3.ObjectClusterSensorName.EXG2_STATUS,
							Shimmer3.ObjectClusterSensorName.EXG2_CH1_16BIT,
							Shimmer3.ObjectClusterSensorName.EXG2_CH2_16BIT);
				}
			}
			else if (sensorName.equals("Bridge Amplifier")){
				signalGroup = new ShimmerSignalGroup(
						Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_HIGH, 
						Shimmer3.ObjectClusterSensorName.BRIDGE_AMP_LOW);
			}
		}
		return signalGroup;
	}
}
