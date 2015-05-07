package com.github.nirmalpatidar123.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;

public class LoadMoreExpendableListView extends ExpandableListView implements OnScrollListener {

    private boolean isLoadingBlocked;
    private OnLoadMoreListener mOnLoadMoreListener;

    public LoadMoreExpendableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnScrollListener(this);
    }

    public LoadMoreExpendableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnScrollListener(this);
    }

    public LoadMoreExpendableListView(Context context) {
        super(context);
        this.setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        if (getAdapter() == null)
            return;

        if (getAdapter().getCount() == 0)
            return;

        int l = visibleItemCount + firstVisibleItem;
        if (l >= totalItemCount && !isLoadingBlocked) {
            // It is time to add new data. We call the listener
            //this.addFooterView(footer);
            isLoadingBlocked = true;
            if (mOnLoadMoreListener != null) {
                mOnLoadMoreListener.onLoadMore();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return mOnLoadMoreListener;
    }

    /**
     * @param onLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }


    public boolean isLoadingBlocked() {
        return isLoadingBlocked;
    }

    public void setLoadingBlocked(boolean isLoading) {
        this.isLoadingBlocked = isLoading;
    }
}
