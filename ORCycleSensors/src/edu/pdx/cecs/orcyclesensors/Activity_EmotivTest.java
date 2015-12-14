package edu.pdx.cecs.orcyclesensors;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

public class Activity_EmotivTest extends Activity {

	private static final String MODULE_TAG = "Fragment_MainEmotiv";

	private EpocPlus epocPlus = null;
	
	private static final int REQUEST_ENABLE_BT = 1;
	Button Start_button,Stop_button;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        epocPlus = EpocPlus.getInstance();

		setContentView(R.layout.activity_emotive_test);
		
		final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

		final BluetoothAdapter mBluetoothAdapter  = bluetoothManager.getAdapter();
		
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        
		Start_button = (Button)findViewById(R.id.startbutton);
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
		
		Stop_button  = (Button)findViewById(R.id.stopbutton);
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
	
    @Override
    public void onStart() {
    	super.onStart();
    	try {
    		epocPlus.connect(this);
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
