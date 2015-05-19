package edu.pdx.cecs.orcyclesensors;

public class AntDeviceInfo {
	
	
	public static final int HEART_RATE_DEVICE = 1;
	public static final int BIKE_POWER_DEVICE = 2;
	public static final int BIKE_CADENCE_DEVICE = 3;
	public static final int BIKE_SPEED_AND_DISTANCE_DEVICE = 4;
	public static final int STRIDE_SDM_DEVICE = 5;
	public static final int WATCH_DOWNLOADER_DEVICE = 6;
	public static final int FITNESS_EQUIPMENT_DEVICE = 7;
	public static final int FITNESS_EQUIPMENT_CONTROLS_DEVICE = 8;
	public static final int BLOOD_PRESSURE_DEVICE = 9;
	public static final int WEIGHT_SCALE_DEVICE = 10;
	public static final int ENVIRONMENT_DEVICE = 11;
	public static final int GEO_CACHE_DEVICE = 12;
	public static final int AUDIO_CONTROLLABLE_DEVICE = 13;
	public static final int AUDIO_REMOTE_DEVICE = 14;
	public static final int VIDEO_CONTROLLABLE_DEVICE = 15;
	public static final int VIDEO_REMOTE_DEVICE = 16;
	public static final int GENERIC_CONTROLLABLE_DEVICE = 17;
	public static final int GENERIC_REMOTE_DEVICE = 18;

	private int number;
	private String name;
	private int type;

	public AntDeviceInfo(int number, String name, int type) {
		this.number = number;
		this.type = type;
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
	
	public int getType() {
		return type;
	}
}
