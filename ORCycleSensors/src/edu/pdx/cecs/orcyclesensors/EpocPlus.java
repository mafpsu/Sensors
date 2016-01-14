package edu.pdx.cecs.orcyclesensors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEdk.IEE_Event_t;

public class EpocPlus {

	IEdk.IEE_MotionDataChannel_t[] Channel_list = {
			IEdk.IEE_MotionDataChannel_t.IMD_COUNTER, IEdk.IEE_MotionDataChannel_t.IMD_GYROX,IEdk.IEE_MotionDataChannel_t.IMD_GYROY,
			IEdk.IEE_MotionDataChannel_t.IMD_GYROZ,IEdk.IEE_MotionDataChannel_t.IMD_ACCX,IEdk.IEE_MotionDataChannel_t.IMD_ACCY,IEdk.IEE_MotionDataChannel_t.IMD_ACCZ,
			IEdk.IEE_MotionDataChannel_t.IMD_MAGX,IEdk.IEE_MotionDataChannel_t.IMD_MAGY,IEdk.IEE_MotionDataChannel_t.IMD_MAGZ,IEdk.IEE_MotionDataChannel_t.IMD_TIMESTAMP};
	
	private static final String MODULE_TAG = "EpocPlus";

	private final Handler handler = new EmotiveMessageHandler();
	
	private boolean hasUser = false;
	private boolean isConnected = false;
	private boolean isEnableWriteFile = false;
	private boolean lock = false;
	private int userId = -1;
	private BufferedWriter motion_writer;
	private Thread processingThread = null;
	
	private final class ProcessMessageThread extends Thread {
		
		@Override
		public void run() {
			super.run();
			
			while(true)
			{
				try
				{
					if (!isConnected) {
						handler.sendEmptyMessage(0);
					}
					else if(!hasUser) {
						handler.sendEmptyMessage(1);
					}
					else if(isEnableWriteFile) {
						handler.sendEmptyMessage(2);
					}
					Thread.sleep(5);
				}
				catch (Exception ex)
				{
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
	}

	private final class EmotiveMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {

			case 0:
				
				if (!isConnected) {
					int number = IEdk.IEE_GetEpocPlusDeviceCount();
					if(number != 0) {
						String deviceName = IEdk.IEE_GetEpocPlusDeviceName(0);
						Log.v(MODULE_TAG, "Device Name: " + deviceName);
						isConnected = IEdk.IEE_ConnectEpocPlusDevice(0, false);
					}
				}
				break;
			
			case 1:
				
				int state = IEdk.IEE_EngineGetNextEvent();
				if (state == IEdkErrorCode.EDK_OK.ToInt()) {

					int eventType = IEdk.IEE_EmoEngineEventGetType();
					
					if(eventType == IEE_Event_t.IEE_UserAdded.ToInt()) {
						Log.v("SDK", "User added");
					    userId = IEdk.IEE_EmoEngineEventGetUserId();
						hasUser = true;
						
						
					}
					else if(eventType == IEE_Event_t.IEE_UserRemoved.ToInt()) {
						Log.v("SDK", "User removed");		
						userId = -1;
						hasUser = false;
					}
				}
				break;
			
			case 2:
				
				int result = IEdk.IEE_MotionDataUpdateHandle(userId);
				if (result == IEdkErrorCode.EDK_OK.ToInt()) {
					int numSamples = IEdk.IEE_MotionDataGetNumberOfSample(userId);
					if(numSamples > 0){
						for(int sample = 0; sample < numSamples; sample++)
						{
							for(int j = 0; j < Channel_list.length; j++){
								double[] eeg_data = IEdk.IEE_MotionDataGet(Channel_list[j]);
								addData(eeg_data[sample]);
							}
							try {
								motion_writer.newLine();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				break;
			}

		}
	}
	
	/**
     * Reference to class instance
     */
    private static EpocPlus epocPlus = null;

    /**
     * Returns the class instance of the EpocPlus object
     */
    public static EpocPlus getInstance() {

    	if (null == epocPlus) {
    		epocPlus = new EpocPlus();
    	}
        return epocPlus;
    }

    public void connect(Context context) {
    	if (!isConnected) {
    		int result = IEdk.IEE_EngineConnect(context, "Emotive Sytems-5");
			if (result == IEdkErrorCode.EDK_OK.ToInt()) {
				IEdk.IEE_MotionDataCreate();
				if (null == processingThread) {
					if (null != (processingThread = new ProcessMessageThread())) {		
						processingThread.start();
					}
				}
			}
    	}
    }
    
    public void disconnect() {
    	if (isConnected) {
    		IEdk.IEE_EngineDisconnect();
    		isConnected = false;
    	}
    }
    
    public void startWriteFile() {
		
    	try {
    		if (!isEnableWriteFile) {
				String eeg_header = "COUNTER_MEMS,GYROX,GYROY,GYROZ,ACCX,ACCY,ACCZ,MAGX,MAGY,MAGZ,TimeStamp";
				File root = Environment.getExternalStorageDirectory();
				String path = root.getAbsolutePath()+ "/MotionLogger/";
				File folder = new File(path);
				if(!folder.exists()) {
					folder.mkdirs();
				}		
				motion_writer = new BufferedWriter(new FileWriter(path+"raw_motion.csv"));
				motion_writer.write(eeg_header);
				motion_writer.newLine();
				isEnableWriteFile = true;
    		}
		} 
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	public void StopWriteFile(){
		try {
			if (null != motion_writer) {
				motion_writer.flush();
				motion_writer.close();
				motion_writer = null;
				isEnableWriteFile = false;
			}
		} 
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	/**
	 * private void addEEGData(Double[][] eegs) Add EEG Data for write int the
	 * EEG File
	 * 
	 * @param eegs
	 *            - double array of eeg data
	 */
	private void addData(double data) {

		if (motion_writer == null) {
			return;
		}

		if (isEnableWriteFile) {
			String input = String.valueOf(data) + ",";
			try {
				motion_writer.write(input);
			}
			catch (IOException ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}
}
