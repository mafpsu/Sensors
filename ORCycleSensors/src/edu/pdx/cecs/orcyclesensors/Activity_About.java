package edu.pdx.cecs.orcyclesensors;

import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.pluginlib.version.PluginLibVersionInfo;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class Activity_About extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		try {
			((TextView) findViewById(R.id.textView_PluginSamplerVersion))
					.setText("Sampler Version: "
							+ getPackageManager().getPackageInfo(
									getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			((TextView) findViewById(R.id.textView_PluginSamplerVersion))
					.setText("Sampler Version: ERR");
		}
		((TextView) findViewById(R.id.textView_PluginLibVersion))
				.setText("Built w/ PluginLib: "
						+ PluginLibVersionInfo.PLUGINLIB_VERSION_STRING);
		((TextView) findViewById(R.id.textView_PluginsPkgVersion))
				.setText("Installed Plugin Version: "
						+ AntPluginPcc.getInstalledPluginsVersionString(this));
	}
}
