package edu.pdx.cecs.orcyclesensors;

public interface IShimmer {
	double getSamplingRate();
	int getAccelRange();
	int getGSRRange();
	double getBattLimitWarning();
	int getInternalExpPower();
	boolean isLowPowerMagEnabled();
	boolean isLowPowerAccelEnabled();
	boolean isLowPowerGyroEnabled();
	int get5VReg();
	int getGyroRange();
	int getMagRange();
	int getPressureResolution();
	int getEXGReferenceElectrode();
	int getLeadOffDetectionMode();
	int getLeadOffDetectionCurrent();
	int getLeadOffComparatorTreshold();

	int getEXG1CH1GainValue();
	int getEXG1CH2GainValue();
	int getEXG2CH1GainValue();
	int getEXG2CH2GainValue();
	long getEnabledSensors();
}
