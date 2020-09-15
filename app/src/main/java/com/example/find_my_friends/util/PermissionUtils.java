package com.example.find_my_friends.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import static com.example.find_my_friends.util.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.example.find_my_friends.util.Constants.REQUEST_GALLERY_ACCESS;

/**
 * The class containing functions regarding the request of permissions on the client's device, inclusive of location, background location and read external storage.
 *
 * @author Harold Carter
 * @version 2.0
 */
public class PermissionUtils {


    /**
     * checks if the activity currently requesting location permissions already has the permission or not, if not then it will request the permission to the user, and display a rational if required
     *
     * @param activity Activity requesting the Location permissions
     */
    public static void requestLocationPermission(Activity activity) {
        if (!hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (shouldShowRational(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(activity, "LOCATION permission is needed to display groups near you", Toast.LENGTH_SHORT)
                        .show();
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }


    /**
     * checks if the activity currently requesting Read External permissions already has the permission or not, if not then it will request the permission to the user, and display a rational if required
     *
     * @param activity Activity requesting the Read External permissions
     */
    public static void requestReadExternalPermission(Activity activity) {
        if (!hasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (shouldShowRational(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(activity, "Read permission is required to access the gallery", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_GALLERY_ACCESS);

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_GALLERY_ACCESS);

            }
        }
    }

    /**
     * checks the status of the read external permissions state for a specific activity
     *
     * @param activity activity that the permissions are being queried on
     * @return boolean representing the state of the permissions
     */
    public static boolean checkReadExternalPermission(Activity activity) {
        return hasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * checks the status of the location permissions state for a specific activity
     *
     * @param activity activity that the permissions are being queried on
     * @return boolean representing the state of the permissions
     */
    public static boolean checkLocationPermission(Activity activity) {
        return hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * checks if the build version is above android lolipop as this is when the method for requesting permissions changes to runtime requests, without this if the app is sideloaded onto an emulator or app then it will cause crashes that cannot be easily traced therefore mal practice to leave this out.
     *
     * @return boolean if the current build version is greater than lolipop
     */
    public static boolean checkRunTimePermissions() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * checks the status of the given permission's state for a specific activity
     *
     * @param activity   activity that the permissions are being queried on
     * @param permission the permission that is being checked.
     * @return boolean representing the state of the permissions
     */
    public static boolean hasPermission(Activity activity, String permission) {
        if (checkRunTimePermissions()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * checks the status of if the rational should be displayed for a gvein permission for a specific activity
     *
     * @param activity   activity that the permissions are being queried on
     * @param permission the permission being checked
     * @return boolean representing if the permission should have a rational attached or not.
     */
    public static boolean shouldShowRational(Activity activity, String permission) {
        if (checkRunTimePermissions()) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

}

