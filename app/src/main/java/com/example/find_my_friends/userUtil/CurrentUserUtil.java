package com.example.find_my_friends.userUtil;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;

public class CurrentUserUtil {
    static private CurrentUserUtil.ChangeListener listener;


    static public void notifyChangeListener(){
        if(listener != null) {
            listener.onChange();
        }
    }

    //each time this called it counts as two writes.
    static public void setLocationCurrentUserUser(LatLng latLng){
        currentUser.setUserLat(latLng.latitude);
        currentUser.setUserLong((latLng.longitude));
        currentUserDocument.getReference().update("userLat", currentUser.getUserLat());
        currentUserDocument.getReference().update("userLong", currentUser.getUserLong());
    }

    //each time this is called it counts as two writes, this is called every time the users location updates (every 10 seconds).
    static public void setLocationCurrentUser(double lat, double lng){
        currentUser.setUserLat(lat);
        currentUser.setUserLong((lng));
        currentUserDocument.getReference().update("userLat", currentUser.getUserLat());
        currentUserDocument.getReference().update("userLong", currentUser.getUserLong());
    }


    //counts as a single write, called upon user interaction.
    static public void setLocationUpToDateCurrentUser(Boolean userLocationUpToDate) {
        currentUser.setUserLocationUpToDate(userLocationUpToDate);
        currentUserDocument.getReference().update("userLocationUpToDate", currentUser.getUserLocationUpToDate());
    }

    //counts as a single write, called upon user interaction.
    static public void setModeOfTransportCurrentUser(String modeOfTransport){
        currentUser.setModeOfTransport(modeOfTransport);
        currentUserDocument.getReference().update("modeOfTransport", currentUser.getModeOfTransport());
    }


    //counts as a single write called upon user interaction.
    static public void appendMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() == null) {
            //update the clientside representation of the current user.
            currentUser.setUsersMemberships(new ArrayList<>());
            currentUser.getUsersMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersMemberships", FieldValue.arrayUnion(groupUID));
        } else {
            //update the clientside representation of the current user.
            currentUser.getUsersMemberships().add(groupUID);
            //push the update to the server in a wasteless way.
            currentUserDocument.getReference().update("usersMemberships", FieldValue.arrayUnion(groupUID));
        }
    }


    //counts as a single write interaction
    static public boolean removeMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() != null) {
            boolean outcome = currentUser.getUsersMemberships().remove(groupUID);
            currentUserDocument.getReference().update("usersMemberships", FieldValue.arrayRemove(groupUID));
            return outcome;

        } else {
            return false;
        }
    }

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

    static public boolean removeRequestedMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() != null) {
            boolean outcome = currentUser.getUsersMemberships().remove(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", FieldValue.arrayRemove(groupUID));
            return outcome;
        } else {
            return false;
        }
    }


    //only one listener is required as we do not want to update all pages at once, only the pages that are currently onscreen, which is limited to one at a time.
    static public ChangeListener getCurrentUserListener() {
        return listener;
    }

    static public void setCurrentUserListener(ChangeListener inputListener) {
        listener = inputListener;
    }

    static public interface ChangeListener {
        void onChange();
    }

}
