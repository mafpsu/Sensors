package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SavedDevicesAdapter extends BaseAdapter {

	private ArrayList<AntDeviceInfo> antDeviceList = new ArrayList<AntDeviceInfo>();
	private LayoutInflater mInflater;

	private static class ViewHolder {
		public TextView txtName;
		public TextView txtNumber;
	}

	public SavedDevicesAdapter(LayoutInflater layoutInflater, ArrayList<AntDeviceInfo> antDeviceInfos) {
		mInflater = layoutInflater;
		this.antDeviceList = antDeviceInfos;
	}

	@Override
	public int getCount() {
		return antDeviceList.size();
	}

	@Override
	public AntDeviceInfo getItem(int position) {
		return antDeviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return antDeviceList.get(position).getNumber();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		AntDeviceInfo item = antDeviceList.get(position);

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.main_device_list_item, null);
			
			// Find the child views of the list item and create a reference to them
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.text1);
			holder.txtNumber = (TextView) convertView.findViewById(R.id.text2);
			
			// Optimization: Tag the row with it's child views, so we don't have to   
			// call findViewById() later when we reuse the row.
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Display item data
		holder.txtName.setText(item.getName());
		holder.txtNumber.setText(String.valueOf(item.getNumber()));
		return convertView;
	}
}
