/**
 * 
 */
package de.rwth.aachen.comsys.androidcv.ui;

import de.rwth.aachen.comsys.androidcv.R;
import de.rwth.aachen.comsys.androidcv.ui.CustomArrayAdapter.RowType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ListViewItem implements Item {

	private final String str1;
	private final String str2;

	public ListViewItem(String text1, String text2) {
		this.str1 = text1;
		this.str2 = text2;
	}

	public int getViewType() {
		return RowType.LIST_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		
		View view;
        if (convertView == null) {
            view = (View) inflater.inflate(R.layout.listview_item, null);
            // Do some initialization
        } else {
            view = convertView;
        }

        TextView deviceName = (TextView) view.findViewById(R.id.tv_device_name);
        TextView deviceAddress = (TextView) view.findViewById(R.id.tv_device_address);
        deviceName.setText(str1);
        deviceAddress.setText(str2);

        return view;
	}

}
