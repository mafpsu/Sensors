package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;

public class MockShimmer implements IShimmer{

	private long enabledSensors;
	private int exgGain = 0;
	
	public MockShimmer() {
		enabledSensors = Shimmer.SENSOR_EXG1_24BIT + Shimmer.SENSOR_EXG2_24BIT;
	}
	
	public double getSamplingRate(){return 0.0;}
	public int getAccelRange(){return 0;}
	public int getGSRRange(){return 0;}
	public double getBattLimitWarning(){return 0.0;}
	public int getInternalExpPower(){return 0;}
	public boolean isLowPowerMagEnabled(){return false;}
	public boolean isLowPowerAccelEnabled(){return false;}
	public boolean isLowPowerGyroEnabled(){return false;}
	public int get5VReg(){return 0;}
	public int getGyroRange(){return 0;}
	public int getMagRange(){return 0;}
	public int getPressureResolution(){return 0;}
	public int getEXGReferenceElectrode(){return 0;}
	public int getLeadOffDetectionMode(){return 0;}
	public int getLeadOffDetectionCurrent(){return 0;}
	public int getLeadOffComparatorTreshold(){return 0;}
	public int getEXG1CH1GainValue(){return exgGain;}
	public int getEXG1CH2GainValue(){return exgGain;}
	public int getEXG2CH1GainValue(){return exgGain;}
	public int getEXG2CH2GainValue(){return exgGain;}
	public long getEnabledSensors(){return enabledSensors;}

}
