package com.github.nirmalpatidar123.views;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.Toast;

public class GifDecoderView extends ImageView {

	private boolean mIsPlayingGif = false;

	private GifDecoder mGifDecoder;

	private Bitmap mTmpBitmap;

	final Handler mHandler = new Handler();

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			try {
				if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
					GifDecoderView.this.setImageBitmap(mTmpBitmap);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
	};

	public GifDecoderView(Context context, final String imageName) {
		super(context);
		setGifImageFromAssets(imageName);
	}

	public GifDecoderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GifDecoderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GifDecoderView(Context context) {
		super(context);
	}

	public void setGifImageFromAssets(String imageName) {
		InputStream stream = null;
		try {
			stream = getContext().getAssets().open(imageName);
			playGif(stream);
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getContext(),
					"Exception in Reading GIf file From PATH: " + imageName,
					Toast.LENGTH_LONG).show();
		}
	}

	private void playGif(InputStream stream) {
		mGifDecoder = new GifDecoder();
		mGifDecoder.read(stream);

		mIsPlayingGif = true;

		new Thread(new Runnable() {
			public void run() {
				try {
					final int n = mGifDecoder.getFrameCount();
					final int ntimes = mGifDecoder.getLoopCount();
					int repetitionCounter = 0;
					do {
						for (int i = 0; i < n; i++) {
							mTmpBitmap = mGifDecoder.getFrame(i);
							int t = mGifDecoder.getDelay(i);
							mHandler.post(mUpdateResults);
							try {
								Thread.sleep(t);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (ntimes != 0) {
							repetitionCounter++;
						}
					} while (mIsPlayingGif && (repetitionCounter <= ntimes));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Error e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void stopRendering() {
		mIsPlayingGif = true;
	}

	public void destroyGif() {

		try {
			if (mTmpBitmap != null) {
				mTmpBitmap.recycle();
				mTmpBitmap = null;
			}
			Bitmap a = mGifDecoder.getBitmap();
			if (a != null) {
				a.recycle();
				a = null;
			}

			Bitmap b = mGifDecoder.getLastBitmap();
			if (b != null) {
				b.recycle();
				b = null;
			}
			Bitmap c = mGifDecoder.getImage();
			if (c != null) {
				c.recycle();
				c = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}
}
