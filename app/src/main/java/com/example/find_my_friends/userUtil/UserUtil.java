package com.example.find_my_friends.userUtil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;


public class UserUtil {

    static public LatLng getUserLocation(User user){
        return new LatLng(user.getUserLat(), user.getUserLong());
    }



    static public void composeEmail(Context context, String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }


    /*special functions for when the user is not known*/


    static public void appendMembership( String groupUID, DocumentSnapshot userSnap) {
        if(userSnap != null && userSnap.exists()){
            userSnap.getReference().update("usersMemberships", FieldValue.arrayUnion(groupUID));
        }
        /*
        originally how the function was working,  simplfied above/
        if(tempUser != null && tempUser.getUsersMemberships() != null) {
            tempUser.getUsersMemberships().add(groupUID);
            userSnap.getReference().update("usersMemberships", tempUser.getUsersMemberships());
        }if(tempUser != null ){
            tempUser.setUsersMemberships(new ArrayList<String>());
            tempUser.getUsersMemberships().add(groupUID);

        }

         */
    }


    static public void removeMembership( String groupUID, DocumentSnapshot userSnap) {
        if(userSnap != null && userSnap.exists()) {
            userSnap.getReference().update("usersMemberships", FieldValue.arrayRemove(groupUID));
        }
    }

    static public void removeMembershipRequest( String groupUID, DocumentSnapshot userSnap) {
        if(userSnap != null && userSnap.exists()) {
            userSnap.getReference().update("usersRequestsMemberships",FieldValue.arrayRemove(groupUID));
        }
    }
}
