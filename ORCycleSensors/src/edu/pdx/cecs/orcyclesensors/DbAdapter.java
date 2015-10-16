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
	
	private static final int DATABASE_VERSION = DATABASE_VERSION_BIKE_POWER_TABLE;

	// Table names
	private static final String DATABASE_NAME = "data";
	private static final String DATA_TABLE_TRIPS = "trips";
	private static final String DATA_TABLE_COORDS = "coords";
	private static final String DATA_TABLE_PAUSES = "pauses";
	private static final String DATA_TABLE_SENSOR_VALUES = "sensor_values";
	private static final String DATA_TABLE_HEART_RATE = "heart_rate";
	private static final String DATA_TABLE_BIKE_POWER = "bike_power";

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
	public static final String K_SENSOR_SSD_0 = "ssd0";
	public static final String K_SENSOR_SSD_1 = "ssd1";
	public static final String K_SENSOR_SSD_2 = "ssd2";

	// Heart rate table columns
	public static final String K_HR_TIME = "time";
	public static final String K_HR_NUM_SAMPLES = "samples";
	public static final String K_HR_AVG_HEART_RATE = "avg_heart_rate";
	public static final String K_HR_SSD_HEART_RATE = "ssd_heart_rate";

	// Bike power table columns
	public static final String K_BP_TIME = "time";
	public static final String K_BP_CALC_POWER_NUM_SAMPLES = "calc_power_samples";
	public static final String K_BP_CALC_POWER_AVG = "calc_power_avg";
	public static final String K_BP_CALC_POWER_SSD = "calc_power_ssd";
	public static final String K_BP_CALC_TORQUE_NUM_SAMPLES = "calc_torque_samples";
	public static final String K_BP_CALC_TORQUE_AVG = "calc_torque_avg";
	public static final String K_BP_CALC_TORQUE_SSD = "calc_torque_ssd";
	public static final String K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES = "calc_crank_cadence_samples";
	public static final String K_BP_CALC_CRANK_CADENCE_AVG = "calc_crank_cadence_avg";
	public static final String K_BP_CALC_CRANK_CADENCE_SSD = "calc_crank_cadence_ssd";
	public static final String K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES = "calc_wheel_speed_samples";
	public static final String K_BP_CALC_WHEEL_SPEED_AVG = "calc_wheel_speed_avg";
	public static final String K_BP_CALC_WHEEL_SPEED_SSD = "calc_wheel_speed_ssd";
	public static final String K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES = "calc_wheel_distance_samples";
	public static final String K_BP_CALC_WHEEL_DISTANCE_AVG = "calc_wheel_distance_avg";
	public static final String K_BP_CALC_WHEEL_DISTANCE_SSD = "calc_wheel_distance_ssd";
	
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
			+ K_SENSOR_SSD_0  + " double, "
			+ K_SENSOR_SSD_1  + " double, "
			+ K_SENSOR_SSD_2  + " double, "
			+ "PRIMARY KEY(" + K_SENSOR_TIME + ", " + K_SENSOR_ID + "));";

	private static final String TABLE_CREATE_HEART_RATE = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_HEART_RATE + " ("
			+ K_HR_TIME           + " double, "
			+ K_HR_NUM_SAMPLES    + " integer, "
			+ K_HR_AVG_HEART_RATE + " double, "
			+ K_HR_SSD_HEART_RATE + " double, "
			+ "PRIMARY KEY(" + K_HR_TIME + "));";
	
	private static final String TABLE_CREATE_BIKE_POWER = SQL_CREATE_TABLE_CMD + " " + DATA_TABLE_BIKE_POWER + " ("
			+ K_BP_TIME                   + " double, "
			+ K_BP_CALC_POWER_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_POWER_AVG         + " double, "
			+ K_BP_CALC_POWER_SSD         + " double, "
			+ K_BP_CALC_TORQUE_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_TORQUE_AVG         + " double, "
			+ K_BP_CALC_TORQUE_SSD         + " double, "
			+ K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_CRANK_CADENCE_AVG         + " double, "
			+ K_BP_CALC_CRANK_CADENCE_SSD         + " double, "
			+ K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_WHEEL_SPEED_AVG         + " double, "
			+ K_BP_CALC_WHEEL_SPEED_SSD         + " double, "
			+ K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES + " integer, "
			+ K_BP_CALC_WHEEL_DISTANCE_AVG         + " double, "
			+ K_BP_CALC_WHEEL_DISTANCE_SSD         + " double, "
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

	public void addSensorReadings(double currentTime, String sensorName, int sensorType, int numSamples, float[] averageValues, float[] sumSquareDifferences) {

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
			cv.put(K_SENSOR_SSD_0, sumSquareDifferences[0]);
			break;
		
		case 3:
			cv.put(K_SENSOR_AVG_0, averageValues[0]);
			cv.put(K_SENSOR_AVG_1, averageValues[1]);
			cv.put(K_SENSOR_AVG_2, averageValues[2]);
			cv.put(K_SENSOR_SSD_0, sumSquareDifferences[0]);
			cv.put(K_SENSOR_SSD_1, sumSquareDifferences[1]);
			cv.put(K_SENSOR_SSD_2, sumSquareDifferences[2]);
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
					K_SENSOR_SSD_0, K_SENSOR_SSD_1, K_SENSOR_SSD_2};
			
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
	// *                 Heart Rate Device table methods
	// ************************************************************************

	public void addHeartRateDeviceReading(double currentTime, int numSamples, double avgHeartRate, double ssdHeartRate) {

		// Add the latest point
		ContentValues cv = new ContentValues();
		cv.put(K_HR_TIME, currentTime);
		cv.put(K_HR_NUM_SAMPLES, numSamples);
		cv.put(K_HR_AVG_HEART_RATE, avgHeartRate);
		cv.put(K_HR_SSD_HEART_RATE, ssdHeartRate);

		if (-1 == mDb.insert(DATA_TABLE_HEART_RATE, null, cv)) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_HEART_RATE + ": failed");
		}
	}

	public Cursor fetchHeartRateDeviceValue(double time) {
		try {
			String[] columns = new String[] {
					K_HR_NUM_SAMPLES, K_HR_AVG_HEART_RATE, K_HR_SSD_HEART_RATE };
			
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
			int nsCalcPower, double avgCalcPower, double ssdCalcPower,
			int nsCalcTorque, double avgCalcTorque, double ssdCalcTorque,
			int nsCalcCrankCadence, double avgCalcCrankCadence, double ssdCalcCrankCadence,
			int nsCalcWheelSpeed, double avgCalcWheelSpeed, double ssdCalcWheelSpeed,
			int nsCalcWheelDistance, double avgCalcWheelDistance, double ssdCalcWheelDistance) {

		// Add the latest point
		ContentValues cv = new ContentValues();
		cv.put(K_BP_TIME, currentTime);
		
		cv.put(K_BP_CALC_POWER_NUM_SAMPLES, nsCalcPower);
		cv.put(K_BP_CALC_POWER_AVG, avgCalcPower);
		cv.put(K_BP_CALC_POWER_SSD, ssdCalcPower);
		
		cv.put(K_BP_CALC_TORQUE_NUM_SAMPLES, nsCalcTorque);
		cv.put(K_BP_CALC_TORQUE_AVG, avgCalcTorque);
		cv.put(K_BP_CALC_TORQUE_SSD, ssdCalcTorque);
		
		cv.put(K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES, nsCalcCrankCadence);
		cv.put(K_BP_CALC_CRANK_CADENCE_AVG, avgCalcCrankCadence);
		cv.put(K_BP_CALC_CRANK_CADENCE_SSD, ssdCalcCrankCadence);
		
		cv.put(K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES, nsCalcWheelSpeed);
		cv.put(K_BP_CALC_WHEEL_SPEED_AVG, avgCalcWheelSpeed);
		cv.put(K_BP_CALC_WHEEL_SPEED_SSD, ssdCalcWheelSpeed);
		
		cv.put(K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES, nsCalcWheelDistance);
		cv.put(K_BP_CALC_WHEEL_DISTANCE_AVG, avgCalcWheelDistance);
		cv.put(K_BP_CALC_WHEEL_DISTANCE_SSD, ssdCalcWheelDistance);

		if (-1 == mDb.insert(DATA_TABLE_BIKE_POWER, null, cv)) {
			Log.e(MODULE_TAG, "Insert " + DATA_TABLE_BIKE_POWER + ": failed");
		}
	}

	public Cursor fetchBikePowerDeviceValue(double time) {
		try {
			String[] columns = new String[] {
					K_BP_CALC_POWER_NUM_SAMPLES,          K_BP_CALC_POWER_AVG,          K_BP_CALC_POWER_SSD, 
					K_BP_CALC_TORQUE_NUM_SAMPLES,         K_BP_CALC_TORQUE_AVG,         K_BP_CALC_TORQUE_SSD, 
					K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES,  K_BP_CALC_CRANK_CADENCE_AVG,  K_BP_CALC_CRANK_CADENCE_SSD, 
					K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES,    K_BP_CALC_WHEEL_SPEED_AVG,    K_BP_CALC_WHEEL_SPEED_SSD, 
					K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES, K_BP_CALC_WHEEL_DISTANCE_AVG, K_BP_CALC_WHEEL_DISTANCE_SSD, 
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
				K_TRIP_DISTANCE },

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
