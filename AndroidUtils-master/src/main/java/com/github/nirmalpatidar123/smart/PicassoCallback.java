package com.github.nirmalpatidar123.smart;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;

/**
 * Created by Saurabh.Jain on 2/5/14.
 */
public class PicassoCallback implements Callback {

    private ProgressBar mProgressBar;
    private Bitmap mImageBitmap;
    private RequestCreator mRequestCreator;
    private GetBitmapCallback mGetBitmapCallback;

    public PicassoCallback() {
    }

    /**
     * This interface is called onSuccess and get bitmap from RequestCreator
     */
    public interface GetBitmapCallback {
        public void onSuccess(Bitmap mImageBitmap);

        public void onError(Throwable throwable);
    }

    /**
     * @param mProgressBar
     */
    public PicassoCallback(ProgressBar mProgressBar) {
        this.mProgressBar = mProgressBar;
    }

    /**
     * @param mRequestCreator
     */
    public PicassoCallback(RequestCreator mRequestCreator, GetBitmapCallback mGetBitmapCallback) {
        this.mRequestCreator = mRequestCreator;
        this.mGetBitmapCallback = mGetBitmapCallback;
    }

    /**
     * @param mRequestCreator
     * @param mProgressBar
     */
    public PicassoCallback(RequestCreator mRequestCreator, GetBitmapCallback mGetBitmapCallback, ProgressBar mProgressBar) {
        this.mProgressBar = mProgressBar;
        this.mRequestCreator = mRequestCreator;
        this.mGetBitmapCallback = mGetBitmapCallback;
    }

    @Override
    public void onSuccess() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        if (mRequestCreator != null) {
            try {
                setImageBitmap(mRequestCreator.get());
                if (mGetBitmapCallback != null) {
                    mGetBitmapCallback.onSuccess(getImageBitmap());
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (mGetBitmapCallback != null) {
                    mGetBitmapCallback.onError(e);
                }
            }
        }
    }

    @Override
    public void onError() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        if (mGetBitmapCallback != null) {
            mGetBitmapCallback.onError(new Exception("Error in ReqeustCallback"));
        }
    }

    /**
     * @param e
     * @param message
     */
    public void onImageDownloadError(Exception e, String message) {

        if (e != null) {
            e.printStackTrace();
        }
        Log.e("onImageDownloadError", message + "");
        onError();
    }

    public Bitmap getImageBitmap() {
        return mImageBitmap;
    }

    public void setImageBitmap(Bitmap mImageBitmap) {
        this.mImageBitmap = mImageBitmap;
    }
}
