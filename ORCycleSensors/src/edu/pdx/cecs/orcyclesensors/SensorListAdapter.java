package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class SensorListAdapter extends BaseAdapter {

	private static final String MODULE_TAG = "SensorListAdapter";
	private ArrayList<SensorListItem> sensorList = new ArrayList<SensorListItem>();
	private LayoutInflater mInflater;

	private static class ViewHolder {
		public TextView txtName;
		public TextView txtType;
		public CheckBox chkSelect;
		public Spinner spnRate;
	}

	public SensorListAdapter(LayoutInflater layoutInflater) {
		mInflater = layoutInflater;
	}

	public void addItem(final SensorListItem item) {
		sensorList.add(item);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return sensorList.size();
	}

	@Override
	public SensorListItem getItem(int position) {
		return sensorList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		System.out.println("getView " + position + " " + convertView);

		SensorListItem item = sensorList.get(position);

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.select_sensor_list_item, (ViewGroup) null);
			
			// Find the child views of the list item and create a reference to them
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.text1);
			holder.txtType = (TextView) convertView.findViewById(R.id.text2);
			holder.chkSelect = (CheckBox) convertView.findViewById(R.id.chk_ssli_select);
			holder.spnRate = (Spinner) convertView.findViewById(R.id.spn_ssli_sensor_rate);
			
			// Optimization: Tag the row with it's child views, so we don't have to   
			// call findViewById() later when we reuse the row.
			convertView.setTag(holder);

			// If CheckBox is toggled, update the item it is tagged with.
			holder.chkSelect.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					SensorListItem item = (SensorListItem) cb.getTag();
					item.setChecked(cb.isChecked());
				}
			});

			// If spinner is changed, update the item it is tagged with.
			holder.spnRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					try {
						SensorListItem item;
						if (null != (item = (SensorListItem) parent.getTag())) {
							item.setRate(position);
						}
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					Log.e(MODULE_TAG, "onNothingSelected");
				}
			});

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Display item data
		holder.txtName.setText(item.getName());
		holder.txtType.setText(item.getStringType());
		holder.chkSelect.setChecked(item.isChecked());
		holder.chkSelect.setTag(item);
		holder.spnRate.setSelection(item.getRate());
		holder.spnRate.setTag(item);
		return convertView;
	}
	
	public ArrayList<SensorItem> getSelectedSensors() {
		
		final ArrayList<SensorItem> selectedSensors = new ArrayList<SensorItem>();
		
		for (SensorListItem sensorListItem: sensorList) {
			if (sensorListItem.isChecked()) {
				selectedSensors.add(new SensorItem(sensorListItem));
			}
		}
		
		return selectedSensors;
	}
}
