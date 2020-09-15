package com.example.find_my_friends.userUtil;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;

/**
 * A class containing a set of utility functions responsible for handling the functionality of the CurrentUser (signed in user).
 *
 * @author Harold Carter
 * @version 1.0
 */
public class CurrentUserUtil {
    static private CurrentUserUtil.ChangeListener listener;


    /**
     * function to notify the current user's listener if registered that the current users data has changes and subsequently make UI changes
     */
    static public void notifyChangeListener() {
        if (listener != null) {
            listener.onChange();
        }
    }


    /**
     * a function that will update the servers values for the users lat and long, and also change the local variable, even though this will be updated, it prohibits decisions being made based off un-synced data in the timeframe between change and snapshotcallback being made
     *
     * @param lat double for the new user latitude
     * @param lng double for the new user longitude
     */
    static public void setLocationCurrentUser(double lat, double lng) {
        currentUser.setUserLat(lat);
        currentUser.setUserLong((lng));
        currentUserDocument.getReference().update("userLat", currentUser.getUserLat());
        currentUserDocument.getReference().update("userLong", currentUser.getUserLong());
    }


    /**
     * updates the server to know the users current preference if the user wants to have their location currently uptodate (tracking them) or not; and also change the local variable, even though this will be updated, it prohibits decisions being made based off un-synced data in the timeframe between change and snapshotcallback being made
     *
     * @param userLocationUpToDate boolean for the current status of the user's location being uptodate or not
     */
    static public void setLocationUpToDateCurrentUser(Boolean userLocationUpToDate) {
        currentUser.setUserLocationUpToDate(userLocationUpToDate);
        currentUserDocument.getReference().update("userLocationUpToDate", currentUser.getUserLocationUpToDate());
    }

    /**
     * updates the server for the users mode of transportation, and also change the local variable, even though this will be updated, it prohibits decisions being made based off un-synced data in the timeframe between change and snapshotcallback being made
     *
     * @param modeOfTransport String for the users new mode of transportation
     */
    static public void setModeOfTransportCurrentUser(String modeOfTransport) {
        currentUser.setModeOfTransport(modeOfTransport);
        currentUserDocument.getReference().update("modeOfTransport", currentUser.getModeOfTransport());
    }

    /**
     * utilizing array unions to reduce write requests the membership for the current users is appended to, the group ID passed as a parameter is group which is appended to the current users memberships; and also updates the local variable, even though this will be updated, it prohibits decisions being made based off un-synced data in the timeframe between change and snapshotcallback being made
     *
     * @param groupUID String the group id which is being appended to the current users memberships
     */
    static public void appendMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() == null) {
            currentUser.setUsersMemberships(new ArrayList<>());
            currentUser.getUsersMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersMemberships", FieldValue.arrayUnion(groupUID));
        } else {
            currentUser.getUsersMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersMemberships", FieldValue.arrayUnion(groupUID));
        }
    }


    /**
     * utilizing array unions to reduce write requests the membership for the current user has group ID passed as a parameter removed from its current memberships
     *
     * @param groupUID String the group id which is being removed to the current users memberships
     * @return boolean representing if the transaction was a success or not
     */
    static public boolean removeMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() != null) {
            boolean outcome = currentUser.getUsersMemberships().remove(groupUID);
            currentUserDocument.getReference().update("usersMemberships", FieldValue.arrayRemove(groupUID));
            return outcome;
        } else {
            return false;
        }
    }

    /**
     * utilizing array unions to reduce write requests the membership for the current users is appended to, the group ID passed as a parameter is group which is appended to the requested users memberships; and also updates the local variable, even though this will be updated, it prohibits decisions being made based off un-synced data in the timeframe between change and snapshotcallback being made
     *
     * @param groupUID String the group id which is being appended to the requested users memberships
     */
    static public void appendRequestedMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() == null) {
            currentUser.setUsersRequestsMemberships(new ArrayList<>());
            currentUser.getUsersRequestsMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", FieldValue.arrayUnion(groupUID));
        } else {
            currentUser.getUsersRequestsMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", FieldValue.arrayUnion(groupUID));
        }
    }

    /**
     * utilizing array unions to reduce write requests the membership for the current user has group ID passed as a parameter removed from its requested memberships
     *
     * @param groupUID String the group id which is being removed to the requested users memberships
     * @return boolean representing if the transaction was successful or not.
     */
    static public boolean removeRequestedMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() != null) {
            boolean outcome = currentUser.getUsersMemberships().remove(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", FieldValue.arrayRemove(groupUID));
            return outcome;
        } else {
            return false;
        }
    }


    /**
     * sets the current listener which is notified whenever the user is updated
     *
     * @param inputListener ChangeListener which is triggered when there are changes to the current users document.
     */
    static public void setCurrentUserListener(ChangeListener inputListener) {
        listener = inputListener;
    }

    /**
     * change listener interface for deteching changes to the current user.
     */
    public interface ChangeListener {
        void onChange();
    }

}
