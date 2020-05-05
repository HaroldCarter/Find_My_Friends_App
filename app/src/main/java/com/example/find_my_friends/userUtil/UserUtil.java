package com.example.find_my_friends.userUtil;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class UserUtil {

    static public LatLng getUserLocation(User user){
        return new LatLng(user.getUserLat(), user.getUserLong());
    }



    /*special functions for when the user is not known*/


    static public void appendMembership( String groupUID, DocumentSnapshot userSnap) {
        User tempUser  = userSnap.toObject(User.class);
        if(tempUser != null && tempUser.getUsersMemberships() != null) {
            tempUser.getUsersMemberships().add(groupUID);
            userSnap.getReference().update("usersMemberships", tempUser.getUsersMemberships());
        }if(tempUser != null ){
            tempUser.setUsersMemberships(new ArrayList<String>());
            tempUser.getUsersMemberships().add(groupUID);
            userSnap.getReference().update("usersMemberships", tempUser.getUsersMemberships());
        }
    }


    //not tested, might not work.
    static public void removeMembership( String groupUID, DocumentSnapshot userSnap) {
        User tempUser  = userSnap.toObject(User.class);
        if(tempUser != null && tempUser.getUsersMemberships() != null) {
            tempUser.getUsersMemberships().remove(groupUID);
            userSnap.getReference().update("usersMemberships", tempUser.getUsersMemberships());
        }
    }

    static public void removeMembershipRequest( String groupUID, DocumentSnapshot userSnap) {
        User tempUser  = userSnap.toObject(User.class);
        if(tempUser != null && tempUser.getUsersRequestsMemberships() != null) {
            tempUser.getUsersRequestsMemberships().remove(groupUID);
            userSnap.getReference().update("usersRequestsMemberships", tempUser.getUsersRequestsMemberships());
        }
    }
}
