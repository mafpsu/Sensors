package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SavedDevicesAdapter extends BaseAdapter {

	private final static String MODULE_TAG = "SavedDevicesAdapter";
	
	private final class ViewHolder {
		public TextView txtName;
		public TextView txtNumber;
	}

	private final LayoutInflater layoutInflater;
	private final ArrayList<AntDeviceInfo> antDeviceList;
	private final int defaultColor;
	private final int selectedColor;
	private final ArrayList<Long> selectedItems = new ArrayList<Long>();

	public SavedDevicesAdapter(LayoutInflater layoutInflater, ArrayList<AntDeviceInfo> antDeviceInfos,
			int defaultColor, int selectedColor) {
		this.layoutInflater = layoutInflater;
		this.antDeviceList = antDeviceInfos;
		this.defaultColor = defaultColor;
		this.selectedColor = selectedColor;
	}

	public ArrayList<Long> getSelectedItems() {
		return selectedItems;
	}
	
	public long[] getSelectedItemsArray() {
		
		long[] selectedItemsArray = new long[selectedItems.size()];

		for(int i = 0; i < selectedItems.size(); ++i) {
			selectedItemsArray[i] = selectedItems.get(i);
		}
		
		return selectedItemsArray;
	}
	
	public void setSelectedItems(long[] selectedItemsArray) {
		selectedItems.clear();
		for (long tripId: selectedItemsArray) {
			selectedItems.add(tripId);
		}
	}
	
	public boolean isSelected(long id) {
		return selectedItems.indexOf(id) >= 0;
	}
	
	public void select(long id, boolean select) {
		if (select) {
			selectedItems.add(id);
		}
		else {
			selectedItems.remove(id);
		}
	}
	
	public int numSelectedItems() {
		return selectedItems.size();
	}
	
	public void toggleSelection(long id) {
		if (isSelected(id)) {
			select(id, false);
		}
		else {
			select(id, true);
		}
	}
	
	public void clearSelectedItems() {
		selectedItems.clear();
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

		try {
			// get current item 
			AntDeviceInfo item = antDeviceList.get(position);
	
			// Create view holder
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.main_device_list_item, null);
				
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
	
			// Set view's data
			holder.txtName.setText(item.getName());
			holder.txtNumber.setText(String.valueOf(item.getNumber()));
	
			// Set view's selection color
			convertView.setBackgroundColor(isSelected(getItemId(position)) ? selectedColor : defaultColor);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return convertView;
	}
}
