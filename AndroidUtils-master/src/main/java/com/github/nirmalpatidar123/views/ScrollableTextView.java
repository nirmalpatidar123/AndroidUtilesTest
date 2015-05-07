package com.github.nirmalpatidar123.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.TextView;

public class ScrollableTextView extends TextView {

	private ListView listView;

	public ScrollableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollableTextView(Context context) {
		super(context);
	}	

	public void setListView(ListView listView) {
		this.listView = listView;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		boolean ret;
		ret = super.dispatchTouchEvent(event);
		if (ret && listView != null)
			listView.requestDisallowInterceptTouchEvent(true);
		return ret;
	}
}