package edu.pdx.cecs.orcyclesensors;

public class EpocDeviceInfo {
	
	private String address;
	private String name;

	public EpocDeviceInfo(String address, String name) {
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
