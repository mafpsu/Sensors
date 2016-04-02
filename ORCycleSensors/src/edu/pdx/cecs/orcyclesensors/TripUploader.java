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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class TripUploader extends AsyncTask<Long, Integer, Boolean> {

	private static final String MODULE_TAG = "TripUploader";
	public static final int kSaveProtocolVersion = 3;
	private static final String POST_URL = "http://bsensor.cecs.pdx.edu/post/";

	// Saving protocol version 3
	public static final String TRIP_COORDS_TIME = "r";
	public static final String TRIP_COORDS_LAT = "l";
	public static final String TRIP_COORDS_LON = "n";
	public static final String TRIP_COORDS_ALT = "a";
	public static final String TRIP_COORDS_SPEED = "s";
	public static final String TRIP_COORDS_HACCURACY = "h";
	public static final String TRIP_COORDS_VACCURACY = "v";
	public static final String TRIP_COORDS_SENSOR_READINGS = "sr";
	public static final String TRIP_COORDS_SHIMMER_READINGS = "shr";
	public static final String TRIP_COORDS_ANT_DEVICE_PREFIX = "ant_";

	public static final String PAUSE_START = "ps";
	public static final String PAUSE_END = "pe";

	public static final String TRIP_COORD_SENSOR_ID = "s_id";
	public static final String TRIP_COORD_SENSOR_TYPE = "s_t";
	public static final String TRIP_COORD_SENSOR_SAMPLES = "s_ns";
	public static final String TRIP_COORD_SENSOR_NUM_VALS = "s_nv";
	public static final String TRIP_COORD_SENSOR_AVG_0 = "s_a0";
	public static final String TRIP_COORD_SENSOR_AVG_1 = "s_a1";
	public static final String TRIP_COORD_SENSOR_AVG_2 = "s_a2";
	public static final String TRIP_COORD_SENSOR_STD_0 = "s_s0";
	public static final String TRIP_COORD_SENSOR_STD_1 = "s_s1";
	public static final String TRIP_COORD_SENSOR_STD_2 = "s_s2";
	
	public static final String TRIP_COORD_SHIMMER_ID = "sh_id";
	public static final String TRIP_COORD_SHIMMER_TYPE = "sh_t";
	public static final String TRIP_COORD_SHIMMER_SAMPLES = "sh_ns";
	public static final String TRIP_COORD_SHIMMER_NUM_VALS = "sh_nv";
	public static final String TRIP_COORD_SHIMMER_AVG_0 = "sh_a0";
	public static final String TRIP_COORD_SHIMMER_AVG_1 = "sh_a1";
	public static final String TRIP_COORD_SHIMMER_AVG_2 = "sh_a2";
	public static final String TRIP_COORD_SHIMMER_STD_0 = "sh_s0";
	public static final String TRIP_COORD_SHIMMER_STD_1 = "sh_s1";
	public static final String TRIP_COORD_SHIMMER_STD_2 = "sh_s2";
	
	public static final String TRIP_COORD_SHIMMER_ECG = "sh_ecg";
	public static final String TRIP_COORD_SHIMMER_ECG_TIMESTAMP = "shc_t";
	public static final String TRIP_COORD_SHIMMER_ECG_EXG1_CH1 = "shc_11";
	public static final String TRIP_COORD_SHIMMER_ECG_EXG1_CH2 = "shc_12";
	public static final String TRIP_COORD_SHIMMER_ECG_EXG2_CH1 = "shc_21";
	public static final String TRIP_COORD_SHIMMER_ECG_EXG2_CH2 = "shc_22";

	public static final String TRIP_COORD_SHIMMER_EMG = "sh_emg";
	public static final String TRIP_COORD_SHIMMER_EMG_TIMESTAMP = "shm_t";
	public static final String TRIP_COORD_SHIMMER_EMG_EXG1_CH1 = "shm_11";
	public static final String TRIP_COORD_SHIMMER_EMG_EXG1_CH2 = "shm_12";
	public static final String TRIP_COORD_SHIMMER_EMG_EXG2_CH1 = "shm_21";
	public static final String TRIP_COORD_SHIMMER_EMG_EXG2_CH2 = "shm_22";

	public static final String TRIP_COORD_HR_SAMPLES = "hr_ns";
	public static final String TRIP_COORD_HR_AVG_HEART_RATE = "hr_avg";
	public static final String TRIP_COORD_HR_STD_HEART_RATE = "hr_ssd";

	public static final String TRIP_COORD_BP_NS_CALC_POWER  = "bp_cp_ns";
	public static final String TRIP_COORD_BP_AVG_CALC_POWER = "bp_cp_avg";
	public static final String TRIP_COORD_BP_STD_CALC_POWER = "bp_cp_ssd";

	public static final String TRIP_COORD_BP_NS_CALC_TORQUE  = "bp_ct_ns";
	public static final String TRIP_COORD_BP_AVG_CALC_TORQUE = "bp_ct_avg";
	public static final String TRIP_COORD_BP_STD_CALC_TORQUE = "bp_ct_ssd";

	public static final String TRIP_COORD_BP_NS_CALC_CRANK_CADENCE  = "bp_ccc_ns";
	public static final String TRIP_COORD_BP_AVG_CALC_CRANK_CADENCE = "bp_ccc_avg";
	public static final String TRIP_COORD_BP_STD_CALC_CRANK_CADENCE = "bp_ccc_ssd";

	public static final String TRIP_COORD_BP_NS_CALC_WHEEL_SPEED  = "bp_cws_ns";
	public static final String TRIP_COORD_BP_AVG_CALC_WHEEL_SPEED = "bp_cws_avg";
	public static final String TRIP_COORD_BP_STD_CALC_WHEEL_SPEED = "bp_cws_ssd";

	public static final String TRIP_COORD_BP_NS_CALC_WHEEL_DISTANCE  = "bp_cwd_ns";
	public static final String TRIP_COORD_BP_AVG_CALC_WHEEL_DISTANCE = "bp_cwd_avg";
	public static final String TRIP_COORD_BP_STD_CALC_WHEEL_DISTANCE = "bp_cwd_ssd";

	private final Context mCtx;
	private final String userId;
	private final DbAdapter mDb;
	
	private boolean hasSensorData;
	private boolean hasAntDeviceData;
	private boolean hasShimmerData;
	private boolean hasEpocData;
	private boolean hasEcgData;
	private boolean hasEmgData;

	private Map<String, Integer> sensorColumn = null;

	public TripUploader(Context ctx, String userId) {
		super();
		this.mCtx = ctx;
		this.userId = userId;
		this.mDb = new DbAdapter(this.mCtx);
	}

	// Column values from coords table
	private boolean gotCoordsColIndexes = false;
	private int colCoordsTime;
	private int colCoordsLat;
	private int colCoordsLng;
	private int colCoordsAlt;
	private int colCoordsSpeed;
	private int colCoordsHAcc;
	private int colCoordsVAcc;

	/**
	 * Get all sensor readings corresponding to this time index
	 * @return
	 */
	private JSONArray getJsonSensorReadings(double coordTime) {
		JSONArray jsonSensorReadings = null;
		JSONObject jsonSensorReading;
		int numVals;
		Cursor cursorSV = null;
		
		try {
			if (null != (cursorSV = mDb.fetchSensorValues(coordTime))) {
				
				// Collect sensor readings into a json object
				jsonSensorReadings = new JSONArray();
				
				// Construct a map between cursor column index
				if (null == sensorColumn) {
					sensorColumn = new HashMap<String, Integer>();
					sensorColumn.put(TRIP_COORD_SENSOR_ID,       cursorSV.getColumnIndex(DbAdapter.K_SENSOR_ID));
					sensorColumn.put(TRIP_COORD_SENSOR_TYPE,     cursorSV.getColumnIndex(DbAdapter.K_SENSOR_TYPE));
					sensorColumn.put(TRIP_COORD_SENSOR_SAMPLES,  cursorSV.getColumnIndex(DbAdapter.K_SENSOR_SAMPLES));
					sensorColumn.put(TRIP_COORD_SENSOR_NUM_VALS, cursorSV.getColumnIndex(DbAdapter.K_SENSOR_NUM_VALS));
					sensorColumn.put(TRIP_COORD_SENSOR_AVG_0,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_AVG_0));
					sensorColumn.put(TRIP_COORD_SENSOR_AVG_1,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_AVG_1));
					sensorColumn.put(TRIP_COORD_SENSOR_AVG_2,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_AVG_2));
					sensorColumn.put(TRIP_COORD_SENSOR_STD_0,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_STD_0));
					sensorColumn.put(TRIP_COORD_SENSOR_STD_1,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_STD_1));
					sensorColumn.put(TRIP_COORD_SENSOR_STD_2,    cursorSV.getColumnIndex(DbAdapter.K_SENSOR_STD_2));
				}
	
				while (!cursorSV.isAfterLast()) {
					
					jsonSensorReading = new JSONObject();
					jsonSensorReading.put(TRIP_COORD_SENSOR_ID,      cursorSV.getString(sensorColumn.get(TRIP_COORD_SENSOR_ID)));
					jsonSensorReading.put(TRIP_COORD_SENSOR_TYPE,    cursorSV.getInt   (sensorColumn.get(TRIP_COORD_SENSOR_TYPE)));
					jsonSensorReading.put(TRIP_COORD_SENSOR_SAMPLES, cursorSV.getInt   (sensorColumn.get(TRIP_COORD_SENSOR_SAMPLES)));
					
					numVals = cursorSV.getInt(sensorColumn.get(TRIP_COORD_SENSOR_NUM_VALS));
	
					switch(numVals) {
					case 1:
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_0)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_STD_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_STD_0)));
						break;
					case 3:
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_0)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_1, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_1)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_AVG_2, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_AVG_2)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_STD_0, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_STD_0)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_STD_1, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_STD_1)));
						jsonSensorReading.put(TRIP_COORD_SENSOR_STD_2, cursorSV.getDouble(sensorColumn.get(TRIP_COORD_SENSOR_STD_2)));
						break;
					}
	
					jsonSensorReadings.put(jsonSensorReading);
					cursorSV.moveToNext();
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != cursorSV) {
				cursorSV.close();
			}
		}
		return jsonSensorReadings;
	}
	
	private boolean gotShimmerColIndexes = false;
	private int colShimmerId;
	private int colShimmerType;
	private int colShimmerSamples;
	private int colShimmerNumVals;
	private int colShimmerAvg0;
	private int colShimmerAvg1;
	private int colShimmerAvg2;
	private int colShimmerStd0;
	private int colShimmerStd1;
	private int colShimmerStd2;
	
	/**
	 * Get all shimmer sensor readings corresponding to this time index
	 * @return
	 */
	private void addJsonShimmerReadings(StringBuilder jsonShimmerReadings, double coordTime) {
		int numVals;
		Cursor cursorSV = null;
		int shimmerType;
		int sdp;
		String ecgSensorId;
		
		try {
			if (null != (cursorSV = mDb.fetchShimmerValues(coordTime))) {

				// Construct a map between cursor column index
				if (!gotShimmerColIndexes) {
					colShimmerId      = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_ID);
					colShimmerType    = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_TYPE);
					colShimmerSamples = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_SAMPLES);
					colShimmerNumVals = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_NUM_VALS);
					colShimmerAvg0    = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_AVG_0);
					colShimmerAvg1    = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_AVG_1);
					colShimmerAvg2    = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_AVG_2);
					colShimmerStd0    = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_STD_0);
					colShimmerStd1    = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_STD_1);
					colShimmerStd2    = cursorSV.getColumnIndex(DbAdapter.K_SHIMMER_STD_2);
					gotShimmerColIndexes = true;
				}
	
				jsonShimmerReadings.append(",\"shr\":[");

				while (!cursorSV.isAfterLast()) {
					
					ecgSensorId = cursorSV.getString(colShimmerId);
					shimmerType = cursorSV.getInt(colShimmerType);
					
					jsonShimmerReadings.append("{\"sh_id\":\"");
					jsonShimmerReadings.append(ecgSensorId);
					jsonShimmerReadings.append("\",\"sh_t\":");
					jsonShimmerReadings.append(shimmerType);
					jsonShimmerReadings.append(",\"sh_ns\":");
					jsonShimmerReadings.append(cursorSV.getInt(colShimmerSamples));

					numVals = cursorSV.getInt(colShimmerNumVals);
					
					sdp = getSDP(shimmerType);
	
					switch(numVals) {

					case 1:
						
						jsonShimmerReadings.append(",\"sh_a0\":");
						jsonShimmerReadings.append(cursorSV.getDouble(colShimmerAvg0));
						jsonShimmerReadings.append(",\"sh_s0\":");
						jsonShimmerReadings.append(MyMath.rnd(cursorSV.getDouble(colShimmerStd0), sdp));
						break;

					case 2:
					
						jsonShimmerReadings.append(",\"sh_a0\":");
						jsonShimmerReadings.append(cursorSV.getDouble(colShimmerAvg0));
						jsonShimmerReadings.append(",\"sh_a1\":");
						jsonShimmerReadings.append(cursorSV.getDouble(colShimmerAvg1));
						
						jsonShimmerReadings.append(",\"sh_s0\":");
						jsonShimmerReadings.append(MyMath.rnd(cursorSV.getDouble(colShimmerStd0), sdp));
						jsonShimmerReadings.append(",\"sh_s1\":");
						jsonShimmerReadings.append(MyMath.rnd(cursorSV.getDouble(colShimmerStd1), sdp));
						break;

					case 3:

						jsonShimmerReadings.append(",\"sh_a0\":");
						jsonShimmerReadings.append(cursorSV.getDouble(colShimmerAvg0));
						jsonShimmerReadings.append(",\"sh_a1\":");
						jsonShimmerReadings.append(cursorSV.getDouble(colShimmerAvg1));
						jsonShimmerReadings.append(",\"sh_a2\":");
						jsonShimmerReadings.append(cursorSV.getDouble(colShimmerAvg2));

						jsonShimmerReadings.append(",\"sh_s0\":");
						jsonShimmerReadings.append(MyMath.rnd(cursorSV.getDouble(colShimmerStd0), sdp));
						jsonShimmerReadings.append(",\"sh_s1\":");
						jsonShimmerReadings.append(MyMath.rnd(cursorSV.getDouble(colShimmerStd1), sdp));
						jsonShimmerReadings.append(",\"sh_s2\":");
						jsonShimmerReadings.append(MyMath.rnd(cursorSV.getDouble(colShimmerStd2), sdp));
						break;
					}
	
					if (hasEcgData) {
						addJsonShimmerECGReadings(jsonShimmerReadings, coordTime, ecgSensorId);
					}
					else if (hasEmgData) {
						addJsonShimmerEMGReadings(jsonShimmerReadings, coordTime, ecgSensorId);
					}

					jsonShimmerReadings.append("}"); // End of shimmer reading
					
					//jsonShimmerReadings.put(jsonShimmerReading);
					//jsonShimmerReadings.append(jsonShimmerReading);
					
					if (cursorSV.moveToNext()) {
						jsonShimmerReadings.append(",");
					}
				}
				jsonShimmerReadings.append("]"); // end of array of shimmer devices "sh_ecg":[]
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != cursorSV) {
				cursorSV.close();
			}
		}
	}

	private boolean gotEcgColIndexes = false;
	private int colEcgTimestamp;
	private int colEcg1Ch1;
	private int colEcg1Ch2;
	private int colEcg2Ch1;
	private int colEcg2Ch2;
	
	/**
	 * Get all shimmer ECG readings corresponding to this time index
	 * @return
	 */
	private void addJsonShimmerECGReadings(StringBuilder sbECG, double coordTime, String ecgSensorId) {
		Cursor cursorECG = null;
		
		try {
			if (null != (cursorECG = mDb.fetchShimmerECGValues(coordTime, ecgSensorId))) {

				if (!gotEcgColIndexes) {
					// Get column indexes
					colEcgTimestamp = cursorECG.getColumnIndex(DbAdapter.K_SHIMMER_ECG_TIMESTAMP);
					colEcg1Ch1 = cursorECG.getColumnIndex(DbAdapter.K_SHIMMER_ECG_EXG1_CH1);
					colEcg1Ch2 = cursorECG.getColumnIndex(DbAdapter.K_SHIMMER_ECG_EXG1_CH2);
					colEcg2Ch1 = cursorECG.getColumnIndex(DbAdapter.K_SHIMMER_ECG_EXG2_CH1);
					colEcg2Ch2 = cursorECG.getColumnIndex(DbAdapter.K_SHIMMER_ECG_EXG2_CH2);
					gotEcgColIndexes = true;
				}
				
				if ((colEcg1Ch1 < 0) || (colEcg1Ch2 < 0) || (colEcg2Ch1 < 0) || (colEcg2Ch2 < 0))
					return;
	
				if (cursorECG.getCount() > 0) {
				
					sbECG.append(",\"sh_ecg\":[");
				
					while (!cursorECG.isAfterLast()) {
						sbECG.append("{\"shc_t\":");
						sbECG.append(cursorECG.getDouble(colEcgTimestamp));
						sbECG.append(",\"shc_11\":");
						sbECG.append(cursorECG.getDouble(colEcg1Ch1));
						sbECG.append(",\"shc_12\":");
						sbECG.append(cursorECG.getDouble(colEcg1Ch2));
						sbECG.append(",\"shc_21\":");
						sbECG.append(cursorECG.getDouble(colEcg2Ch1));
						sbECG.append(",\"shc_22\":");
						sbECG.append(cursorECG.getDouble(colEcg2Ch2));
	
						if (cursorECG.moveToNext()) {
							sbECG.append("},");
						}
						else {
							sbECG.append("}]");
						}
					}
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != cursorECG) {
				cursorECG.close();
			}
		}
	}

	private boolean gotEmgColIndexes = false;
	private int colEmgTimestamp;
	private int colEmg1Ch1;
	private int colEmg1Ch2;
	
	/**
	 * Get all shimmer EMG readings corresponding to this time index
	 * @return
	 */
	private void addJsonShimmerEMGReadings(StringBuilder sbEMG, double coordTime, String ecgSensorId) {
		
		Cursor cursorEMG = null;

		try {
			if (null != (cursorEMG = mDb.fetchShimmerEMGValues(coordTime, ecgSensorId))) {

				if (!gotEmgColIndexes) {
					// Get column indexes
					colEmgTimestamp = cursorEMG.getColumnIndex(DbAdapter.K_SHIMMER_EMG_TIMESTAMP);
					colEmg1Ch1 = cursorEMG.getColumnIndex(DbAdapter.K_SHIMMER_EMG_EXG1_CH1);
					colEmg1Ch2 = cursorEMG.getColumnIndex(DbAdapter.K_SHIMMER_EMG_EXG1_CH2);
					gotEmgColIndexes = true;
				}
	
				if (cursorEMG.getCount() > 0) {

					sbEMG.append(",\"sh_emg\":[");
					
					while (!cursorEMG.isAfterLast()) {
						
						sbEMG.append("{\"shm_t\":");
						sbEMG.append(cursorEMG.getDouble(colEmgTimestamp));
						sbEMG.append(",\"shm_11\":");
						sbEMG.append(cursorEMG.getDouble(colEmg1Ch1));
						sbEMG.append(",\"shm_12\":");
						sbEMG.append(cursorEMG.getDouble(colEmg1Ch2));
	
						if (cursorEMG.moveToNext()) {
							sbEMG.append("},");
						}
						else {
							sbEMG.append("}]");
						}
					}
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != cursorEMG) {
				cursorEMG.close();
			}
		}
	}
	
	/**
	 * Returns the number of decimal places to round standard 
	 * deviation to for the given shimmer sensor type
	 * @param ShimmerType
	 * @return
	 */
	private int getSDP(int shimmerType) {
		switch(shimmerType) {
		case ShimmerFormat.SHIMMER2_ACCELEROMETER: return 4;
		case ShimmerFormat.SHIMMER2_GYROSCOPE: return 4;
		case ShimmerFormat.SHIMMER2_MAGNETOMETER: return 3;
		case ShimmerFormat.SHIMMER2_GSR: return 4;
		case ShimmerFormat.SHIMMER2_ECG: return 4;
		case ShimmerFormat.SHIMMER2_EMG: return 4;
		case ShimmerFormat.SHIMMER2_BRIDGE_AMPLIFIER: return 4;
		case ShimmerFormat.SHIMMER2_HEART_RATE: return 4;
		case ShimmerFormat.SHIMMER2_EXP_BOARD_A0: return 4;
		case ShimmerFormat.SHIMMER2_EXP_BOARD_A7: return 4;
		case ShimmerFormat.SHIMMER2_BATTERY: return 4;
		
		case ShimmerFormat.SHIMMER3_LOW_NOISE_ACCELEROMETER: return 4;
		case ShimmerFormat.SHIMMER3_WIDE_RANGE_ACCELEROMETER: return 4;
		case ShimmerFormat.SHIMMER3_GYROSCOPE: return 4;
		case ShimmerFormat.SHIMMER3_MAGNETOMETER: return 3;
		case ShimmerFormat.SHIMMER3_BATTERY: return 2;
		case ShimmerFormat.SHIMMER3_EXT_ADC_A6: return 4;
		case ShimmerFormat.SHIMMER3_EXT_ADC_A7: return 4;
		case ShimmerFormat.SHIMMER3_EXT_ADC_A15: return 4;
		case ShimmerFormat.SHIMMER3_INT_ADC_1: return 4;
		case ShimmerFormat.SHIMMER3_INT_ADC_12: return 4;
		case ShimmerFormat.SHIMMER3_INT_ADC_13: return 4;
		case ShimmerFormat.SHIMMER3_INT_ADC_14: return 4;
		case ShimmerFormat.SHIMMER3_PRESSURE: return 3;
		case ShimmerFormat.SHIMMER3_GSR: return 4;
		case ShimmerFormat.SHIMMER3_EXG1_24: return 4;
		case ShimmerFormat.SHIMMER3_EXG1_ECG_24: return 4;
		case ShimmerFormat.SHIMMER3_EXG1_EMG_24: return 4;
		case ShimmerFormat.SHIMMER3_EXG2_24: return 4;
		case ShimmerFormat.SHIMMER3_EXG2_ECG_24: return 4;
		case ShimmerFormat.SHIMMER3_EXG2_EMG_24: return 4;
		case ShimmerFormat.SHIMMER3_EXG1_16: return 4;
		case ShimmerFormat.SHIMMER3_EXG1_ECG_16: return 4;
		case ShimmerFormat.SHIMMER3_EXG1_EMG_16: return 4;
		case ShimmerFormat.SHIMMER3_EXG2_16: return 4;
		case ShimmerFormat.SHIMMER3_EXG2_ECG_16: return 4;
		case ShimmerFormat.SHIMMER3_EXG2_EMG_16: return 4;
		case ShimmerFormat.SHIMMER3_BRIDGE_AMPLIFIER: return 4;
		default: return 4;
		}
	}
	
	/**
	 * Get all heart rate readings corresponding to this time index
	 * @return
	 */
	private JSONObject getJsonHeartRateDeviceReadings(double coordTime) {
		JSONObject jsonHeartRateReading = null;
		Cursor cursor = null;
		
		try {
			if (null != (cursor = mDb.fetchHeartRateDeviceValue(coordTime))) {
				
				if (!cursor.isAfterLast()) {
					int colSamples = cursor.getColumnIndex(DbAdapter.K_HR_NUM_SAMPLES);
					int colAvgHeartRate = cursor.getColumnIndex(DbAdapter.K_HR_AVG_HEART_RATE);
					int colStdHeartRate = cursor.getColumnIndex(DbAdapter.K_HR_STD_HEART_RATE);
					jsonHeartRateReading = new JSONObject();
					jsonHeartRateReading.put(TRIP_COORD_HR_SAMPLES, cursor.getInt(colSamples));
					jsonHeartRateReading.put(TRIP_COORD_HR_AVG_HEART_RATE, cursor.getDouble(colAvgHeartRate));
					jsonHeartRateReading.put(TRIP_COORD_HR_STD_HEART_RATE, cursor.getDouble(colStdHeartRate));
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return jsonHeartRateReading;
	}
	
	/**
	 * Get all heart rate readings corresponding to this time index
	 * @return
	 */
	private JSONObject getJsonBikePowerDeviceReadings(double coordTime) {
		JSONObject jsonHeartRateReading = null;
		Cursor cursor = null;
		
		try {
			if (null != (cursor = mDb.fetchBikePowerDeviceValue(coordTime))) {
				
				if (!cursor.isAfterLast()) {

					int colNsCalcPower = cursor.getColumnIndex(DbAdapter.K_BP_CALC_POWER_NUM_SAMPLES);
					int colAvgCalcPower = cursor.getColumnIndex(DbAdapter.K_BP_CALC_POWER_AVG);
					int colStdCalcPower = cursor.getColumnIndex(DbAdapter.K_BP_CALC_POWER_STD);
					
					int colNsCalcTorque = cursor.getColumnIndex(DbAdapter.K_BP_CALC_TORQUE_NUM_SAMPLES);
					int colAvgCalcTorque = cursor.getColumnIndex(DbAdapter.K_BP_CALC_TORQUE_AVG);
					int colStdCalcTorque = cursor.getColumnIndex(DbAdapter.K_BP_CALC_TORQUE_STD);
					
					int colNsCalcCrankCadence = cursor.getColumnIndex(DbAdapter.K_BP_CALC_CRANK_CADENCE_NUM_SAMPLES);
					int colAvgCalcCrankCadence = cursor.getColumnIndex(DbAdapter.K_BP_CALC_CRANK_CADENCE_AVG);
					int colStdCalcCrankCadence = cursor.getColumnIndex(DbAdapter.K_BP_CALC_CRANK_CADENCE_STD);
					
					int colNsCalcWheelSpeed = cursor.getColumnIndex(DbAdapter.K_BP_CALC_WHEEL_SPEED_NUM_SAMPLES);
					int colAvgCalcWheelSpeed = cursor.getColumnIndex(DbAdapter.K_BP_CALC_WHEEL_SPEED_AVG);
					int colStdCalcWheelSpeed = cursor.getColumnIndex(DbAdapter.K_BP_CALC_WHEEL_SPEED_STD);
					
					int colNsCalcWheelDistance = cursor.getColumnIndex(DbAdapter.K_BP_CALC_WHEEL_DISTANCE_NUM_SAMPLES);
					int colAvgCalcWheelDistance = cursor.getColumnIndex(DbAdapter.K_BP_CALC_WHEEL_DISTANCE_AVG);
					int colStdCalcWheelDistance = cursor.getColumnIndex(DbAdapter.K_BP_CALC_WHEEL_DISTANCE_STD);
					
					jsonHeartRateReading = new JSONObject();
					jsonHeartRateReading.put(TRIP_COORD_BP_NS_CALC_POWER, cursor.getInt(colNsCalcPower));
					jsonHeartRateReading.put(TRIP_COORD_BP_AVG_CALC_POWER, cursor.getDouble(colAvgCalcPower));
					jsonHeartRateReading.put(TRIP_COORD_BP_STD_CALC_POWER, cursor.getDouble(colStdCalcPower));

					jsonHeartRateReading.put(TRIP_COORD_BP_NS_CALC_TORQUE, cursor.getInt(colNsCalcTorque));
					jsonHeartRateReading.put(TRIP_COORD_BP_AVG_CALC_TORQUE, cursor.getDouble(colAvgCalcTorque));
					jsonHeartRateReading.put(TRIP_COORD_BP_STD_CALC_TORQUE, cursor.getDouble(colStdCalcTorque));
					
					jsonHeartRateReading.put(TRIP_COORD_BP_NS_CALC_CRANK_CADENCE, cursor.getInt(colNsCalcCrankCadence));
					jsonHeartRateReading.put(TRIP_COORD_BP_AVG_CALC_CRANK_CADENCE, cursor.getDouble(colAvgCalcCrankCadence));
					jsonHeartRateReading.put(TRIP_COORD_BP_STD_CALC_CRANK_CADENCE, cursor.getDouble(colStdCalcCrankCadence));

					jsonHeartRateReading.put(TRIP_COORD_BP_NS_CALC_WHEEL_SPEED, cursor.getInt(colNsCalcWheelSpeed));
					jsonHeartRateReading.put(TRIP_COORD_BP_AVG_CALC_WHEEL_SPEED, cursor.getDouble(colAvgCalcWheelSpeed));
					jsonHeartRateReading.put(TRIP_COORD_BP_STD_CALC_WHEEL_SPEED, cursor.getDouble(colStdCalcWheelSpeed));

					jsonHeartRateReading.put(TRIP_COORD_BP_NS_CALC_WHEEL_DISTANCE, cursor.getInt(colNsCalcWheelDistance));
					jsonHeartRateReading.put(TRIP_COORD_BP_AVG_CALC_WHEEL_DISTANCE, cursor.getDouble(colAvgCalcWheelDistance));
					jsonHeartRateReading.put(TRIP_COORD_BP_STD_CALC_WHEEL_DISTANCE, cursor.getDouble(colStdCalcWheelDistance));
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return jsonHeartRateReading;
	}
	
	@SuppressLint("SimpleDateFormat")
	private JSONArray getPausesJSON(long tripId) throws JSONException {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		mDb.openReadOnly();

		Cursor pausesCursor = mDb.fetchPauses(tripId);

		// Build the map between JSON fieldname and phone db fieldname:
		Map<String, Integer> fieldMap = new HashMap<String, Integer>();
		fieldMap.put(PAUSE_START, pausesCursor.getColumnIndex(DbAdapter.K_PAUSE_START_TIME));
		fieldMap.put(PAUSE_END, pausesCursor.getColumnIndex(DbAdapter.K_PAUSE_END_TIME));

		// Build JSON objects for each coordinate:
		JSONArray jsonPauses = new JSONArray();
		while (!pausesCursor.isAfterLast()) {

			JSONObject json = new JSONObject();

			try {
				json.put(PAUSE_START, df.format(pausesCursor.getDouble(fieldMap.get(PAUSE_START))));
				json.put(PAUSE_END, df.format(pausesCursor.getDouble(fieldMap.get(PAUSE_END))));

				jsonPauses.put(json);
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			pausesCursor.moveToNext();
		}
		pausesCursor.close();
		mDb.close();
		return jsonPauses;
	}

	private JSONArray getShimmerConfigsJSON(long tripId) throws JSONException {

		JSONArray jsonShimmerConfigs = new JSONArray();
		Cursor cursor = null;
		JSONObject jsonShimmerConfig = null;
		int colBlueToothId;
		int colSamplingRate;
		int colAccelRange;
		int colGsrRange;
		int colBatteryLimit;
		int colIntExpPower;
		int colLowPwrMag;
		int colLowPwrAccel;
		int colLowPwrGyro;
		int col5VReg;
		int colGyroRange;
		int colMagRange;
		int colPressureRes;
		int colRefElectrode;
		int colLeadOddDetection;
		int colLeadOddCurrent;
		int colLeadOddComparator;
		int colExgGain;
		int colExgRes;
		int colIsEcgEnabled;
		int colIsEmgEnabled;
		
		hasEcgData = false;
		hasEcgData = false;
		
		mDb.openReadOnly();
		
		try {
			if (null != (cursor = mDb.fetchShimmerConfigs(tripId))) {
			
				colBlueToothId = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_BLUETOOTH_ID);
				colSamplingRate = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_SAMPLING_RATE);
				colAccelRange = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_ACCEL_RANGE);
				colGsrRange = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_GSR_RANGE);
				colBatteryLimit = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_BATTERY_LIMIT);
				colIntExpPower = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_INT_EXP_POWER);
				colLowPwrMag = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_LOW_PWR_MAG);
				colLowPwrAccel = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_LOW_PWR_ACCEL);
				colLowPwrGyro = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_LOW_PWR_GYRO);
				col5VReg = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_5V_REG);
				colGyroRange = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_GYRO_RANGE);
				colMagRange = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_MAG_RANGE);
				colPressureRes = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_PRESSURE_RES);
				colRefElectrode = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_REF_ELECTRODE);
				colLeadOddDetection = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_LEAD_OFF_DETECTION);
				colLeadOddCurrent = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_LEAD_OFF_CURRENT);
				colLeadOddComparator = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_LEAD_OFF_COMPARATOR);
				colExgGain = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_EXG_GAIN);
				colExgRes = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_EXG_RES);
				colIsEcgEnabled = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_HAS_ECG_DATA);
				colIsEmgEnabled = cursor.getColumnIndex(DbAdapter.K_SHIMMER_CONFIG_HAS_EMG_DATA);
				
				// Build JSON objects for each coordinate:
				while (!cursor.isAfterLast()) {
	
					jsonShimmerConfig = new JSONObject();
	
					try {
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_BLUETOOTH_ID, cursor.getString(colBlueToothId));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_SAMPLING_RATE, cursor.getDouble(colSamplingRate));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_ACCEL_RANGE, cursor.getInt(colAccelRange));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_GSR_RANGE, cursor.getInt(colGsrRange));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_BATTERY_LIMIT, cursor.getDouble(colBatteryLimit));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_INT_EXP_POWER, cursor.getInt(colIntExpPower));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_LOW_PWR_MAG, cursor.getInt(colLowPwrMag));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_LOW_PWR_ACCEL, cursor.getInt(colLowPwrAccel));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_LOW_PWR_GYRO, cursor.getInt(colLowPwrGyro));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_5V_REG, cursor.getInt(col5VReg));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_GYRO_RANGE, cursor.getInt(colGyroRange));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_MAG_RANGE, cursor.getInt(colMagRange));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_PRESSURE_RES, cursor.getInt(colPressureRes));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_REF_ELECTRODE, cursor.getInt(colRefElectrode));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_LEAD_OFF_DETECTION, cursor.getInt(colLeadOddDetection));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_LEAD_OFF_CURRENT, cursor.getInt(colLeadOddCurrent));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_LEAD_OFF_COMPARATOR, cursor.getInt(colLeadOddComparator));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_EXG_GAIN, cursor.getInt(colExgGain));
						jsonShimmerConfig.put(DbAdapter.K_SHIMMER_CONFIG_EXG_RES, cursor.getInt(colExgRes));
						jsonShimmerConfigs.put(jsonShimmerConfig);

						// Note: This is designed such that if any shimmer is recording ExG data, then we will 
						// query the database for ExG data for all SHIMMER devices.  THIS SHOULD BE REDIESIGNED!!!
						
						hasEcgData |= (cursor.getInt(colIsEcgEnabled) == 1 ? true : false);
						hasEmgData |= (cursor.getInt(colIsEmgEnabled) == 1 ? true : false);
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
					cursor.moveToNext();
				}
				cursor.close();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
		return jsonShimmerConfigs;
	}	
	
	@SuppressLint("SimpleDateFormat")
	private Vector<String> getTripData(long tripId) {
		Vector<String> tripData = new Vector<String>();
		mDb.openReadOnly();
		Cursor tripCursor = mDb.fetchTrip(tripId);

		String note = tripCursor.getString(tripCursor.getColumnIndex(DbAdapter.K_TRIP_NOTE));
		String purpose = tripCursor.getString(tripCursor.getColumnIndex(DbAdapter.K_TRIP_PURP));
		Double startTime = tripCursor.getDouble(tripCursor.getColumnIndex(DbAdapter.K_TRIP_START));
		Double endTime = tripCursor.getDouble(tripCursor.getColumnIndex(DbAdapter.K_TRIP_END));
		tripCursor.close();
		mDb.close();

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		tripData.add(note);
		tripData.add(purpose);
		tripData.add(df.format(startTime));
		tripData.add(df.format(endTime));

		return tripData;
	}

	public String getAppVersion() {
		String versionName = "";
		int versionCode = 0;

		try {
			PackageInfo pInfo = mCtx.getPackageManager().getPackageInfo(
					mCtx.getPackageName(), 0);
			versionName = pInfo.versionName;
			versionCode = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		String systemVersion = Build.VERSION.RELEASE;

		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return versionName + " (" + versionCode + ") on Android "
					+ systemVersion + " " + capitalize(model);
		} else {
			return versionName + " (" + versionCode + ") on Android "
					+ systemVersion + " " + capitalize(manufacturer) + " "
					+ model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	private StringBuilder getPostData(long tripId) throws JSONException {
		
		
		//PerformanceTimer.start();
		JSONArray shimmerConfigs = getShimmerConfigsJSON(tripId);

		JSONArray pauses = getPausesJSON(tripId);
		String deviceId = userId;
		Vector<String> tripData = getTripData(tripId);
		String notes = tripData.get(0);
		String purpose = tripData.get(1);
		String startTime = tripData.get(2);

		StringBuilder postdata = new StringBuilder();
		postdata.ensureCapacity(16000000);
		
		postdata.append("purpose=");
		postdata.append(purpose);
		postdata.append("&tripid=");
		postdata.append(String.valueOf(tripId));
		// "&user=" + user.toString() +
		postdata.append("&notes=");
		postdata.append(notes);
		postdata.append("&coords=");

		//PerformanceTimer.mark();
		//JSONObject coords = getCoordsJSON(tripId);
		//PerformanceTimer.check("getCoordsJSON(tripId)");
		
		//String tmp1 = coords.toString();
		

		//StringBuilder sbTmp2 = new StringBuilder();
		getCoordsSB(postdata, tripId);
		//String tmp2 = sbTmp2.toString();
		
		//long length = tmp2.length();
		
		//PerformanceTimer.mark();
		//postdata.append(tmp2);
		//PerformanceTimer.check("coords.toString()");
		
		postdata.append("&pauses=");
		postdata.append(pauses.toString());
		postdata.append("&version=");
		postdata.append(String.valueOf(kSaveProtocolVersion));
		postdata.append("&start=");
		postdata.append(startTime);
		postdata.append("&device=");
		postdata.append(deviceId);
		postdata.append("&shimmer_configs=");
		postdata.append(shimmerConfigs);

		//PerformanceTimer.mark();
		return postdata;
	}

	@SuppressLint("SimpleDateFormat")
	private void getCoordsSB(StringBuilder sbTripCoords, long tripId) throws JSONException {
		
		//StringBuilder sbTripCoords = new StringBuilder();
		//sbTripCoords.ensureCapacity(0xffff);
		sbTripCoords.append("{");

		JSONObject jsonHeartRateDeviceReadings = null;
		JSONObject jsonBikePowerDeviceReadings = null;
		JSONArray jsonSensorReadings = null;
		double coordTime;
		
		mDb.openReadOnly();

		// ******************************************************************************
		// Get coordinate details
		// ******************************************************************************

		Cursor coordDetails = null;
		hasSensorData = false;
		hasAntDeviceData = false;
		hasShimmerData = false;
		hasEpocData = false;

		try {
			if (null != (coordDetails = mDb.fetchTripDetails(tripId))) {
				if (coordDetails.getCount() > 0) {
					try {
						hasSensorData = coordDetails.getInt(coordDetails.getColumnIndex(DbAdapter.K_TRIP_HAS_SENSOR_DATA)) == 1 ? true : false;
						hasAntDeviceData = coordDetails.getInt(coordDetails.getColumnIndex(DbAdapter.K_TRIP_HAS_ANT_DEVICE_DATA)) == 1 ? true : false;
						hasShimmerData = coordDetails.getInt(coordDetails.getColumnIndex(DbAdapter.K_TRIP_HAS_SHIMMER_DATA)) == 1 ? true : false;
						hasEpocData = coordDetails.getInt(coordDetails.getColumnIndex(DbAdapter.K_TRIP_HAS_EPOC_DATA)) == 1 ? true : false;
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
				}
				coordDetails.close();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		
		// ******************************************************************************
		// Process coordinates
		// ******************************************************************************
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fCoordTime;

		try {
			Cursor cursorTripCoords = mDb.fetchAllCoordsForTrip(tripId);
	
			if (!gotCoordsColIndexes) {
				colCoordsTime  = cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_TIME);
				colCoordsLat   = cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_LAT);
				colCoordsLng   = cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_LGT);
				colCoordsAlt   = cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_ALT);
				colCoordsSpeed = cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_SPEED);
				colCoordsHAcc  = cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_ACC);
				colCoordsVAcc  = cursorTripCoords.getColumnIndex(DbAdapter.K_POINT_ACC);
				gotCoordsColIndexes = true;
			}
			
			// Build JSON objects for each coordinate:
			
			while (!cursorTripCoords.isAfterLast()) {

				jsonSensorReadings = null;
				jsonSensorReadings = null;
				jsonSensorReadings = null;
				
				// *****************
				// * Get coordinates
				// *****************
				
				coordTime = cursorTripCoords.getDouble(colCoordsTime);
				fCoordTime = df.format(coordTime);
				
				// 
				sbTripCoords.append("\"");
				sbTripCoords.append(fCoordTime);
				sbTripCoords.append("\":");

				// {"r":"2015-09-09 11:30:07",
				sbTripCoords.append("{\"r\":\"");
				sbTripCoords.append(fCoordTime);

				// "l":45.644564
				sbTripCoords.append("\",\"l\":");
				sbTripCoords.append(cursorTripCoords.getDouble(colCoordsLat) / 1E6);
				
				// "n":-122.534299
				sbTripCoords.append(",\"n\":");
				sbTripCoords.append(cursorTripCoords.getDouble(colCoordsLng) / 1E6);
				
				// "a":42
				sbTripCoords.append(",\"a\":");
				sbTripCoords.append(cursorTripCoords.getDouble(colCoordsAlt));
				
				// "s":0
				sbTripCoords.append(",\"s\":");
				sbTripCoords.append(cursorTripCoords.getDouble(colCoordsSpeed));
				
				// "h":0
				sbTripCoords.append(",\"h\":");
				sbTripCoords.append((int) cursorTripCoords.getDouble(colCoordsHAcc));
				
				// "v":0
				sbTripCoords.append(",\"v\":");
				sbTripCoords.append((int) cursorTripCoords.getDouble(colCoordsVAcc));
	
				// **********************************************************
				// * Get all sensor readings corresponding to this time index
				// **********************************************************
				
				if (hasSensorData) {
					jsonSensorReadings = getJsonSensorReadings(coordTime);
				}
				
				// insert sensor readings into coordinate jSON object
				if ((null != jsonSensorReadings) && (jsonSensorReadings.length() > 0)) {
					
					// sbTripCoords.put(TRIP_COORDS_SENSOR_READINGS, jsonSensorReadings);
					// ,"sr":[{ ... }]
					
					sbTripCoords.append(",\"sr\":");
					sbTripCoords.append(jsonSensorReadings.toString());
				}

				// **********************************************************
				// * Get all sensor readings corresponding to this time index
				// **********************************************************
				
				if (hasShimmerData) {
					addJsonShimmerReadings(sbTripCoords, coordTime);
					//jsonShimmerReadings = getJsonShimmerReadings(coordTime);
				}
				
				// insert shimmer readings into coordinate jSON object
				//if ((null != jsonShimmerReadings) && (jsonShimmerReadings.length() > 0)) {
					////sbTripCoords.put(TRIP_COORDS_SHIMMER_READINGS, jsonShimmerReadings);	// ,"shr":[{ ... }]
					
					//sbTripCoords.append(",\"shr\":");
					//sbTripCoords.append(jsonShimmerReadings.toString());
				//}

				if (hasAntDeviceData) {
					
					// *****************************************************************
					// * Get heart rate device readings corresponding to this time index
					// *****************************************************************
						
					jsonHeartRateDeviceReadings = getJsonHeartRateDeviceReadings(coordTime);
					
					// insert heart rate readings into coordinate jSON object
					if ((null != jsonHeartRateDeviceReadings) && (jsonHeartRateDeviceReadings.length() > 0)) {
						// sbTripCoords.put(TRIP_COORDS_ANT_DEVICE_PREFIX + DeviceType.HEARTRATE.getIntValue(), jsonHeartRateDeviceReadings);
						// ,"ant_37":[{ ... }]
						
						sbTripCoords.append(",\"ant_");
						sbTripCoords.append(DeviceType.HEARTRATE.getIntValue());
						sbTripCoords.append("\":");
						sbTripCoords.append(jsonHeartRateDeviceReadings.toString());
					}
	
					// *****************************************************************
					// * Get bike power device readings corresponding to this time index
					// *****************************************************************
						
					jsonBikePowerDeviceReadings = getJsonBikePowerDeviceReadings(coordTime);
					
					// insert bike power readings into coordinate jSON object
					if ((null != jsonBikePowerDeviceReadings) && (jsonBikePowerDeviceReadings.length() > 0)) {
						// sbTripCoords.put(TRIP_COORDS_ANT_DEVICE_PREFIX + DeviceType.BIKE_POWER.getIntValue(), jsonBikePowerDeviceReadings);
						// ,"ant_37":[{ ... }]
						
						sbTripCoords.append(",\"ant_");
						sbTripCoords.append(DeviceType.BIKE_POWER.getIntValue());
						sbTripCoords.append("\":");
						sbTripCoords.append(jsonBikePowerDeviceReadings.toString());
					}
				}

				// ****************************************************
				// * Insert sensor readings into jSON coordinate object
				// ****************************************************

				sbTripCoords.append("}"); // end of point 

				// move to next coordinate
				if (cursorTripCoords.moveToNext()) {
					sbTripCoords.append(","); // point to follow
				}
				else {
					sbTripCoords.append("}"); // end of all points
				}
			}
			cursorTripCoords.close();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
			//sbPostData.append(sbTripCoords);
		}
	}
	
	
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static byte[] compress(String string) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
		GZIPOutputStream gos = new GZIPOutputStream(os);
		gos.write(string.getBytes());
		gos.close();
		byte[] compressed = os.toByteArray();
		os.close();
		return compressed;
	}

	boolean uploadOneTrip(long currentTripId) {
		boolean result = false;
		byte[] postBodyDataZipped;
		StringBuilder postBodyData;
		String responseString;
		
		// Assemble data to post
		try {
			postBodyData = getPostData(currentTripId);
			// PerformanceTimer.check("after getPostData(currentTripId)");
		} catch (JSONException e) {
			e.printStackTrace();
			return result;
		}

		// Post data to server
		HttpClient client = new DefaultHttpClient();
		// TODO: Server URL
		HttpPost postRequest = new HttpPost(POST_URL);

		try {
			postBodyDataZipped = compress(postBodyData.toString());
			postBodyData = null;

			postRequest.setHeader("Cycleatl-Protocol-Version", "3");
			postRequest.setHeader("Content-Encoding", "gzip");
			postRequest.setHeader("Content-Type", "application/vnd.cycleatl.trip-v3+form");

			postRequest.setEntity(new ByteArrayEntity(postBodyDataZipped));
			postBodyDataZipped = null;

			HttpResponse response = client.execute(postRequest);
			responseString = convertStreamToString(response.getEntity().getContent());

		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// Process server response
		JSONObject responseData;
		try {
			responseData = new JSONObject(responseString);
		}
		catch (JSONException e) {
			e.printStackTrace();
			return false;
		}

		try {
			if ((null == responseData) || (!responseData.getString("status").equals("success"))) {
				return false;
			}
			result = true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		// Update upload status in the database
		mDb.open();
		try {
			mDb.updateTripStatus(currentTripId, TripData.STATUS_SENT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			mDb.close();
		}
		
		return result;
	}

	@Override
	protected Boolean doInBackground(Long... tripid) {
		Boolean result = true;
		try {
			// First, send the trip user asked for:
			if (tripid.length != 0) {
				result = uploadOneTrip(tripid[0]);
				Thread.sleep(250);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
			result = false;
		}

		// Send all previously-completed trips
		// that were not sent successfully.
		Vector<Long> unsentTrips;
		if (null != (unsentTrips = getUnsentTrips(tripid[0]))) {
			for (Long trip : unsentTrips) {
				try {
					result &= uploadOneTrip(trip);
					Thread.sleep(250);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
					result = false;
				}
			}
		}

		return result;
	}
	
	/**
	 * Returns a vector containing the trip IDs of unsent trips
	 * @return
	 */
	private Vector<Long> getUnsentTrips(long ignoreTripId) {
		long tripId;
		Vector<Long> unsentTrips = new Vector<Long>();
		mDb.openReadOnly();
		try {
			Cursor cur = mDb.fetchUnsentTrips();
			if (cur != null && cur.getCount() > 0) {
				while (!cur.isAfterLast()) {
					tripId = Long.valueOf(cur.getLong(0));
					if (tripId != ignoreTripId) {
						unsentTrips.add(tripId);
					}
					
					cur.moveToNext();
				}
				cur.close();
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
		return unsentTrips;
	}

	@Override
	protected void onPreExecute() {
		Toast.makeText(mCtx.getApplicationContext(),
				"Submitting. Thanks for using ORcycle!",
				Toast.LENGTH_LONG).show();
	}

	private Fragment_MainTrips fragmentMainTrips;

	public Fragment_MainTrips setFragmentMainTrips(
			Fragment_MainTrips fragmentMainTrips) {
		this.fragmentMainTrips = fragmentMainTrips;
		return fragmentMainTrips;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try {
			if (fragmentMainTrips != null) {
				fragmentMainTrips.populateTripList();
			}
			if (result) {
				Toast.makeText(mCtx.getApplicationContext(),
						"Trip uploaded successfully.", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(
						mCtx.getApplicationContext(),
						"ORcycle couldn't upload the trip, and will retry when your next trip is completed.",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// Just don't toast if the view has gone out of context
			Log.e(MODULE_TAG, e.getMessage());
		}
	}
}
