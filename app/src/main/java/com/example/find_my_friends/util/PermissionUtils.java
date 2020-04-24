package com.example.find_my_friends.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static com.example.find_my_friends.util.Constants.LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE;
import static com.example.find_my_friends.util.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.example.find_my_friends.util.Constants.REQUEST_GALLERY_ACCESS;

public class PermissionUtils {


    public static void requestLocationPermission(Activity activity) {
        if (!hasPermission(activity ,Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (shouldShowRational(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(activity, "LOCATION permission is needed to display groups near you", Toast.LENGTH_SHORT)
                        .show();
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }


    public static void requestLocationBackgroundPermission(Activity activity) {
        if (!hasPermission(activity ,Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            if (shouldShowRational(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                Toast.makeText(activity, "Background location is required to get your location for the map", Toast.LENGTH_SHORT)
                        .show();
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                        LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                        LOCATION_BACKGROUND_PERMISSION_REQUEST_CODE);
            }
        }
    }


    public static void requestReadExternalPermission(Activity activity) {
        if (!hasPermission(activity ,Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (shouldShowRational(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(activity, "Read permission is required to access the gallery", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        REQUEST_GALLERY_ACCESS);

            } else {
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        REQUEST_GALLERY_ACCESS);

            }
        }
    }

    public static boolean checkReadExternalPermission(Activity activity){
        return hasPermission(activity ,Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public static boolean checkLocationPermission(Activity activity){
        return hasPermission(activity ,Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean checkLocationBackgroundPermission(Activity activity){
        return hasPermission(activity ,Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }





    public static boolean useRunTimePermissions() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean hasPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean hasPermission(Fragment fragment, String permission) {
        if (useRunTimePermissions() && fragment.getActivity() != null) {
            return fragment.getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestPermissions(Activity activity, String[] permission, int requestCode) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permission, requestCode);
        }
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        if (useRunTimePermissions() && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



    public static void requestPermissions(Fragment fragment, String[] permission, int requestCode) {
        if (useRunTimePermissions()) {
            fragment.requestPermissions(permission, requestCode);
        }
    }

    public static boolean shouldShowRational(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    /* example code not to be used as depreciated however useful if issues occur

    public static boolean shouldAskForPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return !hasPermission(activity, permission) &&
                    (!hasAskedForPermission(activity, permission) ||
                            shouldShowRational(activity, permission));
        }
        return false;
    }

    public static void goToAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean hasAskedForPermission(Activity activity, String permission) {
        return PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getBoolean(permission, false);
    }

    public static void markedPermissionAsAsked(Activity activity, String permission) {
        PreferenceManager
                .getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(permission, true)
                .apply();
    }

     */
}

