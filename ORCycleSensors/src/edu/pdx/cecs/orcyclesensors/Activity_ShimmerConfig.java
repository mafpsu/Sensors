package edu.pdx.cecs.orcyclesensors;

import com.google.common.collect.BiMap;

import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.FormatCluster;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ObjectCluster;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails.HW_ID;
import edu.pdx.cecs.orcyclesensors.R.id;
import edu.pdx.cecs.orcyclesensors.ShimmerService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;

public class Activity_ShimmerConfig extends ListActivity {

	public static final String MODULE_TAG = "Activity_ShimmerCommands";
	public static final String EXTRA_BLUETOOTH_ADDRESS = "EXTRA_BLUETOOTH_ADDRESS";
	
	private static final int REQUEST_ENABLE_BT = 1;

	private enum ShimmerConfigView { viewScanning, viewScanSuccess, viewScanFailed, viewSensors, viewCommands, viewExg};
	private ShimmerConfigView currentView = ShimmerConfigView.viewScanning;


	private ShimmerService mService = null;
	private String mBluetoothAddress = "";
    private long mEnabledSensors = -1;
    private int mShimmerVersion = -1;
    private String mShimmerFWVersion = "unknown";
    private int exgRes;
    
	private MenuItem mnuCommands;
	private MenuItem mnuSensors;
	private MenuItem mnuExg;
	private MenuItem mnuClose;
    private ListView lvSensors;
    private Button buttonDone;
    private Button buttonTryAgain;
    // Activity_ShimmerCommands Private variables
    
	private final String[] samplingRate = new String [] {"8","16","51.2","102.4","128","204.8","256","512","1024","2048"};
    private final String[] accelRangeArray = {"+/- 1.5g","+/- 6g"};
	private final String[] exgGain = new String [] {"6","1","2","3","4","8","12"};
	private final String[] exgResolution = new String [] {"16 bits","24 bits"};

    private ScrollView svCommandButtons;
    private LinearLayout ll_asc_commands;
    private LinearLayout ll_asc_exg;
    private CheckBox cBoxLowPowerMag;
    private CheckBox cBoxLowPowerAccel;
    private CheckBox cBoxLowPowerGyro;
    private CheckBox cBox5VReg;
    private CheckBox cBoxInternalExpPower;
    private Button buttonGyroRange;
    private Button buttonMagRange;
    private Button buttonGsr;
    private Button buttonPressureResolution;
    private Button buttonSampleRate;
    private Button buttonAccRange;
    private Button buttonBattVoltLimit;
    private Button buttonToggleLED;
    private Button buttonExgGain;
    private Button buttonExgRes;

    private Button buttonReferenceElectrode;
    private Button buttonLeadOffCurrent;
    private Button buttonLeadOffDetection;
    private Button buttonLeadOffComparator;
    
    private TextView tvDevice;
    private TextView tvVersion;
    private TextView tvFWVersion;
    
	static String exgMode="";
	static ImageView statusCircle1, statusCircle2, statusCircle3, statusCircle4, statusCircle5;
	static TextView chip1Item1, chip1Item2, chip1Item3, chip1Item4, chip1Item5, chip1Item6, chip1Item7, chip1Item8, chip1Item9, chip1Item10;
	static TextView chip2Item1, chip2Item2, chip2Item3, chip2Item4, chip2Item5, chip2Item6, chip2Item7, chip2Item8, chip2Item9, chip2Item10;
	static byte[] exgChip1Array = new byte[10];
	static byte[] exgChip2Array = new byte[10];

    
    
    
    private Handler shimmerMessageHandler = new ShimmerMessageHandler();
    

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

		try {
			// Set window features
	        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	        setContentView(R.layout.activity_shimmer_config);
	
	        // Set result CANCELED in case the user backs out
	        setResult(Activity.RESULT_CANCELED);
	
	        tvDevice = (TextView) findViewById(R.id.tv_asc_device);
	        tvVersion = (TextView) findViewById(R.id.tv_asc_version);
	        tvFWVersion = (TextView) findViewById(R.id.tv_asc_firmware);
	        buttonDone = (Button) findViewById(R.id.assl_btn_done);
	        buttonDone.setOnClickListener(new ButtonDone_OnClickListener());
			buttonTryAgain = (Button) findViewById(R.id.assl_btn_try_again);
			buttonTryAgain.setOnClickListener(new ButtonTryAgain_OnClickListener());
			// get the list GUI elements
			lvSensors = (ListView) findViewById(android.R.id.list);
			lvSensors.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			svCommandButtons = (ScrollView) findViewById(R.id.sv_asc_commands);
			ll_asc_commands = (LinearLayout) findViewById(R.id.ll_asc_commands);
			ll_asc_exg = (LinearLayout) findViewById(R.id.ll_asc_exg);
	
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
			}
	        tvDevice.setText("Device: " + mBluetoothAddress);
			
			// Commands window
	        // Set an EditText view to get user input 
	        final EditText editTextBattLimit = new EditText(getApplicationContext());
	        editTextBattLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	        
	        buttonPressureResolution = (Button) findViewById(R.id.buttonPressureAccuracy);
			buttonGyroRange = (Button) findViewById(R.id.buttonGyroRange);
			buttonMagRange = (Button) findViewById(R.id.buttonMagRange);
			buttonGsr = (Button) findViewById(R.id.buttonGSR);
			buttonSampleRate = (Button) findViewById(R.id.buttonRate);
			buttonAccRange = (Button) findViewById(R.id.buttonAccel);
			buttonToggleLED = (Button) findViewById(R.id.buttonToggleLED);
			buttonBattVoltLimit = (Button) findViewById(R.id.buttonBattLimit);
	        buttonExgGain = (Button) findViewById(R.id.buttonExgGain);
	        buttonExgRes = (Button) findViewById(R.id.buttonExgRes);
	        
	        buttonReferenceElectrode = (Button) findViewById(R.id.buttonReferenceElectrode);
	        buttonLeadOffCurrent = (Button) findViewById(R.id.buttonLeadOffCurrent);
	        buttonLeadOffDetection = (Button) findViewById(R.id.buttonLeadOffDetection);
	        buttonLeadOffComparator = (Button) findViewById(R.id.buttonLeadOffComparator);
	        
			cBox5VReg = (CheckBox) findViewById(R.id.checkBox5VReg);
			cBoxLowPowerMag = (CheckBox) findViewById(R.id.checkBoxLowPowerMag);
			cBoxLowPowerAccel = (CheckBox) findViewById(R.id.checkBoxLowPowerAccel);
			cBoxLowPowerGyro = (CheckBox) findViewById(R.id.checkBoxLowPowerGyro);
			cBoxInternalExpPower  = (CheckBox) findViewById(R.id.CheckBoxIntExpPow);
	
	        buttonAccRange.setText("Accel Range" + "\n" + "(N/A)");
	        buttonGsr.setText("GSR Range" + "\n(N/A)");                  
	        
	        // ------------------------------------------------------------------------------
	        // Toggle LED
			
	        buttonToggleLED.setOnClickListener(new ButtonToggleLED_OnClickListener());
	
	        // ------------------------------------------------------------------------------
	        // Set Battery Limit
			
	        final AlertDialog.Builder dialogBattLimit = new AlertDialog.Builder(this);
			dialogBattLimit.setTitle("Battery Limit");
			dialogBattLimit.setMessage("Introduce the battery limit to be set");
			dialogBattLimit.setPositiveButton("Ok", new DialogBattLimit_OnClickListener(editTextBattLimit));
	        
			buttonBattVoltLimit.setText("Set Batt Limit " + "\n" + "(N/A V)");
			buttonBattVoltLimit.setOnClickListener(new ButtonBattVoltLimit_OnClickListener(editTextBattLimit, dialogBattLimit));
	
	        // ------------------------------------------------------------------------------
	    	// Sampling Rate
			
			final AlertDialog.Builder dialogRate = new AlertDialog.Builder(this);		 
	        dialogRate.setTitle("Sample Rate");
	        dialogRate.setItems(samplingRate, new DialogRate_OnClickListener());
	    	
	    	buttonSampleRate.setOnClickListener(new ButtonSampleRate_OnClickListener(dialogRate));
	
	        // ------------------------------------------------------------------------------
	    	// Accel Range
	    	
	    	final AlertDialog.Builder dialogAccelShimmer2 = new AlertDialog.Builder(this);		 
	    	dialogAccelShimmer2.setTitle("Accelerometer range");
	    	dialogAccelShimmer2.setItems(accelRangeArray, new DialogAccelShimmer2_OnClickListener());
	
	        // ------------------------------------------------------------------------------
	    	
	    	final AlertDialog.Builder dialogAccelShimmer3 = new AlertDialog.Builder(this);		 
	    	dialogAccelShimmer3.setTitle("Accelerometer range");
	    	dialogAccelShimmer3.setItems(Configuration.Shimmer3.ListofAccelRange, new DialogAccelShimmer3_OnClickListener());
	
	        buttonAccRange.setOnClickListener(new ButtonAccRange_OnClickListener(dialogAccelShimmer3, dialogAccelShimmer2));
	
	        // ------------------------------------------------------------------------------
	        // Gyro Range
	        
	        final AlertDialog.Builder dialogGyroRangeShimmer3 = new AlertDialog.Builder(this);		 
	        dialogGyroRangeShimmer3.setTitle("Gyroscope Range");
	        dialogGyroRangeShimmer3.setItems(Configuration.Shimmer3.ListofGyroRange, new DialogGyroRangeShimmer3_OnClickListener());
	        
	        buttonGyroRange.setOnClickListener(new ButtonGyroRange_OnClickListener(dialogGyroRangeShimmer3));
	
	        // ------------------------------------------------------------------------------
	        // Mag Range
	        
	        final AlertDialog.Builder dialogMagRangeShimmer2 = new AlertDialog.Builder(this);		 
	        dialogMagRangeShimmer2.setTitle("Magnetometer Range");
	        dialogMagRangeShimmer2.setItems(Configuration.Shimmer2.ListofMagRange, new DialogMagRangeShimmer2_OnClickListener());
	        
	        final AlertDialog.Builder dialogMagRangeShimmer3 = new AlertDialog.Builder(this);		 
	        dialogMagRangeShimmer3.setTitle("Magnetometer Range");
	        dialogMagRangeShimmer3.setItems(Configuration.Shimmer3.ListofMagRange, new DialogMagRangeShimmer3_OnClickListener());
	        
	        buttonMagRange.setOnClickListener(new ButtonMagRange_OnClickListener(dialogMagRangeShimmer3, dialogMagRangeShimmer2));
	
	        // ------------------------------------------------------------------------------
	        // Pressure Res
	        
	        final AlertDialog.Builder dialogPressureResolutionShimmer3 = new AlertDialog.Builder(this);		 
	        dialogPressureResolutionShimmer3.setTitle("Pressure Resolution");
	        dialogPressureResolutionShimmer3.setItems(Configuration.Shimmer3.ListofPressureResolution, new DialogPressureResolutionShimmer3_OnClickListener());
	        
	        buttonPressureResolution.setOnClickListener(new ButtonPressureResolution_OnClickListener(dialogPressureResolutionShimmer3));
	        
	        // ------------------------------------------------------------------------------
	        // GSR Range is the same for the Shimmer 3 and the Shimmer 2 so we only need to do one dialog
	        
	        final AlertDialog.Builder dialogGsrRange = new AlertDialog.Builder(this);		 
	        dialogGsrRange.setTitle("Gsr Range");
	        dialogGsrRange.setItems(Configuration.Shimmer3.ListofGSRRange, new DialogGsrRange_OnClickListener());
	        
	        buttonGsr.setOnClickListener(new ButtonGsr_OnClickListener(dialogGsrRange));

	        // ------------------------------------------------------------------------------
	        // Exg Gain
	        
	        final AlertDialog.Builder dialogExgGain = new AlertDialog.Builder(this);
	        dialogExgGain.setTitle("ExG Gain");
	        dialogExgGain.setItems(exgGain, new DialogExgGain_OnClickListener());
	        
	        buttonExgGain.setOnClickListener(new ButtonExgGain_OnClickListener(dialogExgGain));

	        // ------------------------------------------------------------------------------
	        // Exg Res
	        
	        final AlertDialog.Builder dialogExgRes = new AlertDialog.Builder(this);
	        dialogExgRes.setTitle("ExG Resolution");
	        dialogExgRes.setItems(exgResolution, new DialogExgRes_OnClickListener());
	        
	        buttonExgRes.setOnClickListener(new ButtonExgRes_OnClickListener(dialogExgRes));
	        
	        // ------------------------------------------------------------------------------
	        // Ref. Electrode

	    	final AlertDialog.Builder ecgDialogReference = new AlertDialog.Builder(this);
	    	final String [] ecgListOfReference = Configuration.Shimmer3.ListOfECGReferenceElectrode;
	    	ecgDialogReference.setTitle("ECG Reference Electrode");
	    	ecgDialogReference.setItems(ecgListOfReference, new DialogECGReference_OnClickListener(ecgListOfReference));

	    	final AlertDialog.Builder emgDialogReference = new AlertDialog.Builder(this);
	    	final String [] emgListOfReference = Configuration.Shimmer3.ListOfEMGReferenceElectrode;
	    	emgDialogReference.setTitle("EMG Reference Electrode");
	    	emgDialogReference.setItems(emgListOfReference, new DialogEMGReference_OnClickListener(emgListOfReference));

	    	buttonReferenceElectrode.setOnClickListener(new ButtonReferenceElectrode_OnClickListener(ecgDialogReference,
					emgDialogReference));

	        // ------------------------------------------------------------------------------
			// Lead-Off Detection

	    	final AlertDialog.Builder dialogLeadOffDetection = new AlertDialog.Builder(this);
	    	dialogLeadOffDetection.setTitle("Lead-Off Detection Mode");
	    	dialogLeadOffDetection.setItems(Configuration.Shimmer3.ListOfExGLeadOffDetection, new DialogLeadOffDetection_OnClickListener());
	    	
	    	buttonLeadOffDetection.setOnClickListener(new ButtonLeadOffDetection_OnClickListener(dialogLeadOffDetection));

	        // ------------------------------------------------------------------------------
			// Lead-Off Current

			final AlertDialog.Builder dialogLeadOffCurrent = new AlertDialog.Builder(this);
			dialogLeadOffCurrent.setTitle("Lead-Off Current");
			dialogLeadOffCurrent.setItems(Configuration.Shimmer3.ListOfExGLeadOffCurrent, new DialogLeadOffCurrent_OnClickListener());
			
			buttonLeadOffCurrent.setOnClickListener(new ButtonLeadOffCurrent_OnClickListener(dialogLeadOffCurrent));

	        // ------------------------------------------------------------------------------
			// Lead-Off Comparator
			
			final AlertDialog.Builder dialogLeadOffComparator = new AlertDialog.Builder(this);
			dialogLeadOffComparator.setTitle("Lead-Off Comparator Threshold");
			dialogLeadOffComparator.setItems(Configuration.Shimmer3.ListOfExGLeadOffComparator, new DialogLeadOffComparator_OnClickListener());
			
			buttonLeadOffComparator.setOnClickListener(new ButtonLeadOffComparator_OnClickListener(dialogLeadOffComparator));
	        
	        // ------------------------------------------------------------------------------
	        
	        statusCircle1 = (ImageView) findViewById(R.id.imageLeadOff1);
	        statusCircle2 = (ImageView) findViewById(R.id.imageLeadOff2);
	        statusCircle3 = (ImageView) findViewById(R.id.imageLeadOff3);
	        statusCircle4 = (ImageView) findViewById(R.id.imageLeadOff4);
	        statusCircle5 = (ImageView) findViewById(R.id.imageLeadOff5);
	        
	        chip1Item1 = (TextView) findViewById(R.id.texChip1_1);
	        chip1Item2 = (TextView) findViewById(R.id.texChip1_2);
	        chip1Item3 = (TextView) findViewById(R.id.texChip1_3);
	        chip1Item4 = (TextView) findViewById(R.id.texChip1_4);
	        chip1Item5 = (TextView) findViewById(R.id.texChip1_5);
	        chip1Item6 = (TextView) findViewById(R.id.texChip1_6);
	        chip1Item7 = (TextView) findViewById(R.id.texChip1_7);
	        chip1Item8 = (TextView) findViewById(R.id.texChip1_8);
	        chip1Item9 = (TextView) findViewById(R.id.texChip1_9);
	        chip1Item10 = (TextView) findViewById(R.id.texChip1_10);
	        
	        chip2Item1 = (TextView) findViewById(R.id.texChip2_1);
	        chip2Item2 = (TextView) findViewById(R.id.texChip2_2);
	        chip2Item3 = (TextView) findViewById(R.id.texChip2_3);
	        chip2Item4 = (TextView) findViewById(R.id.texChip2_4);
	        chip2Item5 = (TextView) findViewById(R.id.texChip2_5);
	        chip2Item6 = (TextView) findViewById(R.id.texChip2_6);
	        chip2Item7 = (TextView) findViewById(R.id.texChip2_7);
	        chip2Item8 = (TextView) findViewById(R.id.texChip2_8);
	        chip2Item9 = (TextView) findViewById(R.id.texChip2_9);
	        chip2Item10 = (TextView) findViewById(R.id.texChip2_10);

	        // ------------------------------------------------------------------------------

	        currentView = ShimmerConfigView.viewScanning;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
    }
    
	/**
	 * Creates the menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			// Inflate the menu items for use in the action bar
			getMenuInflater().inflate(R.menu.activity_shimmer_config, menu);

			mnuCommands = menu.findItem(id.action_asc_commands);
			mnuSensors = menu.findItem(id.action_asc_sensors);
			mnuExg = menu.findItem(id.action_asc_exg);
			mnuClose = menu.findItem(id.action_asc_close);
			setCurrentView(currentView);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Handles item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {

			case R.id.action_asc_commands:
				setCurrentView(ShimmerConfigView.viewCommands);
				return true;

			case R.id.action_asc_sensors:
				setCurrentView(ShimmerConfigView.viewSensors);
				return true;

			case R.id.action_asc_exg:
				setCurrentView(ShimmerConfigView.viewExg);
				return true;

			case R.id.action_asc_close:

				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return super.onOptionsItemSelected(item);
	}

	public String getShimmerVersion(int version) {
		switch(version) {
        case HW_ID.SHIMMER_1: return "Shimmer 1";
        case HW_ID.SHIMMER_2: return "Shimmer 2";
        case HW_ID.SHIMMER_2R: return "Shimmer 2r";
        case HW_ID.SHIMMER_3: return "Shimmer 3";
        case HW_ID.SHIMMER_SR30: return "Shimmer SR30";
        case HW_ID.SHIMMER_GQ: return "Shimmer GQ";
        default: return "unknown";
		}
	}
	
    /**
     * 
     */
	@Override
	public void onResume() {
		super.onResume();

		try {
	  		mService.setMessageHandler(shimmerMessageHandler);
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
	// *                              View Management
	// *********************************************************************************

	private void setCurrentView(ShimmerConfigView shimmerConfigView) {

		switch (shimmerConfigView) {

		case viewScanning:

			setTitle(R.string.assl_title_connecting);
	        setProgressBarIndeterminateVisibility(true);
			buttonDone.setText("Cancel");
			buttonTryAgain.setVisibility(View.GONE);

			if (null != mnuCommands) {
				mnuCommands.setVisible(false);
			}
			if (null != mnuSensors) {
				mnuSensors.setVisible(false);
			}
			if (null != mnuExg) {
				mnuExg.setVisible(false);
			}
			if (null != mnuClose) {
				mnuClose.setVisible(false);
			}
			break;

		case viewScanSuccess:

			setTitle("");
	        setProgressBarIndeterminateVisibility(false);
    		buttonDone.setText("Done");
			buttonTryAgain.setVisibility(View.GONE);

			if (null != mnuCommands) {
				mnuCommands.setVisible(true);
			}
			if (null != mnuSensors) {
				mnuSensors.setVisible(true);
			}
			if (null != mnuExg) {
				mnuExg.setVisible(true);
			}
			if (null != mnuClose) {
				mnuClose.setVisible(true);
			}
			
			tvVersion.setText("Version: " + getShimmerVersion(mShimmerVersion));
			tvFWVersion.setText("Firmware: " + mShimmerFWVersion);
			break;

		case viewScanFailed:

			setTitle(R.string.assl_title_not_connected);
	        setProgressBarIndeterminateVisibility(false);
			buttonDone.setText("Cancel");
			buttonTryAgain.setVisibility(View.VISIBLE);

			svCommandButtons.setVisibility(View.INVISIBLE);
			ll_asc_commands.setVisibility(View.INVISIBLE);
			lvSensors.setVisibility(View.INVISIBLE);
			ll_asc_exg.setVisibility(View.INVISIBLE);
			if ((null != mnuCommands) && (null != mnuSensors) && (null != mnuExg)) {
				mnuCommands.setVisible(false);
				mnuSensors.setVisible(false);
				mnuExg.setVisible(false);
			}
			break;

		case viewCommands:

			svCommandButtons.setVisibility(View.VISIBLE);
			ll_asc_commands.setVisibility(View.VISIBLE);
			lvSensors.setVisibility(View.INVISIBLE);
			ll_asc_exg.setVisibility(View.INVISIBLE);
			if ((null != mnuCommands) && (null != mnuSensors) && (null != mnuExg)) {
				mnuCommands.setVisible(false);
				mnuSensors.setVisible(true);
				mnuExg.setVisible(true);
			}
			break;

		case viewSensors:

			svCommandButtons.setVisibility(View.INVISIBLE);
			ll_asc_commands.setVisibility(View.INVISIBLE);
			lvSensors.setVisibility(View.VISIBLE);
			ll_asc_exg.setVisibility(View.INVISIBLE);
			if ((null != mnuCommands) && (null != mnuSensors) && (null != mnuExg)) {
				mnuCommands.setVisible(true);
				mnuSensors.setVisible(false);
				mnuExg.setVisible(true);
			}
			break;

		case viewExg:

			svCommandButtons.setVisibility(View.INVISIBLE);
			ll_asc_commands.setVisibility(View.INVISIBLE);
			lvSensors.setVisibility(View.INVISIBLE);
			ll_asc_exg.setVisibility(View.VISIBLE);
			if ((null != mnuCommands) && (null != mnuSensors) && (null != mnuExg)) {
				mnuCommands.setVisible(true);
				mnuSensors.setVisible(true);
				mnuExg.setVisible(false);
			}
			printExGArrays(mService.getShimmer(mBluetoothAddress));
			break;
		}
		currentView = shimmerConfigView;
	}
	
	/**
	 * Display the list of enabled sensors
	 */
	private void showEnableSensors(Shimmer shimmer, int shimmerVersion) {

		// get the list of sensor names
		final String[] supportedSensors = shimmer.getListofSupportedSensors();
		
		String[] sensorNames = new String[supportedSensors.length+2]; 
		//remove from the list of sensors EXG1, EXG2, EXG1 16bits, EXG2 16bits, and add ECG,EMG,TestSignal,ECG 16Bits,EMG 16Bits, TestSignal 16Bits
		for(int i=0; i<supportedSensors.length-5;i++)
			sensorNames[i] = supportedSensors[i];
		
		sensorNames[supportedSensors.length-5] = supportedSensors[supportedSensors.length-1]; // add "Strain gauge" which is the last one in the sensor list
		sensorNames[supportedSensors.length-4] = "ECG";
		sensorNames[supportedSensors.length-3] = "ECG 16Bit";
		sensorNames[supportedSensors.length-2] = "EMG";
		sensorNames[supportedSensors.length-1] = "EMG 16Bit";
		sensorNames[supportedSensors.length] = "Test Signal";
		sensorNames[supportedSensors.length+1] = "Test Signal 16Bit";
		
		// Create an adapter to hold sensor names
		ArrayAdapter<String> adapterSensorNames = new ArrayAdapter<String>(
				this, 
				android.R.layout.simple_list_item_multiple_choice,
				android.R.id.text1, 
				sensorNames);
		
		// Attach adapter to ListView
		lvSensors.setAdapter(adapterSensorNames);
		
		// translate the bit field value to specific sensor names to be displayed
		final BiMap<String, String> sensorBitmaptoName;
		sensorBitmaptoName = Shimmer.generateBiMapSensorIDtoSensorName(shimmerVersion);

		// get the bit field value corresponding to the enabled sensors
		mEnabledSensors = mService.getEnabledSensors(mBluetoothAddress);
		
		for (int i = 0; i < sensorNames.length; i++) {
			
			// get the bit mask for the given sensor name
			if(shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3 && 
					(sensorNames[i].equals("ECG") || sensorNames[i].equals("EMG") || sensorNames[i].equals("Test Signal") ||
					sensorNames[i].equals("ECG 16Bit") || sensorNames[i].equals("EMG 16Bit") || sensorNames[i].equals("Test Signal 16Bit"))){
				
				if(sensorNames[i].equals("ECG") && mService.isEXGUsingECG24Configuration(mBluetoothAddress))
					lvSensors.setItemChecked(i, true);
				else if(sensorNames[i].equals("ECG 16Bit") && mService.isEXGUsingECG16Configuration(mBluetoothAddress))
					lvSensors.setItemChecked(i, true);
				else if(sensorNames[i].equals("EMG") && mService.isEXGUsingEMG24Configuration(mBluetoothAddress))
					lvSensors.setItemChecked(i, true);
				else if(sensorNames[i].equals("EMG 16Bit") && mService.isEXGUsingEMG16Configuration(mBluetoothAddress))
					lvSensors.setItemChecked(i, true);
				else if(sensorNames[i].equals("Test Signal") && mService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
					lvSensors.setItemChecked(i, true);
				else if(sensorNames[i].equals("Test Signal 16Bit") && mService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
					lvSensors.setItemChecked(i, true);
			}
			else{	
				int bitMask = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));
				if ((bitMask & mEnabledSensors) > 0) {
					lvSensors.setItemChecked(i, true);
				}
			}
		}
		lvSensors.setOnItemClickListener(new Sensors_OnClickListener(sensorBitmaptoName, sensorNames));
	}
	
	public String[] createListWithNewExgNames(String[] sensors) {
		
		String[] newSensorNames = new String[sensors.length+2]; 

		// add old sensor names to new list
		for(int i = 0; i < sensors.length - 5; i++)
			newSensorNames[i] = sensors[i];
		
		//remove from the list of sensors EXG1, EXG2, EXG1 16bits, EXG2 16bits, and add ECG,EMG,TestSignal,ECG 16Bits,EMG 16Bits, TestSignal 16Bits
		newSensorNames[sensors.length-5] = sensors[sensors.length-1]; // add "Strain gauge" which is the last one in the sensor list
		newSensorNames[sensors.length-4] = "ECG";
		newSensorNames[sensors.length-3] = "ECG 16Bit";
		newSensorNames[sensors.length-2] = "EMG";
		newSensorNames[sensors.length-1] = "EMG 16Bit";
		newSensorNames[sensors.length] = "Test Signal";
		newSensorNames[sensors.length+1] = "Test Signal 16Bit";
		return newSensorNames;
	}

	public void updateCommandsView(Shimmer shimmer, int shimmerVersion) {

		Log.d("ShimmerService", "service connected");

    	double mSamplingRateV = mService.getSamplingRate(mBluetoothAddress);
    	int mAccelerometerRangeV = mService.getAccelRange(mBluetoothAddress);
    	int mGSRRangeV = mService.getGSRRange(mBluetoothAddress);
    	final double batteryLimit = mService.getBattLimitWarning(mBluetoothAddress);

		buttonGsr.setText("GSR Range" + "\n" + Configuration.Shimmer3.ListofGSRRange[mGSRRangeV]);
		buttonSampleRate.setText("Sampling Rate " + "\n(" + Double.toString(mSamplingRateV) + ") Hz");
		buttonBattVoltLimit.setText("Set Batt Limit " + "\n" + "(" + Double.toString(batteryLimit) + " V)");

        if (mAccelerometerRangeV == 0) {
			if (shimmerVersion != ShimmerVerDetails.HW_ID.SHIMMER_3) {
				buttonAccRange.setText("Accel Range" + "\n" + "(+/- 1.5g)");
			}
			else {
				buttonAccRange.setText("Accel Range" + "\n" + "(+/- 2g)");
			}
		}
		else if (mAccelerometerRangeV == 1) {
			buttonAccRange.setText("Accel Range" + "\n" + "(+/- 4g)");
		}
		else if (mAccelerometerRangeV == 2) {
			buttonAccRange.setText("Accel Range" + "\n" + "(+/- 8g)");
		} 
		else if (mAccelerometerRangeV == 3) {
			if (shimmerVersion != ShimmerVerDetails.HW_ID.SHIMMER_3) {
				buttonAccRange.setText("Accel Range" + "\n" + "(+/- 6g)");
			}
			else {
				buttonAccRange.setText("Accel Range" + "\n" + "(+/- 16g)");
			}
		}
        
  		if (shimmer.getInternalExpPower()==1){
  			cBoxInternalExpPower.setChecked(true);
  		} else {
  			cBoxInternalExpPower.setChecked(false);
  		}
  		
  		if (shimmer.isLowPowerMagEnabled()){
    		cBoxLowPowerMag.setChecked(true);
    	}
    	
    	if (shimmer.isLowPowerAccelEnabled()){
    		cBoxLowPowerAccel.setChecked(true);
    	}
    	
    	if (shimmer.isLowPowerGyroEnabled()){
    		cBoxLowPowerGyro.setChecked(true);
    	}

    	if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3){
    		
    		// GSR
        	buttonGsr.setVisibility(View.VISIBLE);
        	
        	// 5V Reg
        	cBox5VReg.setEnabled(false);
        	
        	// Gyro
        	String currentGyroRange = "("+Configuration.Shimmer3.ListofGyroRange[shimmer.getGyroRange()]+")";
        	buttonGyroRange.setText("Gyro Range"+"\n"+currentGyroRange);
        	
        	// Magnetic Range
        	String currentMagRange = "("+Configuration.Shimmer3.ListofMagRange[shimmer.getMagRange()-1]+")";
    		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
    		
    		// Pressure resolution
    		String currentPressureResolution = "("+Configuration.Shimmer3.ListofPressureResolution[shimmer.getPressureResolution()]+")";
    		buttonPressureResolution.setText("Pressure Res"+"\n"+currentPressureResolution);
        	
    		// Accel Range
        	if (shimmer.getAccelRange()==0){
        		cBoxLowPowerAccel.setEnabled(false);
        	}
        	
        	//currently not supported for the moment 
    		buttonPressureResolution.setEnabled(true);
    		
    		// Reference Electrode
    		buttonReferenceElectrode.setEnabled(true);
        	int referenceElectrode = mService.getExGReferenceElectrode(mBluetoothAddress);
        	if(referenceElectrode==0)
        		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (Fixed Potencial)");
        	else if(referenceElectrode==13)
        		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (Inverse Wilson CT)");
        	else if(referenceElectrode==3)
        		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (Inverse of Ch1)");
        	else
        		buttonReferenceElectrode.setText("Ref. Electrode"+"\n (No Ref.)");
        	
    		// Lead Off Detection
        	buttonLeadOffDetection.setEnabled(true);
        	int leadOffDetection = mService.getExGLeadOffDetectionMode(mBluetoothAddress);
        	if(leadOffDetection!=-1)
        		buttonLeadOffDetection.setText("Lead-Off Detection"+"\n ("+Configuration.Shimmer3.ListOfExGLeadOffDetection[leadOffDetection]+")");
        	else
        		buttonLeadOffDetection.setText("Lead-Off Detection"+"\n (No detec.)");
        	
        	// Lead Off Current
        	buttonLeadOffCurrent.setEnabled(true);
        	int leadOffCurrent = mService.getExGLeadOffCurrent(mBluetoothAddress);
        	if(leadOffCurrent!=-1)
        		buttonLeadOffCurrent.setText("Lead-Off Current"+"\n ("+Configuration.Shimmer3.ListOfExGLeadOffCurrent[leadOffCurrent]+")");
        	else
        		buttonLeadOffCurrent.setText("Lead-Off Current"+"\n (No current)");
        	
        	// Lead Off Comparator
        	buttonLeadOffComparator.setEnabled(true);
        	int leadOffComparator = mService.getExGLeadOffComparatorTreshold(mBluetoothAddress);
        	if(leadOffComparator!=-1)
        		buttonLeadOffComparator.setText("Lead-Off Comparator"+"\n ("+Configuration.Shimmer3.ListOfExGLeadOffComparator[leadOffComparator]+")");
        	else
        		buttonLeadOffComparator.setText("Lead-Off Comparator"+"\n (No comp.)");
    	}
    	else {
        	buttonGsr.setVisibility(View.INVISIBLE);
    		cBoxInternalExpPower.setEnabled(false);
        	cBox5VReg.setEnabled(true);
    		buttonPressureResolution.setEnabled(false);
    		buttonGyroRange.setEnabled(false);
    		cBoxLowPowerAccel.setEnabled(false);
    		cBoxLowPowerGyro.setEnabled(false);
    		String currentMagRange = "("+Configuration.Shimmer2.ListofMagRange[shimmer.getMagRange()]+")";
    		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
    		buttonReferenceElectrode.setEnabled(false);
        	buttonLeadOffDetection.setEnabled(false);
        	buttonLeadOffCurrent.setEnabled(false);
        	buttonLeadOffComparator.setEnabled(false);
    	}
    	
  		//update the view
  		if (mService.get5VReg(mBluetoothAddress)==1){
  			cBox5VReg.setChecked(true);
  		}
  		
  		// EXG --------------------------------------------------

  		if(shimmerVersion != ShimmerVerDetails.HW_ID.SHIMMER_3){
        	buttonExgGain.setEnabled(false);
        	buttonExgRes.setEnabled(false);
        }
        else{
        	buttonExgGain.setEnabled(true);
        	buttonExgRes.setEnabled(true);
        }
        
    	int gain = mService.getEXGGain(mBluetoothAddress);
    	if(gain!=-1)
    		buttonExgGain.setText("EXG Gain"+"\n ("+gain+")");
    	else
    		buttonExgGain.setText("EXG Gain"+"\n (no gain set)");
    	
    	exgRes = mService.getEXGResolution(mBluetoothAddress);
    	if(exgRes==16 || exgRes==24)
    		buttonExgRes.setText("EXG Res"+"\n ("+exgRes+" bit)");
    	else
    		buttonExgRes.setText("EXG Res"+"\n (no res. set)");
        
    	cBox5VReg.setOnCheckedChangeListener(new CBox5VReg_OnCheckedChangeListener());
  		
  		cBoxLowPowerAccel.setOnCheckedChangeListener(new CBoxLowPowerAccel_OnCheckedChangeListener(shimmer));
  		
  		cBoxLowPowerGyro.setOnCheckedChangeListener(new CBoxLowPowerGyro_OnCheckedChangeListener(shimmer));
  		
  		cBoxInternalExpPower.setOnCheckedChangeListener(new CBoxInternalExpPower_OnCheckedChangeListener(shimmer));

  		cBoxLowPowerMag.setChecked(mService.isLowPowerMagEnabled(mBluetoothAddress));
  		
  		cBoxLowPowerMag.setOnCheckedChangeListener(new CBoxLowPowerMag_OnCheckedChangeListener());
	}

 	public static  void printExGArrays(Shimmer shimmer){
 		
 		exgChip1Array = shimmer.getExG1Register();
 		exgChip2Array = shimmer.getExG2Register();
 		
 		chip1Item1.setText(""+exgChip1Array[0]);
 		chip1Item2.setText(""+(int)exgChip1Array[1]);
 		chip1Item3.setText(""+exgChip1Array[2]);
 		chip1Item4.setText(""+exgChip1Array[3]);
 		chip1Item5.setText(""+exgChip1Array[4]);
 		chip1Item6.setText(""+exgChip1Array[5]);
 		chip1Item7.setText(""+exgChip1Array[6]);
 		chip1Item8.setText(""+exgChip1Array[7]);
 		chip1Item9.setText(""+exgChip1Array[8]);
 		chip1Item10.setText(""+exgChip1Array[9]);
 		
 		chip2Item1.setText(""+exgChip2Array[0]);
 		chip2Item2.setText(""+exgChip2Array[1]);
 		chip2Item3.setText(""+exgChip2Array[2]);
 		chip2Item4.setText(""+exgChip2Array[3]);
 		chip2Item5.setText(""+exgChip2Array[4]);
 		chip2Item6.setText(""+exgChip2Array[5]);
 		chip2Item7.setText(""+exgChip2Array[6]);
 		chip2Item8.setText(""+exgChip2Array[7]);
 		chip2Item9.setText(""+exgChip2Array[8]);
 		chip2Item10.setText(""+exgChip2Array[9]);
 	}

 	// *********************************************************************************
	// *                          Button OnClickListeners
	// *********************************************************************************

	private final class ButtonTryAgain_OnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				setTitle(R.string.assl_title_connecting);
		        setProgressBarIndeterminateVisibility(true);
		        setCurrentView(ShimmerConfigView.viewScanning);
				mService.connectShimmer(mBluetoothAddress, "Device");
			}
	  		catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
	  		}
		}
	}

	private final class ButtonDone_OnClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			if (null != mService) {
				mService.setEnabledSensors(mEnabledSensors, mBluetoothAddress);
	            Intent intent = new Intent();
	            intent.putExtra(EXTRA_BLUETOOTH_ADDRESS, mBluetoothAddress);
	            setResult(Activity.RESULT_OK, intent);              // Set result and finish this Activity
			}
			finish();
		}
	}

	private final class ButtonGsr_OnClickListener implements OnClickListener {
		
		private final Builder dialogGsrRange;

		private ButtonGsr_OnClickListener(Builder dialogGsrRange) {
			this.dialogGsrRange = dialogGsrRange;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogGsrRange.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonPressureResolution_OnClickListener implements OnClickListener {
		
		private final Builder dialogPressureResolutionShimmer3;

		private ButtonPressureResolution_OnClickListener(Builder dialogPressureResolutionShimmer3) {
			this.dialogPressureResolutionShimmer3 = dialogPressureResolutionShimmer3;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogPressureResolutionShimmer3.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonMagRange_OnClickListener implements OnClickListener {
		
		private final Builder dialogMagRangeShimmer3;
		private final Builder dialogMagRangeShimmer2;

		private ButtonMagRange_OnClickListener(Builder dialogMagRangeShimmer3, Builder dialogMagRangeShimmer2) {
			this.dialogMagRangeShimmer3 = dialogMagRangeShimmer3;
			this.dialogMagRangeShimmer2 = dialogMagRangeShimmer2;
		}

		@Override
		public void onClick(View v) {
			try {
				if (mService.getShimmerVersion(mBluetoothAddress) == ShimmerVerDetails.HW_ID.SHIMMER_3)
					dialogMagRangeShimmer3.show();
				else
					dialogMagRangeShimmer2.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonGyroRange_OnClickListener implements OnClickListener {
		
		private final Builder dialogGyroRangeShimmer3;

		private ButtonGyroRange_OnClickListener(Builder dialogGyroRangeShimmer3) {
			this.dialogGyroRangeShimmer3 = dialogGyroRangeShimmer3;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogGyroRangeShimmer3.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonAccRange_OnClickListener implements OnClickListener {
		
		private final Builder dialogAccelShimmer3;
		private final Builder dialogAccelShimmer2;

		private ButtonAccRange_OnClickListener(Builder dialogAccelShimmer3, Builder dialogAccelShimmer2) {
			this.dialogAccelShimmer3 = dialogAccelShimmer3;
			this.dialogAccelShimmer2 = dialogAccelShimmer2;
		}

		@Override
		public void onClick(View v) {
			try {
				if (mService.getShimmerVersion(mBluetoothAddress)!=ShimmerVerDetails.HW_ID.SHIMMER_3)
					dialogAccelShimmer2.show();
				else
					dialogAccelShimmer3.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonSampleRate_OnClickListener implements OnClickListener {
		
		private final Builder dialogRate;

		private ButtonSampleRate_OnClickListener(Builder dialogRate) {
			this.dialogRate = dialogRate;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogRate.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonBattVoltLimit_OnClickListener implements OnClickListener {
		
		private final EditText editTextBattLimit;
		private final Builder dialogBattLimit;

		private ButtonBattVoltLimit_OnClickListener(EditText editTextBattLimit, Builder dialogBattLimit) {
			this.editTextBattLimit = editTextBattLimit;
			this.dialogBattLimit = dialogBattLimit;
		}

		public void onClick(View arg0) {
			try {
				// This is done in order to avoid an error when the dialog is
				// displayed again after being cancelled
				if (editTextBattLimit.getParent() != null) {
					ViewGroup parentViewGroup = (ViewGroup) editTextBattLimit.getParent();
					parentViewGroup.removeView(editTextBattLimit);
				}
	
				dialogBattLimit.setView(editTextBattLimit);
				dialogBattLimit.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonToggleLED_OnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			try {
				mService.toggleLED(mBluetoothAddress);
				Toast.makeText(getApplicationContext(), "Toggle LED", Toast.LENGTH_SHORT).show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonExgGain_OnClickListener implements OnClickListener {
		private final Builder dialogExgGain;

		private ButtonExgGain_OnClickListener(Builder dialogExgGain) {
			this.dialogExgGain = dialogExgGain;
		}

		@Override
		public void onClick(View arg0) {
			if(mService.getShimmer(mBluetoothAddress).getFirmwareCode() > 2){
				dialogExgGain.show();
			}
			else
				Toast.makeText(getApplicationContext(), "Operation not supported in this FW Version", Toast.LENGTH_SHORT).show();
		}
	}

	private final class ButtonExgRes_OnClickListener implements OnClickListener {
		private final Builder dialogExgRes;

		private ButtonExgRes_OnClickListener(Builder dialogExgRes) {
			this.dialogExgRes = dialogExgRes;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			dialogExgRes.show();
		}
	}

	private final class ButtonReferenceElectrode_OnClickListener implements OnClickListener {
		private final Builder ecgDialogReference;
		private final Builder emgDialogReference;

		private ButtonReferenceElectrode_OnClickListener(Builder ecgDialogReference, Builder emgDialogReference) {
			this.ecgDialogReference = ecgDialogReference;
			this.emgDialogReference = emgDialogReference;
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(mService.isEXGUsingECG16Configuration(mBluetoothAddress) ||
					mService.isEXGUsingECG24Configuration(mBluetoothAddress))
				ecgDialogReference.show();
			else
				emgDialogReference.show();
		}
	}

	private final class ButtonLeadOffCurrent_OnClickListener implements OnClickListener {
		private final Builder dialogLeadOffCurrent;
		
		private ButtonLeadOffCurrent_OnClickListener(
				Builder dialogLeadOffCurrent) {
			this.dialogLeadOffCurrent = dialogLeadOffCurrent;
		}
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			dialogLeadOffCurrent.show();
		}
	}

	private final class ButtonLeadOffDetection_OnClickListener implements OnClickListener {
		private final Builder dialogLeadOffDetection;
		
		private ButtonLeadOffDetection_OnClickListener(
				Builder dialogLeadOffDetection) {
			this.dialogLeadOffDetection = dialogLeadOffDetection;
		}
		
		@Override
		public void onClick(View arg0) {
			dialogLeadOffDetection.show();
		}
	}

	private final class ButtonLeadOffComparator_OnClickListener implements OnClickListener {
		
		private final Builder dialogLeadOffComparator;
		
		private ButtonLeadOffComparator_OnClickListener(
				Builder dialogLeadOffComparator) {
			this.dialogLeadOffComparator = dialogLeadOffComparator;
		}
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			dialogLeadOffComparator.show();
		}
	}

	// *********************************************************************************
	// *                          Checkbox OnClickListeners
	// *********************************************************************************

	private final class CBoxInternalExpPower_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		private final Shimmer shimmer;

		private CBoxInternalExpPower_OnCheckedChangeListener(Shimmer shimmer) {
			this.shimmer = shimmer;
		}

		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					shimmer.writeInternalExpPower(1);
				} else {
					shimmer.writeInternalExpPower(0);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBoxLowPowerGyro_OnCheckedChangeListener implements OnCheckedChangeListener {
		private final Shimmer shimmer;

		private CBoxLowPowerGyro_OnCheckedChangeListener(Shimmer shimmer) {
			this.shimmer = shimmer;
		}

		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					shimmer.enableLowPowerGyro(true);
				} else {
					shimmer.enableLowPowerGyro(false);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBoxLowPowerAccel_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		private final Shimmer shimmer;

		private CBoxLowPowerAccel_OnCheckedChangeListener(Shimmer shimmer) {
			this.shimmer = shimmer;
		}

		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					shimmer.enableLowPowerAccel(true);
				} else {
					shimmer.enableLowPowerAccel(false);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBox5VReg_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					mService.write5VReg(mBluetoothAddress, 1);
				} else {
					mService.write5VReg(mBluetoothAddress, 0);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBoxLowPowerMag_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					mService.enableLowPowerMag(mBluetoothAddress, true);
				} else {
					mService.enableLowPowerMag(mBluetoothAddress, false);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                          Dialog OnClickListeners
	// *********************************************************************************

	private final class DialogGsrRange_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			
			try {
				Log.d("Shimmer",Configuration.Shimmer3.ListofGSRRange[item]);
			    int gsrRange = 0;
			    if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[0]){
			    	gsrRange = 0;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[1]){
			    	gsrRange = 1;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[2]){
			    	gsrRange = 2;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[3]){
			    	gsrRange = 3;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[4]){
			    	gsrRange = 4;
			    }
	
			    mService.writeGSRRange(mBluetoothAddress, gsrRange);
			    Toast.makeText(getApplicationContext(), "Gsr range changed. New range = "+Configuration.Shimmer3.ListofGSRRange[item], Toast.LENGTH_SHORT).show();
			    buttonGsr.setText("GSR Range"+"\n"+"("+Configuration.Shimmer3.ListofGSRRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogPressureResolutionShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer", Configuration.Shimmer3.ListofPressureResolution[item]);
				int pressureRes = 0;
	
				if (Configuration.Shimmer3.ListofPressureResolution[item] == "Low") {
					pressureRes = 0;
				} else if (Configuration.Shimmer3.ListofPressureResolution[item] == "Standard") {
					pressureRes = 1;
				} else if (Configuration.Shimmer3.ListofPressureResolution[item] == "High") {
					pressureRes = 2;
				} else if (Configuration.Shimmer3.ListofPressureResolution[item] == "Very High") {
					pressureRes = 3;
				}
	
				mService.writePressureResolution(mBluetoothAddress, pressureRes);
				Toast.makeText(getApplicationContext(), "Pressure resolution changed. New resolution = " + Configuration.Shimmer3.ListofPressureResolution[item], Toast.LENGTH_SHORT).show();
				buttonPressureResolution.setText("Pressure Res" + "\n" + "(" + Configuration.Shimmer3.ListofPressureResolution[item] + ")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogMagRangeShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int item) {
			try {
				 Log.d("Shimmer",Configuration.Shimmer3.ListofMagRange[item]);
				 int magRange=0;
			  
				 if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[0]){
					magRange=1;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[1]){
					magRange=2;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[2]){
					magRange=3;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[3]){
					magRange=4;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[4]){
					magRange=5;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[5]){
					magRange=6;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[6]){
					magRange=7;
				}

		    	mService.writeMagRange(mBluetoothAddress, magRange);
			    Toast.makeText(getApplicationContext(), "Magnometer rate changed. New rate = "+Configuration.Shimmer3.ListofMagRange[item], Toast.LENGTH_SHORT).show();
			    buttonMagRange.setText("Mag Range"+"\n"+"("+Configuration.Shimmer3.ListofMagRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogMagRangeShimmer2_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer",Configuration.Shimmer2.ListofMagRange[item]);
				int magRange=0;
				if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 0.8Ga"){
					magRange=0;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 1.3Ga"){
					magRange=1;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 1.9Ga"){
					magRange=2;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 2.5Ga"){
					magRange=3;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 4.0Ga"){
					magRange=4;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 4.7Ga"){
					magRange=5;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 5.6Ga"){
					magRange=6;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 8.1Ga"){
						magRange=7;
				}
 
				mService.writeMagRange(mBluetoothAddress, magRange);
				Toast.makeText(getApplicationContext(), "Magnometer rate changed. New rate = "+Configuration.Shimmer2.ListofMagRange[item], Toast.LENGTH_SHORT).show();
				buttonMagRange.setText("Mag Range"+"\n"+"("+Configuration.Shimmer2.ListofMagRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogGyroRangeShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer",Configuration.Shimmer3.ListofGyroRange[item]);
			    int gyroRange=0;
			  
			    if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[0]){
				    gyroRange=0;
			    } else if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[1]){
			    	gyroRange=1;
			    } else if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[2]){
			    	gyroRange=2;
			    } else if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[3]){
			    	gyroRange=3;
			    }

			    mService.writeGyroRange(mBluetoothAddress, gyroRange);
			    Toast.makeText(getApplicationContext(), "Gyroscope rate changed. New rate = "+Configuration.Shimmer3.ListofGyroRange[item], Toast.LENGTH_SHORT).show();
			    buttonGyroRange.setText("Gyro Range"+"\n"+"("+Configuration.Shimmer3.ListofGyroRange[item]+")");
            }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogAccelShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer",Configuration.Shimmer3.ListofAccelRange[item]);
				int accelRange=0;
				
				if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 2g"){
					accelRange=0;
				} else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 4g"){
					accelRange=1;
				} else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 8g"){
					accelRange=2;
				} else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 16g"){
					accelRange=3;
				}

			    if(accelRange==0)
			    	cBoxLowPowerAccel.setEnabled(false);
			    else
			    	cBoxLowPowerAccel.setEnabled(true);
			    
			    mService.writeAccelRange(mBluetoothAddress, accelRange);
				Toast.makeText(getApplicationContext(), "Accelerometer rate changed. New rate = "+Configuration.Shimmer3.ListofAccelRange[item], Toast.LENGTH_SHORT).show();
				buttonAccRange.setText("Accel Range"+"\n"+"("+Configuration.Shimmer3.ListofAccelRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogAccelShimmer2_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
			 	Log.d("Shimmer",accelRangeArray[item]);
		    	int accelRange=0;

			    if (accelRangeArray[item]=="+/- 1.5g"){
			    	accelRange=0;
			    } else if (accelRangeArray[item]=="+/- 6g"){
			    	accelRange=3;
			    }
			    mService.writeAccelRange(mBluetoothAddress, accelRange);
			    Toast.makeText(getApplicationContext(), "Accelerometer rate changed. New rate = "+accelRangeArray[item], Toast.LENGTH_SHORT).show();
			    buttonAccRange.setText("Accel Range"+"\n"+"("+accelRangeArray[item]+")");
            }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogRate_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				 Log.d("Shimmer",samplingRate[item]);
				 double newRate = Double.valueOf(samplingRate[item]);
				 mService.writeSamplingRate(mBluetoothAddress, newRate);
				 Toast.makeText(getApplicationContext(), "Sample rate changed. New rate = "+newRate+" Hz", Toast.LENGTH_SHORT).show();
				 buttonSampleRate.setText("Sampling Rate "+"\n"+"("+newRate+" Hz)");
            }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogBattLimit_OnClickListener implements DialogInterface.OnClickListener {
		
		private final EditText editTextBattLimit;

		private DialogBattLimit_OnClickListener(EditText editTextBattLimit) {
			this.editTextBattLimit = editTextBattLimit;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			try {
				double newLimit = Double.parseDouble(editTextBattLimit.getText().toString());
				mService.setBattLimitWarning(mBluetoothAddress, newLimit);
				Toast.makeText(getApplicationContext(), "Battery limit changed. New limit = " + newLimit + " V", Toast.LENGTH_SHORT).show();
				buttonBattVoltLimit.setText("Set Batt Limit " + "\n" + "(" + newLimit + " V)");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogExgGain_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			
			int exgGainNew = 0;

			if (exgGain[item] == "6") {
				exgGainNew = 0;
			} else if (exgGain[item] == "1") {
				exgGainNew = 1;
			} else if (exgGain[item] == "2") {
				exgGainNew = 2;
			} else if (exgGain[item] == "3") {
				exgGainNew = 3;
			} else if (exgGain[item] == "4") {
				exgGainNew = 4;
			} else if (exgGain[item] == "8") {
				exgGainNew = 5;
			} else if (exgGain[item] == "12") {
				exgGainNew = 6;
			}

		    mService.writeEXGGainSetting(mBluetoothAddress, exgGainNew); // TODO: in MultiShimmer example project, this is writeEXGGainSetting 
		    Toast.makeText(getApplicationContext(), "Exg gain changed. New gain = "+exgGain[item], Toast.LENGTH_SHORT).show();
		    buttonExgGain.setText("EXG Gain"+"\n"+"("+exgGain[item]+")");
		}
	}

	private final class DialogExgRes_OnClickListener implements DialogInterface.OnClickListener {

		public void onClick(DialogInterface dialog, int item) {

		    long enabledSensors = mService.getEnabledSensors(mBluetoothAddress);
		    
		    
		    if (exgResolution[item]=="16 bits") {
		    	// if 16-bit was chosen and 24-bit sensors are enabled, change to 16 bit
		    	if (((enabledSensors & Shimmer.SENSOR_EXG1_24BIT) > 0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT) > 0)){
			    	enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors, Shimmer.SENSOR_EXG1_16BIT, (int)mShimmerVersion);
			    	enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors, Shimmer.SENSOR_EXG2_16BIT, (int)mShimmerVersion);
		    	}
		    	exgRes = 16;
		    }
		    else { 
		    	//if 24-bit was chosen and 16-bit sensors are enabled, change to 24 bit
		    	if (((enabledSensors & Shimmer.SENSOR_EXG1_16BIT) > 0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT) > 0)){
			    	enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors, Shimmer.SENSOR_EXG1_24BIT, (int)mShimmerVersion);
			    	enabledSensors = mService.sensorConflictCheckandCorrection(enabledSensors, Shimmer.SENSOR_EXG2_24BIT, (int)mShimmerVersion);
		    	}
		    	exgRes = 24;
		    }
			//shimmerConfig.setEnabledSensors(enabledSensor);
			//mService.mShimmerConfigurationList.set(currentPosition, shimmerConfig);					
			mService.setEnabledSensors(enabledSensors, mBluetoothAddress);
			Toast.makeText(getApplicationContext(), "Exg resolution changed. New resolution = " + exgResolution[item], Toast.LENGTH_SHORT).show();
			buttonExgRes.setText("EXG Res" + "\n" + "(" + exgResolution[item] + ")");
		}
	}
	
	private final class DialogECGReference_OnClickListener implements DialogInterface.OnClickListener {

		private final String[] listOfReference;

		private DialogECGReference_OnClickListener(String[] listOfReference) {
			this.listOfReference = listOfReference;
		}

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub

			Log.d("Shimmer ",listOfReference[arg1]);
			
			String newReference = listOfReference[arg1];
			
			int reference = 0;
			if(listOfReference[arg1].equals("Fixed Potential"))
				reference = 0;
			else
				reference = 13;
			
			mService.writeExGReferenceElectrode(mBluetoothAddress, reference);
			Toast.makeText(getApplicationContext(), "Referecen electrode changed. New reference electrode = "+newReference, Toast.LENGTH_SHORT).show();
			buttonReferenceElectrode.setText("Ref. Electrode "+"\n ("+newReference+")");
		}
	}

	private final class DialogEMGReference_OnClickListener implements DialogInterface.OnClickListener {
		
		private final String[] listOfReference;
		
		private DialogEMGReference_OnClickListener(String[] listOfReference) {
			this.listOfReference = listOfReference;
		}
		
		@Override
		public void onClick(DialogInterface arg0, int arg1) {

			Log.d("Shimmer ",listOfReference[arg1]);
			
			String newReference = listOfReference[arg1];
			
			int reference = 0;
			if(listOfReference[arg1].equals("Fixed Potential"))
				reference = 0;
			else
				reference = 3;

			mService.writeExGReferenceElectrode(mBluetoothAddress, reference);
			Toast.makeText(getApplicationContext(), "Referecen electrode changed. New reference electrode = "+newReference, Toast.LENGTH_SHORT).show();
			buttonReferenceElectrode.setText("Ref. Electrode "+"\n ("+newReference+")");
		}
	}

	private final class DialogLeadOffCurrent_OnClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			Log.d("Shimmer ",Configuration.Shimmer3.ListOfExGLeadOffCurrent[arg1]);
			String newCurrent = Configuration.Shimmer3.ListOfExGLeadOffCurrent[arg1];
			mService.writeExGLeadOffDetectionCurrent(mBluetoothAddress, arg1);
			Toast.makeText(getApplicationContext(), "Lead-Off Current changed. New Lead-Off Current = "+newCurrent, Toast.LENGTH_SHORT).show();
			buttonLeadOffCurrent.setText("Lead-Off Current "+"\n ("+newCurrent+")");
		}
	}

	private final class DialogLeadOffDetection_OnClickListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {

			Log.d("Shimmer ",Configuration.Shimmer3.ListOfExGLeadOffDetection[arg1]);
			
			String newDetection = Configuration.Shimmer3.ListOfExGLeadOffDetection[arg1];
			mService.writeExGLeadOffDetectionMode(mBluetoothAddress, arg1);
			Toast.makeText(getApplicationContext(), "Lead-Off Detection changed. New Lead-Off Detection = " + newDetection, Toast.LENGTH_SHORT).show();
			buttonLeadOffDetection.setText("Lead-Off Detection " + "\n (" + newDetection + ")");
		}
	}

	private final class DialogLeadOffComparator_OnClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			Log.d("Shimmer ",Configuration.Shimmer3.ListOfExGLeadOffComparator[arg1]);
			String newComparator = Configuration.Shimmer3.ListOfExGLeadOffComparator[arg1];
			mService.writeExGLeadOffDetectionCurrent(mBluetoothAddress, arg1);
			mService.writeExGLeadOffDetectionComparatorTreshold(mBluetoothAddress, arg1);
			Toast.makeText(getApplicationContext(), "Lead-Off Comparator changed. New Lead-Off Comparator = "+newComparator, Toast.LENGTH_SHORT).show();
			buttonLeadOffComparator.setText("Lead-Off Comparator "+"\n ("+newComparator+")");
		}
	}

	// *********************************************************************************
	// *                          Shimmer Message Handler
	// *********************************************************************************

	private final class ShimmerMessageHandler extends Handler {
		
		public void handleMessage(Message msg) {
			
			Shimmer shimmer;

			try {
				switch (msg.what) {
	            
	            case Shimmer.MESSAGE_STATE_CHANGE:
	            	
	                switch (msg.arg1) {
	                
	                case Shimmer.STATE_CONNECTED:
	                    break;
	                    
	                case Shimmer.MSG_STATE_FULLY_INITIALIZED:

	        			// the displayed list depends on the shimmer version
	        			mShimmerVersion = mService.getShimmerVersion(mBluetoothAddress);
	        			mShimmerFWVersion = mService.getFWVersion(mBluetoothAddress);
		                if(mService.isEXGUsingECG16Configuration(mBluetoothAddress) || mService.isEXGUsingECG24Configuration(mBluetoothAddress)){
		                	exgMode="ECG";
		                }
		                else 
		                	exgMode="";

	        			// get the shimmer object
	        			if (null != (shimmer = mService.getShimmer(mBluetoothAddress))) {
		        			showEnableSensors(shimmer, mShimmerVersion);
		        			updateCommandsView(shimmer, mShimmerVersion);
			                printExGArrays(shimmer);
		        			setCurrentView(ShimmerConfigView.viewScanSuccess);
		        			setCurrentView(ShimmerConfigView.viewCommands);
		                }
	        			else {
		                	setCurrentView(ShimmerConfigView.viewScanFailed);
	        			}
	                    break;
	
	                case Shimmer.STATE_CONNECTING:
	                    break;
	                    
	                case Shimmer.STATE_NONE:
	                	setCurrentView(ShimmerConfigView.viewScanFailed);
	                    break;
	                }
	                break;
	            
	            case Shimmer.MESSAGE_READ:
	            	if ((msg.obj instanceof ObjectCluster)){
	            	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj;   
	            	    FormatCluster fc1 = objectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get("EXG1 STATUS"), "RAW");
	            	    FormatCluster fc2 = objectCluster.returnFormatCluster(objectCluster.mPropertyCluster.get("EXG2 STATUS"), "RAW");
	            	    
	            	    if(fc1!=null && fc2!=null){
	            	    	int statusChip1 = (int) fc1.mData;
		            	    int statusChip2 = (int) fc2.mData;
		            	    int status1 = 0, status2 = 0, status3 = 0, status4 = 0, status5 = 0;
	            	    	if(exgMode.equals("ECG")){
	            	    		status1 = (statusChip1 & 4) >> 2;
								status2 = (statusChip1 & 8) >> 3;
								status3 = (statusChip1 & 1);
								status4 = (statusChip2 & 4) >> 2;
								status5 = (statusChip1 & 16) >> 4;
	            	    	}
	            	    	
	            	    	if(status1==0)
								statusCircle1.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle1.setBackgroundResource(R.drawable.circle_red);
							
							if(status2==0)
								statusCircle2.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle2.setBackgroundResource(R.drawable.circle_red);
							
							if(status3==0)
								statusCircle3.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle3.setBackgroundResource(R.drawable.circle_red);
							
							if(status4==0)
								statusCircle4.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle4.setBackgroundResource(R.drawable.circle_red);
							
							if(status5==0)
								statusCircle5.setBackgroundResource(R.drawable.circle_green);
							else
								statusCircle5.setBackgroundResource(R.drawable.circle_red);
	            	    }
	            	                	    
	            	}
					
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

	// *********************************************************************************
	// *                          ListView
	// *********************************************************************************

	private final class Sensors_OnClickListener implements OnItemClickListener {
		private final BiMap<String, String> sensorBitmaptoName;
		private final String[] sensorNames;

		private Sensors_OnClickListener(
				BiMap<String, String> sensorBitmaptoName, String[] sensorNames) {
			this.sensorBitmaptoName = sensorBitmaptoName;
			this.sensorNames = sensorNames;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int clickIndex, long arg3) {
			// ECG-24
			if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ECG")){
				int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
				int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
				if (!((mEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (mEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				
				if(!lvSensors.isItemChecked(clickIndex)){
					lvSensors.setItemChecked(clickIndex, false); //ECG
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				else
					lvSensors.setItemChecked(clickIndex, true); //ECG
				
				lvSensors.setItemChecked(clickIndex+1, false); //ECG 16Bit
				lvSensors.setItemChecked(clickIndex+2, false);// EMG
				lvSensors.setItemChecked(clickIndex+3, false);// EMG 16Bit
				lvSensors.setItemChecked(clickIndex+4, false);// Test Signal
				lvSensors.setItemChecked(clickIndex+5, false);// Test Signal 16Bit
				if(lvSensors.isItemChecked(clickIndex))
					mService.writeEXGSetting(mBluetoothAddress, 0);
			}
			// ECG-16
			else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("ECG 16Bit")){
					int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
					int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
					if (!((mEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (mEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
						mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
						mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
					}
					
					if(!lvSensors.isItemChecked(clickIndex)){
						lvSensors.setItemChecked(clickIndex, false); //ECG 16Bit
						mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
						mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
					}
					else
						lvSensors.setItemChecked(clickIndex, true); //ECG 16Bit
					
					lvSensors.setItemChecked(clickIndex-1, false); //ECG
					lvSensors.setItemChecked(clickIndex+1, false);// EMG
					lvSensors.setItemChecked(clickIndex+2, false);// EMG 16Bit
					lvSensors.setItemChecked(clickIndex+3, false);// Test Signal
					lvSensors.setItemChecked(clickIndex+4, false);// Test Signal 16Bit
					if(lvSensors.isItemChecked(clickIndex))
						mService.writeEXGSetting(mBluetoothAddress, 0);
			}
			// EMG-24
			else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("EMG")){
				int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
				int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
				if (!((mEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (mEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				
				if(!lvSensors.isItemChecked(clickIndex)){
					lvSensors.setItemChecked(clickIndex, false); //EMG
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				else
					lvSensors.setItemChecked(clickIndex, true); //EMG
				
				lvSensors.setItemChecked(clickIndex-2, false); //ECG
				lvSensors.setItemChecked(clickIndex-1, false);// ECG 16Bit
				lvSensors.setItemChecked(clickIndex+1, false);// EMG 16Bit
				lvSensors.setItemChecked(clickIndex+2, false);// Test Signal
				lvSensors.setItemChecked(clickIndex+3, false);// Test Signal 16Bit
				if(lvSensors.isItemChecked(clickIndex))
					mService.writeEXGSetting(mBluetoothAddress, 1);
			}
			// EMG-16
			else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("EMG 16Bit")){
				int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
				int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
				if (!((mEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (mEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				
				if(!lvSensors.isItemChecked(clickIndex)){
					lvSensors.setItemChecked(clickIndex, false); //EMG 16Bit
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				else
					lvSensors.setItemChecked(clickIndex, true); //EMG 16Bit
				
				lvSensors.setItemChecked(clickIndex-3, false); //ECG
				lvSensors.setItemChecked(clickIndex-2, false);// ECG 16Bit
				lvSensors.setItemChecked(clickIndex-1, false);// EMG 
				lvSensors.setItemChecked(clickIndex+1, false);// Test Signal
				lvSensors.setItemChecked(clickIndex+2, false);// Test Signal 16Bit
				if(lvSensors.isItemChecked(clickIndex))
					mService.writeEXGSetting(mBluetoothAddress, 1);
			}
			// Test Signal-24
			else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("Test Signal")){
				int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1"));
				int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2"));
				if (!((mEnabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (mEnabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0)){
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				
				if(!lvSensors.isItemChecked(clickIndex)){
					lvSensors.setItemChecked(clickIndex, false); //Test Signal
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				else
					lvSensors.setItemChecked(clickIndex, true); //Test Signal
				
				lvSensors.setItemChecked(clickIndex-4, false); //ECG
				lvSensors.setItemChecked(clickIndex-3, false);// ECG 16Bit
				lvSensors.setItemChecked(clickIndex-2, false);// EMG 
				lvSensors.setItemChecked(clickIndex-1, false);// EMG 16Bit
				lvSensors.setItemChecked(clickIndex+1, false);// Test Signal 16Bit
				if(lvSensors.isItemChecked(clickIndex))
				mService.writeEXGSetting(mBluetoothAddress, 2);
			}
			// Test Signal-16
			else if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3 && sensorNames[clickIndex].equals("Test Signal 16Bit")){
				int iDBMValue1 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG1 16Bit"));
				int iDBMValue3 = Integer.parseInt(sensorBitmaptoName.inverse().get("EXG2 16Bit"));
				if (!((mEnabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (mEnabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0)){
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
					
				if(!lvSensors.isItemChecked(clickIndex)){
					lvSensors.setItemChecked(clickIndex, false); //Test Signal 16Bit
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue1);
					mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress, mEnabledSensors,iDBMValue3);
				}
				else
					lvSensors.setItemChecked(clickIndex, true); //Test Signal 16Bit
					
				lvSensors.setItemChecked(clickIndex-5, false); //ECG
				lvSensors.setItemChecked(clickIndex-4, false);// ECG 16Bit
				lvSensors.setItemChecked(clickIndex-3, false);// EMG 
				lvSensors.setItemChecked(clickIndex-2, false);// EMG 16Bit
				lvSensors.setItemChecked(clickIndex-1, false);// Test Signal
				if(lvSensors.isItemChecked(clickIndex))
					mService.writeEXGSetting(mBluetoothAddress, 2);
			}
			else{
				int sensorIdentifier = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[clickIndex]));
				//check and remove any old daughter boards (sensors) which will cause a conflict with sensorIdentifier 
				mEnabledSensors = mService.sensorConflictCheckandCorrection(mBluetoothAddress,mEnabledSensors,sensorIdentifier);
				//update the checkbox accordingly
				int end=0;
				if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3)
					end=sensorNames.length-6;
				else
					end=sensorNames.length;
				
				for (int i=0;i<end;i++){
					int iDBMValue = Integer.parseInt(sensorBitmaptoName.inverse().get(sensorNames[i]));	
					if( (iDBMValue & mEnabledSensors) >0){
						lvSensors.setItemChecked(i, true);
					} else {
						lvSensors.setItemChecked(i, false);
					}
				}
			}
		}
	}
}
