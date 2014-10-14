/**
 * Custom ArrayAdapter to populate ListView
 * with section header and items.
 */
package de.rwth.aachen.comsys.androidcv.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CustomArrayAdapter extends ArrayAdapter<Item> {

	private LayoutInflater mInflater;

	public CustomArrayAdapter(Context context, List<Item> items) {
		super(context, 0, items);
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
    public int getViewTypeCount() {
        return RowType.values().length;

    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(mInflater, convertView);
    }
    
    public enum RowType {
        LIST_ITEM, HEADER_ITEM
    }

}
