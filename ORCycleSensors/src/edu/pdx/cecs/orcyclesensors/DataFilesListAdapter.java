package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DataFilesListAdapter extends BaseAdapter {
	private final ArrayList<DataFileInfo> dataFilesList;
	private final LayoutInflater mInflater;
	private final ArrayList<Long> selectedItems;
	private final int defaultColor;
	private final int selectedColor;

	private static class ViewHolder {
		public TextView txtName;
		public TextView txtNumber;
	}

	public DataFilesListAdapter(LayoutInflater layoutInflater, 
			ArrayList<DataFileInfo> dataFilesList, 
			ArrayList<Long> selectedItems,
			int defaultColor,
			int selectedColor) {
		this.mInflater = layoutInflater;
		this.dataFilesList = dataFilesList;
		this.selectedItems = selectedItems;
		this.defaultColor = defaultColor;
		this.selectedColor = selectedColor;
	}

	@Override
	public int getCount() {
		return dataFilesList.size();
	}

	@Override
	public DataFileInfo getItem(int position) {
		return dataFilesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		DataFileInfo item = dataFilesList.get(position);

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

		// Display item data
		holder.txtName.setText(item.getName());
		holder.txtNumber.setText(String.valueOf(item.getLength()));

		long id = (long) position;
		if (selectedItems.indexOf(id) > -1) {
			// highlight view
			convertView.setBackgroundColor(selectedColor);
		} else {
			// Remove highlight view
			convertView.setBackgroundColor(defaultColor);
		}

		return convertView;
	}
}
