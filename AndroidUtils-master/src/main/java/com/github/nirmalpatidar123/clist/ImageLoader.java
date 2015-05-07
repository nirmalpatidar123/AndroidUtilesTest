package com.github.nirmalpatidar123.clist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.nirmalpatidar123.utils.CommonUtils;

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;
	boolean isThumb = true;
	private Integer widthOrWidthPercentage;
	private boolean isApplyingForWidth, isDeviceSpecific, isBitmapFixed;
	public static int DEVICE_WIDTH = 0;

	/**
	 * ALWAYS SET static variable (DEVICE_WIDTH) to the device' width before
	 * creating object of ImageLoader if u are calling getFormattedBitmap
	 */
	/**
	 * @param context
	 */
	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	private void setBitmapToImage(ImageView imageView, Bitmap bitmap) {

		if (bitmap == null)
			return;

		if (isBitmapFixed) {
			imageView.setImageBitmap(bitmap);
			return;
		}
		try {
			if (isDeviceSpecific) {

				int width = bitmap.getWidth();
				int height = bitmap.getHeight();

				int newWidth, newHeight;
				if (isApplyingForWidth) {
					newWidth = widthOrWidthPercentage;
					newHeight = (height * newWidth) / width;
				} else {
					newHeight = widthOrWidthPercentage;
					newWidth = (width * newHeight) / height;
				}

				if (newWidth < newHeight)
					newHeight = newWidth;

				bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight,
						true);
			} else {

				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				int wOrhPercentage = widthOrWidthPercentage;

				if (isApplyingForWidth)
					width = (width * wOrhPercentage) / 100;
				else
					height = (height * wOrhPercentage) / 100;

				bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setImageSpecificProperty(final Integer widthOrWidthPercentage,
			final boolean isApplyingForWidth, final boolean isDeviceSpecific) {
		this.widthOrWidthPercentage = widthOrWidthPercentage;
		this.isApplyingForWidth = isApplyingForWidth;
		this.isDeviceSpecific = isDeviceSpecific;
	}

	/**
	 * @param url
	 * @param imageView
	 * @param bar
	 */
	public void displayImage(String url, ImageView imageView, ProgressBar bar) {
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			setBitmapToImage(imageView, bitmap);
			if (bar != null)
			bar.setVisibility(View.GONE);
		} else {
			queuePhoto(url, imageView, bar);
			// imageView.setImageResource(stub_id);
		}
	}

	/**
	 * @param url
	 * @param imageView
	 */
	public void displayImage(String url, ImageView imageView,
			final boolean isBitmapFixed) {
		this.isBitmapFixed = isBitmapFixed;
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			setBitmapToImage(imageView, bitmap);
		} else {
			queuePhoto(url, imageView);
			// imageView.setImageResource(stub_id);
		}
	}

	/**
	 * @param url
	 * @param imageView
	 * @param bar
	 */
	private void queuePhoto(String url, ImageView imageView, ProgressBar bar) {
		System.out.println("----queuePhoto--------");
		PhotoToLoad p = new PhotoToLoad(url, imageView, bar);
		executorService.submit(new PhotosLoader(p));
	}

	/**
	 * @param url
	 * @param imageView
	 */
	private void queuePhoto(String url, ImageView imageView) {
		System.out.println("----queuePhoto--------");
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap bitmap = decodeFile(f);
		if (bitmap != null) {
			// int width = bitmap.getWidth();
			// int height = bitmap.getHeight();
			return bitmap;
		}

		// from web
		try {
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();

			bitmap = BitmapFactory.decodeStream(is, null,

			CommonUtils.getOptionsForBitmap(8 * 1024,1));

			BufferedOutputStream ostream = new BufferedOutputStream(
					new FileOutputStream(f), 2 * 1024);

			// OutputStream os = new FileOutputStream(f);
			bitmap.compress(CompressFormat.JPEG, 100, ostream);
			ostream.close();
			is.close();

			// Utils.CopyStream(is, os);
			// bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param bitmap
	 * @return Bitmap
	 */
	private Bitmap getFormattedBitmap(Bitmap bitmap) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		if (width <= DEVICE_WIDTH)
			return bitmap;
		else {
			int newWidth, newHeight;
			newWidth = DEVICE_WIDTH;
			newHeight = (height * newWidth) / width;
			bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight,
					false);
			return bitmap;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 150;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		ProgressBar bar;

		public PhotoToLoad(String u, ImageView i, ProgressBar b) {
			url = u;
			imageView = i;
			bar = b;
		}

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null) {
				setBitmapToImage(photoToLoad.imageView, bitmap);

				// photoToLoad.imageView.setBackgroundDrawable(new
				// BitmapDrawable(bitmap));
				// photoToLoad.imageView.setImageBitmap(bitmap);
				if (photoToLoad.bar != null)
					photoToLoad.bar.setVisibility(View.GONE);
			}
			/*
			 * else photoToLoad.imageView.setImageResource(stub_id);
			 */
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clearFileCache();
	}

}
