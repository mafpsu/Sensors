package edu.pdx.cecs.orcyclesensors;

public class ShimmerDeviceInfo {
	
	private String address;
	private String name;

	public ShimmerDeviceInfo(String address, String name) {
		this.address = address;
		if ((name == null) || (name.equals(""))) {
			this.name = "Un-named";
		}
		else {
			this.name = name;
		}
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getName() {
		return name;
	}
}
