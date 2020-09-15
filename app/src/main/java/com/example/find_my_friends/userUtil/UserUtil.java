package com.example.find_my_friends.userUtil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

/**
 * A class containing the set of utilities responsible for handling the functionality required to complete the feature list of the application, note this set of utils only applies other users, not the current user.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class UserUtil {

    /**
     * get the user's last known location
     *
     * @param user User, the user who's location is being requested
     * @return LatLng containing the users GPS coordinates
     */
    static public LatLng getUserLocation(User user) {
        return new LatLng(user.getUserLat(), user.getUserLong());
    }


    /**
     * compose an email to a specific target addresses through means of an implicit intent
     *
     * @param context   context of the activity/app calling this function so that the intent can be made
     * @param addresses list of string's representing the email addresses of the target mailboxes
     * @param subject   the subject of the message (not the message itself)
     */
    static public void composeEmail(Context context, String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * append a membership to the user
     *
     * @param groupUID group that is being appended to the user's current memberships
     * @param userSnap the snapshot of the user that is having the group appended to it's current membership's
     */
    static public void appendMembership(String groupUID, DocumentSnapshot userSnap) {
        if (userSnap != null && userSnap.exists()) {
            userSnap.getReference().update("usersMemberships", FieldValue.arrayUnion(groupUID));
        }
    }

    /**
     * remove a membership request -to the user
     *
     * @param groupUID group that is being remove to the user's requested memberships
     * @param userSnap the snapshot of the user that is having the group removed to it's requested membership's
     */
    static public void removeMembershipRequest(String groupUID, DocumentSnapshot userSnap) {
        if (userSnap != null && userSnap.exists()) {
            userSnap.getReference().update("usersRequestsMemberships", FieldValue.arrayRemove(groupUID));
        }
    }
}
