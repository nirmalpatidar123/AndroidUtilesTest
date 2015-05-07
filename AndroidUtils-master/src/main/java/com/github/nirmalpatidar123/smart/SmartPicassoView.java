package com.github.nirmalpatidar123.smart;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

public class SmartPicassoView extends ImageView {

	private Context context;
	private boolean isSmartPicassoViewSquared = false;

	public boolean isSmartPicassoViewSquared() {
		return isSmartPicassoViewSquared;
	}

	public void seSmartPicassoViewSquared(boolean isSmartPicassoViewSquared) {
		this.isSmartPicassoViewSquared = isSmartPicassoViewSquared;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (isSmartPicassoViewSquared)
			setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}

	public SmartPicassoView(Context paramContext) {
		super(paramContext);
		this.context = paramContext;
	}

	public SmartPicassoView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		this.context = paramContext;
	}

	public SmartPicassoView(Context paramContext,
			AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		this.context = paramContext;
	}

	/**
	 * 
	 * @param imageUrl
	 * @param progressBar
	 */
	public void downloadImageWithPicasso(final String imageUrl,
			ProgressBar progressBar) {
		Picasso.with(context).load(imageUrl)
				.into(this, new PicassoCallback(progressBar));
	}

	/**
	 * 
	 * @param imageUri
	 * @param progressBar
	 */
	public void downloadImageWithPicasso(final Uri imageUri,
			ProgressBar progressBar) {
		Picasso.with(context).load(imageUri)
				.into(this, new PicassoCallback(progressBar));
	}

    /**
     *
     * @param imageUrl
     * @param progressBar
     * @param placeholderResId
     */
	public void downloadImageWithPicasso(final String imageUrl,
			ProgressBar progressBar, final int placeholderResId) {
		Picasso.with(context).load(imageUrl).placeholder(placeholderResId)
				.into(this, new PicassoCallback(progressBar));
	}

	/****
	 * 
	 * @param imageUrl
	 * @param progressBar
	 * @param placeholderResId
	 * @param width
	 * @param height
	 */
	public void downloadImageWithPicasso(final String imageUrl,
			ProgressBar progressBar, final int placeholderResId,
			final int width, final int height) {
		Picasso.with(context).load(imageUrl).placeholder(placeholderResId)
				.resize(width, height)
				.into(this, new PicassoCallback(progressBar));
	}

	/**
	 * 
	 * @param file
	 * @param progressBar
	 */
	public void downloadImageWithPicasso(final File file,
			ProgressBar progressBar) {
		Picasso.with(context).load(file)
				.into(this, new PicassoCallback(progressBar));
	}
}
