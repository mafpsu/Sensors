package edu.pdx.cecs.orcyclesensors;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;

public class AntPlusPccCommonResponse {

	private final Context context;
	
	public AntPlusPccCommonResponse(Context context) {
		this.context = context;
	}
	
	public String onBadResult(RequestAccessResult resultCode,
            DeviceState initialDeviceState) {
		
        switch(resultCode)
        {
	        case SUCCESS:
	        	
	            return "Device Connected.";
            
            case CHANNEL_NOT_AVAILABLE:

            	Toast.makeText(context, "Channel Not Available", Toast.LENGTH_SHORT).show();
                return("Error. Do Menu->Reset.");

            case ADAPTER_NOT_DETECTED:
            	
                Toast.makeText(context, "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", Toast.LENGTH_SHORT).show();
                return("Error. Do Menu->Reset.");

            case BAD_PARAMS:
            	
                //Note: Since we compose all the params ourself, we should never see this result
                Toast.makeText(context, "Bad request parameters.", Toast.LENGTH_SHORT).show();
                return("Error. Do Menu->Reset.");

            case OTHER_FAILURE:
            	
                Toast.makeText(context, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
                return("Error. Do Menu->Reset.");
                
            case DEPENDENCY_NOT_INSTALLED:
            	
                AlertDialog.Builder adlgBldr = new AlertDialog.Builder(context);
                adlgBldr.setTitle("Missing Dependency");
                adlgBldr.setMessage("The required service\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to update your existing version if you already have it. Do you want to launch the Play Store to get it?");
                adlgBldr.setCancelable(true);
                adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent startStore = null;
                        startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                        startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        AntPlusPccCommonResponse.this.context.startActivity(startStore);
                    }
                });
                adlgBldr.setNegativeButton("Cancel", new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });

                final AlertDialog waitDialog = adlgBldr.create();
                waitDialog.show();
                return("Error. Do Menu->Reset.");
            
            case USER_CANCELLED:

            	return("Cancelled. Do Menu->Reset.");

            case UNRECOGNIZED:
            	
                Toast.makeText(context,
                    "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                    Toast.LENGTH_SHORT).show();
                return("Error. Do Menu->Reset.");

            default:
                Toast.makeText(context, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
                return("Error. Do Menu->Reset.");
        }
	}
}
