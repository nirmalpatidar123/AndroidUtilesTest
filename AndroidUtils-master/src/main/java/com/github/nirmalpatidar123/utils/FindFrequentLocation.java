package com.github.nirmalpatidar123.utils;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Class that implements LocationListener to find device current location from
 * various providers
 */
public final class FindFrequentLocation implements LocationListener {

	private LocationManager locationManager;
	private Timer timer;
	private FoundLocationInterface foundLocationInterface;
	private Location location;
	private long minTimeForLocation, minDistanceForLocation;

	/***
	 * An Inner Interface of this class have method public void
	 * doOnLocationReceived(Location location); This method is called when
	 * device' location changed or we found a new location. This interface
	 * offers a callback method for classes who need current location.
	 */
	public interface FoundLocationInterface {
		/**
		 * The callback method when you get location.
		 * 
		 * @param location
		 */
		public void doOnLocationReceived(Location location);
	}

	/************
	 * Parameterized constructor having argument of FoundLocationInterface
	 * 
	 * @param context
	 * @param minTimeForLocation
	 * @param minDistanceForLocation
	 * @param LOCATION_DELAY
	 * @param foundLocationInterface
	 */
	public FindFrequentLocation(final Context context,
			final long minTimeForLocation, final long minDistanceForLocation,
			final long LOCATION_DELAY,
			final FoundLocationInterface foundLocationInterface) {

		this.minDistanceForLocation = minDistanceForLocation;
		this.minTimeForLocation = minTimeForLocation;
		this.foundLocationInterface = foundLocationInterface;
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		requestLocationUpdates();
		timer = new Timer();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.i("Location", "Timer Task Executed ");
				// ///DialogUtils.dismissProgressDialog();

				if (location != null) {
					Log.i("Location in TimerTask", location.getLatitude()
							+ " : " + location.getLongitude());
					// ////////locationManager.removeUpdates(FindFrequentLocation.this);
					return;
				}
				location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				if (location != null)
					FindFrequentLocation.this.foundLocationInterface
							.doOnLocationReceived(location);
				else {
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null)
						FindFrequentLocation.this.foundLocationInterface
								.doOnLocationReceived(location);
				}
				if (location == null && context instanceof Activity) {
					((Activity) context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
//							Toast.makeText(((Activity) context),
//									"LOCATION_NOT_FOUND", Toast.LENGTH_LONG)
//									.show();
						}
					});
				}
				removeTimer();
			}
		}, LOCATION_DELAY);
		System.out.println("Location Manager Registered");
	}

	public void requestLocationUpdates() {
		try {
			if (locationManager == null)
				return;
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, minTimeForLocation,
					minDistanceForLocation, this);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, minTimeForLocation,
					minDistanceForLocation, this);
			Log.e("FindFrequentLocation","requestLocationUpdates");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeLocationUpdates() {
		if (locationManager == null)
			return;

		locationManager.removeUpdates(FindFrequentLocation.this);
		Log.e("FindFrequentLocation","removeLocationUpdates");
	}

	/**
	 * Remove TimerTask to not execute again
	 */
	private void removeTimer() {
		try {
			timer.cancel();
			timer.purge();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the location has changed. There are no restrictions on the
	 * use of the supplied Location object.
	 */
	@Override
	public void onLocationChanged(Location location) {
		Log.i("Location", "onLocationChanged Called");
		if (location != null && FindFrequentLocation.this.location == null) {
			FindFrequentLocation.this.location = location;
			FindFrequentLocation.this.foundLocationInterface
					.doOnLocationReceived(location);
			removeTimer();
			// /////locationManager.removeUpdates(FindFrequentLocation.this);
			// ///DialogUtils.dismissProgressDialog();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	protected void finalize() throws Throwable {

		if (locationManager != null) {
			locationManager.removeUpdates(FindFrequentLocation.this);
			Log.e("finalize","removeUpdates");
		}

		super.finalize();
	}
}
