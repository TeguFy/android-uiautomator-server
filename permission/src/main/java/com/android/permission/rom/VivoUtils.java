package com.android.permission.rom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by qiaoruikai on 2019-03-28 17:01.
 */
public class VivoUtils {
    private static final String TAG = "QikuUtils";

    /**
     * Check Vivo floating window permission
     */
    public static boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 21) {
            try {
                return getFloatPermissionStatus(context) == 0;
            } catch (Exception e) {
                e.printStackTrace();

                // Check via normal method
                if (Build.VERSION.SDK_INT >= 23) {
                    try {
                        Class clazz = Settings.class;
                        Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                        return (Boolean) canDrawOverlays.invoke(null, context);
                    } catch (Exception e1) {
                        Log.e(TAG, Log.getStackTraceString(e1));
                    }
                }
                return true;
            }
        }
        return true;
    }

    /**
     * Go to iManager request page
     */
    public static void applyPermission(final Context context) {
        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
        if(appIntent != null){
            try {
                context.startActivity(appIntent);
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Please go to \"App Management->Permission Management->Floating Window\" page to enable permission", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();

                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Please manually open iManager and go to \"App Management->Permission Management->Floating Window\" page to enable permission", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        } else {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Please manually open iManager and go to \"App Management->Permission Management->Floating Window\" page to enable permission", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    /**
     * Get floating window permission status
     *
     * @param context
     * @return 1 or other means not enabled, 0 means enabled. The definition of this status is similar to {@link android.app.AppOpsManager#MODE_ALLOWED}, MODE_IGNORED, etc. Refer to source code for details
     */
    public static int getFloatPermissionStatus(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        String packageName = context.getPackageName();
        Uri uri = Uri.parse("content://com.iqoo.secure.provider.secureprovider/allowfloatwindowapp");
        String selection = "pkgname = ?";
        String[] selectionArgs = new String[]{packageName};
        Cursor cursor = context
                .getContentResolver()
                .query(uri, null, selection, selectionArgs, null);
        if (cursor != null) {
            cursor.getColumnNames();
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("currentlmode");
                int currentMode = 0;
                if (columnIndex != -1) {
                    currentMode = cursor.getInt(columnIndex);
                }
                cursor.close();
                return currentMode;
            } else {
                cursor.close();
                return getFloatPermissionStatus2(context);
            }

        } else {
            return getFloatPermissionStatus2(context);
        }
    }

    /**
     * Vivo's newer system query method
     *
     * @param context
     * @return
     */
    private static int getFloatPermissionStatus2(Context context) {
        String packageName = context.getPackageName();
        Uri uri2 = Uri.parse("content://com.vivo.permissionmanager.provider.permission/float_window_apps");
        String selection = "pkgname = ?";
        String[] selectionArgs = new String[]{packageName};
        Cursor cursor = context
                .getContentResolver()
                .query(uri2, null, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("currentmode");
                int currentmode = 0;
                if (columnIndex != -1) {
                    cursor.getInt(columnIndex);
                }
                cursor.close();
                return currentmode;
            } else {
                cursor.close();
                return 1;
            }
        }
        return 1;
    }
}
