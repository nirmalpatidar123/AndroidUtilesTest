package com.github.nirmalpatidar123.clist;

import java.io.File;
import java.net.URLEncoder;

import android.content.Context;

public class FileCache {

	private File cacheDir;

	public FileCache(Context context) {
		// Find the dir to save cached images
		
		String diskCachePath = context.getCacheDir().getAbsolutePath()
				+ "clist";
		cacheDir = new File(diskCachePath);

//		if (android.os.Environment.getExternalStorageState().equals(
//				android.os.Environment.MEDIA_MOUNTED)) {
//
//
//		} else {
//			cacheDir = context.getCacheDir();
//		}

		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	@SuppressWarnings("deprecation")
	public File getFile(String url) {
		// I identify images by hashcode. Not a perfect solution, good for the
		// demo.
		// String filename=String.valueOf(url.hashCode());
		// Another possible solution (thanks to grantland)
		String filename = URLEncoder.encode(url);
		// File f = new File(cacheDir, filename);
		File f = new File(cacheDir, filename);
		return f;
	}

	public void clearFileCache() {
		try {
			File[] files = cacheDir.listFiles();
			if (files == null)
				return;
			for (File f : files)
				f.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}