package edu.pdx.cecs.orcyclesensors;

public class Controller {

	public static final String EXTRA_KEEP_ME = "EXTRA_KEEP_ME";
	public static final String EXTRA_SHOW_FRAGMENT = "EXTRA_SHOW_FRAGMENT";
	public static final int EXTRA_SHOW_FRAGMENT_RECORD = 0;
	public static final int EXTRA_SHOW_FRAGMENT_TRIPS = 1;
	public static final int EXTRA_SHOW_FRAGMENT_DEVICES = 2;
	public static final int EXTRA_SHOW_FRAGMENT_SENSORS = 3;

	public static final String EXTRA_TRIP_ID = "EXTRA_TRIP_ID";
	public static final int EXTRA_TRIP_ID_UNDEFINED = -1;
	public static final String EXTRA_IS_NEW_TRIP = "isNewTrip";
	
	public static final String EXTRA_TRIP_SOURCE = "EXTRA_TRIP_SOURCE";
	public static final int EXTRA_TRIP_SOURCE_UNDEFINED = -1;
	public static final int EXTRA_TRIP_SOURCE_MAIN_RECORD = 0;
	public static final int EXTRA_TRIP_SOURCE_MAIN_TRIPS = 1;
}
