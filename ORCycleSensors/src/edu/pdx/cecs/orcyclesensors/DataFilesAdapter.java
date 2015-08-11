package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DataFilesAdapter extends BaseAdapter {
	
	private final static String MODULE_TAG = "DataFilesAdapter";
	
	private final class DataFileListItem extends DataFileInfo {
		
		private long id;
		
		public DataFileListItem(DataFileInfo d, long id) {
			super(d.getName(), d.getPath(), d.getLength());
			this.id = id;
		}
		
		public long getId() {
			return id;
		}
	}

	private final class ViewHolder {
		public TextView txtName;
		public TextView txtNumber;
	}

	private final LayoutInflater layoutInflater;
	private final ArrayList<DataFileListItem> dataFiles;
	private final int defaultColor;
	private final int selectedColor;
	private final ArrayList<Long> selectedItems = new ArrayList<Long>();
	
	public DataFilesAdapter(LayoutInflater layoutInflater, ArrayList<DataFileInfo> dataFilesInfos, 
			int defaultColor, int selectedColor) {
		
		this.layoutInflater = layoutInflater;
		this.defaultColor = defaultColor;
		this.selectedColor = selectedColor;
		
		this.dataFiles = new ArrayList<DataFileListItem>();
		for (DataFileInfo d: dataFilesInfos) {
			this.dataFiles.add(new DataFileListItem(d, dataFiles.size()));
		}
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
		for (long id: selectedItemsArray) {
			selectedItems.add(id);
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
	
	public ArrayList<DataFileInfo> getSelectedDataFileInfos() {
		
		ArrayList<DataFileInfo> dataFileInfos = new ArrayList<DataFileInfo>(); 
		DataFileInfo dataFileInfo;
		
		for (DataFileListItem item: dataFiles) {
			if (isSelected(item.getId())) {
				dataFileInfo = new DataFileInfo(item.getName(), item.getPath(), item.getLength()); 
				dataFileInfos.add(dataFileInfo);
			}
		}
		
		return dataFileInfos;
	}
	
	@Override
	public int getCount() {
		return dataFiles.size();
	}

	@Override
	public DataFileInfo getItem(int position) {
		return dataFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return dataFiles.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		try {
			// get current item 
			DataFileListItem item = dataFiles.get(position);
	
			// Create view holder
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.main_data_file_list_item, null);
				
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
			holder.txtNumber.setText(String.valueOf(item.getLength()));
			
			// Set view's selection color
			convertView.setBackgroundColor(isSelected(item.getId()) ? selectedColor : defaultColor);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return convertView;
	}
}
