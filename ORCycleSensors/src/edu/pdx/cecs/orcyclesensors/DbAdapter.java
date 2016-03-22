/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  ORcycle 2.2.0 has introduced new app features: safety focus with new buttons
 *  to report safety issues and crashes (new questionnaires), expanded trip
 *  questionnaire (adding questions besides trip purpose), app utilization
 *  reminders, app tutorial, and updated font and color schemes.
 *
 *  @author Bryan.Blanc <bryanpblanc@gmail.com>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires and new ORcycle features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
 *
 *  Updated/modified for Oregon pilot study and app deployment.
 *
 *  ORcycle is free software: you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or any later version.
 *  ORcycle is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with
 *  ORcycle. If not, see <http://www.gnu.org/licenses/>.
 *
 *************************************************************************************
 *
 *	 Cycle Altanta, Copyright 2012 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong15@gmail.com>
 *
 *   Updated/Modified for Atlanta's app deployment. Based on the
 *   CycleTracks codebase for SFCTA.
 *
 *   CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 * 	 @author Billy Charlton <billy.charlton@sfcta.org>
 *
 *   This file is part of CycleTracks.
 *
 *   CycleTracks is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple database access helper class. Defines the basic CRUD operations, and
 * gives the ability to list all trips as well as retrieve or modify a specific
 * trip.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 *
 * **This code borrows heavily from Google demo app "Notepad" in the Android
 * SDK**
 */
public class DbAdapter {

	private static final String MODULE_TAG = "DbAdapter";

	// Database versions
	private static final int DATABASE_VERSION_START = 1;
	private static final int DATABASE_VERSION_SENSOR_VALUES_TABLE = 2;
	private static final int DATABASE_VERSION_HEART_RATE_TABLE = 3;
	private static final int DATABASE_VERSION_BIKE_POWER_TABLE = 4;
	private static final int DATABASE_VERSION_SHIMMER_VALUES_TABLE = 5;
	private static final int DATABASE_VERSION_SHIMMER_EXG_VALUES_TABLE = 6;
	private static final int DATABASE_VERSION_SHIMMER_CONFIG_TABLE = 7;
	private static final int DATABASE_VERSION_SHIMMER_EXG_INDEXES = 8;
	
	private static final int DATABASE_VERSION = DATABASE_VERSION_SHIMMER_EXG_INDEXES;

	// Table names
	private static final String DATABASE_NAME = "data";
	private static final String DATA_TABLE_TRIPS = "trips";
	private static final String DATA_TABLE_COORDS = "coords";
	private static final String DATA_TABLE_PAUSES = "pauses";
	private static final String DATA_TABLE_SENSOR_VALUES = "sensor_values";
	private static final String DATA_TABLE_SHIMMER_VALUES = "shimmer_values";
	private static final String DATA_TABLE_SHIMMER_ECG_VALUES = "shimmer_ecg_values";
	private static final String DATA_TABLE_SHIMMER_EMG_VALUES = "shimmer_emg_values";
	private static final String DATA_TABLE_SHIMMER_CONFIG = "shimmer_config";
	private static final String DATA_TABLE_HEART_RATE = "heart_rate";
	private static final String DATA_TABLE_BIKE_POWER = "bike_power";

	// Index names
	private static final String INDEX_SHIMMER_ECG_VALUES = "index_shimmer_ecg_values";
	private static final String INDEX_SHIMMER_EMG_VALUES = "index_shimmer_emg_values";

	
	// Trips table columns
	public static final String K_TRIP_ROWID = "_id";
	public static final String K_TRIP_PURP = "purp";
	public static final String K_TRIP_START = "start";
	public static final String K_TRIP_END = "endtime";
	public static final String K_TRIP_FANCYSTART = "fancystart";
	public static final String K_TRIP_FANCYINFO = "fancyinfo";
	public static final String K_TRIP_NOTE = "note";
	public static final String K_TRIP_DISTANCE = "distance";
	public static final String K_TRIP_LATHI = "lathi";
	public static final String K_TRIP_LATLO = "latlo";
	public static final String K_TRIP_LGTHI = "lgthi";
	public static final String K_TRIP_LGTLO = "lgtlo";
	public static final String K_TRIP_STATUS = "status";
	public static final String K_TRIP_HAS_SENSOR_DATA = "has_sensor_data";
	public static final String K_TRIP_HAS_ANT_DEVICE_DATA = "has_ant_device_data";
	public static final String K_TRIP_HAS_SHIMMER_DATA = "has_shimmer_data";
	public static final String K_TRIP_HAS_EPOC_DATA = "has_epoc_data";

	// Coords table columns
	public static final String K_POINT_ROWID = "_id";
	public static final String K_POINT_TRIP = "trip";
	public static final String K_POINT_TIME = "time";
	public static final String K_POINT_LAT = "lat";
	public static final String K_POINT_LGT = "lgt";
	public static final String K_POINT_ACC = "acc";
	public static final String K_POINT_ALT = "alt";
	public static final String K_POINT_SPEED = "speed";

	// Pauses table columns
	public static final String K_PAUSE_TRIP_ID = "_id";
	public static final String K_PAUSE_START_TIME = "starttime";
	public static final String K_PAUSE_END_TIME = "endtime";
	
	// Sensor table columns
	public static final String K_SENSOR_TIME = "time";
	public static final String K_SENSOR_ID = "id";
	public static final String K_SENSOR_TYPE = "type";
	public static final String K_SENSOR_SAMPLES = "samples";
	public static final String K_SENSOR_NUM_VALS = "numvals";
	public static final String K_SENSOR_AVG_0 = "avg0";
	public static final String K_SENSOR_AVG_1 = "avg1";
	public static final String K_SENSOR_AVG_2 = "avg2";
	public static final String K_SENSOR_STD_0 = "ssd0";
	public static final String K_SENSOR_STD_1 = "ssd1";
	public static final String K_SENSOR_STD_2 = "ssd2";

	// Shimmer Sensor table columns
	public static final String K_SHIMMER_TIME = "time";
	public static final String K_SHIMMER_ID = "id";
	public static final String K_SHIMMER_TYPE = "type";
	public static final String K_SHIMMER_SAMPLES = "samples";
	public static final String K_SHIMMER_NUM_VALS = "numvals";
	public static final String K_SHIMMER_AVG_0 = "avg0";
	public static final String K_SHIMMER_AVG_1 = "avg1";
	public static final String K_SHIMMER_AVG_2 = "avg2";
	public static final String K_SHIMMER_STD_0 = "ssd0";
	public static final String K_SHIMMER_STD_1 = "ssd1";
	public static final String K_SHIMMER_STD_2 = "ssd2";

	// Shimmer ECG table columns
	public static final String K_SHIMMER_ECG_ID = "ecg_id";
	public static final String K_SHIMMER_ECG_COORD_TIME = "ecg_coord_time";
	public static final String K_SHIMMER_ECG_SENSOR_ID = "ecg_sensor_id";
	public static final String K_SHIMMER_ECG_TIMESTAMP = "ecg_timestamp";
	public static final String K_SHIMMER_ECG_EXG1_CH1 = "ecg_exg1_ch1";
	public static final String K_SHIMMER_ECG_EXG1_CH2 = "ecg_exg1_ch2";
	public static final String K_SHIMMER_ECG_EXG2_CH1 = "ecg_exg2_ch1";
	public static final String K_SHIMMER_ECG_EXG2_CH2 = "ecg_exg2_ch2";
	
	// Shimmer EMG table columns
	public static final String K_SHIMMER_EMG_ID = "emg_id";
	public static final String K_SHIMMER_EMG_COORD_TIME = "emg_coord_time";
	public static final String K_SHIMMER_EMG_SENSOR_ID = "emg_sensor_id";
	public static final String K_SHIMMER_EMG_TIMESTAMP = "emg_timestamp";
	public static final String K_SHIMMER_EMG_EXG1_CH1 = "emg_exg1_ch1";
	public static final String K_SHIMMER_EMG_EXG1_CH2 = "emg_exg1_ch2";

	// Simmer Configuration table columns
	public static final String K_SHIMMER_CONFIG_TRIP_ID = "sc_trip_id";
	public static final String K_SHIMMER_CONFIG_BLUETOOTH_ID = "sc_bluetooth_id";
	public static final String K_SHIMMER_CONFIG_SAMPLING_RATE = "sc_sampling_rate";
	public static final String K_SHIMMER_CONFIG_ACCEL_RANGE = "sc_accel_range";
	public static final String K_SHIMMER_CONFIG_GSR_RANGE = "sc_gsr_range";
	public static final String K_SHIMMER_CONFIG_BATTERY_LIMIT = "sc_battery_limit";
	public static final String K_SHIMMER_CONFIG_INT_EXP_POWER = "sc_int_exp_power";
	public static final String K_SHIMMER_CONFIG_LOW_PWR_MAG = "sc_low_pwr_mag";
	public static final String K_SHIMMER_CONFIG_LOW_PWR_ACCEL = "sc_low_pwr_accel";
	public static final String K_SHIMMER_CONFIG_LOW_PWR_GYRO = "sc_gyro";
	public static final String K_SHIMMER_CONFIG_5V_REG = "sc_5v_reg";
	public static final String K_SHIMMER_CONFIG_GYRO_RANGE = "sc_gyro_range";
	public static final String K_SHIMMER_CONFIG_MAG_RANGE = "sc_mag_range";
	public static final String K_SHIMMER_CONFIG_PRESSURE_RES = "sc_pressure_range";
	public static final String K_SHIMMER_CONFIG_REF_ELECTRODE = "sc_ref_electrode";
	public static final String K_SHIMMER_CONFIG_LEAD_OFF_DETECTION = "sc_lead_off_detection";
	public static final String K_SHIMMER_CONFIG_LEAD_OFF_CURRENT = "sc_lead_off_current";
	public static final String K_SHIMMER_CONFIG_LEAD_OFF_COMPARATOR = "sc_lead_off_comparator";
	public static final String K_SHIMMER_CONFIG_EXG_GAIN = "sc_exg_gain";
	public static final String K_SHIMMER_CONFIG_EXG_RES = "sc_exg_res";
	public static final String K_SHIMMER_CONFIG_HAS_ECG_DATA = "sc_has_ecg";
	public static final String K_SHIMMER_CONFIG_HAS_EMG_DATA = "sc_has_emg";

	// Heart rate table columns
	public static final String K_HR_TIME = "time";
	public static final String K_HR_NUM_SAMPLES = "samples";
	public static final String K_HR_AVG_HEART_RATE = "avg_heart_rate";
	public static final String K_HR_STD_HEART_RATE = "ssd_heart_rate";

	// Bike power table columns
	public static final String K_BP_TIME = "time";
	public static final String K_BP_CALC_POWER_NUM_SAMPLES = "calc_power_samples";
	public static final String K_BP_CALC_POWER_AVG = "calc_power_avg";
	public static final String K_BP_CALC_POWER_STD = "calc_power_ssd";
	public static final String K_BP_CALC_TORQUE_NUM_SAMPLES = "calc_torque_samples";
	public static final String K_BP_CALC_TORQUE_AVG = "calc_torque_avg";
	public static final String K_BP_CALC_TORQUE_STD = "calc_torque_ssd";
	public static final String K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES = "calc_crank_cadence_samples";
	public static final String K_BP_CALC_CRANK_CADENCE_AVG = "calc_crank_cadence_avg";
	public static final String K_BP_CALC_CRANK_CADENCE_STD = "calc_crank_cadence_ssd";
	public static final String K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES = "calc_wheel_speed_samples";
	public static final String K_BP_CALC_WHEEL_SPEED_AVG = "calc_wheel_speed_avg";
	public static final String K_BP_CALC_WHEEL_SPEED_STD = "calc_wheel_speed_ssd";
	public static final String K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES = "calc_wheel_distance_samples";
	public static final String K_BP_CALC_WHEEL_DISTANCE_AVG = "calc_wheel_distance_avg";
	public static final String K_BP_CALC_WHEEL_DISTANCE_STD = "calc_wheel_distance_ssd";
	
	public static final String K_SHIMMER_="";
    public static final String K_SHIMMER_ACCEL_LN_X = "";
    public static final String K_SHIMMER_ACCEL_LN_Y = "";
    public static final String K_SHIMMER_ACCEL_LN_Z = "";
    public static final String K_SHIMMER_ACCEL_WR_X = "";
    public static final String K_SHIMMER_ACCEL_WR_Y = "";
    public static final String K_SHIMMER_ACCEL_WR_Z = "";
    public static final String K_SHIMMER_GYRO_X = "";
    public static final String K_SHIMMER_GYRO_Y = "";
    public static final String K_SHIMMER_GYRO_Z = "";
    public static final String K_SHIMMER_MAG_X = "";
    public static final String K_SHIMMER_MAG_Y = "";
    public static final String K_SHIMMER_MAG_Z = "";
	public static final String K_SHIMMER_GSR = "";
	public static final String K_SHIMMER_EMG_CH1_24 = "";
	public static final String K_SHIMMER_EMG_CH2_24 = "";
	public static final String K_SHIMMER_EMG_CH1_16 = "";
	public static final String K_SHIMMER_EMG_CH2_16 = "";
	public static final String K_SHIMMER_ECG_LL_RA_24 = "";
	public static final String K_SHIMMER_ECG_LA_RA_24 = "";
	public static final String K_SHIMMER_ECG_LL_RA_16 = "";
	public static final String K_SHIMMER_ECG_LA_RA_16 = "";
	public static final String K_SHIMMER_BRIDGE_AMP_HIGH = "";
	public static final String K_SHIMMER_BRIDGE_AMP_LOW = "";
	public static final String K_SHIMMER_HEART_RATE = "";
	public static final String K_SHIMMER_EXP_BOARD_A0 = "";
	public static final String K_SHIMMER_EXP_BOARD_A7 = "";
	public static final String K_SHIMMER_BATTERY = "";
	public static final String K_SHIMMER_TIMESTAMP = "";
	public static final String K_SHIMMER_EXT_EXP_A7 = "";
	public static final String K_SHIMMER_EXT_EXP_A6 = "";
	public static final String K_SHIMMER_EXT_EXP_A15 = "";
	public static final String K_SHIMMER_INT_EXP_A1 = "";
	public static final String K_SHIMMER_INT_EXP_A12 = "";
	public static final String K_SHIMMER_INT_EXP_A13 = "";
	public static final String K_SHIMMER_INT_EXP_A14 = "";
	public static final String K_SHIMMER_PRESSURE = "";

	private static final String SQL_CREATE_TABLE_CMD = "create table";
	
	private static final String SQL_CREATE_INDEX_CMD = "create index";
	
	private static final String TABLE_CREATE_TRIPS = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_TRIPS + " ("
			+ K_TRIP_ROWID      + " integer primary key autoincrement, "
			+ K_TRIP_PURP       + " text, " 
			+ K_TRIP_START      + " double, "
			+ K_TRIP_END        + " double, "
			+ K_TRIP_FANCYSTART + " text, "
			+ K_TRIP_FANCYINFO  + " text, "
			+ K_TRIP_DISTANCE   + " float, "
			+ K_TRIP_NOTE       + " text,"
			+ K_TRIP_LATHI      + " integer, "
			+ K_TRIP_LATLO      + " integer, "
			+ K_TRIP_LGTHI      + " integer, "
			+ K_TRIP_LGTLO      + " integer, "
			+ K_TRIP_HAS_SENSOR_DATA     + " integer, "
			+ K_TRIP_HAS_ANT_DEVICE_DATA + " integer, "
			+ K_TRIP_HAS_SHIMMER_DATA    + " integer, "
			+ K_TRIP_HAS_EPOC_DATA       + " integer, "
			+ K_TRIP_STATUS     + " integer);";

	private static final String TABLE_CREATE_COORDS = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_COORDS + " ("
			+ K_POINT_ROWID + " integer primary key autoincrement, "
			+ K_POINT_TRIP  + " integer, "
			+ K_POINT_LAT   + " integer, "
			+ K_POINT_LGT   + " integer, "
			+ K_POINT_TIME  + " double, "
			+ K_POINT_ACC   + " float, "
			+ K_POINT_ALT   + " double, "
			+ K_POINT_SPEED + " float);";

	private static final String TABLE_CREATE_PAUSES = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_PAUSES + " ("
			+ K_PAUSE_TRIP_ID    + " integer, "
			+ K_PAUSE_START_TIME + " double, "
			+ K_PAUSE_END_TIME   + " double, "
			+ "PRIMARY KEY(" + K_PAUSE_TRIP_ID + ", " + K_PAUSE_START_TIME + "), "
			+ "FOREIGN KEY(" + K_PAUSE_TRIP_ID + ") REFERENCES "  + DATA_TABLE_TRIPS + "(" + K_TRIP_ROWID + "));";

	private static final String TABLE_CREATE_SENSOR_VALUES = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_SENSOR_VALUES + " ("
			+ K_SENSOR_TIME     + " double, "
			+ K_SENSOR_ID       + " text, "
			+ K_SENSOR_TYPE     + " integer, "
			+ K_SENSOR_SAMPLES  + " integer, "
			+ K_SENSOR_NUM_VALS + " integer, "
			+ K_SENSOR_AVG_0  + " double, "
			+ K_SENSOR_AVG_1  + " double, "
			+ K_SENSOR_AVG_2  + " double, "
			+ K_SENSOR_STD_0  + " double, "
			+ K_SENSOR_STD_1  + " double, "
			+ K_SENSOR_STD_2  + " double, "
			+ "PRIMARY KEY(" + K_SENSOR_TIME + ", " + K_SENSOR_ID + "));";

	private static final String TABLE_CREATE_SHIMMER_VALUES = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_SHIMMER_VALUES + " ("
			+ K_SHIMMER_TIME     + " double, "
			+ K_SHIMMER_ID       + " text, "
			+ K_SHIMMER_TYPE     + " integer, "
			+ K_SHIMMER_SAMPLES  + " integer, "
			+ K_SHIMMER_NUM_VALS + " integer, "
			+ K_SHIMMER_AVG_0  + " double, "
			+ K_SHIMMER_AVG_1  + " double, "
			+ K_SHIMMER_AVG_2  + " double, "
			+ K_SHIMMER_STD_0  + " double, "
			+ K_SHIMMER_STD_1  + " double, "
			+ K_SHIMMER_STD_2  + " double, "
			+ "PRIMARY KEY(" + K_SHIMMER_TIME + ", " + K_SHIMMER_ID + "));";

	private static final String TABLE_CREATE_SHIMMER_ECG_VALUES = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_SHIMMER_ECG_VALUES + " ("
			+ K_SHIMMER_ECG_ID         + " integer primary key autoincrement, "
			+ K_SHIMMER_ECG_COORD_TIME + " double, "
			+ K_SHIMMER_ECG_SENSOR_ID  + " text, "
			+ K_SHIMMER_ECG_TIMESTAMP  + " double, "
			+ K_SHIMMER_ECG_EXG1_CH1   + " double, "
			+ K_SHIMMER_ECG_EXG1_CH2   + " double, "
			+ K_SHIMMER_ECG_EXG2_CH1   + " double, "
			+ K_SHIMMER_ECG_EXG2_CH2   + " double);";
	
	private static final String INDEX_CREATE_SHIMMER_ECG_VALUES = SQL_CREATE_INDEX_CMD + " " + INDEX_SHIMMER_ECG_VALUES + " ON " +
			DATA_TABLE_SHIMMER_ECG_VALUES + "(" + K_SHIMMER_ECG_COORD_TIME + ")";

	private static final String TABLE_CREATE_SHIMMER_EMG_VALUES = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_SHIMMER_EMG_VALUES + " ("
			+ K_SHIMMER_EMG_ID         + " integer primary key autoincrement, "
			+ K_SHIMMER_EMG_COORD_TIME + " double, "
			+ K_SHIMMER_EMG_SENSOR_ID  + " text, "
			+ K_SHIMMER_EMG_TIMESTAMP  + " double, "
			+ K_SHIMMER_EMG_EXG1_CH1   + " double, "
			+ K_SHIMMER_EMG_EXG1_CH2   + " double);";

	private static final String INDEX_CREATE_SHIMMER_EMG_VALUES = SQL_CREATE_INDEX_CMD + " " + INDEX_SHIMMER_EMG_VALUES + " ON " +
			DATA_TABLE_SHIMMER_EMG_VALUES + "(" + K_SHIMMER_EMG_COORD_TIME + ")";

	private static final String TABLE_CREATE_SHIMMER_CONFIG = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_SHIMMER_CONFIG + " ("
			+ K_SHIMMER_CONFIG_TRIP_ID             + " integer primary key, "
			+ K_SHIMMER_CONFIG_BLUETOOTH_ID        + " text, "
			+ K_SHIMMER_CONFIG_SAMPLING_RATE       + " double, "
			+ K_SHIMMER_CONFIG_ACCEL_RANGE         + " integer, "
			+ K_SHIMMER_CONFIG_GSR_RANGE           + " integer, "
			+ K_SHIMMER_CONFIG_BATTERY_LIMIT       + " double, "
			+ K_SHIMMER_CONFIG_INT_EXP_POWER       + " integer, "
			+ K_SHIMMER_CONFIG_LOW_PWR_MAG         + " integer, "
			+ K_SHIMMER_CONFIG_LOW_PWR_ACCEL       + " integer, "
			+ K_SHIMMER_CONFIG_LOW_PWR_GYRO        + " integer, "
			+ K_SHIMMER_CONFIG_5V_REG              + " integer, "
			+ K_SHIMMER_CONFIG_GYRO_RANGE          + " integer, "
			+ K_SHIMMER_CONFIG_MAG_RANGE           + " integer, "
			+ K_SHIMMER_CONFIG_PRESSURE_RES        + " integer, "
			+ K_SHIMMER_CONFIG_REF_ELECTRODE       + " integer, "
			+ K_SHIMMER_CONFIG_LEAD_OFF_DETECTION  + " integer, "
			+ K_SHIMMER_CONFIG_LEAD_OFF_CURRENT    + " integer, "
			+ K_SHIMMER_CONFIG_LEAD_OFF_COMPARATOR + " integer, "
			+ K_SHIMMER_CONFIG_EXG_GAIN            + " integer, "
			+ K_SHIMMER_CONFIG_EXG_RES             + " integer, "
			+ K_SHIMMER_CONFIG_HAS_ECG_DATA        + " integer, "
			+ K_SHIMMER_CONFIG_HAS_EMG_DATA        + " integer );";

	private static final String TABLE_CREATE_HEART_RATE = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_HEART_RATE + " ("
			+ K_HR_TIME           + " double, "
			+ K_HR_NUM_SAMPLES    + " integer, "
			+ K_HR_AVG_HEART_RATE + " double, "
			+ K_HR_STD_HEART_RATE + " double, "
			+ "PRIMARY KEY(" + K_HR_TIME + "));";
	
	private static final String TABLE_CREATE_BIKE_POWER = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_BIKE_POWER + " ("
			+ K_BP_TIME                   + " double, "
			+ K_BP_CALC_POWER_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_POWER_AVG         + " double, "
			+ K_BP_CALC_POWER_STD         + " double, "
			+ K_BP_CALC_TORQUE_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_TORQUE_AVG         + " double, "
			+ K_BP_CALC_TORQUE_STD         + " double, "
			+ K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_CRANK_CADENCE_AVG         + " double, "
			+ K_BP_CALC_CRANK_CADENCE_STD         + " double, "
			+ K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_WHEEL_SPEED_AVG         + " double, "
			+ K_BP_CALC_WHEEL_SPEED_STD         + " double, "
			+ K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_WHEEL_DISTANCE_AVG         + " double, "
			+ K_BP_CALC_WHEEL_DISTANCE_STD         + " double, "
			+ "PRIMARY KEY(" + K_BP_TIME + "));";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;

	// ************************************************************************
	// *                         DatabaseHelper
	// ************************************************************************

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE_TRIPS);
			db.execSQL(TABLE_CREATE_COORDS);
			db.execSQL(TABLE_CREATE_PAUSES);
			db.execSQL(TABLE_CREATE_SENSOR_VALUES);
			db.execSQL(TABLE_CREATE_HEART_RATE);
			db.execSQL(TABLE_CREATE_BIKE_POWER);
			db.execSQL(TABLE_CREATE_SHIMMER_VALUES);
			db.execSQL(TABLE_CREATE_SHIMMER_ECG_VALUES);
			db.execSQL(TABLE_CREATE_SHIMMER_EMG_VALUES);
			db.execSQL(TABLE_CREATE_SHIMMER_CONFIG);
			db.execSQL(INDEX_CREATE_SHIMMER_ECG_VALUES);
			db.execSQL(INDEX_CREATE_SHIMMER_EMG_VALUES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			Log.w(MODULE_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

			// Create table for holding sensor data
			if (oldVersion < DATABASE_VERSION_SENSOR_VALUES_TABLE) {
				try {
					db.execSQL(TABLE_CREATE_SENSOR_VALUES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
			
			// Create table for holding heart rate data
			if (oldVersion < DATABASE_VERSION_HEART_RATE_TABLE) {
				try {
					db.execSQL(TABLE_CREATE_HEART_RATE);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

			// Create table for holding heart rate data
			if (oldVersion < DATABASE_VERSION_BIKE_POWER_TABLE) {
				try {
					db.execSQL(TABLE_CREATE_BIKE_POWER);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

			// Create table for holding shimmer sensor data
			if (oldVersion < DATABASE_VERSION_SHIMMER_VALUES_TABLE) {
				try {
					db.execSQL(TABLE_CREATE_SHIMMER_VALUES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

			// Create tables for holding Shimmer ECG and EMG data
			if (oldVersion < DATABASE_VERSION_SHIMMER_EXG_VALUES_TABLE) {
				try {
					db.execSQL(TABLE_CREATE_SHIMMER_ECG_VALUES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				try {
					db.execSQL(TABLE_CREATE_SHIMMER_EMG_VALUES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

			// Create table for holding shimmer configuration data
			if (oldVersion < DATABASE_VERSION_SHIMMER_CONFIG_TABLE) {
				try {
					db.execSQL(TABLE_CREATE_SHIMMER_CONFIG);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

			// Create index for Shimmer ECG and EMG data
			if (oldVersion < DATABASE_VERSION_SHIMMER_EXG_INDEXES) {
				try {
					db.execSQL(INDEX_CREATE_SHIMMER_ECG_VALUES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
				try {
					db.execSQL(INDEX_CREATE_SHIMMER_EMG_VALUES);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}

		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	// ************************************************************************
	// *                    DbAdapter table methods
	// ************************************************************************

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 *
	 * @param ctx
	 *            the Context within which to work
	 */
	public DbAdapter(Context ctx) {
		mCtx = ctx;
	}

	/**
	 * Open the database. If it cannot be opened, try to create a new instance
	 * of the database. If it cannot be created, throw an exception to signal
	 * the failure
	 *
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public DbAdapter openReadOnly() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getReadableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	// ************************************************************************
	// *                    Coordinate table methods
	// ************************************************************************

	public boolean addCoordToTrip(long tripid, CyclePoint pt) {

		// Add the latest point
		ContentValues coordValues = new ContentValues();
		coordValues.put(K_POINT_TRIP, tripid);
		coordValues.put(K_POINT_LAT, pt.latitude);
		coordValues.put(K_POINT_LGT, pt.longitude);
		coordValues.put(K_POINT_TIME, pt.time);
		coordValues.put(K_POINT_ACC, pt.accuracy);
		coordValues.put(K_POINT_ALT, pt.altitude);
		coordValues.put(K_POINT_SPEED, pt.speed);

		long rowId = mDb.insert(DATA_TABLE_COORDS, null, coordValues);

		if (rowId == -1) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_COORDS + ": failed");
		}
		else {
			/* Log.i(MODULE_TAG, "Insert " + DATA_TABLE_COORDS + "[" + String.valueOf(rowId) + "]("
					+ K_POINT_TRIP + ", " + K_POINT_LAT + ", " + K_POINT_LGT + ", "
					+ K_POINT_TIME + ", " + K_POINT_ACC + ", " + K_POINT_ALT + ", "
					+ K_POINT_SPEED +")"); */
		}

		// And update the trip stats
		ContentValues tripValues = new ContentValues();
		tripValues.put(K_TRIP_END, pt.time);

		int numRows = mDb.update(DATA_TABLE_TRIPS, tripValues, K_TRIP_ROWID + "=" + tripid, null);

		boolean success = ((rowId != -1) && (numRows > 0));

		return success;
	}

	public boolean deleteAllCoordsForTrip(long tripid) {

		int numRows = mDb.delete(DATA_TABLE_COORDS, K_POINT_TRIP + "=" + tripid, null);

		Log.i(MODULE_TAG, "Deleted " + DATA_TABLE_COORDS + "[" + String.valueOf(tripid) +"]: " + String.valueOf(numRows) + " rows.");

		return numRows > 0;
	}

	public Cursor fetchAllCoordsForTrip(long tripid) {
		try {
			Cursor mCursor = mDb.query(true, DATA_TABLE_COORDS, new String[] {
					K_POINT_LAT, K_POINT_LGT, K_POINT_TIME, K_POINT_ACC,
					K_POINT_ALT, K_POINT_SPEED }, K_POINT_TRIP + "=" + tripid,
					null, null, null, K_POINT_TIME, null);

			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
			return null;
		}
	}

	// ************************************************************************
	// *                    Sensor table methods
	// ************************************************************************

	public void addSensorReadings(double currentTime, String sensorName, int sensorType, int numSamples, float[] averageValues, double[] standardDeviations) {

		// Add the latest point
		ContentValues cv = new ContentValues();
		cv.put(K_SENSOR_TIME, currentTime);
		cv.put(K_SENSOR_ID, sensorName);
		cv.put(K_SENSOR_TYPE, sensorType);
		cv.put(K_SENSOR_SAMPLES, numSamples);
		cv.put(K_SENSOR_NUM_VALS, averageValues.length);

		switch(averageValues.length) {
		
		case 1:
			cv.put(K_SENSOR_AVG_0, averageValues[0]);
			cv.put(K_SENSOR_STD_0, standardDeviations[0]);
			break;
		
		case 3:
			cv.put(K_SENSOR_AVG_0, averageValues[0]);
			cv.put(K_SENSOR_AVG_1, averageValues[1]);
			cv.put(K_SENSOR_AVG_2, averageValues[2]);
			cv.put(K_SENSOR_STD_0, standardDeviations[0]);
			cv.put(K_SENSOR_STD_1, standardDeviations[1]);
			cv.put(K_SENSOR_STD_2, standardDeviations[2]);
			break;
			
		default:
			Log.e(MODULE_TAG, "addSensorReadings failed: invalid number of values encountered");
			return;
		}

		if (-1 == mDb.insert(DATA_TABLE_SENSOR_VALUES, null, cv)) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_SENSOR_VALUES + ": failed");
		}
	}

	public Cursor fetchSensorValues(double time) {
		try {
			String[] columns = new String[] {
					K_SENSOR_ID, K_SENSOR_TYPE, K_SENSOR_SAMPLES, K_SENSOR_NUM_VALS,
					K_SENSOR_AVG_0, K_SENSOR_AVG_1, K_SENSOR_AVG_2,
					K_SENSOR_STD_0, K_SENSOR_STD_1, K_SENSOR_STD_2};
			
			Cursor cursor = mDb.query(true, DATA_TABLE_SENSOR_VALUES, columns, 
					K_SENSOR_TIME + "=" + time,
					null, null, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
			}
			return cursor;
		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
			return null;
		}
	}

	// ************************************************************************
	// *                  Shimmer Sensor table methods
	// ************************************************************************

	public void addShimmerReadings(double currentTime, String sensorId, int sensorType, int numSamples, double[] averageValues, double[] standardDeviations) {

		// Add the latest point
		ContentValues cv = new ContentValues();
		cv.put(K_SHIMMER_TIME, currentTime);
		cv.put(K_SHIMMER_ID, sensorId);
		cv.put(K_SHIMMER_TYPE, sensorType);
		cv.put(K_SHIMMER_SAMPLES, numSamples);
		cv.put(K_SHIMMER_NUM_VALS, averageValues.length);

		switch(averageValues.length) {
		
		case 1:
			cv.put(K_SHIMMER_AVG_0, averageValues[0]);
			cv.put(K_SHIMMER_STD_0, standardDeviations[0]);
			break;
		
		case 2:
			cv.put(K_SHIMMER_AVG_0, averageValues[0]);
			cv.put(K_SHIMMER_AVG_1, averageValues[1]);
			cv.put(K_SHIMMER_STD_0, standardDeviations[0]);
			cv.put(K_SHIMMER_STD_1, standardDeviations[1]);
			break;
		
		case 3:
			cv.put(K_SHIMMER_AVG_0, averageValues[0]);
			cv.put(K_SHIMMER_AVG_1, averageValues[1]);
			cv.put(K_SHIMMER_AVG_2, averageValues[2]);
			cv.put(K_SHIMMER_STD_0, standardDeviations[0]);
			cv.put(K_SHIMMER_STD_1, standardDeviations[1]);
			cv.put(K_SHIMMER_STD_2, standardDeviations[2]);
			break;
			
		default:
			Log.e(MODULE_TAG, "addShimmerReadings failed: invalid number of values encountered");
			return;
		}

		if (-1 == mDb.insert(DATA_TABLE_SHIMMER_VALUES, null, cv)) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_SHIMMER_VALUES + ": failed");
		}
	}

	public void addShimmerReadingECGData(double currentTime, String sensorId,
			ArrayList<Double> timestamps, 
			ArrayList<Double> exg1Ch1Readings, 
			ArrayList<Double> exg1Ch2Readings, 
			ArrayList<Double> exg2Ch1Readings, 
			ArrayList<Double> exg2Ch2Readings) {
		
		mDb.beginTransaction();
		try {
		
			// Add the latest point
			ContentValues cv = new ContentValues();
			
			int numItems = timestamps.size();
			if (numItems > exg1Ch1Readings.size()) numItems = exg1Ch1Readings.size(); 
			if (numItems > exg1Ch2Readings.size()) numItems = exg1Ch2Readings.size(); 
			if (numItems > exg2Ch1Readings.size()) numItems = exg2Ch1Readings.size(); 
			if (numItems > exg2Ch2Readings.size()) numItems = exg2Ch2Readings.size(); 
			
			for (int i = 0; i < numItems; ++i) {
				
				cv.put(K_SHIMMER_ECG_COORD_TIME, currentTime);
				cv.put(K_SHIMMER_ECG_SENSOR_ID, sensorId);
				cv.put(K_SHIMMER_ECG_TIMESTAMP, timestamps.get(i));
				cv.put(K_SHIMMER_ECG_EXG1_CH1, exg1Ch1Readings.get(i));
				cv.put(K_SHIMMER_ECG_EXG1_CH2, exg1Ch2Readings.get(i));
				cv.put(K_SHIMMER_ECG_EXG2_CH1, exg2Ch1Readings.get(i));
				cv.put(K_SHIMMER_ECG_EXG2_CH2, exg2Ch2Readings.get(i));
	
				if (-1 == mDb.insert(DATA_TABLE_SHIMMER_ECG_VALUES, null, cv)) {
					Log.e(MODULE_TAG, "Insert " + DATA_TABLE_SHIMMER_ECG_VALUES + ": failed");
				}
				cv.clear();
			}
			mDb.setTransactionSuccessful();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_SHIMMER_ECG_VALUES + " failed: " + ex.getMessage());
		}
		finally {
			mDb.endTransaction();
		}
	}

	public void addShimmerReadingEMGData(double currentTime, String sensorId,
		ArrayList<Double> timestamps, 
		ArrayList<Double> emg1Ch1Readings, 
		ArrayList<Double> emg1Ch2Readings) {
		
		// Add the latest point
		ContentValues cv = new ContentValues();
		
		int numItems = timestamps.size();
		if (numItems > emg1Ch1Readings.size()) numItems = emg1Ch1Readings.size(); 
		if (numItems > emg1Ch2Readings.size()) numItems = emg1Ch2Readings.size(); 
		
		for (int i = 0; i < numItems; ++i) {
			
			cv.put(K_SHIMMER_EMG_COORD_TIME, currentTime);
			cv.put(K_SHIMMER_EMG_SENSOR_ID, sensorId);
			cv.put(K_SHIMMER_EMG_TIMESTAMP, timestamps.get(i));
			cv.put(K_SHIMMER_EMG_EXG1_CH1, emg1Ch1Readings.get(i));
			cv.put(K_SHIMMER_EMG_EXG1_CH2, emg1Ch2Readings.get(i));

			if (-1 == mDb.insert(DATA_TABLE_SHIMMER_EMG_VALUES, null, cv)) {
				Log.e(MODULE_TAG, "Insert " + DATA_TABLE_SHIMMER_EMG_VALUES + ": failed");
			}
			cv.clear();
		}
	}

	private final static String[] SHIMMER_SENSOR_COLUMNS = new String[] {
			K_SHIMMER_ID, K_SHIMMER_TYPE, K_SHIMMER_SAMPLES, K_SHIMMER_NUM_VALS,
			K_SHIMMER_AVG_0, K_SHIMMER_AVG_1, K_SHIMMER_AVG_2,
			K_SHIMMER_STD_0, K_SHIMMER_STD_1, K_SHIMMER_STD_2};
	
	public Cursor fetchShimmerValues(double coordTime) {
		
		Cursor cursor = null;
		
		try {
			cursor = mDb.query(true, DATA_TABLE_SHIMMER_VALUES, SHIMMER_SENSOR_COLUMNS, 
					K_SHIMMER_TIME + "=" + coordTime,
					null, null, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.toString());
		}
		return cursor;
	}

	private static final String[] SHIMMER_ECG_COLUMNS = new String[] {
		K_SHIMMER_ECG_ID, 
		K_SHIMMER_ECG_TIMESTAMP, 
		K_SHIMMER_ECG_EXG1_CH1, 
		K_SHIMMER_ECG_EXG1_CH2, 
		K_SHIMMER_ECG_EXG2_CH1,
		K_SHIMMER_ECG_EXG2_CH2 };
	
	public Cursor fetchShimmerECGValues(double coordTime) {
		
		Cursor cursor = null;
		
		try {
			cursor = mDb.query(
					DATA_TABLE_SHIMMER_ECG_VALUES,				// Table 
					SHIMMER_ECG_COLUMNS,						// Columns
					K_SHIMMER_ECG_COORD_TIME + "=" + coordTime, // Selection
					null,										// Selection args 
					null,										// Group By
					null,										// Having
					//K_SHIMMER_ECG_ID,							// Order by
					null,										// Order by
					null);										// Limit

			if (cursor != null) {
				cursor.moveToFirst();
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.toString());
		}
		return cursor;
	}

	private static final String[] SHIMMER_EMG_COLUMNS = new String[] {
		K_SHIMMER_EMG_ID, 
		K_SHIMMER_EMG_TIMESTAMP, 
		K_SHIMMER_EMG_EXG1_CH1, 
		K_SHIMMER_EMG_EXG1_CH2 };
	
	public Cursor fetchShimmerEMGValues(double coordTime) {
		
		Cursor cursor = null;
		
		try {
			cursor = mDb.query(
					DATA_TABLE_SHIMMER_EMG_VALUES,				// Table 
					SHIMMER_EMG_COLUMNS,						// Columns
					K_SHIMMER_EMG_COORD_TIME + "=" + coordTime,	// Selection
					null,										// Selection args 
					null,										// Group By
					null,										// Having
					//K_SHIMMER_EMG_ID,							// Order by
					null,										// Order by
					null);										// Limit

			if (cursor != null) {
				cursor.moveToFirst();
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.toString());
		}
		return cursor;
	}

	// ************************************************************************
	// *                 Heart Rate Device table methods
	// ************************************************************************

	public void addHeartRateDeviceReading(double currentTime, int numSamples, double avgHeartRate, double stdHeartRate) {

		// Add the latest point
		ContentValues cv = new ContentValues();
		cv.put(K_HR_TIME, currentTime);
		cv.put(K_HR_NUM_SAMPLES, numSamples);
		cv.put(K_HR_AVG_HEART_RATE, avgHeartRate);
		cv.put(K_HR_STD_HEART_RATE, stdHeartRate);

		if (-1 == mDb.insert(DATA_TABLE_HEART_RATE, null, cv)) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_HEART_RATE + ": failed");
		}
	}

	public Cursor fetchHeartRateDeviceValue(double time) {
		try {
			String[] columns = new String[] {
					K_HR_NUM_SAMPLES, K_HR_AVG_HEART_RATE, K_HR_STD_HEART_RATE };
			
			Cursor cursor = mDb.query(true, DATA_TABLE_HEART_RATE, columns, 
					K_HR_TIME + "=" + time,
					null, null, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
			}
			return cursor;
		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
			return null;
		}
	}

	// ************************************************************************
	// *                 Bike Power Device table methods
	// ************************************************************************

	public void addBikePowerDeviceReading(double currentTime, 
			int nsCalcPower, double avgCalcPower, double stdCalcPower,
			int nsCalcTorque, double avgCalcTorque, double stdCalcTorque,
			int nsCalcCrankCadence, double avgCalcCrankCadence, double stdCalcCrankCadence,
			int nsCalcWheelSpeed, double avgCalcWheelSpeed, double stdCalcWheelSpeed,
			int nsCalcWheelDistance, double avgCalcWheelDistance, double stdCalcWheelDistance) {

		// Add the latest point
		ContentValues cv = new ContentValues();
		cv.put(K_BP_TIME, currentTime);
		
		cv.put(K_BP_CALC_POWER_NUM_SAMPLES, nsCalcPower);
		cv.put(K_BP_CALC_POWER_AVG, avgCalcPower);
		cv.put(K_BP_CALC_POWER_STD, stdCalcPower);
		
		cv.put(K_BP_CALC_TORQUE_NUM_SAMPLES, nsCalcTorque);
		cv.put(K_BP_CALC_TORQUE_AVG, avgCalcTorque);
		cv.put(K_BP_CALC_TORQUE_STD, stdCalcTorque);
		
		cv.put(K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES, nsCalcCrankCadence);
		cv.put(K_BP_CALC_CRANK_CADENCE_AVG, avgCalcCrankCadence);
		cv.put(K_BP_CALC_CRANK_CADENCE_STD, stdCalcCrankCadence);
		
		cv.put(K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES, nsCalcWheelSpeed);
		cv.put(K_BP_CALC_WHEEL_SPEED_AVG, avgCalcWheelSpeed);
		cv.put(K_BP_CALC_WHEEL_SPEED_STD, stdCalcWheelSpeed);
		
		cv.put(K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES, nsCalcWheelDistance);
		cv.put(K_BP_CALC_WHEEL_DISTANCE_AVG, avgCalcWheelDistance);
		cv.put(K_BP_CALC_WHEEL_DISTANCE_STD, stdCalcWheelDistance);

		if (-1 == mDb.insert(DATA_TABLE_BIKE_POWER, null, cv)) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_BIKE_POWER + ": failed");
		}
	}

	public Cursor fetchBikePowerDeviceValue(double time) {
		try {
			String[] columns = new String[] {
					K_BP_CALC_POWER_NUM_SAMPLES,          K_BP_CALC_POWER_AVG,          K_BP_CALC_POWER_STD, 
					K_BP_CALC_TORQUE_NUM_SAMPLES,         K_BP_CALC_TORQUE_AVG,         K_BP_CALC_TORQUE_STD, 
					K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES,  K_BP_CALC_CRANK_CADENCE_AVG,  K_BP_CALC_CRANK_CADENCE_STD, 
					K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES,    K_BP_CALC_WHEEL_SPEED_AVG,    K_BP_CALC_WHEEL_SPEED_STD, 
					K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES, K_BP_CALC_WHEEL_DISTANCE_AVG, K_BP_CALC_WHEEL_DISTANCE_STD, 
					};
			
			Cursor cursor = mDb.query(true, DATA_TABLE_BIKE_POWER, columns, 
					K_HR_TIME + "=" + time,
					null, null, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
			}
			return cursor;
		} catch (Exception e) {
			Log.e(MODULE_TAG, e.toString());
			return null;
		}
	}

	// ************************************************************************
	// *                       Trip table methods
	// ************************************************************************

	/**
	 * Create a new trip using the data provided. If the trip is successfully
	 * created return the new rowId for that trip, otherwise return a -1 to
	 * indicate failure.
	 */
	public long createTrip(String purp, double starttime, String fancystart, String note) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(K_TRIP_PURP, purp);
		initialValues.put(K_TRIP_START, starttime);
		initialValues.put(K_TRIP_FANCYSTART, fancystart);
		initialValues.put(K_TRIP_NOTE, note);
		initialValues.put(K_TRIP_STATUS, TripData.STATUS_INCOMPLETE);
		initialValues.put(K_TRIP_HAS_SENSOR_DATA, 0);
		initialValues.put(K_TRIP_HAS_ANT_DEVICE_DATA, 0);
		initialValues.put(K_TRIP_HAS_SHIMMER_DATA, 0);
		initialValues.put(K_TRIP_HAS_EPOC_DATA, 0);

		Long rowId = mDb.insert(DATA_TABLE_TRIPS, null, initialValues);

		Log.i(MODULE_TAG, "Insert " + DATA_TABLE_TRIPS + "[" + String.valueOf(rowId) +"]");

		return rowId;
	}

	public long createTrip() {
		return createTrip("", System.currentTimeMillis(), "", "");
	}

	/**
	 * Delete the trip with the given rowId
	 *
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteTrip(long rowId) {

		int numRows = mDb.delete(DATA_TABLE_TRIPS, K_TRIP_ROWID + "=" + rowId, null);

		Log.i(MODULE_TAG, "Deleted " + DATA_TABLE_TRIPS + "[" + String.valueOf(rowId) +"]: " + String.valueOf(numRows) + " rows.");

		return  numRows > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 *
	 * @return Cursor over all trips
	 */
	public Cursor fetchAllTrips() {
		Cursor c = mDb.query(DATA_TABLE_TRIPS, new String[] { K_TRIP_ROWID,
				K_TRIP_PURP, K_TRIP_START, K_TRIP_FANCYSTART, K_TRIP_NOTE,
				K_TRIP_FANCYINFO, K_TRIP_END, K_TRIP_DISTANCE, K_TRIP_STATUS },
				null, null, null, null, "-" + K_TRIP_START);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
		}
		return c;
	}

	public Cursor fetchUnsentTrips() {
		Cursor c = mDb.query(DATA_TABLE_TRIPS, new String[] { K_TRIP_ROWID },
				K_TRIP_STATUS + "=" + TripData.STATUS_COMPLETE, null, null,
				null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
		}
		return c;
	}

	public int cleanTripsCoordsTables() {
		int badTrips = 0;

		Cursor c = mDb.query(DATA_TABLE_TRIPS, new String[] { K_TRIP_ROWID,
				K_TRIP_STATUS }, K_TRIP_STATUS + "="
				+ TripData.STATUS_INCOMPLETE, null, null, null, null);

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			badTrips = c.getCount();

			while (!c.isAfterLast()) {
				long tripid = c.getInt(0);
				deleteAllCoordsForTrip(tripid);
				deletePauses(tripid);
				c.moveToNext();
			}
		}
		c.close();
		if (badTrips > 0) {
			mDb.delete(DATA_TABLE_TRIPS, K_TRIP_STATUS + "="
					+ TripData.STATUS_INCOMPLETE, null);
		}
		return badTrips;
	}

	/**
	 * Return a Cursor positioned at the trip that matches the given rowId
	 *
	 * @param rowId
	 *            id of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 * @throws SQLException
	 *             if trip could not be found/retrieved
	 */
	public Cursor fetchTrip(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATA_TABLE_TRIPS, new String[] {
				K_TRIP_ROWID, K_TRIP_PURP, K_TRIP_START, K_TRIP_FANCYSTART,
				K_TRIP_NOTE, K_TRIP_LATHI, K_TRIP_LATLO, K_TRIP_LGTHI,
				K_TRIP_LGTLO, K_TRIP_STATUS, K_TRIP_END, K_TRIP_FANCYINFO,
				K_TRIP_DISTANCE, K_TRIP_HAS_SENSOR_DATA, K_TRIP_HAS_ANT_DEVICE_DATA,
				K_TRIP_HAS_SHIMMER_DATA, K_TRIP_HAS_EPOC_DATA },

		K_TRIP_ROWID + "=" + rowId,

		null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor positioned at the trip that matches the given rowId
	 *
	 * @param rowId
	 *            id of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 * @throws SQLException
	 *             if trip could not be found/retrieved
	 */
	public Cursor fetchTripDetails(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATA_TABLE_TRIPS, new String[] {
				K_TRIP_HAS_SENSOR_DATA, K_TRIP_HAS_ANT_DEVICE_DATA,
				K_TRIP_HAS_SHIMMER_DATA, K_TRIP_HAS_EPOC_DATA },

		K_TRIP_ROWID + "=" + rowId,

		null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public boolean updateTrip(long tripid, String fancystart, String fancyinfo, String note) {

		int numRows;

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_TRIP_FANCYSTART, fancystart);
		contentValues.put(K_TRIP_FANCYINFO, fancyinfo);
		contentValues.put(K_TRIP_NOTE, note);

		numRows = mDb.update(DATA_TABLE_TRIPS, contentValues, K_TRIP_ROWID + "=" + tripid, null);

		/* Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_FANCYSTART + ", " + K_TRIP_FANCYINFO + ", " + K_TRIP_NOTE +"): "
				+ String.valueOf(numRows) + " rows.");		*/

		return numRows > 0;
	}

	public void updateTripPurpose(long tripid, String purpose) {

		int numRows;

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_TRIP_PURP, purpose);

		numRows = mDb.update(DATA_TABLE_TRIPS, contentValues, K_TRIP_ROWID + "=" + tripid, null);

		/* Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_PURP + "): " + String.valueOf(numRows) + " rows."); */

		return;
	}

	public boolean updateTrip(long tripid, int lathigh, int latlow, int lgthigh, int lgtlow, float distance) {

		int numRows;

		ContentValues contentValues = new ContentValues();

		contentValues.put(K_TRIP_LATHI, lathigh);
		contentValues.put(K_TRIP_LATLO, latlow);
		contentValues.put(K_TRIP_LGTHI, lgthigh);
		contentValues.put(K_TRIP_LGTLO, lgtlow);
		contentValues.put(K_TRIP_DISTANCE, distance);

		numRows = mDb.update(DATA_TABLE_TRIPS, contentValues, K_TRIP_ROWID + "=" + tripid, null);

		/* Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_LATHI + ", " + K_TRIP_LATLO + ", " + K_TRIP_LGTHI + ", " +
				K_TRIP_LGTLO + ", " + K_TRIP_DISTANCE +"): " + String.valueOf(numRows) + " rows."); */

		return numRows > 0;
	}

	public boolean updateTripStatus(long tripid, int tripStatus) {

		int numRows;

		ContentValues initialValues = new ContentValues();
		initialValues.put(K_TRIP_STATUS, tripStatus);

		numRows = mDb.update(DATA_TABLE_TRIPS, initialValues, K_TRIP_ROWID + "=" + tripid, null);

		/* Log.i(MODULE_TAG, "Updated " + DATA_TABLE_TRIPS + "[" + String.valueOf(tripid)
				+ "](" + K_TRIP_STATUS +"): " + String.valueOf(numRows) + " rows."); */

		return numRows > 0;
	}

	public void updateTrip(long tripId, ShimmerConfig shimmerConfig) {

		ContentValues row = new ContentValues();
		
		try {
			row.put(K_SHIMMER_CONFIG_TRIP_ID, tripId);
			row.put(K_SHIMMER_CONFIG_BLUETOOTH_ID, shimmerConfig.getBluetoothAddress());
			row.put(K_SHIMMER_CONFIG_SAMPLING_RATE, shimmerConfig.getSamplingRate());
			row.put(K_SHIMMER_CONFIG_ACCEL_RANGE, shimmerConfig.getAccelerometerRange());
			row.put(K_SHIMMER_CONFIG_GSR_RANGE, shimmerConfig.getGSRRange());
			row.put(K_SHIMMER_CONFIG_BATTERY_LIMIT, shimmerConfig.getBatteryLimit());
			row.put(K_SHIMMER_CONFIG_INT_EXP_POWER, shimmerConfig.getInternalExpPower());
			row.put(K_SHIMMER_CONFIG_LOW_PWR_MAG, shimmerConfig.isLowPowerMagEnabled());
			row.put(K_SHIMMER_CONFIG_LOW_PWR_ACCEL, shimmerConfig.isLowPowerAccelEnabled());
			row.put(K_SHIMMER_CONFIG_LOW_PWR_GYRO, shimmerConfig.isLowPowerGyroEnabled());
			row.put(K_SHIMMER_CONFIG_5V_REG, shimmerConfig.get5VReg());
			row.put(K_SHIMMER_CONFIG_GYRO_RANGE, shimmerConfig.getGyroRange());
			row.put(K_SHIMMER_CONFIG_MAG_RANGE, shimmerConfig.getMagRange());
			row.put(K_SHIMMER_CONFIG_PRESSURE_RES, shimmerConfig.getPressureResolution());
			row.put(K_SHIMMER_CONFIG_REF_ELECTRODE, shimmerConfig.getReferenceElectrode());
			row.put(K_SHIMMER_CONFIG_LEAD_OFF_DETECTION, shimmerConfig.getLeadOffDetection());
			row.put(K_SHIMMER_CONFIG_LEAD_OFF_CURRENT, shimmerConfig.getLeadOffCurrent());
			row.put(K_SHIMMER_CONFIG_LEAD_OFF_COMPARATOR, shimmerConfig.getLadOffComparator());
			row.put(K_SHIMMER_CONFIG_EXG_GAIN, shimmerConfig.getExgGain());
			row.put(K_SHIMMER_CONFIG_EXG_RES, shimmerConfig.getExgRes());
			row.put(K_SHIMMER_CONFIG_HAS_ECG_DATA, shimmerConfig.isEcgEnabled() ? 1 : 0);
			row.put(K_SHIMMER_CONFIG_HAS_EMG_DATA, shimmerConfig.isEmgEnabled() ? 1 : 0);

			if (-1 == mDb.insert(DATA_TABLE_SHIMMER_CONFIG, null, row)) {
				Log.e(MODULE_TAG, "failed to insert row into DATA_TABLE_SHIMMER_CONFIG");
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	public void updateTrip(long tripId, boolean hasSensorData, boolean hasAntDeviceData, boolean hasShimmerData, boolean hasEpocData) {

		ContentValues vals = new ContentValues();
		
		vals.put(K_TRIP_HAS_SENSOR_DATA,     hasSensorData    ? 1 : 0);
		vals.put(K_TRIP_HAS_ANT_DEVICE_DATA, hasAntDeviceData ? 1 : 0);
		vals.put(K_TRIP_HAS_SHIMMER_DATA,    hasShimmerData   ? 1 : 0);
		vals.put(K_TRIP_HAS_EPOC_DATA,       hasEpocData      ? 1 : 0);

		if (1 != mDb.update(DATA_TABLE_TRIPS, vals, K_TRIP_ROWID + "=" + tripId, null)) {
			Log.e(MODULE_TAG, "failed to update row in DATA_TABLE_TRIPS");
		}
	}

	/**
	 * Return a Cursor positioned at the shimmer config that matches the given rowId
	 *
	 * @param tripId
	 *            id of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 */
	public Cursor fetchShimmerConfigs(long tripId) {

		Cursor cursor = null;
		final String[] columns = new String[] { 
				K_SHIMMER_CONFIG_BLUETOOTH_ID, 
				K_SHIMMER_CONFIG_SAMPLING_RATE, 
				K_SHIMMER_CONFIG_ACCEL_RANGE, 
				K_SHIMMER_CONFIG_GSR_RANGE, 
				K_SHIMMER_CONFIG_BATTERY_LIMIT, 
				K_SHIMMER_CONFIG_INT_EXP_POWER, 
				K_SHIMMER_CONFIG_LOW_PWR_MAG, 
				K_SHIMMER_CONFIG_LOW_PWR_ACCEL, 
				K_SHIMMER_CONFIG_LOW_PWR_GYRO, 
				K_SHIMMER_CONFIG_5V_REG, 
				K_SHIMMER_CONFIG_GYRO_RANGE, 
				K_SHIMMER_CONFIG_MAG_RANGE, 
				K_SHIMMER_CONFIG_PRESSURE_RES, 
				K_SHIMMER_CONFIG_REF_ELECTRODE, 
				K_SHIMMER_CONFIG_LEAD_OFF_DETECTION, 
				K_SHIMMER_CONFIG_LEAD_OFF_CURRENT, 
				K_SHIMMER_CONFIG_LEAD_OFF_COMPARATOR, 
				K_SHIMMER_CONFIG_EXG_GAIN, 
				K_SHIMMER_CONFIG_EXG_RES,
				K_SHIMMER_CONFIG_HAS_ECG_DATA,
				K_SHIMMER_CONFIG_HAS_EMG_DATA };

		try {
			String whereClause = K_SHIMMER_CONFIG_TRIP_ID + "=" + tripId;
	
			if (null != (cursor = mDb.query(true, DATA_TABLE_SHIMMER_CONFIG, columns, whereClause, 
					null, null, null, null, null))) {
				cursor.moveToFirst();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return cursor;
	}
	
	// ************************************************************************
	// *                       Pauses table methods
	// ************************************************************************

	/**
	 * Insert a row into the 'pauses' table
	 * @param tripId Trip ID of associated trip
	 * @param startTime Starting date-time of the pause
	 * @param endTime Ending date-time of the pause
	 * @throws SQLException
	 */
	public void addPauseToTrip(long tripId, double startTime, double endTime) throws SQLException{

		// Assemble row data
		ContentValues rowValues = new ContentValues();
		rowValues.put(K_PAUSE_TRIP_ID, tripId);
		rowValues.put(K_PAUSE_START_TIME, startTime);
		rowValues.put(K_PAUSE_END_TIME, endTime);

		// Insert row in table
		mDb.insertOrThrow(DATA_TABLE_PAUSES, null, rowValues);
	}

	/**
	 * Delete pauses with the given trip ID
	 * @param tripId id of the pauses to delete
	 */
	public void deletePauses(long tripId) {
		mDb.delete(DATA_TABLE_PAUSES, K_PAUSE_TRIP_ID + "=" + tripId, null);
	}

	/**
	 * Return a Cursor positioned at the pause that matches the given rowId
	 *
	 * @param rowId
	 *            id of trip to retrieve
	 * @return Cursor positioned to matching trip, if found
	 * @throws SQLException
	 *             if trip could not be found/retrieved
	 */
	public Cursor fetchPauses(long tripId) throws SQLException {

		Cursor cursor;

		String[] columns = new String[] { K_PAUSE_START_TIME, K_PAUSE_END_TIME };
		String whereClause = K_PAUSE_TRIP_ID + "=" + tripId;

		if (null != (cursor = mDb.query(true, DATA_TABLE_PAUSES, columns, whereClause, null, null, null, null, null))) {
			cursor.moveToFirst();
		}

		return cursor;
	}
}
