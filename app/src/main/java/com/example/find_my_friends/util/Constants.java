package com.example.find_my_friends.util;

import com.example.find_my_friends.userUtil.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * a class containing constants used throughout the application, so reference doesn't need to be constantly maintained through out each activity
 * contains constants such as current user , current user document reference
 * and tag keys
 */
public class Constants {
    public static final String FIND_FRIENDS_KEY = "Find my friends application";
    public static final String MAPVIEW_BUNDLE_KEY = "GroupDetailsMapViewKey";
    public static final String DATEPICKER_TAG_KEY = "date_picker_key";
    public static final String TIMEPICKER_TAG_KEY = "time_picker_key";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    public static final int REQUEST_GALLERY_ACCESS = 1000;
    public static int GPS_UPDATE_RATE = 10000;
    public static final int RESULT_LOADED_IMAGE = 1;
    public static final int RESULT_LOCATION_REQUEST = 2;
    public static User currentUser;
    public static FirebaseUser currentUserFirebase;
    public static DocumentSnapshot currentUserDocument;
    public static Boolean CurrentUserLoaded = false;

}
