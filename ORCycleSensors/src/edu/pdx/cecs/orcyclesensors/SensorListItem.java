package edu.pdx.cecs.orcyclesensors;

import android.hardware.Sensor;

public class SensorListItem extends SensorItem {
	
	private boolean checked = false;

	public SensorListItem(Sensor sensor, int rate, boolean checked) {
		this.fifoMaxEventCount = sensor.getFifoMaxEventCount();
		this.fifoReservedEventCount = sensor.getFifoReservedEventCount();
		this.maxDelay = -1;
		this.maximumRange = sensor.getMaximumRange();
		this.minDelay = sensor.getMinDelay();
		this.name = sensor.getName();
		this.power = sensor.getPower();
		this.reportingMode = -1;
		this.resolution = sensor.getResolution();
		this.type = sensor.getType();
		this.vendor = sensor.getVendor();
		this.version = sensor.getVersion();
		this.isWakeUpSensor = false;
		this.checked = checked;
		this.rate = rate;
	}
	
	public boolean isChecked() {
		return this.checked;
	}

	public SensorListItem setChecked(boolean value) {
		checked = value;
		return this;
	}
	
	public SensorListItem setRate(int rate) {
		this.rate = rate;
		return this;
	}
}
