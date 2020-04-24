package com.example.find_my_friends.userUtil;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.Constants.currentUserDocument;

public class User {

    private String UID;
    private String UserEmailAddress;
    private String UserPhotoURL;
    private String Username;
   // private String UserLat;
   // private String UserLong;



    //should be used so a user can see their out going requests and memberships.
    private ArrayList<String> usersMemberships;
    private ArrayList<String> usersRequestsMemberships;
    private String modeOfTransport = "Person";
    //should store the mode of transport here, so that we know what time estimates to give.

    public User() {
    }
/*
    public void setLocationCurrentUser(LatLng latLng){
        currentUser.setUserLat(Double.toString(latLng.latitude));
        currentUser.setUserLong(Double.toString((latLng.longitude)));
        currentUserDocument.getReference().update("UserLat", currentUser.UserLat);
        currentUserDocument.getReference().update("UserLong", currentUser.UserLat);
    }

    public String getUserLat() {
        return UserLat;
    }

    public void setUserLat(String userLat) {
        UserLat = userLat;
    }

    public String getUserLong() {
        return UserLong;
    }

    public void setUserLong(String userLong) {
        UserLong = userLong;
    }

 */

    public ArrayList<String> getUsersMemberships() {
        return usersMemberships;
    }

    public void setUsersMemberships(ArrayList<String> usersMemberships) {
        this.usersMemberships = usersMemberships;
    }

    public ArrayList<String> getUsersRequestsMemberships() {
        return usersRequestsMemberships;
    }

    public void setUsersRequestsMemberships(ArrayList<String> usersRequestsMemberships) {
        this.usersRequestsMemberships = usersRequestsMemberships;
    }

    public String getModeOfTransport() {
        return modeOfTransport;
    }

    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }


    //when referring to the user, we are always referring to the current user in util's however this class is also used for loading user's tberefore cannot be static itself.


    public void appendMembership(String groupUID) {
        if (currentUser.usersMemberships == null) {
            currentUser.usersMemberships = new ArrayList<>();
            currentUser.usersMemberships.add(groupUID);
            currentUserDocument.getReference().update("usersMemberships", currentUser.usersMemberships);
        } else {
            currentUser.usersMemberships.add(groupUID);
            currentUserDocument.getReference().update("usersMemberships", currentUser.usersMemberships);
        }
    }


    public boolean removeMembership(String groupUID) {
        if (currentUser.usersMemberships != null) {
            boolean outcome = currentUser.usersMemberships.remove(groupUID);
            currentUserDocument.getReference().update("usersMemberships", currentUser.usersMemberships);
            return outcome;

        } else {
            return false;
        }
    }

    public void appendRequestedMembership(String groupUID) {
        if (currentUser.usersRequestsMemberships == null) {
            currentUser.usersRequestsMemberships = new ArrayList<>();
            currentUser.usersRequestsMemberships.add(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", currentUser.usersRequestsMemberships);
        } else {
            currentUser.usersRequestsMemberships.add(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", currentUser.usersRequestsMemberships);
        }
    }

    public boolean removeRequestedMembership(String groupUID) {
        if (currentUser.usersRequestsMemberships != null) {
            boolean outcome = currentUser.usersRequestsMemberships.remove(groupUID);
            currentUserDocument.getReference().update("usersRequestsMemberships", currentUser.usersRequestsMemberships);
            return outcome;
        } else {
            return false;
        }
    }

    public User(String UID, String userEmailAddress, String userPhotoURL, String username) {
        this.UID = UID;
        this.UserEmailAddress = userEmailAddress;
        this.UserPhotoURL = userPhotoURL;
        this.Username = username;

    }



    /*special functions for when the user is not known*/


    public void appendMembership( String groupUID, DocumentSnapshot userSnap) {
        User tempUser  = userSnap.toObject(User.class);
        if(tempUser != null && tempUser.getUsersMemberships() != null) {
            tempUser.getUsersMemberships().add(groupUID);
            userSnap.getReference().update("usersMemberships", tempUser.usersMemberships);
        }if(tempUser != null ){
            tempUser.setUsersMemberships(new ArrayList<String>());
            tempUser.getUsersMemberships().add(groupUID);
            userSnap.getReference().update("usersMemberships", tempUser.usersMemberships);
        }

        /*
        db.collection("Users").document(userUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult() != null) {
                    User userSnap = task.getResult().toObject(User.class);
                    if(userSnap != null && userSnap.getUsersMemberships() != null) {
                        userSnap.getUsersMemberships().remove(groupUID);
                        currentUserDocument.getReference().update("usersMemberships", userSnap.usersMemberships);
                    }
                }
            }
        });

         */
    }


    //not tested, might not work.
    public void removeMembership( String groupUID, DocumentSnapshot userSnap) {
        User tempUser  = userSnap.toObject(User.class);
        if(tempUser != null && tempUser.getUsersMemberships() != null) {
            tempUser.getUsersMemberships().remove(groupUID);
            userSnap.getReference().update("usersMemberships", tempUser.usersMemberships);
        }

        /*
        db.collection("Users").document(userUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult() != null) {
                    User userSnap = task.getResult().toObject(User.class);
                    if(userSnap != null && userSnap.getUsersMemberships() != null) {
                        userSnap.getUsersMemberships().remove(groupUID);
                        currentUserDocument.getReference().update("usersMemberships", userSnap.usersMemberships);
                    }
                }
            }
        });

         */
    }

    public void removeMembershipRequest( String groupUID, DocumentSnapshot userSnap) {
        User tempUser  = userSnap.toObject(User.class);
        if(tempUser != null && tempUser.getUsersRequestsMemberships() != null) {
            tempUser.getUsersRequestsMemberships().remove(groupUID);
            userSnap.getReference().update("usersRequestsMemberships", tempUser.getUsersRequestsMemberships());
        }
        /*
        db.collection("Users").document(userUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult() != null) {
                    User userSnap = task.getResult().toObject(User.class);
                    if(userSnap != null && userSnap.getUsersMemberships() != null) {
                        userSnap.getUsersRequestsMemberships().remove(groupUID);
                        currentUserDocument.getReference().update("usersRequestsMemberships", userSnap.usersRequestsMemberships);
                    }
                }
            }
        });

         */
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUserEmailAddress() {
        return UserEmailAddress;
    }

    public void setUserEmailAddress(String userEmailAddress) {
        UserEmailAddress = userEmailAddress;
    }

    public String getUserPhotoURL() {
        return UserPhotoURL;
    }

    public void setUserPhotoURL(String userPhotoURL) {
        UserPhotoURL = userPhotoURL;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}
