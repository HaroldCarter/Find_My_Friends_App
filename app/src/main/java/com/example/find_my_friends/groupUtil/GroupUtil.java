package com.example.find_my_friends.groupUtil;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;

import uk.co.mgbramwell.geofire.android.GeoFire;

import static com.example.find_my_friends.userUtil.CurrentUserUtil.appendMembershipCurrentUser;
import static com.example.find_my_friends.userUtil.CurrentUserUtil.appendRequestedMembershipCurrentUser;
import static com.example.find_my_friends.util.LocationUtils.distanceBetweenTwoPointMiles;


/**
 * A class containing the set of utilities responsible for handling the functionality required to complete the feature list of the application, note this set of utils only applies groups and not to users.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class GroupUtil {

    /**
     * as the name indicates this util function takes an input group and uploads this to the groups collection on the database reference passed to this function.
     * upon success a log message will be printed stating so, if this fails a warning will be printed giving the error (most likely lack of permissions, this is called before authentication)
     * @param groupToUpload Group, the group that is being uploaded to the database
     * @param db FirebaseFirestore, reference to the database the file is being uploaded to
     * @return Boolean, for the validity of the transaction (if the transaction was possible to be submitted returns true, else false)
     */
    public boolean uploadGroup(final Group groupToUpload, final FirebaseFirestore db) {
        if (groupToUpload.getMembersOfGroupIDS() == null || groupToUpload.getGroupCreatorUserID() == null || groupToUpload.getGroupPhotoURI() == null) {
            return false;
        } else {
            db.collection("Groups").document(groupToUpload.getGroupID())
                    .set(groupToUpload, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            GeoFire geoFire = new GeoFire(db.collection("Groups"));
                            geoFire.setLocation(groupToUpload.getGroupID(), groupToUpload.getGroupLatitude(), groupToUpload.getGroupLongitude());
                            Log.d("Group Class :", "group uploaded to fireStore successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Group Class :", "Error writing group to the fireStore", e);
                        }
                    });
            return true;
        }
    }

    /**
     * this function takes the title for the group and will split the text at each space, then iterate through the overall string building an accumulate into an array of stages of the incomplete and complete words.
     * once complete this will be set to the give groups internal variable for the keywords.
     *
     * @param group group to generate keywords for
     * @param enteredText the title for the group that is being index and fragmented to generate the keywords.
     */
    static public void generateKeywords(Group group, String enteredText) {
        group.setGroupTitleKeywords(new ArrayList<>());
        enteredText = enteredText.toLowerCase().trim();
        StringBuilder previousWord = new StringBuilder();
        StringBuilder currentWord = new StringBuilder();
        String[] splitStr = enteredText.split("\\s+");
        ArrayList<String> accumulator = new ArrayList<>();
        for (String S : splitStr
        ) {
            accumulator.clear();
            currentWord.setLength(0);
            for (Character c : S.toCharArray()
            ) {
                if (accumulator.isEmpty()) {
                    accumulator.add(c.toString());
                    currentWord.append(c);
                } else {
                    accumulator.add(accumulator.get(accumulator.size() - 1) + c.toString());
                    currentWord.append(c);
                }
                if (previousWord.length() != 0) {
                    group.getGroupTitleKeywords().add(previousWord.toString() + " " + currentWord.toString());
                }
            }
            group.getGroupTitleKeywords().addAll(accumulator);
            group.getGroupTitleKeywords().add((S + " "));
            if (previousWord.length() != 0) {
                previousWord.append(" ");
                previousWord.append(S);
                if (previousWord.length() != enteredText.length()) {
                    group.getGroupTitleKeywords().add(previousWord.toString());
                    group.getGroupTitleKeywords().add(previousWord.toString() + " ");
                }
            } else {
                previousWord.append(S);
            }
        }
        group.getGroupTitleKeywords().add(group.getGroupTitle());
    }

    /**
     * appends the User provided as a parameter to the group's list of members which is also provided as a parameter
     * @param group Group to Append the member too
     * @param user User to append to the group
     */
    static public void appendMember(Group group, FirebaseUser user) {
        if (group.getMembersOfGroupIDS() == null) {
            group.setMembersOfGroupIDS(new ArrayList<>());
            group.getMembersOfGroupIDS().add(user.getUid());
            appendMembershipCurrentUser(group.getGroupID());
        } else {
            group.getMembersOfGroupIDS().add(user.getUid());
        }
    }

    /**
     * appends the User's UID String provided as a parameter to the group's list of members which is also provided as a parameter
     * @param group Group to Append the member too
     * @param userUID String for the user's UID (the user object isn't always known)
     */
    static public void appendMember(Group group, String userUID) {
        if (group.getMembersOfGroupIDS() == null) {
            group.setMembersOfGroupIDS(new ArrayList<>());
            group.getMembersOfGroupIDS().add(userUID);
            appendMembershipCurrentUser(group.getGroupID());
        } else {
            group.getMembersOfGroupIDS().add(userUID);
            appendMembershipCurrentUser(group.getGroupID());
        }
    }

    /**
     * appends the User's UID String provided as a parameter to the group's list of member requests which is also provided as a parameter
     *
     * @param group Group to Append the member request too
     * @param userUID String for the user's UID (the user object isn't always known)
     */
    static public void appendMemberRequest(Group group, String userUID) {
        if (group.getRequestedMemberIDS() == null) {
            group.setRequestedMemberIDS(new ArrayList<>());
            group.getRequestedMemberIDS().add(userUID);
            appendRequestedMembershipCurrentUser(group.getGroupID());
        } else {
            group.getRequestedMemberIDS().add(userUID);
            appendRequestedMembershipCurrentUser(group.getGroupID());
        }
    }


    /**
     * functionally the same as AppendMemberGroup() however this doesn't append the membership to the users account, but just appends the membership to groups document
     * @param group Group to Append the member request too
     * @param userUID String for the user's UID (the user object isn't always known)
     */
    static public void appendMemberGroupOnly(Group group, String userUID) {
        if (group.getMembersOfGroupIDS() == null) {
            //if this is the first member of the group being added.
            group.setMembersOfGroupIDS(new ArrayList<>());
            group.getMembersOfGroupIDS().add(userUID);
        } else {
            group.getMembersOfGroupIDS().add(userUID);

        }
    }

    /**
     * appends the User's UID String provided from the firebase user parameter to the group's list of member requests which is also provided as a parameter
     *
     * @param group Group to Append the member request too
     * @param user FirebaseUser (authentication Layer) from which the user's UID will be extracted and appended to the groups membership requests
     */
    static public void appendMemberRequest(Group group, FirebaseUser user) {
        if (group.getRequestedMemberIDS() == null) {
            group.setRequestedMemberIDS(new ArrayList<>());
            group.getRequestedMemberIDS().add(user.getUid());
            appendRequestedMembershipCurrentUser(group.getGroupID());
        } else {
            group.getRequestedMemberIDS().add(user.getUid());
            appendRequestedMembershipCurrentUser(group.getGroupID());
        }
    }

    /**
     * removes the User's UID String provided from the firebase user parameter to the group's list of members which is also provided as a parameter
     *
     * @param group Group to remove the member request too
     * @param user FirebaseUser (authentication Layer) from which the user's UID will be extracted and removed from the groups memberships
     * @return boolean representing the success of failure of the transaction true-success else-failure
     */
    static public boolean removeMember(Group group, FirebaseUser user) {
        if (group.getMembersOfGroupIDS() != null) {
            return group.getMembersOfGroupIDS().remove(user.getUid());
        } else {
            return false;
        }
    }

    /**
     *
     *this function checks if the User passed is a member of the group passed, if the user is a member of the aforementioned group the function will return true, else will return false.
     *
     * @param group Group to check the membership from
     * @param user User to check the membership for
     * @return boolean result of the membership of the user, true-user is a member of the group, else-not a member
     */
    static public boolean isUserAMember(Group group, User user) {
        if (group.getMembersOfGroupIDS() != null) {
            return (group.getMembersOfGroupIDS().indexOf(user.getUID()) != -1);
        } else {
            return false;
        }
    }

    /**
     *this function checks if the User passed is a completed member of the group passed, if the user is a completed member of the aforementioned group the function will return true, else will return false.
     *
     * @param group Group to check the list of complete members
     * @param user User to check the complete status for
     * @return boolean result of the complete status of the user, true-user is a complete member of the group, else-not a complete member
     */
    static public boolean isUserAlreadyCompleted(Group group, User user) {
        if (group.getCompletedMemberIDS() != null) {
            return (group.getCompletedMemberIDS().indexOf(user.getUID()) != -1);
        } else {
            return false;
        }
    }

    /**
     * this function checks if the user and the group are trigonometrical less than 1.0 mile apart, if so the user is close enough to register arrival and therefore can be moved to complete upon user request.
     * @param group Group to check distance to the user for
     * @param user User to check distance to the group for
     * @return Boolean, true-user is close enough to the group to complete, else-not close enough
     */
    static public boolean canUserComplete(Group group, User user) {
        float distance = distanceBetweenTwoPointMiles(group.getGroupLatitude(), group.getGroupLongitude(), user.getUserLat(), user.getUserLong());
        if (distance < 1.0) {
            return isUserAMember(group, user);
        } else {
            return false;
        }

    }

    /**
     *
     * appends a member to the completed member's list of the group passed as a parameter.
     * @param group group to append completed member to
     * @param user user to append to the completed member's list of the group
     * @return boolean representing the validity of the transaction, true-valid request sent, else false- request invalid
     */
    static public boolean appendCompletedMember(Group group, FirebaseUser user) {
        if (group.getCompletedMemberIDS() == null) {
            group.setCompletedMemberIDS(new ArrayList<>());
            group.getCompletedMemberIDS().add(user.getUid());
            return true;
        } else {
            if (group.getCompletedMemberIDS().indexOf(user.getUid()) != -1) {
                group.getCompletedMemberIDS().add(user.getUid());
                return true;
            } else {
                return false;
            }

        }
    }

    /**
     * remove a member to the completed member's list of the group passed as a parameter.
     * @param group group to remove completed member from
     * @param user user to remove from the completed member's list of the group
     * @return boolean representing the validity of the transaction, true-valid request sent, else false- request invalid
     */
    static public boolean removeCompletedMember(Group group, FirebaseUser user) {
        if (group.getCompletedMemberIDS() != null) {
            return group.getCompletedMemberIDS().remove(user.getUid());
        } else {
            return false;
        }
    }


}
