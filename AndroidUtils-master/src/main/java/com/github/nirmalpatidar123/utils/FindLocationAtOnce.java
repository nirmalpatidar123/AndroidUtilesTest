package com.github.nirmalpatidar123.utils;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Class that implements LocationListener to find device current location from
 * various providers
 */
public final class FindLocationAtOnce implements LocationListener {

	private LocationManager locationManager;
	private Timer timer;
	private FoundLocationInterface foundLocationInterface;
	private Location location;

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

    /**
     *
     * @param activity
     * @param minTimeForLocation
     * @param minDistanceForLocation
     * @param LOCATION_DELAY
     * @param foundLocationInterface
     */
	public FindLocationAtOnce(final FragmentActivity activity,
			final long minTimeForLocation, final long minDistanceForLocation,
			final long LOCATION_DELAY,
			final FoundLocationInterface foundLocationInterface) {

		this.foundLocationInterface = foundLocationInterface;
		locationManager = (LocationManager) activity
				.getSystemService(Context.LOCATION_SERVICE);

		try {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					minTimeForLocation,
					minDistanceForLocation, this);
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER,
					minTimeForLocation,
					minDistanceForLocation, this);
		} catch (Exception e) {

			e.printStackTrace();
		}
		timer = new Timer();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Log.i("Location", "Timer Task Executed ");
				/////DialogUtils.dismissProgressDialog();

				if (location != null) {
					locationManager.removeUpdates(FindLocationAtOnce.this);
					return;
				}
				location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				if (location != null)
					FindLocationAtOnce.this.foundLocationInterface
							.doOnLocationReceived(location);
				else {
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null)
						FindLocationAtOnce.this.foundLocationInterface
								.doOnLocationReceived(location);
				}
				if (location == null && activity instanceof Activity) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
//							Toast.makeText(activity, "LOCATION_NOT_FOUND",
//									Toast.LENGTH_LONG).show();
						}
					});
				}
				removeTimer();
			}
		}, LOCATION_DELAY);
		System.out.println("Location Manager Registered");
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
		if (location != null && FindLocationAtOnce.this.location == null) {
			FindLocationAtOnce.this.location = location;
			FindLocationAtOnce.this.foundLocationInterface
					.doOnLocationReceived(location);
			removeTimer();
			locationManager.removeUpdates(FindLocationAtOnce.this);
			/////DialogUtils.dismissProgressDialog();
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
}
