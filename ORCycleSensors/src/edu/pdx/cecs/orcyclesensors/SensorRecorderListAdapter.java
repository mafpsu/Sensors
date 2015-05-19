package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SensorRecorderListAdapter extends BaseAdapter {

	private ArrayList<SensorItem> sensorItems = new ArrayList<SensorItem>();
	private LayoutInflater mInflater;

	private static class ViewHolder {
		public TextView txtName;
		public TextView txtType;
		public TextView txtRate;
	}

	public SensorRecorderListAdapter(LayoutInflater layoutInflater) {
		mInflater = layoutInflater;
	}

	public void setSensors(ArrayList<SensorItem> sensorItems) {
		this.sensorItems = sensorItems;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return sensorItems.size();
	}

	@Override
	public SensorItem getItem(int position) {
		return sensorItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		System.out.println("getView " + position + " " + convertView);

		SensorItem item = sensorItems.get(position);

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.main_sensor_list_item, (ViewGroup) null);
			
			// Find the child views of the list item and create a reference to them
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.text1);
			holder.txtType = (TextView) convertView.findViewById(R.id.text2);
			holder.txtRate = (TextView) convertView.findViewById(R.id.text3);
			
			// Optimization: Tag the row with it's child views, so we don't have to   
			// call findViewById() later when we reuse the row.
			convertView.setTag(holder);
		} 
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Display item data
		holder.txtName.setText(item.getName());
		holder.txtType.setText(item.getStringType());
		holder.txtRate.setText(item.getStringRate());
		return convertView;
	}
}
