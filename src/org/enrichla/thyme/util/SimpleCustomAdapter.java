package org.enrichla.thyme.util;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class SimpleCustomAdapter extends SimpleAdapter {

	public SimpleCustomAdapter(Context context,	List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		// Same as superclass
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		
		view.setBackgroundColor(position % 2 == 0 ? Color.rgb(0xB3, 0xCF, 0xF2) : Color.WHITE);
		return view;
	}

}
