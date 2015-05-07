package com.github.nirmalpatidar123.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MultiLineSimpleSpinnerArrayAdapter extends ArrayAdapter<String> {
	private int textViewResId, layoutResId;
	private String[] objects;
	private LayoutInflater inflater;
	private List<String> objectsList;

	public MultiLineSimpleSpinnerArrayAdapter(Context context, int resource,
			int textViewResourceId, String[] objects) {
		super(context, resource, textViewResourceId, objects);

		this.textViewResId = textViewResourceId;
		this.layoutResId = resource;
		this.objects = objects;

		inflater = LayoutInflater.from(context);
	}

	public MultiLineSimpleSpinnerArrayAdapter(Context context, int resource,
			int textViewResourceId, List<String> objectsList) {
		super(context, resource, textViewResourceId, objectsList);

		this.textViewResId = textViewResourceId;
		this.layoutResId = resource;
		this.objectsList = objectsList;
		inflater = LayoutInflater.from(context);
	}

	// our ViewHolder.
    // caches our TextView
	static class ViewHolderItem {
		TextView textViewResId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolderItem viewHolderItem;
		if(convertView == null)
		{
			convertView  = inflater.inflate(layoutResId, null);

			// well set up the ViewHolder
			viewHolderItem = new ViewHolderItem();
			viewHolderItem.textViewResId = (TextView) convertView.findViewById(textViewResId);

			// store the holder with the view.
			convertView.setTag(viewHolderItem);

		}else{
			// we've just avoided calling findViewById() on resource everytime
			// just use the viewHolder
			viewHolderItem = (ViewHolderItem) convertView.getTag();
		}
		if (objectsList != null)
			viewHolderItem.textViewResId.setText(objectsList.get(position));
		else
			viewHolderItem.textViewResId.setText(objects[position]);

		return convertView;
	}
}
