package com.github.nirmalpatidar123.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.nirmalpatidar123.smart.PicassoCallback;
import com.github.nirmalpatidar123.web.HttpsConnection;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("SimpleDateFormat")
public class CommonUtils {

    private static float PHOTO_SCALE_FACTOR = 17f / 100f;
    private static float PREVIEW_SCALE_FACTOR = 17f / 100f;
    private static boolean isAppLogShown = true;

    public static void disableAppLog() {
        isAppLogShown = false;
    }

    /**
     * @param context
     * @return boolean
     */
    public static boolean checkForAppNewVersionAvailableOnPlayStore(
            final Context context) {

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);

            String playStoreResponse = getResponseFromUrl("https://play.google.com/store/apps/details?id="
                    + pInfo.packageName);
            Log.d("URl", "https://play.google.com/store/apps/details?id="
                    + pInfo.packageName);

            Pattern pattern = Pattern.compile("softwareVersion\">[^<]*</dd");
            Matcher matcher = pattern.matcher(playStoreResponse);
            matcher.find();

            String marketVersionName = matcher.group(0).substring(
                    matcher.group(0).indexOf(">") + 1,
                    matcher.group(0).indexOf("<"));

            Log.d("VersionAvailble", marketVersionName);
            String existingVersionName = pInfo.versionName;
            Log.d("VersionActual", existingVersionName);

            if (!marketVersionName.equals(existingVersionName)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getMD5EncryptedString(String encTarget) {
        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    /**
     * @param imageUrl
     * @param callback
     * @param activity
     */
    public static void setBitmapFromURL(final String imageUrl, final PicassoCallback callback,
                                        final Activity activity) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (callback == null) {
                    Log.e("setBitmapFromURL", "callback is null");
                    return;
                } else if (activity == null) {
                    callback.onImageDownloadError(null, "activity is null");
                    return;
                } else if (TextUtils.isEmpty(imageUrl)) {
                    callback.onImageDownloadError(null, "image url is not found");
                    return;
                }

                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);

                    connection.connect();
                    InputStream input = connection.getInputStream();
                    final Bitmap myBitmap = BitmapFactory.decodeStream(input);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (myBitmap != null) {
                                callback.setImageBitmap(myBitmap);
                                callback.onSuccess();
                            } else {
                                callback.onImageDownloadError(null, "bitmap is found null");
                            }
                        }
                    });
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onImageDownloadError(e, e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * @param activity
     * @return
     */
    public static int getScreenOrientation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e("getScreenOrientation", "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    Log.e("getScreenOrientation", "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    public static String getResponseFromUrl(String urlString) {
        String str = "";
        try {
            URL url = new URL(urlString);
            URLConnection spoof = url.openConnection();
            spoof.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    spoof.getInputStream()));
            String strLine = "";
            // Loop through every line in the source
            while ((strLine = in.readLine()) != null) {
                str = str + strLine;
            }
        } catch (Exception e) {
        }
        return str;
    }

    /**
     * @param context
     * @return String
     */
    public static String getDeviceUniqueId(Context context) {
        return Secure
                .getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    /**
     * @param date
     * @param patternRequired
     * @param timeZone
     * @return String
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("SimpleDateFormat")
    public static Date getDateWithPattern(String date, String patternRequired,
                                          TimeZone timeZone) {
        try {
            SimpleDateFormat requiredSdf = new SimpleDateFormat(patternRequired);
            if (timeZone == null) {
                requiredSdf.setTimeZone(TimeZone.getDefault());
            } else {
                requiredSdf.setTimeZone(timeZone);
            }
            return requiredSdf.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date(date);
        }
    }

    /**
     * @param date
     * @param patternRequired
     * @param timeZone
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDateWithPattern(Date date, String patternRequired,
                                            TimeZone timeZone) {
        try {
            SimpleDateFormat requiredSdf = new SimpleDateFormat(patternRequired);
            if (timeZone == null) {
                requiredSdf.setTimeZone(TimeZone.getDefault());
            } else {
                requiredSdf.setTimeZone(timeZone);
            }
            return requiredSdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return date.toString();
        }
    }

    /**
     * @param activity
     * @return an array having width in 0 index and height in 1 index
     */
    public static int[] getDeviceWidthAndHeight(Activity activity) {
        int[] wh = new int[2];
        try {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay()
                    .getMetrics(displaymetrics);
            wh[0] = displaymetrics.heightPixels;
            wh[1] = displaymetrics.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wh;
    }

    /**
     * @param fileName
     * @return fileExtension
     */
    public static String getFileExtension(String fileName) {
        String fileExtension = "";
        if (!TextUtils.isEmpty(fileName) && fileName.contains(".")) {

            int lastIndexOfDot = fileName.lastIndexOf('.');
            fileExtension = fileName.substring(lastIndexOfDot + 1,
                    fileName.length());
        }
        return fileExtension;
    }

    /**
     * @param context
     */
    public static void printHashKey(Context context) {

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getApplicationContext().getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("TEMPTAGHASH KEY:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    /**
     * This method use PackageManager Class to check for instagram package.
     *
     * @param context
     * @param appPackage
     * @return boolean
     */
    public static boolean isAppInstalledOnDevice(Context context,
                                                 final String appPackage) {

        boolean app_installed = false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(appPackage, 0);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    /**
     * @param activity
     * @return int[]
     */
    public static int[] getDeviceWidthHeight(Activity activity) {
        int[] mDeviceWidthHeight = new int[2];
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        mDeviceWidthHeight[0] = displaymetrics.widthPixels;
        mDeviceWidthHeight[1] = displaymetrics.heightPixels;
        return mDeviceWidthHeight;
    }

    /**
     * Get Active Fragment From fragment Stack
     *
     * @param fActivity
     * @return Fragment
     */
    public static Fragment getCurrentFragment(FragmentActivity fActivity) {
        FragmentManager fragmentManager = fActivity.getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();

        Fragment currentFragment = null;
        try {
            String fragmentTag = fragmentManager.getBackStackEntryAt(count - 1)
                    .getName();
            currentFragment = fActivity.getSupportFragmentManager()
                    .findFragmentByTag(fragmentTag);
        } catch (Exception e) {
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList != null && fragmentList.size() > 0) {
                currentFragment = fragmentList.get(fragmentList.size() - 1);
            }
            if (currentFragment == null)
                currentFragment = fragmentList.get(0);
        }
        return currentFragment;
    }

    /**
     *
     * @param fActivity
     * @return
     */
    public static List<Fragment> getAllFragments(FragmentActivity fActivity) {
        FragmentManager fm = fActivity.getSupportFragmentManager();

        List<Fragment> allFragments = fActivity.getSupportFragmentManager().getFragments();

        if (allFragments == null || allFragments.isEmpty()) {
            List<Fragment> fragments = new ArrayList<Fragment>();
            int count = fm.getBackStackEntryCount();
            for (int i = 0; i < count; i++) {
                String fragmentTag = fm.getBackStackEntryAt(i).getName();
                Fragment currentFragment = fActivity.getSupportFragmentManager()
                        .findFragmentByTag(fragmentTag);
                fragments.add(currentFragment);
            }
            return fragments;
        } else {
            return allFragments;
        }
    }
	
    /**
     * Get Active Fragment From fragment Stack
     *
     * @param activity
     * @return Fragment
     */
    @SuppressLint("NewApi")
    public static android.app.Fragment getCurrentFragment(Activity activity) {
        android.app.FragmentManager fragmentManager = activity
                .getFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();

        android.app.Fragment currentFragment = null;
        try {
            String fragmentTag = fragmentManager.getBackStackEntryAt(count - 1)
                    .getName();
            currentFragment = fragmentManager.findFragmentByTag(fragmentTag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentFragment;
    }

    // /**
    // * @param dateString
    // * @param patternRequired
    // * @param dateStringPattern
    // * @return String
    // */
    @SuppressLint("SimpleDateFormat")
    public static String getDateWithPattern(Date date, String patternRequired) {
        try {
            SimpleDateFormat requiredSdf = new SimpleDateFormat(patternRequired);
            requiredSdf.setTimeZone(TimeZone.getTimeZone("gmt"));
            return requiredSdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return date.toString();
        }
    }

    /**
     * @param date
     * @param datePattern
     * @param requiredDatePattern
     * @return requiredDate
     */
    @SuppressWarnings("deprecation")
    public static String getGMTDateWithRequiredPattern(String date,
                                                       String datePattern, String requiredDatePattern) {
        String requiredDate = null;
        try {

            SimpleDateFormat formatter = new SimpleDateFormat(datePattern);

            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date requestDate = formatter.parse(date);
            System.out.println("AtuallDate" + requestDate.toLocaleString());

            SimpleDateFormat requiredFormatter = new SimpleDateFormat(
                    requiredDatePattern);
            requiredDate = requiredFormatter.format(requestDate);

            System.out.println("DATE REEE" + requiredDate);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return requiredDate;
    }

    /**
     * @param fragment
     * @param TAG
     */
    public static void addNewFragment(FragmentActivity fActivity,
                                      Fragment fragment, final String TAG, final int frameLayoutId) {
        FragmentTransaction transaction = fActivity.getSupportFragmentManager()
                .beginTransaction();
        transaction.add(frameLayoutId, fragment, TAG);
        transaction.addToBackStack(TAG);

        transaction.commit();
    }

    /**
     * @param activity
     * @param fragment
     * @param TAG
     * @param frameLayoutId
     */
    @SuppressLint("NewApi")
    public static void addNewFragment(Activity activity,
                                      android.app.Fragment fragment, final String TAG,
                                      final int frameLayoutId) {
        android.app.FragmentTransaction transaction = activity
                .getFragmentManager().beginTransaction();
        transaction.add(frameLayoutId, fragment, TAG);
        transaction.addToBackStack(TAG);
        transaction.commit();
    }

    /**
     * @param fActivity
     */
    public static void removeFragment(FragmentActivity fActivity) {
        FragmentManager fm = fActivity.getSupportFragmentManager();
        fm.popBackStackImmediate();
    }

    /**
     * @param fActivity
     */
    public static void removeFragment(Activity fActivity) {
        android.app.FragmentManager fm = fActivity.getFragmentManager();
        fm.popBackStackImmediate();
    }

    /**
     * Apply all Email validations and return true if succeed.
     *
     * @param email
     * @return boolean
     */
    public static boolean isEmailValid(final String email) {

        CharSequence inputStr = email;

        final Pattern EMAIL_ADDRESS = Pattern
                .compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
                        + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                        + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

        // //////// Pattern pattern =
        // Pattern.compile(,Pattern.CASE_INSENSITIVE);
        Matcher matcher = EMAIL_ADDRESS.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Apply all phone number validations and return true if succeed.
     *
     * @param phoneNumber
     * @return boolean [0-9]{6}([0-9]{3})?
     */
    public static boolean isPhoneNumberValid(final String phoneNumber) {

        // CharSequence inputStr = phoneNumber;

        // final Pattern PHONE_NUMBER_PATTERN = Pattern
        // .compile("[0-9]{6}([0-9]{3})?");

        // global-number-digits = "+" *phonedigit DIGIT *phonedigit
        // phonedigit = DIGIT / visual-separator
        // visual-separator = "-" / "." / "(" / ")"
        // String DIGIT = "0" / "1" / "2" / "3" / "4" / "5" / "6" / "7" / "8" /
        // "9";

        // // Pattern pattern = Pattern.compile(,Pattern.CASE_INSENSITIVE);
        // Matcher matcher = PHONE_NUMBER_PATTERN.matcher(inputStr);
        // if (matcher.matches())
        // return true;
        // else
        // return false;

        Pattern onlyNumberPattern = Pattern.compile("\\d+");
        if (phoneNumber.length() > 0
                && onlyNumberPattern.matcher(phoneNumber).matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param context
     * @param filePath
     * @return String location
     */
    public static String getFileLocationFromExif(Context context,
                                                 String filePath) {
        String location = null;
        try {
            float orgLat = 0.0f, orgLng = 0.0f;
            ExifInterface exif = new ExifInterface(filePath);
            String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String longitude = exif
                    .getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            String latitudeRef = exif
                    .getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String longitudeRef = exif
                    .getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            if (latitude != null && longitude != null && latitudeRef != null
                    && longitudeRef != null) {

                if (latitudeRef.equals("N")) {
                    orgLat = convertToDegree(latitude);
                } else {
                    orgLat = 0 - convertToDegree(latitude);
                }

                if (longitudeRef.equals("E")) {
                    orgLng = convertToDegree(longitude);
                } else {
                    orgLng = 0 - convertToDegree(longitude);
                }
                Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geoCoder.getFromLocation(orgLat,
                        orgLng, 1);
                if (addresses.size() > 0) {
                    location = addresses.get(0).getLocality() + ", "
                            + addresses.get(0).getCountryName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public static int getLatitudeE6(float lat) {
        return (int) (lat * 1000000);
    }

    public static int getLongitudeE6(float lng) {
        return (int) (lng * 1000000);
    }

    /**
     * @param date
     * @param datePattern
     * @param requiredDatePattern
     * @return requiredDate
     */
    @SuppressWarnings("deprecation")
    public static String getDateWithRequiredPattern(String date,
                                                    String datePattern, String requiredDatePattern) {
        String requiredDate = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
            Date requestDate = formatter.parse(date);
            System.out.println("ActualDate" + requestDate.toLocaleString());

            SimpleDateFormat requiredFormatter = new SimpleDateFormat(
                    requiredDatePattern);
            requiredDate = requiredFormatter.format(requestDate);

            System.out.println("requiredDate" + requiredDate);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return requiredDate;
    }

    /**
     * *
     *
     * @param filePath
     * @return dateTime
     */
    public static String getFileCreationDateFromExif(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            // String make = exif.getAttribute(ExifInterface.TAG_MAKE);
            // String gps = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);

            return dateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param stringDMS
     * @return Float
     */
    private static Float convertToDegree(String stringDMS) {
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        double FloatS = S0 / S1;

        result = (float) (FloatD + (FloatM / 60) + (FloatS / 3600));
        return result;
    }

    /**
     * Apply all web Url validations and return true if succeed.
     *
     * @param webUrl
     * @return boolean
     */
    public static boolean isWebUrlValid(final String webUrl) {

        CharSequence inputStr = webUrl;

        final Pattern WEB_URL_PATTERN = Pattern
                .compile("(((file|gopher|news|nntp|telnet|http|ftp|https|ftps|sftp)://)|(www\\.))+(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(/[a-zA-Z0-9\\&amp;%_\\./-~-]*)?");

        // Pattern pattern = Pattern.compile(,Pattern.CASE_INSENSITIVE);
        Matcher matcher = WEB_URL_PATTERN.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * return true if given String has special characters
     *
     * @param string
     * @return boolean
     */
    public static boolean isStringHasSpecialCharacter(String string) {
        return !string.matches("[a-zA-Z0-9. ]*");
    }

    /**
     * @param context
     * @param editText
     */
    public static void showKeyboard(final Context context,
                                    final EditText editText) {
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(editText,
                                InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }

    /**
     * @param context
     * @param textView
     */
    public static void hideKeyboard(final Context context,
                                    final TextView textView) {
        textView.post(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(textView.getWindowToken(), 0);
            }
        });
    }

    /**
     * @param tempStorageForBitmapByteArrayHolder (Temp storage to use for decoding. Suggest 16K or so.)(default
     *                                            you can provide (16*1024)
     * @return
     */
    public static Options getOptionsForBitmap(
            int tempStorageForBitmapByteArrayHolder, int inSampleSize) {
        Options opt = new Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inSampleSize = inSampleSize;
        opt.inTempStorage = new byte[tempStorageForBitmapByteArrayHolder];
        return opt;
    }

    /**
     * @param uri
     * @return String array that contains ID and path at zero index and first
     * index
     */
    public static String[] getImageID_Path(Uri uri, Activity activity) {
        String[] projection = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID};
        @SuppressWarnings("deprecation")
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index_path = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        int column_index_id = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        cursor.moveToFirst();
        return new String[]{cursor.getString(column_index_id),
                cursor.getString(column_index_path)};
    }

    /**
     * This return bitmap of thumbnail of Uri passed to funtion
     */
    public static Bitmap getThumbBitmap(String id, Context context) {
        long longid = Long.parseLong(id);

        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                context.getContentResolver(), longid,
                MediaStore.Images.Thumbnails.MICRO_KIND, (Options) null);
        return bitmap;
    }

    /**
     * **
     *
     * @param uri
     * @param currentActivity
     * @return Bitmap
     */
    public static Bitmap readThumbImageFromIntentUri(Uri uri,
                                                     Activity currentActivity, int deviceWidth) {
        try {
            String[] imagedata = getImageID_Path(uri, currentActivity);
            Bitmap thumbbitmap = getThumbBitmap(imagedata[0], currentActivity);
            return thumbbitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * We had given the path of where image is being store while calling intent,
     * now here we read bitmap from same path
     *
     * @return Bitmap
     */
    public static Bitmap readThumbImageFromExistingPath(Uri uriToRead,
                                                        Context context, int deviceWidth) {
        try {
            ExifInterface exif = new ExifInterface(uriToRead.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1);
            int rotateAngle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateAngle = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateAngle = 90;
                    break;
                default:
                    break;
            }
            Log.v("ORI:ANGLE", orientation + " :: " + rotateAngle);

            Bitmap thumbbitmap = readImageBitmapFromUri(context, uriToRead,
                    deviceWidth);
            Log.v("W:::H",
                    thumbbitmap.getWidth() + ":" + thumbbitmap.getHeight());
            thumbbitmap = ThumbnailUtils.extractThumbnail(thumbbitmap,
                    (int) (deviceWidth * PHOTO_SCALE_FACTOR),
                    (int) (deviceWidth * PHOTO_SCALE_FACTOR),
                    MediaStore.Images.Thumbnails.MICRO_KIND);
            // thumbbitmap =
            // Utils.getRescaledBitmap(thumbbitmap,(int)(Constant.DEVICE_WIDTH*Constant.PHOTO_SCALE_FACTOR),
            // (int)(Constant.DEVICE_WIDTH*Constant.PHOTO_SCALE_FACTOR),true);
            if (rotateAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotateAngle);
                thumbbitmap = Bitmap.createBitmap(thumbbitmap, 0, 0,
                        thumbbitmap.getWidth(), thumbbitmap.getHeight(),
                        matrix, true);
            }
            return thumbbitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * We had given the path of where image is being store while calling intent,
     * now here we read bitmap from same path
     *
     * @return Bitmap
     */
    public static Bitmap readThumbImageFromExistingPath(Uri uriToRead,
                                                        Context context, int requiredWidth, int requiredHeight) {
        try {
            ExifInterface exif = new ExifInterface(uriToRead.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1);
            int rotateAngle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateAngle = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateAngle = 90;
                    break;
                default:
                    break;
            }
            Log.v("ORI:ANGLE", orientation + " :: " + rotateAngle);

            Bitmap thumbbitmap = readImageBitmapFromUri(context, uriToRead,
                    requiredWidth, requiredHeight);
            thumbbitmap = Bitmap.createScaledBitmap(thumbbitmap, requiredWidth,
                    requiredHeight, false);
            Log.v("W:::H",
                    thumbbitmap.getWidth() + ":" + thumbbitmap.getHeight());

            if (rotateAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotateAngle);

                thumbbitmap = Bitmap.createBitmap(thumbbitmap, 0, 0,
                        thumbbitmap.getWidth(), thumbbitmap.getHeight(),
                        matrix, true);
                // saveBitmapInPath(context, uriToRead);
            }
            return thumbbitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * *
     *
     * @param context
     * @param uriPath
     * @param deviceWidth
     * @return Bitmap
     */
    public static Bitmap readImageBitmapFromUri(Context context, Uri uriPath,
                                                int deviceWidth) {

        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uriPath);
            return BitmapFactory.decodeStream(inputStream, null,
                    getThumbImageBitmapFactoryOptions(deviceWidth));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param context
     * @param uriToRead
     */
    public static void saveBitmapInPath(final Context context,
                                        final Uri uriToRead) {

        final ContentResolver contentResolver = context.getContentResolver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = contentResolver
                            .openInputStream(uriToRead);
                    FileOutputStream fo = new FileOutputStream(uriToRead
                            .getPath());

                    byte[] b = new byte[inputStream.available()];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(b)) != -1) {
                        fo.write(b, 0, bytesRead);
                    }
                    fo.close();
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * @param context
     * @param uriPath
     * @param width
     * @param height
     * @return Bitmap
     */
    public static Bitmap readImageBitmapFromUri(Context context, Uri uriPath,
                                                int width, int height) {

        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uriPath);
            return BitmapFactory.decodeStream(inputStream, null,
                    getThumbImageBitmapFactoryOptions(width, height));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param realPath
     * @return Bitmap
     */
    public static Bitmap getBitmapToSendPicture(String realPath) {

        try {
            File file = new File(realPath);
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param realPath
     * @param options
     * @return Bitmap
     */
    public static Bitmap getBitmapFromRealPath(String realPath, Options options) {

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(realPath, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param realPath
     * @return byte[]
     */
    public static byte[] getByteArrayToSendPicture(String realPath) {

        if (realPath != null && realPath.length() != 0) {
            try {
                File file = new File(realPath);
                FileInputStream fis = new FileInputStream(file);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                fis.close();

                return CommonUtils.getBitmapArray(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * ****
     *
     * @param deviceWidth
     * @return BitmapFactory.Options
     */
    public static Options getThumbImageBitmapFactoryOptions(int deviceWidth) {
        Options opt = new Options();
        opt.inTempStorage = new byte[8 * 1024];
        opt.inSampleSize = 4;
        opt.outWidth = (int) (deviceWidth * PREVIEW_SCALE_FACTOR);
        opt.outHeight = (int) (deviceWidth * PREVIEW_SCALE_FACTOR);
        return opt;
    }

    /**
     * @param width
     * @param height
     * @return BitmapFactory.Options
     */
    public static Options getThumbImageBitmapFactoryOptions(int width,
                                                            int height) {
        Options opt = new Options();
        opt.inTempStorage = new byte[8 * 1024];
        opt.inSampleSize = 2;
        opt.outWidth = width;
        opt.outHeight = height;
        return opt;
    }

    public static long getAvailableSpaceInSdCard() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        long bytesAvailable = (long) stat.getBlockSize()
                * (long) stat.getBlockCount();
        return bytesAvailable;
    }

    /**
     * @param contentUri
     * @param activity
     * @return String
     */
    public static String getRealPathOfGalleryPhotosFromURI(Uri contentUri,
                                                           Activity activity) {
        Cursor cursor = null;
        String path = null;
        try {
            cursor = activity.getApplication().getContentResolver()
                    .query(contentUri, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local
                // file path
                return contentUri.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor
                        .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
            }

            if (TextUtils.isEmpty(path)
                    && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)) {
                path = getRealPathOfGalleryPhotosFromURI(
                        activity.getApplicationContext(), contentUri);
            }
            if (TextUtils.isEmpty(path)) {

                String[] proj = {MediaStore.Images.Media.DATA};

                cursor = activity.managedQuery(contentUri, proj, null, null,
                        null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                path = cursor.getString(column_index);
                cursor.close();
            }
        } catch (android.database.StaleDataException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("getPathGalleryFromURI", e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }
        return path;
    }

    /**
     * @param context
     * @param photoUri
     * @return String
     */
    @TargetApi(19)
    public static String getRealPathOfGalleryPhotosFromURI(
            final Context context, Uri photoUri) {
        Cursor cursor = null;
        String filePath = null;
        try {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(photoUri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            // Cursor cursor = activity.getContentResolver().query(
            // MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
            // new String[] { id }, null);

            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
                    new String[]{id}, null);
            // //activity.startManagingCursor(cursor);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }
        return filePath;
    }

    /**
     * @param contentUri
     * @param activity
     * @return String
     */
    @SuppressWarnings("deprecation")
    public static String getRealPathOfGalleryVideosFromURI(Uri contentUri,
                                                           Activity activity) {
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            Cursor cursor = activity.managedQuery(contentUri, proj, null, null,
                    null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    /**
     * @param bitmap
     * @return byte[]
     */
    public static byte[] getBitmapArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /**
     * @param tv
     */
    public static void setTextViewTextUnderline(TextView tv) {
        String mystring = tv.getText().toString();
        android.text.SpannableString content = new android.text.SpannableString(
                mystring);
        content.setSpan(new android.text.style.UnderlineSpan(), 0,
                mystring.length(), 0);
        tv.setText(content);
    }

    /**
     * @param valueObj
     * @return Map
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("rawtypes")
    public static Map getFieldNamesAndValues(final Object valueObj)
            throws IllegalArgumentException, IllegalAccessException {
        Class c1 = valueObj.getClass();

        Map fieldMap = new HashMap();
        Field[] valueObjFields = c1.getDeclaredFields();

        // compare values now
        for (int i = 0; i < valueObjFields.length; i++) {

            String fieldName = valueObjFields[i].getName();

            valueObjFields[i].setAccessible(true);

            Object newObj = valueObjFields[i].get(valueObj);

            fieldMap.put(fieldName, newObj);
        }
        return fieldMap;
    }

    /**
     * @param valueMap
     * @param className
     * @return Object
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("rawtypes")
    public static Object getObjectWithFieldNamesAndValues(final Map valueMap,
                                                          final Class className) throws IllegalArgumentException,
            IllegalAccessException, InstantiationException {

        Object object = className.newInstance();

        // Class c1 = valueObj.getClass();

        Field[] valueObjFields = className.getDeclaredFields();

        // compare values now
        for (int i = 0; i < valueObjFields.length; i++) {

            String fieldName = valueObjFields[i].getName();
            Object valueInMap = valueMap.get(fieldName);

            if (valueInMap != null) {
                valueObjFields[i].setAccessible(true);
                valueObjFields[i].set(object, valueInMap);
            }
        }

        return object;
    }

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
            Log.e("Utils", ex.toString());
        }
        return "Not Available";
    }

    /**
     * **
     *
     * @param orgBitmap
     * @param newWidth
     * @param newHeight
     * @param newBitmapIsLarge
     * @return
     */
    public static Bitmap getRescaledBitmap(Bitmap orgBitmap, int newWidth,
                                           int newHeight, boolean newBitmapIsLarge) {
        return Bitmap.createScaledBitmap(orgBitmap, newWidth, newHeight,
                newBitmapIsLarge);
    }

    /**
     * @param context
     * @return
     */
    public static boolean isDeviceSupportCamera(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * To find support to do action corresponding to given Intent
     *
     * @param intent  , for which the method find support to take action
     * @param context
     * @return boolean , true if device has support , otherwise false
     */
    public static boolean isDeviceHaveAnySupportedAppForIntent(Intent intent,
                                                               Context context) {
        List<ResolveInfo> supportedApps = context.getPackageManager()
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (supportedApps != null && supportedApps.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDeviceHasSdCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * @param videoUri
     * @return duration in miliseconds or -1 if not found
     */
    public static long getVideoDurationFromUri(Context context, Uri videoUri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = MediaStore.Video.query(contentResolver, videoUri,
                    new String[]{MediaStore.Video.VideoColumns.DURATION});
            if (cursor.moveToFirst()) {
                String duration = cursor.getString(0);
                return Long.parseLong(duration);
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * @param mediaPath
     * @return duration in miliseconds or -1 if not found
     */
    public static int getMediaDurationFromRealPath(String mediaPath) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(mediaPath);
            mp.prepare();
            return mp.getDuration();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * @param source
     * @param dest
     * @throws java.io.IOException
     */
    public static void copyFile(File source, File dest) throws IOException {
        if (!dest.exists())
            dest.createNewFile();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);
            byte[] buf = new byte[1024 * 2];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     *
     * @param sourceFileName
     * @param dirName
     */
    public static void moveFileWithUnixCommand(File sourceFileName,
                                               String dirName) {
        try {
            Runtime rt = Runtime.getRuntime();
            String cmd = "mv" + " " + sourceFileName + " " + dirName;
            Process proc = rt.exec(cmd);
            InputStream is = proc.getInputStream();
            String result = HttpsConnection.getResultFromInputStream(is);
            Log.v("result", result + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(File resource) throws IOException {
        if (resource.isDirectory()) {
            File[] childFiles = resource.listFiles();
            for (File child : childFiles) {
                delete(child);
            }
        }
        return resource.delete();
    }

    /**
     * This method is used to find a 'hh:mm:ss' format' String from seconds
     *
     * @param seconds
     * @return String
     */
    public static String getHoursMinutesSecondsString(int seconds) {

        try {
            int hours = seconds / 3600;
            int minutes = (seconds / 60) - (hours * 60);
            seconds = seconds - (hours * 3600) - (minutes * 60);
            return String.format(Locale.getDefault(),"%d:%02d:%02d", hours, minutes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
            return seconds + "";
        }
    }

    public static String getCommaSeparetedString(long number) {

        try {
            return NumberFormat.getNumberInstance(Locale.US).format(number);
        } catch (Exception e) {
            e.printStackTrace();
            return number + "";
        }
        // try {
        // DecimalFormat df = new DecimalFormat();
        // return df.format(number).toString();
        // } catch (Exception e) {
        // e.printStackTrace();
        // return null;
        // }
    }

    /**
     * @param folderPath
     */
    public static void readFilesFromPath(final String folderPath,
                                         ArrayList<String> list) {
        File file = new File(folderPath);
        listFilesForFolder(file, list);
    }

    /**
     * @param folder
     * @param fileList
     */
    public static void listFilesForFolder(final File folder,
                                          ArrayList<String> fileList) {

        try {
            if (folder.exists() && !folder.isHidden()) {

                File[] list = folder.listFiles();

                for (final File fileEntry : list) {
                    if (fileEntry.isDirectory() && !fileEntry.isHidden()) {
                        listFilesForFolder(fileEntry, fileList);
                    } else if (!fileEntry.isHidden()) {
                        fileList.add(fileEntry.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileList
     */
    public static void readVideoFilesOfExternalCameraDcimPath(File folder,
                                                              ArrayList<String> fileList) {
        if (folder == null) {
            folder = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        }

        try {
            if (folder.exists() && !folder.isHidden()) {

                File[] list = folder.listFiles();

                for (final File fileEntry : list) {
                    if (fileEntry.isDirectory() && !fileEntry.isHidden()) {
                        readVideoFilesOfExternalCameraDcimPath(fileEntry,
                                fileList);
                    } else if (!fileEntry.isHidden()
                            && (fileEntry.getName().endsWith(".mp4")
                            || fileEntry.getName().endsWith(".3gp")
                            || fileEntry.getName().endsWith(".MP4") || fileEntry
                            .getName().endsWith(".3GP"))) {
                        fileList.add(fileEntry.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param folder
     * @param fileList
     * @param fileType
     */
    public static void readFilesfromExternalPath(File folder,
                                                 ArrayList<String> fileList, final String fileType) {
        if (folder == null) {
            folder = Environment.getExternalStorageDirectory();
        }

        try {
            if (folder.exists() && !folder.isHidden()) {

                File[] list = folder.listFiles();

                for (final File fileEntry : list) {
                    if (fileEntry.isDirectory() && !fileEntry.isHidden()) {
                        readFilesfromExternalPath(fileEntry, fileList, fileType);
                    } else if (!fileEntry.isHidden()
                            && (fileEntry.getName().endsWith(fileType)
                            || fileEntry.getName().endsWith(
                            fileType.toUpperCase(Locale.getDefault())) || fileEntry
                            .getName().endsWith(fileType.toLowerCase(Locale.getDefault())))) {
                        fileList.add(fileEntry.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileList
     */
    public static void readPhotoFilesOfExternalCameraDcimPath(File folder,
                                                              ArrayList<String> fileList) {
        if (folder == null) {
            folder = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        }

        try {
            if (folder.exists() && !folder.isHidden()) {

                File[] list = folder.listFiles();

                for (final File fileEntry : list) {
                    if (fileEntry.isDirectory() && !fileEntry.isHidden()) {
                        readPhotoFilesOfExternalCameraDcimPath(fileEntry,
                                fileList);
                    } else if (!fileEntry.isHidden()
                            && (fileEntry.getName().endsWith(".png")
                            || fileEntry.getName().endsWith(".jpg")
                            || fileEntry.getName().endsWith(".jpeg")
                            || fileEntry.getName().endsWith(".PNG")
                            || fileEntry.getName().endsWith(".JPG") || fileEntry
                            .getName().endsWith(".JPEG"))) {
                        fileList.add(fileEntry.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param imageFile
     */
    public static void rotateFileImageIfNecessary(File imageFile) {

        try {
            String filePath = imageFile.getAbsolutePath();

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1);
            int rotateAngle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotateAngle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotateAngle = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotateAngle = 90;
                    break;
                default:
                    break;
            }

            Bitmap mybitmap = null;
            try {
                if (rotateAngle != 0) {
                    Bitmap orgBitmap = BitmapFactory.decodeFile(filePath);
                    Matrix matrix = new Matrix();
                    matrix.setRotate(rotateAngle);
                    mybitmap = Bitmap.createBitmap(orgBitmap, 0, 0,
                            orgBitmap.getWidth(), orgBitmap.getHeight(),
                            matrix, true);
                }
            } catch (OutOfMemoryError error) {
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = 4;
                Bitmap orgBitmap = BitmapFactory.decodeFile(filePath, o2);

                if (rotateAngle != 0) {
                    Matrix matrix = new Matrix();
                    matrix.setRotate(rotateAngle);
                    mybitmap = Bitmap.createBitmap(orgBitmap, 0, 0,
                            orgBitmap.getWidth(), orgBitmap.getHeight(),
                            matrix, true);
                }
                error.printStackTrace();
            }
            if (mybitmap == null || rotateAngle == 0)
                return;

            try {
                FileOutputStream fOut = new FileOutputStream(filePath);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mybitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                bos.writeTo(fOut);
                bos.close();
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * *
     *
     * @param context
     * @param selectedImage
     * @return Bitmap
     */
    public static Bitmap decodeUri(Context context, Uri selectedImage) {
        try {
            // Decode image size
            Options o = new Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 400;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            Options o2 = new Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(selectedImage), null, o2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param bmp
     * @param fileName
     * @return isSaved
     */
    public static File saveBitmapInExternalStorage(Bitmap bmp, String fileName) {
        try {
            File f = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + fileName + ".png");

            if (f.exists())
                f.delete();

            FileOutputStream fos = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            Runtime.getRuntime().gc();
            return f;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @param bmp
     * @param filePath
     * @return
     */
    public static File saveBitmapAtGivenPath(Bitmap bmp, String filePath) {
        try {
            File f = new File(filePath);
            if (f.exists())
                f.delete();

            FileOutputStream fos = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            Runtime.getRuntime().gc();
            return f;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * ** To set Video Layout params according to the video' width and height
     *
     * @param mediaPlayer
     * @param videoView
     * @param context
     * @return int activity orientation
     */
    public static int setVideoLayoutParams(MediaPlayer mediaPlayer,
                                           VideoView videoView, Context context) {

        int orientation;
        // Get the dimensions of the video
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        // Get the width of the screen
        float screenProportion = (float) screenWidth / (float) screenHeight;

        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }

        if (lp.width > lp.height)
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        else
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        // Commit the layout parameters
        videoView.setLayoutParams(lp);
        return orientation;
    }

    /**
     * @param context
     * @return
     */
    public static ArrayList<String> getEmailsFromDeviceContacs(Context context) {
        HashSet<String> names = new HashSet<String>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur
                        .getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                + " = ?", new String[]{id}, null);
                while (cur1.moveToNext()) {
                    // to get the contact names
                    String name = cur1
                            .getString(cur1
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Log.e("Name :", name);
                    String email = cur1
                            .getString(cur1
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.e("Email", email);

                    if (!TextUtils.isEmpty(name) && isEmailValid(name)) {
                        names.add(name);
                    }

                    if (!TextUtils.isEmpty(email) && isEmailValid(email)) {
                        names.add(email);
                    }
                }
                cur1.close();
                cur.close();
            }
        }
        return new ArrayList<String>(names);
    }

    /**
     * ** To set Video Layout params according to the video' width and height
     *
     * @param mediaPlayer
     * @param videoView
     * @param activity
     */
    public static void setVideoLayoutParams(MediaPlayer mediaPlayer,
                                            VideoView videoView, Activity activity) {

        int orientation;
        // Get the dimensions of the video
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;

        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        // Get the width of the screen
        float screenProportion = (float) screenWidth / (float) screenHeight;

        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }

        if (lp.width > lp.height) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        activity.setRequestedOrientation(orientation);
        // Commit the layout parameters
        videoView.setLayoutParams(lp);
    }

    /**
     * @param editText
     * @param errorMessage
     */
    public static void setError(final EditText editText,
                                final String errorMessage) {
        int ecolor = Color.RED; // whatever color you want
        ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(
                errorMessage);
        ssbuilder.setSpan(fgcspan, 0, errorMessage.length(), 0);
        editText.setError(ssbuilder);
    }

    /**
     * Checks if is my service running.
     *
     * @return true, if is my service running
     */
    public static boolean isMyServiceRunning(Context context,
                                             String serviceClassName) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {

            String serviceName = service.service.getClassName();
            if (serviceClassName.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param fActivity
     * @param fragment
     * @param TAG
     * @param frameLayoutId
     */
    public static void replaceFragment(FragmentActivity fActivity,
                                       Fragment fragment, final String TAG, final int frameLayoutId) {
        FragmentTransaction transaction = fActivity.getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(frameLayoutId, fragment, TAG);
        transaction.commit();
    }

    // public static boolean setVideoSizeAccordingToNetworkType(Context ctx) {
    // ConnectivityManager cm = (ConnectivityManager) ctx
    // .getSystemService(Context.CONNECTIVITY_SERVICE);
    // NetworkInfo info = cm.getActiveNetworkInfo();
    // int type = info.getType();
    // int subType = info.getSubtype();
    // if (type == ConnectivityManager.TYPE_WIFI) {
    // System.out.println("CONNECTED VIA WIFI");
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx, "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true;
    // } else if (type == ConnectivityManager.TYPE_MOBILE) {
    // switch (subType) {
    // case TelephonyManager.NETWORK_TYPE_1xRTT:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 5;
    // Toast.makeText(ctx,
    // "Current network can send only 5 MB of video",
    // Toast.LENGTH_LONG).show();
    // return false; // ~ 50-100 kbps
    // case TelephonyManager.NETWORK_TYPE_CDMA:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 5;
    // Toast.makeText(ctx,
    // "Current network can send only 5 MB of video",
    // Toast.LENGTH_LONG).show();
    // return false; // ~ 14-64 kbps
    // case TelephonyManager.NETWORK_TYPE_EDGE:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 5;
    // Toast.makeText(ctx,
    // "Current network can send only 5 MB of video",
    // Toast.LENGTH_LONG).show();
    // return false; // ~ 50-100 kbps
    // case TelephonyManager.NETWORK_TYPE_EVDO_0:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 10;
    // Toast.makeText(ctx,
    // "Current network can send only 10 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 400-1000 kbps
    // case TelephonyManager.NETWORK_TYPE_EVDO_A:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 15;
    // Toast.makeText(ctx,
    // "Current network can send only 15 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 600-1400 kbps
    // case TelephonyManager.NETWORK_TYPE_GPRS:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 5;
    // Toast.makeText(ctx,
    // "Current network can send only 5 MB of video",
    // Toast.LENGTH_LONG).show();
    // return false; // ~ 100 kbps
    // case TelephonyManager.NETWORK_TYPE_HSDPA:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx,
    // "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 2-14 Mbps
    // case TelephonyManager.NETWORK_TYPE_HSPA:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 20;
    // Toast.makeText(ctx,
    // "Current network can send only 20 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 700-1700 kbps
    // case TelephonyManager.NETWORK_TYPE_HSUPA:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx,
    // "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 1-23 Mbps
    // case TelephonyManager.NETWORK_TYPE_UMTS:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx,
    // "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 400-7000 kbps
    // // NOT AVAILABLE YET IN API LEVEL 7
    // case NETWORK_TYPE_EHRPD:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx,
    // "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 1-2 Mbps
    // case NETWORK_TYPE_EVDO_B:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx,
    // "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 5 Mbps
    // case NETWORK_TYPE_HSPAP:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx,
    // "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 10-20 Mbps
    // case NETWORK_TYPE_IDEN:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 5;
    // Toast.makeText(ctx,
    // "Current network can send only 5 MB of video",
    // Toast.LENGTH_LONG).show();
    // return false; // ~25 kbps
    // case NETWORK_TYPE_LTE:
    // LibraryConstant.VIDEO_SIZE = 1024 * 1024 * 30;
    // Toast.makeText(ctx,
    // "Current network can send only 30 MB of video",
    // Toast.LENGTH_LONG).show();
    // return true; // ~ 10+ Mbps
    // // Unknown
    // case TelephonyManager.NETWORK_TYPE_UNKNOWN:
    // return false;
    // default:
    // return false;
    // }
    // } else {
    // return false;
    // }
    // }
}
