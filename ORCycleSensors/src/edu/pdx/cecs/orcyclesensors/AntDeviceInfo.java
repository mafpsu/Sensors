package edu.pdx.cecs.orcyclesensors;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

public class AntDeviceInfo {
	
	private int number;
	private String name;
	private DeviceType deviceType;

	public AntDeviceInfo(int number, String name, DeviceType deviceType) {
		this.number = number;
		this.deviceType = deviceType;
		if ((name == null) || (name.equals(""))) {
			this.name = "Un-named";
		}
		else {
			this.name = name;
		}
	}
	
	public int getNumber() {
		return number;
	}
	
	public String getName() {
		return name;
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}
}
