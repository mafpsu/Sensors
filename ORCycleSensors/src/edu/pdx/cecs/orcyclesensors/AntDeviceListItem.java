package edu.pdx.cecs.orcyclesensors;

public class AntDeviceListItem {

	private String name;
	private String description;
	private boolean checked;
	
	public AntDeviceListItem(String name, String description, boolean checked) {
		this.name = name;
		this.description = description;
		this.checked = checked;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public AntDeviceListItem setChecked(boolean value) {
		checked = value;
		return this;
	}
	
	public boolean isChecked() {
		return this.checked;
	}
}
