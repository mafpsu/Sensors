package edu.pdx.cecs.orcyclesensors;

import com.google.common.collect.BiMap;

import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;
import edu.pdx.cecs.orcyclesensors.ShimmerService;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private long mEnabledSensors = -1;
    private int mShimmerVersion = -1;
    
    private ListView listView;
    private Button buttonDone;
    private Button buttonTryAgain;
    private Handler mHandler = new ShimmerMessageHandler();

	// *********************************************************************************
	// *                          Fragment Life Cycle
	// *********************************************************************************

    /**
     * Initialize Activity and inflate the UI
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

		// Set window features
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_shimmer_sensor_view);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        buttonDone = (Button) findViewById(R.id.assl_btn_done);
		buttonDone.setText("Cancel");
        buttonDone.setOnClickListener(new DoneButton_OnClickListener());
		buttonTryAgain = (Button) findViewById(R.id.assl_btn_try_again);
		buttonTryAgain.setVisibility(View.GONE);
		buttonTryAgain.setOnClickListener(new ButtonTryAgain_OnClickListener());

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
	  		mService.setMessageHandler(mHandler);
			mService.connectShimmer(mBluetoothAddress, "Device");
		}
  		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
  		}
	}

	/**
	 * 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		try {
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
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
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
			savedInstanceState.putString(EXTRA_BLUETOOTH_ADDRESS, mBluetoothAddress);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(savedInstanceState);
	}
	
	/**
	 * 
	 */
	@Override
	public void onPause() {
		super.onPause();
		try {
			if ((null != mBluetoothAddress) && !mBluetoothAddress.equals("") && (null != mService)) {
				mService.disconnectShimmer(mBluetoothAddress);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
    
	// *********************************************************************************
	// *                          Button OnClickListeners
	// *********************************************************************************

	private final class ButtonTryAgain_OnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				setTitle(mBluetoothAddress + " " + getString(R.string.assl_title_connecting));
		        setProgressBarIndeterminateVisibility(true);
				buttonTryAgain.setVisibility(View.GONE);
				mService.connectShimmer(mBluetoothAddress, "Device");
			}
	  		catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
	  		}
		}
	}

	private final class DoneButton_OnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (null != mService) {
				mService.setEnabledSensors(mEnabledSensors, mBluetoothAddress);
	            Intent intent = new Intent();
	            intent.putExtra(EXTRA_BLUETOOTH_ADDRESS, mBluetoothAddress);
	            setResult(Activity.RESULT_OK, intent);              // Set result and finish this Activity
			}
			finish();
		}
	}

	// *********************************************************************************
	// *                          Shimmer Message Handler
	// *********************************************************************************

	private final class ShimmerMessageHandler extends Handler {
		
		public void handleMessage(Message msg) {
			
			try {
				switch (msg.what) {
	            
	            case Shimmer.MESSAGE_STATE_CHANGE:
	            	
	                switch (msg.arg1) {
	                
	                case Shimmer.STATE_CONNECTED:
	                    break;
	                    
	                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
	                    setProgressBarIndeterminateVisibility(false);
	            		buttonDone.setText("Done");
	        			buttonTryAgain.setVisibility(View.GONE);
	        			showEnableSensors();
	                    break;
	
	                case Shimmer.STATE_CONNECTING:
	                    break;
	                    
	                case Shimmer.STATE_NONE:
	                    setProgressBarIndeterminateVisibility(false);
	        			setTitle(R.string.assl_title_not_connected);
	            		buttonDone.setText("Cancel");
	        			buttonTryAgain.setVisibility(View.VISIBLE);
	                    break;
	                }
	                break;
	            
	            case Shimmer.MESSAGE_READ:
	                break;
	
	            case Shimmer.MESSAGE_ACK_RECEIVED:
	            	
	            	break;
	            case Shimmer.MESSAGE_DEVICE_NAME:
	                break;
	       
	            	
	            case Shimmer.MESSAGE_TOAST:
	                break;
	            }
	        }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	/**
	 * Display the list of enabled sensors
	 */
	private void showEnableSensors() {
		
		Shimmer shimmer;

		// the displayed list depends on the shimmer version
		mShimmerVersion = mService.getShimmerVersion(mBluetoothAddress);

		// get the shimmer object
		if (null != (shimmer = mService.getShimmer(mBluetoothAddress))) {
			
			// get the list of sensor names
			final String[] sensorNames = shimmer.getListofSupportedSensors();
			
			// get the bit field value corresponding to the enabled sensors
			mEnabledSensors = mService.getEnabledSensors(mBluetoothAddress);
			
			// get the list GUI elements
			listView = (ListView) findViewById(android.R.id.list);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(
					this, 
					android.R.layout.simple_list_item_multiple_choice,
					android.R.id.text1, 
					sensorNames);
			listView.setAdapter(adapterSensorNames);
			
			// translate the bit field value to specific sensor names to be displayed
			final BiMap<String, String> sensorBitmaptoName;
			sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(mShimmerVersion);

			for (int i = 0; i < sensorNames.length; i++) {
				// get the bit mask for the given sensor name
				int bitMask = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));
				if ((bitMask & mEnabledSensors) > 0) {
					listView.setItemChecked(i, true);
				}
			}

			listView.setOnItemClickListener(new ListView_OnClickListener(sensorBitmaptoName, sensorNames));
			setTitle(R.string.assl_title_connected);
		}
		else {
			setTitle(R.string.assl_title_not_connected);
		}
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
			mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors, sensorIdentifier);
			// update the checkbox accordingly
			for (int i = 0; i < sensorNames.length; i++) {
				int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));
				if ((iDBMValue & mEnabledSensors) > 0) {
					listView.setItemChecked(i, true);
				} else {
					listView.setItemChecked(i, false);
				}
			}
		}
	}
}
