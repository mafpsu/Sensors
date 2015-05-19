package edu.pdx.cecs.orcyclesensors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

public class Activity_SensorDetail extends Activity {

	private static final String MODULE_TAG = "Activity_SensorDetail";

	public static final String EXTRA_SENSOR_NAME = "noteId";

	private TextView tv_fifoMaxEventCount;
	private TextView tv_fifoReservedEventCount;
	private TextView tv_maxDelay;
	private TextView tv_maximumRange;
	private TextView tv_minDelay;
	private TextView tv_name;
	private TextView tv_power;
	private TextView tv_reportingMode;
	private TextView tv_resolution;
	private TextView tv_stringType;
	private TextView tv_type;
	private TextView tv_vendor;
	private TextView tv_version;
	private TextView tv_isWakeUpSensor;
	private Spinner spn_rate;
	private SensorItem sensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			setContentView(R.layout.activity_sensor_detail);
		
			(tv_fifoMaxEventCount = (TextView) findViewById(R.id.tv_fifoMaxEventCount)).setText("xxxx");
			(tv_fifoReservedEventCount = (TextView) findViewById(R.id.tv_fifoReservedEventCount)).setText("xxxx");
			(tv_maxDelay = (TextView) findViewById(R.id.tv_maxDelay)).setText("xxxx");
			(tv_maximumRange = (TextView) findViewById(R.id.tv_maximumRange)).setText("xxxx");
			(tv_minDelay = (TextView) findViewById(R.id.tv_minDelay)).setText("xxxx");
			(tv_name = (TextView) findViewById(R.id.tv_sensor_name)).setText("Sensor Name: ");
			(tv_power = (TextView) findViewById(R.id.tv_power)).setText("xxxx");
			(tv_reportingMode = (TextView) findViewById(R.id.tv_reportingMode)).setText("xxxx");
			(tv_resolution = (TextView) findViewById(R.id.tv_resolution)).setText("xxxx");
			(tv_stringType = (TextView) findViewById(R.id.tv_stringType)).setText("xxxx");
			(tv_type = (TextView) findViewById(R.id.tv_type)).setText("xxxx");
			(tv_vendor = (TextView) findViewById(R.id.tv_vendor)).setText("xxxx");
			(tv_version = (TextView) findViewById(R.id.tv_version)).setText("xxxx");
			(tv_isWakeUpSensor = (TextView) findViewById(R.id.tv_isWakeUpSensor)).setText("xxxx");
			
			// If spinner is changed, update the item it is tagged with.
			spn_rate = (Spinner) findViewById(R.id.spn_asd_sensor_rate);

		} catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: Activity_SensorDetail onResume");
			
			Intent intent;
			String sensorName;
			
			if (null != (intent = getIntent())) {
				if (null != (sensorName = intent.getStringExtra(EXTRA_SENSOR_NAME))) {
					if (null != (sensor = MyApplication.getInstance().getAppSensor(sensorName))) {
						tv_fifoMaxEventCount.setText(String.valueOf(sensor.getFifoMaxEventCount()));
						tv_fifoReservedEventCount.setText(String.valueOf(sensor.getFifoReservedEventCount()));
						tv_maxDelay.setText(String.valueOf(sensor.getMaxDelay()));
						tv_maximumRange.setText(String.valueOf(sensor.getMaximumRange()));
						tv_minDelay.setText(String.valueOf(sensor.getMinDelay()));
						tv_name.setText(String.valueOf(sensor.getName()));
						tv_power.setText(String.valueOf(sensor.getPower()));
						tv_reportingMode.setText(String.valueOf(sensor.getReportingMode()));
						tv_resolution.setText(String.valueOf(sensor.getResolution()));
						tv_stringType.setText(String.valueOf(sensor.getStringType()));
						tv_type.setText(String.valueOf(sensor.getType()));
						tv_vendor.setText(String.valueOf(sensor.getVendor()));
						tv_version.setText(String.valueOf(sensor.getVersion()));
						tv_isWakeUpSensor.setText(String.valueOf(sensor.isWakeUpSensor()));
						spn_rate.setSelection(sensor.getRate());
						spn_rate.setTag(sensor);
						spn_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
								try {
									SensorItem item;
									if (null != (item = (SensorItem) parent.getTag())) {
										item.setRate(position);
										MyApplication.getInstance().updateSensor(item);									}
								}
								catch(Exception ex) {
									Log.e(MODULE_TAG, ex.getMessage());
								}
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
								Log.e(MODULE_TAG, "onNothingSelected");
							}
						});

					}
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
}
