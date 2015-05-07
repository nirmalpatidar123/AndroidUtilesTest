package com.github.nirmalpatidar123.web;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import com.github.nirmalpatidar123.R;
import com.github.nirmalpatidar123.utils.DialogUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Network Utility Class to handle network connection, modes and availability
 */
public final class NetworkUtils {
    private static boolean isNetwokAvailable;

    /**
     * Return true when device has any type of active network
     *
     * @param ctx
     * @return boolean
     */
    public static boolean isNetworkAvailable(Context ctx) {

        ConnectivityManager connectionManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectionManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectionManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected())
                || (mobileInfo != null && mobileInfo.isConnected())) {
            isNetwokAvailable = true;
        } else {
            isNetwokAvailable = false;
        }
        return isNetwokAvailable;
    }

	/*
     * public static boolean checkNetworkConnection(Activity activity) {
	 * 
	 * // Just need to check whether client need to show Dialog or Toast if //
	 * network not available, // since Toast is already applied, so I just
	 * returning true from here // till client confirmation. // //////if (true)
	 * return true;
	 * 
	 * if (NetworkUtils.isNetworkAvailable(activity.getApplicationContext())) {
	 * return true; } else { DialogUtils.showInfoDialog(activity,
	 * activity.getString(R.string.alert_network_not_exist)); return false; } }
	 */

    /**
     * Return true when device has any type of active network
     *
     * @param ctx
     * @return boolean
     */
    public static boolean isNetworkAvailable(Activity ctx, boolean isAlertShown) {

        ConnectivityManager connectionManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectionManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectionManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected())
                || (mobileInfo != null && mobileInfo.isConnected())) {
            isNetwokAvailable = true;
        } else {
            isNetwokAvailable = false;
        }

        if (!isNetwokAvailable && isAlertShown) {
            DialogUtils.showInfoDialog(ctx, "Alert!",
                    ctx.getString(R.string.alert_network_not_exist));
        }
        return isNetwokAvailable;
    }

    /**
     * Return true when device has any type of active network
     *
     * @param ctx
     * @return boolean
     */
    public static boolean finishActivityIfNetworkIsNotAvailable(Activity ctx) {

        ConnectivityManager connectionManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectionManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectionManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected())
                || (mobileInfo != null && mobileInfo.isConnected())) {
            isNetwokAvailable = true;
        } else {
            isNetwokAvailable = false;
        }
        if (!isNetwokAvailable) {
            DialogUtils.showFinishDialog(ctx, "Alert!",
                    ctx.getString(R.string.alert_network_not_exist));
        }
        return isNetwokAvailable;
    }

    /**
     * Return true when device has WIFI type of active network
     *
     * @param ctx
     * @return boolean
     */
    public static boolean isNetworkAvailable_wifi(Context ctx) {
        ConnectivityManager connectionManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectionManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiInfo.isConnected()) {
            isNetwokAvailable = true;
        } else {
            isNetwokAvailable = false;
        }

        return isNetwokAvailable;
    }

    /**
     * To Get Wifi Signal Strength
     *
     * @param context
     * @return int
     */
    public static int getWifiSignalStrength(Context context) {
        int MIN_RSSI = -100;
        int MAX_RSSI = -55;
        int levels = 101;
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        int rssi = info.getRssi();

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            return WifiManager.calculateSignalLevel(info.getRssi(), levels);
        } else {
            // this is the code since 4.0.1
            if (rssi <= MIN_RSSI) {
                return 0;
            } else if (rssi >= MAX_RSSI) {
                return levels - 1;
            } else {
                float inputRange = (MAX_RSSI - MIN_RSSI);
                float outputRange = (levels - 1);
                return (int) ((float) (rssi - MIN_RSSI) * outputRange / inputRange);
            }
        }
    }// end method

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

    }

    /**
     * To get Device Local IP address
     *
     * @return String
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "Not Available";
    }
}
