package com.github.nirmalpatidar123.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

/**
 * Class to keep static utility methods for various utility tasks Please Define
 * DIALOG_ICON before using the class
 */
public final class DialogUtils {

    private static ProgressDialog pd;
    public static int DIALOG_ICON = 0;

     /***
     * To show Dialog with message
     * @param activity
     * @param message
     */
    public static void showInfoDialog(final Activity activity,
                                      final String message) {

        AlertDialog.Builder builder = null;
        if (activity.getParent() != null)
            builder = new AlertDialog.Builder(activity.getParent());
        else
            builder = new AlertDialog.Builder(activity);

        // builder.setTitle("Alert!");
        builder.setIcon(DIALOG_ICON);
        if (message != null)
            builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Runtime.getRuntime().gc();
            }
        });
        AlertDialog msg = builder.create();

        msg.show();
    }

     /**
     * To show Dialog with message
     * @param activity
     * @param title
     * @param message
     */
    public static void showInfoDialog(final Activity activity,
                                      final String title, final String message) {
        AlertDialog.Builder builder = null;
        if (activity.getParent() != null)
            builder = new AlertDialog.Builder(activity.getParent());
        else
            builder = new AlertDialog.Builder(activity);

        if (title != null)
            builder.setTitle(title);
        builder.setIcon(DIALOG_ICON);
        if (message != null)
            builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Runtime.getRuntime().gc();
            }
        });
        AlertDialog msg = builder.create();

        msg.show();
    }

    public static void showInfoDialogWithTwoButtons(final Activity activity,
                                                    final String title, final String message, final String buttonOK,
                                                    final String buttonCancel) {
        AlertDialog.Builder builder = null;
        if (activity.getParent() != null)
            builder = new AlertDialog.Builder(activity.getParent());
        else
            builder = new AlertDialog.Builder(activity);

        if (title != null)
            builder.setTitle(title);
        builder.setIcon(DIALOG_ICON);
        if (message != null)
            builder.setMessage(message);

        builder.setPositiveButton(buttonOK,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Runtime.getRuntime().gc();
                    }
                });

        builder.setNegativeButton(buttonCancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Runtime.getRuntime().gc();
                    }
                });
        AlertDialog msg = builder.create();

        msg.show();
    }

     /**
     * To show Dialog with message and finish an activity when tap on Ok
     * @param activity
     * @param message
     * @return
     */
    public static AlertDialog showFinishDialog(final Activity activity,
                                        final String message) {
        try {
            AlertDialog.Builder builder = null;
            if (activity.getParent() != null)
                builder = new AlertDialog.Builder(activity.getParent());
            else
                builder = new AlertDialog.Builder(activity);

            builder.setCancelable(false);
            builder.setIcon(DIALOG_ICON);
            if (message != null)
                builder.setMessage(message);

            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Runtime.getRuntime().gc();
                            activity.finish();
                        }
                    });
            AlertDialog msg = builder.create();

            msg.show();
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * To show Dialog with message and finish an activity when tap on Ok
     * @param activity
     * @param title
     * @param message
     */
    public static void showFinishDialog(final Activity activity,
                                        final String title, final String message) {
        try {
            AlertDialog.Builder builder = null;
            if (activity.getParent() != null)
                builder = new AlertDialog.Builder(activity.getParent());
            else
                builder = new AlertDialog.Builder(activity);

            if (title != null)
                builder.setTitle(title);
            builder.setCancelable(false);
            builder.setIcon(DIALOG_ICON);
            if (message != null)
                builder.setMessage(message);

            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Runtime.getRuntime().gc();
                            activity.finish();
                        }
                    });
            AlertDialog msg = builder.create();

            msg.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * **
     * To show Dialog with message and pop current fragment when tap on Ok
     *
     * @param fActivity
     * @param title
     * @param message
     */
    public static void showFinishFragmentDialog(
            final FragmentActivity fActivity, final String title,
            final String message) {
        try {
            AlertDialog.Builder builder = null;
            if (fActivity.getParent() != null)
                builder = new AlertDialog.Builder(fActivity.getParent());
            else
                builder = new AlertDialog.Builder(fActivity);

            if (title != null)
                builder.setTitle(title);
            builder.setCancelable(false);
            builder.setIcon(DIALOG_ICON);
            if (message != null)
                builder.setMessage(message);

            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Runtime.getRuntime().gc();
                            fActivity.getSupportFragmentManager()
                                    .popBackStackImmediate();
                        }
                    });
            AlertDialog msg = builder.create();
            msg.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To show confirmation dialog with custom title,message,icon and buttons
     * name. With an interface to respond with tapping on dialog' buttons.
     * @param activity
     * @param title
     * @param message
     * @param isCancelable
     * @param iconResId
     * @param positiveBtnName
     * @param negativeBtnName
     * @param dri
     * @return
     */
    public static AlertDialog showConfirmationDialog(final Activity activity,
                                                     final String title, final String message,
                                                     final boolean isCancelable, final int iconResId,
                                                     final String positiveBtnName, final String negativeBtnName,
                                                     final DialogResponseInterface dri) {

        try {
            AlertDialog.Builder builder = null;
            if (activity.getParent() != null)
                builder = new AlertDialog.Builder(activity.getParent());
            else
                builder = new AlertDialog.Builder(activity);

            if (title != null)
                builder.setTitle(title);
            builder.setCancelable(isCancelable);
            builder.setIcon(iconResId);
            if (message != null)
                builder.setMessage(message);

            if (positiveBtnName != null) {
                builder.setPositiveButton(positiveBtnName,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                dri.doOnPositiveBtnClick(activity);
                            }
                        });
            }
            if (negativeBtnName != null) {
                builder.setNegativeButton(negativeBtnName,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                dri.doOnNegativeBtnClick(activity);
                            }
                        });
            }
            AlertDialog msg = builder.create();
            msg.show();
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * To show ProgressDialog while fetching in progress
     *
     * @param activity
     * @param title
     * @param message
     * @param isCancelable
     */
    public static void showProgressDialog(final Activity activity,
                                          final String title, final String message, final boolean isCancelable) {
        try {
            if (pd == null) {
                Activity parent = activity.getParent();
                if (parent != null)
                    pd = new ProgressDialog(parent);
                else
                    pd = new ProgressDialog(activity);
            } else
                pd.dismiss();

            //////////////////////////////////pd.setTitle(title);
            pd.setMessage(message);
            pd.setIcon(DIALOG_ICON);
            pd.setCancelable(isCancelable);
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * To dismiss progressDialog
     */
    public static void dismissProgressDialog() {
        try {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
                pd = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            pd = null;
        }
    }

    /**
     *
     * @param date
     * @param patternRequired
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDateWithPattern(Date date, String patternRequired) {
        try {
            SimpleDateFormat requiredSdf = new SimpleDateFormat(patternRequired);
            return requiredSdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return date.toString();
        }
    }
}
