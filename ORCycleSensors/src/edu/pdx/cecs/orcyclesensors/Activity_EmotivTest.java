package edu.pdx.cecs.orcyclesensors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.emotiv.insight.IEdk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEdk.IEE_DataChannel_t;
import com.emotiv.insight.IEdk.IEE_Event_t;


public class Activity_EmotivTest extends Activity {

	public static final String MODULE_TAG = "Fragment_MainEmotiv";

    public static String EXTRA_BLUETOOTH_ADDRESS = "EXTRA_BLUETOOTH_ADDRESS";
    public static String EXTRA_BLUETOOTH_NAME = "EXTRA_BLUETOOTH_NAME";
    
	private static final int REQUEST_ENABLE_BT = 1;
	private Button Start_button,Stop_button;
    private String mBluetoothAddress;
    private String mBluetoothName;
    private TextView tvStatus;
	private int userId = -1;
    
	private IEdk.IEE_MotionDataChannel_t[] Channel_list = {
			IEdk.IEE_MotionDataChannel_t.IMD_COUNTER, IEdk.IEE_MotionDataChannel_t.IMD_GYROX,IEdk.IEE_MotionDataChannel_t.IMD_GYROY,
			IEdk.IEE_MotionDataChannel_t.IMD_GYROZ,IEdk.IEE_MotionDataChannel_t.IMD_ACCX,IEdk.IEE_MotionDataChannel_t.IMD_ACCY,IEdk.IEE_MotionDataChannel_t.IMD_ACCZ,
			IEdk.IEE_MotionDataChannel_t.IMD_MAGX,IEdk.IEE_MotionDataChannel_t.IMD_MAGY,IEdk.IEE_MotionDataChannel_t.IMD_MAGZ,IEdk.IEE_MotionDataChannel_t.IMD_TIMESTAMP};
	
    private boolean isConnected = false;
	private BufferedWriter motion_writer;
	private int samplesRead;
    
    private static final int STATE_ERROR = -1;
    private static final int STATE_INIT_ENGINE = 0;
    private static final int STATE_FIND_DEVICE = 1;
    private static final int STATE_CONNECTING_DEVICE = 2;
    private static final int STATE_ADD_USER = 3;
    private static final int STATE_REMOVE_USER = 4;
    private static final int STATE_READING_DEVICE = 5;
    
    private ProcessingThread processingThread = new ProcessingThread();
    
    @SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		BluetoothAdapter mBluetoothAdapter = null;

		setContentView(R.layout.activity_emotive_test);
		
		// Get bluetooth address
        if(null == (mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter())) {
        	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
        }
        else if(!mBluetoothAdapter.isEnabled()) {     	
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
        else if ((null == mBluetoothAddress) || mBluetoothAddress.equals("")) {
			if (null != savedInstanceState) {
				mBluetoothAddress = savedInstanceState.getString(EXTRA_BLUETOOTH_ADDRESS, "");
				mBluetoothName = savedInstanceState.getString(EXTRA_BLUETOOTH_NAME, "");
			}
			else {
				mBluetoothAddress = getIntent().getStringExtra(EXTRA_BLUETOOTH_ADDRESS);
				mBluetoothName = getIntent().getStringExtra(EXTRA_BLUETOOTH_NAME);
			}
		}

		if ((null == mBluetoothAddress) || mBluetoothAddress.equals("")) {
        	Toast.makeText(this, "Device Bluetooth address not set\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
		}
		else {
			Start_button = (Button)findViewById(R.id.startbutton);
			Start_button.setEnabled(false);
			Start_button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					try {
						processingThread.setRecording(true);
						Start_button.setEnabled(false);
						Stop_button.setEnabled(true);
					}
			    	catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
			    	}
				}
			});
			
			Stop_button  = (Button)findViewById(R.id.stopbutton);
			Stop_button.setEnabled(false);
			Stop_button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					try {
						processingThread.setRecording(false);
						Stop_button.setEnabled(false);
						Start_button.setEnabled(true);
					}
			    	catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
			    	}
				}
			});
			
			tvStatus = (TextView) findViewById(R.id.tv_aet_status);
		}
	}
	
    @Override
    public void onStart() {
    	super.onStart();
    	try {
    		int result = IEdk.IEE_EngineConnect(Activity_EmotivTest.this, "Emotive Sytems-5");
			if (result == IEdkErrorCode.EDK_OK.ToInt()) {
				IEdk.IEE_MotionDataCreate();
			}
			else {
				handler.sendEmptyMessage(STATE_ERROR);
				return;
			}
			
    		//epocPlus.connect(this);
    		processingThread.start();
    	}
    	catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
    	}
    }

    @Override
    public void onStop() {
    	super.onStop();
    	
    	try {
    		processingThread.setCancelled(true);
    	}
    	catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
    	}
    }

    // ******************************************************************************
    // *                            ProcessingThread
    // ******************************************************************************
    
	private final class ProcessingThread extends Thread {
		
		private boolean cancelled = false;
		private boolean isRecording = false;
		private boolean msgStartRecording = false;
		private boolean msgStopRecording = false;
		
		synchronized public void setCancelled(boolean value) {
			cancelled = value;
		}
		
		synchronized public boolean isCancelled() {
			return cancelled;
		}
		
		private void finish() {
			IEdk.IEE_MotionDataFree();
			IEdk.IEE_EngineDisconnect();
		}
		
		@Override
		public void run() {
			super.run();
			
			// Alert user searching for device 
			handler.sendEmptyMessage(STATE_FIND_DEVICE);

		    int deviceIndex = -1;
		    boolean deviceFound = false;
			while(!deviceFound)
			{
				int numDevices = IEdk.IEE_GetEpocPlusDeviceCount();
				if(numDevices > 0) {
					for (int i = 0; i < numDevices; ++i) {
						String deviceName = IEdk.IEE_GetEpocPlusDeviceName(i);
						Log.v(MODULE_TAG, "Device Name: " + deviceName);
						if (deviceName.equals(mBluetoothName)) {
							deviceFound = true;
							deviceIndex = i;
							break;
						}
					}
				}
				try {
					Thread.sleep(5);
				}
				catch(InterruptedException ex) {
					
				}
				if (cancelled) {
					finish();
					return;
				}
			}

			
			handler.sendEmptyMessage(STATE_CONNECTING_DEVICE);

			while(!isConnected) {
				if (isConnected = IEdk.IEE_ConnectEpocPlusDevice(deviceIndex, false)) {
				}
				try {
					Thread.sleep(5);
				}
				catch(InterruptedException ex) {
					
				}
				if (cancelled) {
					finish();
					return;
				}
			}
			
			int state;
			while (true) {
				
				state = IEdk.IEE_EngineGetNextEvent();
				if (state == IEdkErrorCode.EDK_OK.ToInt()) {
	
					int eventType = IEdk.IEE_EmoEngineEventGetType();
					
					if(eventType == IEE_Event_t.IEE_UserAdded.ToInt()) {
						Log.v(MODULE_TAG, "User added");
					    userId = IEdk.IEE_EmoEngineEventGetUserId();
						handler.sendEmptyMessage(STATE_ADD_USER);
					}
					else if(eventType == IEE_Event_t.IEE_UserRemoved.ToInt()) {
						Log.v(MODULE_TAG, "User removed");		
						userId = -1;
						handler.sendEmptyMessage(STATE_REMOVE_USER);
					}
				}
					
				if (userId != -1) {
					
					if (msgStartRecording) {
						msgStartRecording = false;
						setRecording(true);
					}
					else if (msgStopRecording) {
						msgStopRecording = false;
						setRecording(false);
					}
					
					if (isRecording) {
						IEdk.IEE_MotionDataUpdateHandle(userId);
						if((samplesRead = IEdk.IEE_MotionDataGetNumberOfSample(userId)) > 0){
							
							handler.sendEmptyMessage(STATE_READING_DEVICE);

							for(int sampleIdx = 0; sampleIdx < samplesRead; sampleIdx++)
							{
								try {
									for(int j = 0; j < Channel_list.length; j++){
										double[] eeg_data = IEdk.IEE_MotionDataGet(Channel_list[j]);
										addData(eeg_data[sampleIdx]);
									}
									motion_writer.newLine();
								}
								catch (IOException ex) {
									Log.v(MODULE_TAG, ex.getMessage());
									handler.sendEmptyMessage(STATE_ERROR);
									finish();
									return;
								}
							}
						}
					}

					if (cancelled) {
						finish();
						return;
					}
				}
				try {
					Thread.sleep(5);
				}
				catch(InterruptedException ex) {
					
				}
			}
		}
		
		synchronized public void startRecording() {
			msgStartRecording = true;
		}
		
		synchronized public void stopRecording() {
			msgStopRecording = true;
		}

		private void setRecording(boolean value) {

			if (!isRecording && (value == true)) {
				String eeg_header = "COUNTER_MEMS,GYROX,GYROY,GYROZ,ACCX,ACCY,ACCZ,MAGX,MAGY,MAGZ,TimeStamp";
				File root = Environment.getExternalStorageDirectory();
				String file_path = root.getAbsolutePath()+ "/MotionLogger/";
				File folder = new File(file_path);
				if(!folder.exists()) {
					folder.mkdirs();
				}
				
				try {
					motion_writer = new BufferedWriter(new FileWriter(file_path+"raw_motion.csv"));
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
					return;
				}
				
				try {
					motion_writer.write(eeg_header);
					motion_writer.newLine();
					isRecording = true;
				}
				catch(IOException ex) {
					try {
						motion_writer.close();
					}
					catch(IOException exx) {
						Log.e(MODULE_TAG, exx.getMessage());
					}
					motion_writer = null;
				}
			}
			else if (isRecording && (value == false)) {
				try {
					isRecording = false;
					motion_writer.flush();
					motion_writer.close();
				} catch (Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				} finally {
					motion_writer = null;
				}
			}
		}

		/**
		 * public void addEEGData(Double[][] eegs) Add EEG Data for write int the
		 * EEG File
		 * 
		 * @param eegs
		 *            - double array of eeg data
		 */
		public void addData(double data) {

			if (motion_writer == null) {
				return;
			}

			String input = "";
				input += (String.valueOf(data) + ",");
			try {
				motion_writer.write(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
    // ******************************************************************************
    // *                        ProcessingThread UI Handler
    // ******************************************************************************

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {

			case STATE_ERROR:
				tvStatus.setText("Error!");
				break;

			case STATE_INIT_ENGINE:
				tvStatus.setText("Initializing");
				break;
			
			case STATE_FIND_DEVICE:
				tvStatus.setText("Searching for device: " + mBluetoothName);
				break;

			case STATE_CONNECTING_DEVICE:
				tvStatus.setText("Connecting: " + mBluetoothName);
				break;

			case STATE_ADD_USER:
				Start_button.setEnabled(true);
				tvStatus.setText("User added: " + userId);
				break;
				
			case STATE_REMOVE_USER:
				Start_button.setEnabled(false);
				tvStatus.setText("User removed");
				break;
				
			case STATE_READING_DEVICE:
				tvStatus.setText("Samples read: " + samplesRead);
				break;
			}
		}
	};

}
