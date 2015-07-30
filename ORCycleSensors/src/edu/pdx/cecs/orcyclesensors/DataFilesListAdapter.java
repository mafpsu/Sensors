package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DataFilesListAdapter extends BaseAdapter {
	private final ArrayList<DataFileListItem> dataFiles;
	private final LayoutInflater mInflater;
	private final ArrayList<Long> selectedItems = new ArrayList<Long>();
	private final int defaultColor;
	private final int selectedColor;
	
	private class DataFileListItem extends DataFileInfo {
		
		private long id;
		
		public DataFileListItem(DataFileInfo d, long id) {
			super(d.getName(), d.getPath(), d.getLength());
			this.id = id;
		}
		
		public long getId() {
			return id;
		}
	}

	private static class ViewHolder {
		public TextView txtName;
		public TextView txtNumber;
	}

	public DataFilesListAdapter(LayoutInflater layoutInflater, 
			ArrayList<DataFileInfo> dataFilesInfos, 
			int defaultColor,
			int selectedColor) {
		
		this.mInflater = layoutInflater;
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

		DataFileListItem item = dataFiles.get(position);

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.main_data_file_list_item, null);
			
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

		// Display item data and selection color
		holder.txtName.setText(item.getName());
		holder.txtNumber.setText(String.valueOf(item.getLength()));
		convertView.setBackgroundColor(isSelected(item.getId()) ? selectedColor : defaultColor);

		return convertView;
	}
}
