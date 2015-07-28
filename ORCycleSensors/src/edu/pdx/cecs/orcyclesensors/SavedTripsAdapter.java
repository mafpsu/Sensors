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
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SavedTripsAdapter extends SimpleCursorAdapter {

	private final static String MODULE_TAG = "SavedTripsAdapter";

	private final Context context;
	private final String[] from;
	private final int[] to;
	Cursor cursor;

	public SavedTripsAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, R.layout.saved_trips_list_item, c, from, to, flags);
		this.context = context;
		this.from = from;
		this.to = to;
		this.cursor = c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		try {
			//Log.v(MODULE_TAG, "getView(Position): " + position);

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			rowView = inflater.inflate(R.layout.saved_trips_list_item, parent, false);
			TextView textViewStart = (TextView) rowView.findViewById(R.id.TextViewStart);
			TextView textViewPurpose = (TextView) rowView.findViewById(R.id.TextViewPurpose);
			TextView textViewInfo = (TextView) rowView.findViewById(R.id.TextViewInfo);
			ImageView imageTripPurpose = (ImageView) rowView.findViewById(R.id.ImageTripPurpose);

			cursor.moveToPosition(position);

			SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  h:mm a");
			Double startTime = cursor.getDouble(cursor.getColumnIndex("start"));
			String start = sdfStart.format(startTime);

			textViewStart.setText(start);
			//textViewPurpose.setText(cursor.getString(cursor.getColumnIndex("purp")));
			textViewPurpose.setText("ID: " + String.valueOf(cursor.getLong(cursor.getColumnIndex(DbAdapter.K_TRIP_ROWID))));

			SimpleDateFormat sdfDuration = new SimpleDateFormat("HH:mm:ss");
			sdfDuration.setTimeZone(TimeZone.getTimeZone("UTC"));
			Double endTime = cursor.getDouble(cursor.getColumnIndex("endtime"));
			String duration = sdfDuration.format(endTime - startTime);

			//Log.v(MODULE_TAG, "Duration: " + duration);

			textViewInfo.setText(duration);


			// -------------------------------------------------------
			//
			// -------------------------------------------------------

			int status = cursor.getInt(cursor.getColumnIndex("status"));

			//Log.v(MODULE_TAG, "Status: " + status);

			if (status == 0){
				//textViewPurpose.setText("In Progress");
				rowView.setVisibility(View.GONE);
				rowView = inflater.inflate(R.layout.saved_trips_list_item_null, parent, false);
			}
			else {
				rowView.setVisibility(View.VISIBLE);
			}

			int columnIndex;
			String value;

			if (status == 2) {
				if (-1 != (columnIndex = cursor.getColumnIndex("purp"))) {
					if (null != (value = cursor.getString(columnIndex))) {
						imageTripPurpose.setImageResource(R.drawable.other_high);
					}
				}
			} else if (status == 1) {
				imageTripPurpose.setImageResource(R.drawable.failedupload_high);
			}
			return rowView;
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return rowView;
	}
}