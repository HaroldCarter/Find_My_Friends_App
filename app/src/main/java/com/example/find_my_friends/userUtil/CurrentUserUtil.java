package com.example.find_my_friends.userUtil;

import com.google.android.gms.maps.model.LatLng;

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

    static public void setLocationCurrentUserUser(LatLng latLng){
        currentUser.setUserLat(latLng.latitude);
        currentUser.setUserLong((latLng.longitude));
        currentUserDocument.getReference().update("userLat", currentUser.getUserLat());
        currentUserDocument.getReference().update("userLong", currentUser.getUserLong());
    }

    static public void setLocationCurrentUser(double lat, double lng){
        currentUser.setUserLat(lat);
        currentUser.setUserLong((lng));
        currentUserDocument.getReference().update("userLat", currentUser.getUserLat());
        currentUserDocument.getReference().update("userLong", currentUser.getUserLong());
    }


    static public void setLocationUpToDateCurrentUser(Boolean userLocationUpToDate) {
        currentUser.setUserLocationUpToDate(userLocationUpToDate);
        currentUserDocument.getReference().update("UserLocationUpToDate", currentUser.getUserLocationUpToDate());
    }

    static public void setModeOfTransportCurrentUser(String modeOfTransport){
        currentUser.setModeOfTransport(modeOfTransport);
        currentUserDocument.getReference().update("modeOfTransport", currentUser.getModeOfTransport());
    }


    static public void appendMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() == null) {
            currentUser.setUsersMemberships(new ArrayList<>());
            currentUser.getUsersMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersMemberships", currentUser.getUsersMemberships());
        } else {
            currentUser.getUsersMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersMemberships", currentUser.getUsersMemberships());
        }
    }


    static public boolean removeMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() != null) {
            boolean outcome = currentUser.getUsersMemberships().remove(groupUID);
            currentUserDocument.getReference().update("usersMemberships", currentUser.getUsersMemberships());
            return outcome;

        } else {
            return false;
        }
    }

    static public void appendRequestedMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() == null) {
            currentUser.setUsersMemberships(new ArrayList<>());
            currentUser.getUsersMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", currentUser.getUsersMemberships());
        } else {
            currentUser.getUsersMemberships().add(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", currentUser.getUsersMemberships());
        }
    }

    static public boolean removeRequestedMembershipCurrentUser(String groupUID) {
        if (currentUser.getUsersMemberships() != null) {
            boolean outcome = currentUser.getUsersMemberships().remove(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", currentUser.getUsersMemberships());
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
