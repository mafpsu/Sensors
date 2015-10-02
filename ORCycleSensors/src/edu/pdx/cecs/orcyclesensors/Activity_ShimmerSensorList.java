package edu.pdx.cecs.orcyclesensors;

import com.google.common.collect.BiMap;

import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ObjectCluster;
import edu.pdx.cecs.orcyclesensors.ShimmerService;
import edu.pdx.cecs.orcyclesensors.ShimmerService.LocalBinder;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;

public class Activity_ShimmerSensorList extends ListActivity {
	
	public static final String MODULE_TAG = "Activity_ShimmerSensorList";
	public static final String EXTRA_BLUETOOTH_ADDRESS = "EXTRA_BLUETOOTH_ADDRESS";

	private static final int REQUEST_ENABLE_BT = 1;

	private ShimmerService mService = null;
	private String mBluetoothAddress = "";
    private long enabledSensors=0;
    private ListView listView;

	// *********************************************************************************
	// *                          Fragment Life Cycle
	// *********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_shimmer_sensor_view);

        Button mDoneButton = (Button) findViewById(R.id.buttonEnableSensors);
		mDoneButton.setOnClickListener(new DoneButton_OnClickListener());

		BluetoothAdapter mBluetoothAdapter = null;
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
			}
			else {
				mBluetoothAddress = getIntent().getStringExtra(EXTRA_BLUETOOTH_ADDRESS);
			}
		}

		if ((null == mBluetoothAddress) || mBluetoothAddress.equals("")) {
        	Toast.makeText(this, "Device Bluetooth address not set\nExiting...", Toast.LENGTH_LONG).show();
        	finish();
		}
		else {
	        mService = MyApplication.getInstance().getShimmerService();
			setTitle(mBluetoothAddress + " " + getString(R.string.assl_title_connecting));
	        setProgressBarIndeterminateVisibility(true);
		}
    }

    /**
     * 
     */
	@Override
	public void onResume() {
		super.onResume();

		try {
			mService.connectShimmer(mBluetoothAddress, "Device");
	  		mService.setGraphHandler(mHandler);
		}
  		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
  		}
	}

	/**
	 * 
	 */
	@Override
	public void onPause() {
		super.onPause();
		if ((null != mBluetoothAddress) && !mBluetoothAddress.equals("") && (null != mService)) {
			mService.disconnectShimmer(mBluetoothAddress);
		}
	}
    
	/**
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
    	switch (requestCode) {
    	
    	case REQUEST_ENABLE_BT:
    		
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	
                //setMessage("\nBluetooth is now enabled");
                Toast.makeText(this, "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occured
            	Toast.makeText(this, "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
                finish();       
            }
            break;
        }
	}

	/**
	 * Save UI state changes to the savedInstanceState variable.
	 * This bundle will be passed to onCreate, onCreateView, and
	 * onCreateView if the parent activity is killed and restarted
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			savedInstanceState.putString(EXTRA_BLUETOOTH_ADDRESS, mBluetoothAddress);		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(savedInstanceState);
	}
	
	// *********************************************************************************
	// *                                   Misc
	// *********************************************************************************

	public void showEnableSensors(final String[] sensorNames, long enabledSensors) {

		this.enabledSensors = enabledSensors;

		listView = (ListView) findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(
				this, 
				android.R.layout.simple_list_item_multiple_choice,
				android.R.id.text1, 
				sensorNames);
		listView.setAdapter(adapterSensorNames);
		
		final BiMap<String, String> sensorBitmaptoName;
		sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mService.getShimmerVersion(mBluetoothAddress));
		for (int i = 0; i < sensorNames.length; i++) {
			int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));
			if ((iDBMValue & enabledSensors) > 0) {
				listView.setItemChecked(i, true);
			}
		}

		listView.setOnItemClickListener(new ListView_OnClickListener(sensorBitmaptoName, sensorNames));
	}
	
	// *********************************************************************************
	// *                          ListView
	// *********************************************************************************

	private final class ListView_OnClickListener implements OnItemClickListener {
		private final BiMap<String, String> sensorBitmaptoName;
		private final String[] sensorNames;

		private ListView_OnClickListener(
				BiMap<String, String> sensorBitmaptoName, String[] sensorNames) {
			this.sensorBitmaptoName = sensorBitmaptoName;
			this.sensorNames = sensorNames;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int clickIndex, long arg3) {
			int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[clickIndex]));
			// check and remove any old daughter boards (sensors) which will
			// cause a conflict with sensorIdentifier
			enabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, enabledSensors, sensorIdentifier);
			// update the checkbox accordingly
			for (int i = 0; i < sensorNames.length; i++) {
				int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));
				if ((iDBMValue & enabledSensors) > 0) {
					listView.setItemChecked(i, true);
				} else {
					listView.setItemChecked(i, false);
				}
			}
		}
	}

	private final class DoneButton_OnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (null != mService) {
				mService.setEnabledSensors(enabledSensors, mBluetoothAddress);
			}
			finish();
		}
	}
	
	public void doSensors() {
        setProgressBarIndeterminateVisibility(false);
		Shimmer shimmer = null;
		if (null != (shimmer = mService.getShimmer(mBluetoothAddress))) {
			showEnableSensors(shimmer.getListofSupportedSensors(), mService.getEnabledSensors(mBluetoothAddress));
			setTitle(R.string.assl_title_connected);
		}
		else {
			setTitle(R.string.assl_title_not_connected);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
    private Handler mHandler = new Handler() {
   

		public void handleMessage(Message msg) {
			switch (msg.what) {
            
            case Shimmer.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case Shimmer.STATE_CONNECTED:
                	//this has been deprecated
                    break;
                    
                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
                	Log.d("ShimmerActivity","Message Fully Initialized Received from Shimmer driver");
                	doSensors();
                    break;

                case Shimmer.STATE_CONNECTING:
                	Log.d("ShimmerActivity","Driver is attempting to establish connection with Shimmer device");
                    //mTitle.setText(R.string.title_connecting);
                    break;
                case Shimmer.STATE_NONE:
                    setProgressBarIndeterminateVisibility(false);
        			setTitle(R.string.assl_title_not_connected);
                	Log.d("ShimmerActivity","Shimmer No State");
                    //mTitle.setText(R.string.title_not_connected);;
                    //mBluetoothAddress=null;
                    // this also stops streaming
                    break;
                }
                break;
            
            case Shimmer.MESSAGE_READ:
                break;

            case Shimmer.MESSAGE_ACK_RECEIVED:
            	
            	break;
            case Shimmer.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                
                //Toast.makeText(this, "Connected to " + mBluetoothAddress, Toast.LENGTH_SHORT).show();
                break;
       
            	
            case Shimmer.MESSAGE_TOAST:
                //Toast.makeText(this, msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
                break;
           
            }
        }
    };
	
}
