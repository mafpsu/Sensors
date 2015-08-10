/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  @author Robin Murray <robin5@pdx.edu>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires and new ORcycle features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
 *
 *  Updated/modified for Oregon pilot study and app deployment.
 *
 *  ORcycle is free software: you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or any later version.
 *  ORcycle is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with
 *  ORcycle. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.pdx.cecs.orcyclesensors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class SavedTripsAdapter extends SimpleCursorAdapter {

	private final static String MODULE_TAG = "SavedTripsAdapter";
	
	private final static String[] from = new String[] { DbAdapter.K_TRIP_ROWID };
	private final static SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  h:mm a");
	private final static SimpleDateFormat sdfDuration = new SimpleDateFormat("HH:mm:ss");

	private static class ViewHolder {
		public TextView tvStartTime;
		public TextView tvTripId;
		public TextView tvTripDuration;
		public ImageView ivIcon;
	}

	private final Context context;
	private final Cursor cursor;
	private final int listItemLayout;
	private final ArrayList<Long> selectedItems = new ArrayList<Long>();
	private final int defaultColor;
	private final int selectedColor;

	public SavedTripsAdapter(Context context, int listItemLayout, Cursor cursor,
			int defaultColor, int selectedColor) {
		super(context, listItemLayout, cursor, from, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		SavedTripsAdapter.sdfDuration.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.context = context;
		this.cursor = cursor;
		this.listItemLayout = listItemLayout;
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
	
	public ArrayList<Long> getSelectedTrips() {
		
		ArrayList<Long> selectedTripIds = new ArrayList<Long>(selectedItems);
		
		return selectedTripIds;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		try {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// move cursor to item to be displayed 
			cursor.moveToPosition(position);
			Double startTime = cursor.getDouble(cursor.getColumnIndex(DbAdapter.K_TRIP_START));
			String formattedStartTime = sdfStart.format(startTime);
			long tripId = cursor.getLong(cursor.getColumnIndex(DbAdapter.K_TRIP_ROWID));
			Double endTime = cursor.getDouble(cursor.getColumnIndex(DbAdapter.K_TRIP_END));
			String formattedDuration = sdfDuration.format(endTime - startTime);
			int status = cursor.getInt(cursor.getColumnIndex(DbAdapter.K_TRIP_STATUS));

			// Create view holder
			ViewHolder holder = null;
			if (convertView == null) { // then this is the first time this item is being drawn
				// Inflate the list item
				convertView = inflater.inflate(listItemLayout, null);
				
				// Find the child views of the list item and create a reference to them
				holder = new ViewHolder();
				holder.tvStartTime = (TextView) convertView.findViewById(R.id.tv_start_time);
				holder.tvTripId = (TextView) convertView.findViewById(R.id.tv_trip_id);
				holder.tvTripDuration = (TextView) convertView.findViewById(R.id.tv_trip_duration);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.ImageTripPurpose);
				
				// Optimization: Tag the row with it's child views, so we don't have to   
				// call findViewById() later when we reuse the row.
				convertView.setTag(holder);
			} else { // this list item's view already exist
				holder = (ViewHolder) convertView.getTag();
			}
			
			// Display item data and selection color into view
			holder.tvStartTime.setText(formattedStartTime);
			holder.tvTripId.setText("ID: " + String.valueOf(tripId));
			holder.tvTripDuration.setText(formattedDuration);
			holder.ivIcon.setImageResource(getImageResource(status));
			convertView.setBackgroundColor(isSelected(tripId) ? selectedColor : defaultColor);

		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return convertView;
	}
	
	private int getImageResource(int status) {
		switch(status) {
		case 2:
			return R.drawable.other_high;
		case 1:
			return R.drawable.failedupload_high;
		default: // really 0 is the only other value which should never happen
			return R.drawable.failedupload_high;
		}
	}
	
}