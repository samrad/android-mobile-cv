/**
 * 
 */
package de.rwth.aachen.comsys.androidcv.ui;

import de.rwth.aachen.comsys.androidcv.R;
import de.rwth.aachen.comsys.androidcv.ui.CustomArrayAdapter.RowType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ListViewHeader implements Item {
	
	private final String name; 

	public ListViewHeader(String name) {
		this.name = name;
	}

	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
        if (convertView == null) {
            view = (View) inflater.inflate(R.layout.listview_header, null);
            // Do some initialization
        } else {
            view = convertView;
        }

        TextView text = (TextView) view.findViewById(R.id.separator);
        text.setText(name);

        return view;
	}

}
