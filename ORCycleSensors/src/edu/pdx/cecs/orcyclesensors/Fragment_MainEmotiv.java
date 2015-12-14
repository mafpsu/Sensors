package edu.pdx.cecs.orcyclesensors;

import android.support.v4.app.Fragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

public class Fragment_MainEmotiv extends Fragment {

	private static final String MODULE_TAG = "Fragment_MainEmotiv";

	private EpocPlus epocPlus = null;
	
	private static final int REQUEST_ENABLE_BT = 1;
	Button Start_button,Stop_button;
	
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Activity activity = getActivity();
		View rootView = null;
        epocPlus = EpocPlus.getInstance();

		if (null != (rootView = inflater.inflate(R.layout.fragment_main_emotiv, (ViewGroup) null))) {
		
			final BluetoothManager bluetoothManager =
	                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);

			final BluetoothAdapter mBluetoothAdapter  = bluetoothManager.getAdapter();
			
	        if (!mBluetoothAdapter.isEnabled()) {
	            if (!mBluetoothAdapter.isEnabled()) {
	                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	            }
	        }
	        
			Start_button = (Button)rootView.findViewById(R.id.startbutton);
			Start_button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					try {
						epocPlus.startWriteFile();
					}
			    	catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
			    	}
				}
			});
			
			Stop_button  = (Button)rootView.findViewById(R.id.stopbutton);
			Stop_button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					try {
						epocPlus.StopWriteFile();
					}
			    	catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
			    	}
				}
			});
			
		}
		return rootView;
	}
	
    @Override
    public void onStart() {
    	super.onStart();
    	try {
    		epocPlus.connect(getActivity());
    	}
    	catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
    	}
    }

    @Override
    public void onStop() {
    	super.onStop();
    	
    	try {
    		epocPlus.disconnect();
    	}
    	catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
    	}
    }
}
