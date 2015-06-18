package edu.pdx.cecs.orcyclesensors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;

import android.util.Log;

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.AutoZeroStatus;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalculatedWheelDistanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalculatedWheelSpeedReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalibrationId;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalibrationMessage;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CrankParameters;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.DataSource;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IAutoZeroStatusReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedCrankCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedPowerReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedTorqueReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalibrationMessageReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICrankParametersReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IInstantaneousCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IPedalPowerBalanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IPedalSmoothnessReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawCrankTorqueDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawCtfDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawPowerOnlyDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawWheelTorqueDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ITorqueEffectivenessReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IBatteryStatusReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IManufacturerIdentificationReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IProductInformationReceiver;

public class AntDeviceBikePowerRecorder extends AntDeviceRecorder implements
	IPluginAccessResultReceiver<AntPlusBikePowerPcc>,
	ICalculatedPowerReceiver,
	ICalculatedTorqueReceiver,
	ICalculatedCrankCadenceReceiver,
	IInstantaneousCadenceReceiver, 
	IRawPowerOnlyDataReceiver, 
	IPedalPowerBalanceReceiver, 
	IRawWheelTorqueDataReceiver,
	IRawCrankTorqueDataReceiver,
	ITorqueEffectivenessReceiver,
	IPedalSmoothnessReceiver,
	IRawCtfDataReceiver,
	ICalibrationMessageReceiver,
	IAutoZeroStatusReceiver,
	ICrankParametersReceiver
	{

    //NOTE: We're using 2.07m as the wheel circumference to pass to the calculated events
	private BigDecimal wheelCircumferenceInMeters = new BigDecimal("2.07"); // TODO: Determine what should be used here

    private long estTimestamp;

    private ArrayList<BigDecimal> calculatedPower = new ArrayList<BigDecimal>();
    private ArrayList<BigDecimal> calculatedTorque = new ArrayList<BigDecimal>();
    private ArrayList<BigDecimal> calculatedCrankCadence = new ArrayList<BigDecimal>();
    private ArrayList<BigDecimal> calculatedWheelSpeed = new ArrayList<BigDecimal>();
	synchronized public void addCalculatedWheelSpeed(BigDecimal calculatedWheelSpeed) {
		this.calculatedWheelSpeed.add(calculatedWheelSpeed);
	}
	ArrayList<BigDecimal> calculatedWheelDistance = new ArrayList<BigDecimal>();
    synchronized public void addCalculatedWheelDistance(BigDecimal calculatedWheelDistance) {
		this.calculatedWheelDistance.add(calculatedWheelDistance);
	}

    private int instantaneousCadence;
    private long powerOnlyUpdateEventCount;
    private int instantaneousPower;
    private long accumulatedPower;
    
    private int pedalPowerPercentage;
    private boolean rightPedalPowerIndicator;

    private long wheelTorqueUpdateEventCount;
    private long accumulatedWheelTicks;
    private BigDecimal accumulatedWheelPeriod;
    private BigDecimal accumulatedWheelTorque;

    private long crankTorqueUpdateEventCount;
    private long accumulatedCrankTicks;
    private BigDecimal accumulatedCrankPeriod;
    private BigDecimal accumulatedCrankTorque;

    private long tePsEventCount;
    private BigDecimal leftTorqueEffectiveness;
    private BigDecimal rightTorqueEffectiveness;

    private boolean separatePedalSmoothnessSupport;
    private BigDecimal leftOrCombinedPedalSmoothness;
    private BigDecimal rightPedalSmoothness;

    private long ctfUpdateEventCount;
    private BigDecimal instantaneousSlope;
    private BigDecimal accumulatedTimeStamp;
    private long accumulatedTorqueTicksStamp;

	// onNewCalibrationMessage ------------------
    // TextView textView_CalibrationData;
    // TextView textView_CtfOffset;
    // TextView textView_ManufacturerSpecificBytes;

	// onNewAutoZeroStatus ----------------------
    // TextView textView_AutoZeroStatus;

	// onNewCrankParameters ---------------------
    // TextView textView_FullCrankLength;
    // TextView textView_CrankLengthStatus;
    // TextView textView_SensorSoftwareMismatchStatus;
    // TextView textView_SensorAvailabilityStatus;
    // TextView textView_CustomCalibrationStatus;
    // TextView textView_AutoCrankLengthSupport;

	private static final String MODULE_TAG = "AntDeviceBikePowerRecorder";

	// **************************************************************
    // *                        Constructors
    // **************************************************************

    public AntDeviceBikePowerRecorder(int deviceNumber) {
		super(deviceNumber);
	}
    
	


	// **************************************************************
    // *             Connection variables and methods
    // **************************************************************

    private AntPlusBikePowerPcc pwrPcc = null;

    private PccReleaseHandle<AntPlusBikePowerPcc> releaseHandle = null;
    
	/**
	 * Starts the plugins UI search
	 */
	public void requestAccessToPcc()
    {
    	state = State.CONNECTING;
	    releaseHandle = AntPlusBikePowerPcc.requestAccess(this.context, deviceNumber, 0, this, this);
    }
    
	
	/** 
	 * Handle the result, connecting to events on success or reporting failure to user.
	 * @param result
	 * @param resultCode
	 * @param initialDeviceState
	 */
	@Override
	synchronized public void onResultReceived(AntPlusBikePowerPcc result,
			RequestAccessResult resultCode, DeviceState initialDeviceState) {

		try {
			if ((null != result) && (RequestAccessResult.SUCCESS == resultCode)) {
				showResultStatus(context, result.getDeviceName(), result.supportsRssi(), resultCode, initialDeviceState);
				pwrPcc = result;
				state = State.RUNNING;
				subscribeToEvents();
			} else {
				pwrPcc = null;
				state = State.FAILED;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	synchronized public void unregister() {
        if(releaseHandle != null) {
            releaseHandle.close();
            releaseHandle = null;
        }
	}

    // **************************************************************
    // *                     Data Event Handlers
    // **************************************************************

    private void subscribeToEvents()
    {
        pwrPcc.subscribeCalculatedPowerEvent(this);
        pwrPcc.subscribeCalculatedTorqueEvent(this);
        pwrPcc.subscribeCalculatedCrankCadenceEvent(this);
        pwrPcc.subscribeCalculatedWheelSpeedEvent(new CalculatedWheelSpeedEvent(wheelCircumferenceInMeters));
        pwrPcc.subscribeCalculatedWheelDistanceEvent(new CalculatedWheelDistanceEvent(wheelCircumferenceInMeters));
        //pwrPcc.subscribeInstantaneousCadenceEvent(this);
        //pwrPcc.subscribeRawPowerOnlyDataEvent(this);
        //pwrPcc.subscribePedalPowerBalanceEvent(this);
        //pwrPcc.subscribeRawWheelTorqueDataEvent(this);
        //pwrPcc.subscribeRawCrankTorqueDataEvent(this);
        //pwrPcc.subscribeTorqueEffectivenessEvent(this);
        //pwrPcc.subscribePedalSmoothnessEvent(this);
        //pwrPcc.subscribeRawCtfDataEvent(this);
        //pwrPcc.subscribeCalibrationMessageEvent(this);
        //pwrPcc.subscribeAutoZeroStatusEvent(this);
        //pwrPcc.subscribeCrankParametersEvent(this);
        //pwrPcc.subscribeManufacturerIdentificationEvent(this);
        //pwrPcc.subscribeProductInformationEvent(this);
        //pwrPcc.subscribeBatteryStatusEvent(this);
    }

    private final class CalculatedWheelDistanceEvent extends
			CalculatedWheelDistanceReceiver {
		
		private CalculatedWheelDistanceEvent(BigDecimal wheelCircumference) {
			super(wheelCircumference);
		}

		@Override
		public void onNewCalculatedWheelDistance(final long estTimestamp,
				final EnumSet<EventFlag> eventFlags,
				final DataSource dataSource,
				final BigDecimal calculatedWheelDistance) {
			
			AntDeviceBikePowerRecorder.this.addCalculatedWheelDistance(calculatedWheelDistance);

			String source;

			// NOTE: The calculated distance event will
			// send an initial value code if it needed
			// to calculate a NEW average.
			// This is important if using the calculated
			// distance event to record user data, as an
			// initial value indicates an average could
			// not be guaranteed.
			switch (dataSource) {
			case WHEEL_TORQUE_DATA:
				source = dataSource.toString();
				break;
			case INITIAL_VALUE_WHEEL_TORQUE_DATA:
				// New data calculated from initial
				// value data source
			case COAST_OR_STOP_DETECTED:
				// A coast or stop condition detected by the ANT+ Plugin.
				// This is automatically sent by the plugin after 3 seconds of
				// unchanging events.
				// NOTE: This value should be ignored by apps which are
				// archiving the data for accuracy.
				source = dataSource.toString();
				break;
			case UNRECOGNIZED:
				Log.e(MODULE_TAG, "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
			default:
				source = "N/A";
				break;
			}
		}
	}

	private final class CalculatedWheelSpeedEvent extends
			CalculatedWheelSpeedReceiver {
		
		private CalculatedWheelSpeedEvent(BigDecimal wheelCircumference) {
			super(wheelCircumference);
		}

		@Override
		public void onNewCalculatedWheelSpeed(final long estTimestamp,
				final EnumSet<EventFlag> eventFlags,
				final DataSource dataSource,
				final BigDecimal calculatedWheelSpeed) {
			
			//AntDeviceBikePowerRecorder.this.setEstTimestamp(estTimestamp);
			AntDeviceBikePowerRecorder.this.addCalculatedWheelSpeed(calculatedWheelSpeed);

			String source;

			// NOTE: The calculated speed event will
			// send an initial value code if it needed
			// to calculate a NEW average.
			// This is important if using the calculated
			// speed event to record user data, as an
			// initial value indicates an average could
			// not be guaranteed.
			switch (dataSource) {
			case WHEEL_TORQUE_DATA:
			case INITIAL_VALUE_WHEEL_TORQUE_DATA:
				// New data calculated from initial
				// value data source
			case COAST_OR_STOP_DETECTED:
				// A coast or stop condition detected by the ANT+ Plugin.
				// This is automatically sent by the plugin after 3 seconds of
				// unchanging events.
				// NOTE: This value should be ignored by apps which are
				// archiving the data for accuracy.
				source = dataSource.toString();
				break;
			case UNRECOGNIZED:
				Log.e(MODULE_TAG, "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
			default:
				//textView_CalculatedSpeed.setText("N/A");
				source = "N/A";
				break;
			}
		}
	}
	
	@Override
	synchronized public void onNewInstantaneousCadence(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final DataSource dataSource, final int instantaneousCadence) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			String source = "";
	
			switch (dataSource) {
			case POWER_ONLY_DATA:
			case WHEEL_TORQUE_DATA:
			case CRANK_TORQUE_DATA:
				source = " from Pg " + dataSource.getIntValue();
				break;
			case COAST_OR_STOP_DETECTED:
				// A coast or stop condition detected by the ANT+ Plugin.
				// This is automatically sent by the plugin after 3 seconds of
				// unchanging events.
				// NOTE: This value should be ignored by apps which are
				// archiving the data for accuracy.
				source = dataSource.toString();
				break;
			case UNRECOGNIZED:
				Log.e(MODULE_TAG, "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
	
			default:
				break;
			}
	
			// Check if the instantaneous cadence field
			// is valid
			if (instantaneousCadence >= 0)
				this.instantaneousCadence = instantaneousCadence;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewRawPowerOnlyData(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final long powerOnlyUpdateEventCount,
			final int instantaneousPower, final long accumulatedPower) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			this.powerOnlyUpdateEventCount = powerOnlyUpdateEventCount;
			this.instantaneousPower = instantaneousPower;
			this.accumulatedPower = accumulatedPower;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewPedalPowerBalance(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final boolean rightPedalIndicator,
			final int pedalPowerPercentage) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			this.pedalPowerPercentage = pedalPowerPercentage;
			this.rightPedalPowerIndicator = rightPedalPowerIndicator;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewRawWheelTorqueData(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final long wheelTorqueUpdateEventCount,
			final long accumulatedWheelTicks,
			final BigDecimal accumulatedWheelPeriod,
			final BigDecimal accumulatedWheelTorque) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			this.wheelTorqueUpdateEventCount = wheelTorqueUpdateEventCount;
			this.accumulatedWheelTicks = accumulatedWheelTicks;
			this.accumulatedWheelPeriod = accumulatedWheelPeriod;
			this.accumulatedWheelTorque = accumulatedWheelTorque;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewRawCrankTorqueData(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final long crankTorqueUpdateEventCount,
			final long accumulatedCrankTicks,
			final BigDecimal accumulatedCrankPeriod,
			final BigDecimal accumulatedCrankTorque) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			this.crankTorqueUpdateEventCount = crankTorqueUpdateEventCount;
			this.accumulatedCrankTicks = accumulatedCrankTicks;
			this.accumulatedCrankPeriod = accumulatedCrankPeriod;
			this.accumulatedCrankTorque = accumulatedCrankTorque;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewTorqueEffectiveness(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final long powerOnlyUpdateEventCount,
			final BigDecimal leftTorqueEffectiveness,
			final BigDecimal rightTorqueEffectiveness) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			this.tePsEventCount = powerOnlyUpdateEventCount;
	
			if (leftTorqueEffectiveness.intValue() != -1)
				this.leftTorqueEffectiveness = leftTorqueEffectiveness;
	
			if (rightTorqueEffectiveness.intValue() != -1)
				this.rightTorqueEffectiveness = rightTorqueEffectiveness;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewPedalSmoothness(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final long powerOnlyUpdateEventCount,
			final boolean separatePedalSmoothnessSupport,
			final BigDecimal leftOrCombinedPedalSmoothness,
			final BigDecimal rightPedalSmoothness) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			this.tePsEventCount = powerOnlyUpdateEventCount;
			this.separatePedalSmoothnessSupport = separatePedalSmoothnessSupport;
	
			if (leftOrCombinedPedalSmoothness.intValue() != -1)
				this.leftOrCombinedPedalSmoothness = leftOrCombinedPedalSmoothness;
	
			if (rightPedalSmoothness.intValue() != -1)
				this.rightPedalSmoothness = rightPedalSmoothness;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewRawCtfData(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final long ctfUpdateEventCount,
			final BigDecimal instantaneousSlope,
			final BigDecimal accumulatedTimeStamp,
			final long accumulatedTorqueTicksStamp) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			this.ctfUpdateEventCount = ctfUpdateEventCount;
			this.instantaneousSlope = instantaneousSlope;
			this.accumulatedTimeStamp = accumulatedTimeStamp;
			this.accumulatedTorqueTicksStamp = accumulatedTorqueTicksStamp;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewCalibrationMessage(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final CalibrationMessage calibrationMessage) {
		
		try {
			this.estTimestamp = estTimestamp;
			
			switch (calibrationMessage.calibrationId) {
			case GENERAL_CALIBRATION_FAIL:
			case GENERAL_CALIBRATION_SUCCESS:
				//textView_CtfOffset.setText("N/A");
				//textView_ManufacturerSpecificBytes.setText("N/A");
				//textView_CalibrationData.setText(calibrationMessage.calibrationData.toString());
				break;
	
			case CUSTOM_CALIBRATION_RESPONSE:
			case CUSTOM_CALIBRATION_UPDATE_SUCCESS:
				//textView_CalibrationData.setText("N/A");
				//textView_CtfOffset.setText("N/A");
	
				//String bytes = "";
				//for (byte manufacturerByte : calibrationMessage.manufacturerSpecificData)
				//	bytes += "[" + manufacturerByte + "]";
	
				//textView_ManufacturerSpecificBytes.setText(bytes);
				break;
	
			case CTF_ZERO_OFFSET:
				//textView_ManufacturerSpecificBytes.setText("N/A");
				//textView_CalibrationData.setText("N/A");
				//textView_CtfOffset.setText(calibrationMessage.ctfOffset.toString());
				break;
				
			case UNRECOGNIZED:
				//Toast.makeText(context,
				//		"Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
				//		Toast.LENGTH_SHORT).show();
				break;
	
			default:
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewAutoZeroStatus(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final AutoZeroStatus autoZeroStatus) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			String autoZeroUiString;
			switch (autoZeroStatus) {
			case NOT_SUPPORTED:
			case ON:
			case OFF:
				autoZeroUiString = autoZeroStatus.toString();
				break;
			default:
				autoZeroUiString = "N/A";
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		// textView_AutoZeroStatus.setText(autoZeroUiString);
	}

	@Override
	synchronized public void onNewCrankParameters(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final CrankParameters crankParameters) {
		
		try {
			this.estTimestamp = estTimestamp;
	
			//textView_CrankLengthStatus.setText(crankParameters.getCrankLengthStatus().toString());
			//textView_SensorSoftwareMismatchStatus.setText(crankParameters.getSensorSoftwareMismatchStatus().toString());
			//textView_SensorAvailabilityStatus.setText(crankParameters.getSensorAvailabilityStatus().toString());
			//textView_CustomCalibrationStatus.setText(crankParameters.getCustomCalibrationStatus().toString());
			//textView_AutoCrankLengthSupport.setText(String.valueOf(crankParameters.isAutoCrankLengthSupported()));
	
			switch (crankParameters.getCrankLengthStatus()) {
			case INVALID_CRANK_LENGTH:
				//textView_FullCrankLength.setText("Invalid");
				break;
			case DEFAULT_USED:
			case SET_AUTOMATICALLY:
			case SET_MANUALLY:
				//textView_FullCrankLength.setText(crankParameters.getFullCrankLength() + "mm");
				break;
			default:
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewCalculatedCrankCadence(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final DataSource dataSource,
			final BigDecimal calculatedCrankCadence) {
		
		try {
			this.calculatedCrankCadence.add(calculatedCrankCadence);
	
			String source;
	
			// NOTE: The calculated crank cadence event
			// will send an initial value code if it
			// needed to calculate a NEW average.
			// This is important if using the calculated
			// crank cadence event to record user data,
			// as an initial value indicates an average
			// could not be guaranteed.
			switch (dataSource) {
			case CRANK_TORQUE_DATA:
			case INITIAL_VALUE_CRANK_TORQUE_DATA:
				// New data calculated from initial
				// value data source
			case CTF_DATA:
			case INITIAL_VALUE_CTF_DATA:
				// New data calculated from initial
				// value data source
				source = dataSource.toString();
				break;
			case INVALID_CTF_CAL_REQ:
				// The event cannot calculate
				// cadence from CTF until a zero
				// offset is collected from the
				// sensor.
			case COAST_OR_STOP_DETECTED:
				// A coast or stop condition detected by the ANT+ Plugin.
				// This is automatically sent by the plugin after 3 seconds of
				// unchanging events.
				// NOTE: This value should be ignored by apps which are
				// archiving the data for accuracy.
				source = dataSource.toString();
				break;
			case UNRECOGNIZED:
				Log.e(MODULE_TAG, "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
			default:
				//textView_CalculatedCrankCadence.setText("N/A");
				source = "N/A";
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewCalculatedTorque(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final DataSource dataSource, final BigDecimal calculatedTorque) {
		
		try {
			this.calculatedTorque.add(calculatedTorque);
	
			String source;
	
			// NOTE: The calculated torque event will
			// send an initial value code if it needed
			// to calculate a NEW average.
			// This is important if using the calculated
			// torque event to record user data, as an
			// initial value indicates an average could
			// not be guaranteed.
			switch (dataSource) {
			case WHEEL_TORQUE_DATA:
			case INITIAL_VALUE_WHEEL_TORQUE_DATA:
				// New data calculated from initial
				// value data source
			case CRANK_TORQUE_DATA:
			case INITIAL_VALUE_CRANK_TORQUE_DATA:
				// New data calculated from initial
				// value data source
			case CTF_DATA:
			case INITIAL_VALUE_CTF_DATA:
				// New data calculated from initial
				// value data source
				source = dataSource.toString();
				break;
			case INVALID_CTF_CAL_REQ:
				// The event cannot calculate torque
				// from CTF until a zero offset is
				// collected from the sensor.
			case COAST_OR_STOP_DETECTED:
				// A coast or stop condition detected by the ANT+ Plugin.
				// This is automatically sent by the plugin after 3 seconds of
				// unchanging events.
				// NOTE: This value should be ignored by apps which are
				// archiving the data for accuracy.
				source = dataSource.toString();
				break;
			case UNRECOGNIZED:
				Log.e(MODULE_TAG, "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
			default:
				//textView_CalculatedTorque.setText("N/A");
				source = "N/A";
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	@Override
	synchronized public void onNewCalculatedPower(final long estTimestamp,
			final EnumSet<EventFlag> eventFlags,
			final DataSource dataSource, final BigDecimal calculatedPower) {
		
		try {
			this.calculatedPower.add(calculatedPower);
	
			String source;
	
			// NOTE: The calculated power event will send an
			// initial value code if it needed
			// to calculate a NEW average. This is important if
			// using the calculated power event to record user
			// data, as an
			// initial value indicates an average could not be
			// guaranteed.
			// The event prioritizes calculating with torque
			// data over power only data.
			switch (dataSource) {
			case POWER_ONLY_DATA:
			case INITIAL_VALUE_POWER_ONLY_DATA:
				// New data calculated from initial
				// value data source
			case WHEEL_TORQUE_DATA:
			case INITIAL_VALUE_WHEEL_TORQUE_DATA:
				// New data calculated from initial
				// value data source
			case CRANK_TORQUE_DATA:
			case INITIAL_VALUE_CRANK_TORQUE_DATA:
				// New data calculated from initial
				// value data source
			case CTF_DATA:
			case INITIAL_VALUE_CTF_DATA:
				source = dataSource.toString();
				break;
			case INVALID_CTF_CAL_REQ:
				// The event cannot calculate power
				// from CTF until a zero offset is
				// collected from the sensor.
			case COAST_OR_STOP_DETECTED:
				// A coast or stop condition detected by the ANT+ Plugin.
				// This is automatically sent by the plugin after 3 seconds of
				// unchanging events.
				// NOTE: This value should be ignored by apps which are
				// archiving the data for accuracy.
				source = dataSource.toString();
				break;
			case UNRECOGNIZED:
				Log.e(MODULE_TAG, "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
			default:
				source = "N/A";
				break;
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}
	
	synchronized public void writeResult(TripData tripData, long currentTimeMillis) {

		// Calculate averages and sum square differences
		float avgCalcPower = 0.0f;
		float ssdCalcPower = 0.0f;
		float avgCalcTorque = 0.0f;
		float ssdCalcTorque = 0.0f;
		float avgCalcCrankCadence = 0.0f;
		float ssdCalcCrankCadence = 0.0f;
		float avgCalcWheelSpeed = 0.0f;
		float ssdCalcWheelSpeed = 0.0f;
		float avgCalcWheelDistance = 0.0f;
		float ssdCalcWheelDistance = 0.0f;
		
		try {
			// Calculate averages and sum square differences
			if (calculatedPower.size() > 0) {
				avgCalcPower = MyMath.getAverageValueBD(calculatedPower);
				ssdCalcPower = MyMath.getSumSquareDifferenceBD(calculatedPower, avgCalcPower);
			}
			if (calculatedTorque.size() > 0) {
				avgCalcTorque = MyMath.getAverageValueBD(calculatedTorque);
				ssdCalcTorque = MyMath.getSumSquareDifferenceBD(calculatedTorque, avgCalcTorque);
			}
			if (calculatedCrankCadence.size() > 0) {
				avgCalcCrankCadence = MyMath.getAverageValueBD(calculatedCrankCadence);
				ssdCalcCrankCadence = MyMath.getSumSquareDifferenceBD(calculatedCrankCadence, avgCalcCrankCadence);
			}
			if (calculatedWheelSpeed.size() > 0) {
				avgCalcWheelSpeed = MyMath.getAverageValueBD(calculatedWheelSpeed);
				ssdCalcWheelSpeed = MyMath.getSumSquareDifferenceBD(calculatedWheelSpeed, avgCalcWheelSpeed);
			}
			if (calculatedWheelDistance.size() > 0) {
				avgCalcWheelDistance = MyMath.getAverageValueBD(calculatedWheelDistance);
				ssdCalcWheelDistance = MyMath.getSumSquareDifferenceBD(calculatedWheelDistance, avgCalcWheelDistance);
			}
			
			// Save results to database
			tripData.addBikePowerDeviceReading(currentTimeMillis, 
					calculatedPower.size(), avgCalcPower, ssdCalcPower,
					calculatedTorque.size(), avgCalcTorque, ssdCalcTorque,
					calculatedCrankCadence.size(), avgCalcCrankCadence, ssdCalcCrankCadence,
					calculatedWheelSpeed.size(), avgCalcWheelSpeed, ssdCalcWheelSpeed,
					calculatedWheelDistance.size(), avgCalcWheelDistance, ssdCalcWheelDistance);
			reset();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private void reset() {
		calculatedPower.clear();
		calculatedTorque.clear();
		calculatedCrankCadence.clear();
		calculatedWheelSpeed.clear();
		calculatedWheelDistance.clear();
	}
}
