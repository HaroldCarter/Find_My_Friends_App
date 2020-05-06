package com.example.find_my_friends.groupUtil;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;

import uk.co.mgbramwell.geofire.android.GeoFire;

import static com.example.find_my_friends.userUtil.CurrentUserUtil.appendMembershipCurrentUser;
import static com.example.find_my_friends.userUtil.CurrentUserUtil.appendRequestedMembershipCurrentUser;

public class GroupUtil {


    //using a non static class for this method because there is an issue uploading groups to the firestore database
    public boolean uploadGroup(final Group groupToUpload, final FirebaseFirestore db){
        //do not allow a profile containing nulls to be uploaded, nulls are very bad practise
        if(groupToUpload.getMembersOfGroupIDS() == null || groupToUpload.getGroupCreatorUserID() == null || groupToUpload.getGroupPhotoURI() == null){
            return false;
        }else{
            //when pushing against the daily limit of 20k writes this fails without warning.
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



    //this should generate an array of keywords, which can then be appended to the database.
    static public void generateKeywords(Group group , String enteredText){
        group.setGroupTitleKeywords(new ArrayList<>());
        enteredText = enteredText.toLowerCase().trim();
        StringBuilder previousWord = new StringBuilder();
        StringBuilder currentWord = new StringBuilder();
        String[] splitStr = enteredText.split("\\s+");
        //now that the title has been split at the spaces, we need to incrementally build another array which builds partial completion of each word.
        ArrayList<String> accumulator = new ArrayList<>();
        for (String S: splitStr
        ) {
            accumulator.clear();
            currentWord.setLength(0);
            for (Character c: S.toCharArray()
            ) {
                if(accumulator.isEmpty()){
                    accumulator.add(c.toString());
                    currentWord.append(c);
                }else{
                    accumulator.add(accumulator.get(accumulator.size()-1) + c.toString());
                    currentWord.append(c);
                }
                if(previousWord.length() != 0) {
                    group.getGroupTitleKeywords().add(previousWord.toString() + " " + currentWord.toString());
                }
            }
            //gives the breakdown of the contents of a word
            group.getGroupTitleKeywords().addAll(accumulator);

            //gives the work itself with a space at the end
            group.getGroupTitleKeywords().add((S + " "));
            //if the word isn't the first word, it adds the current word to the sentence.
            if(previousWord.length() != 0){
                previousWord.append(" ");
                previousWord.append(S);
                //adds the full sentence up to this point with a space at the end.
                if(previousWord.length() != enteredText.length()) {
                    group.getGroupTitleKeywords().add(previousWord.toString());
                    group.getGroupTitleKeywords().add(previousWord.toString() + " ");
                };
            }else{
                previousWord.append(S);
            }
        }
        group.getGroupTitleKeywords().add(group.getGroupTitle());
    }




    //for appending users to a group.
    static public void appendMember(Group group, FirebaseUser user){
        if(group.getMembersOfGroupIDS() == null) {
            //if this is the first member of the group being added.
            group.setMembersOfGroupIDS( new ArrayList<>());
            group.getMembersOfGroupIDS().add(user.getUid());
            appendMembershipCurrentUser(group.getGroupID());
        }
        else{
            group.getMembersOfGroupIDS().add(user.getUid());
        }
    }

    static public void appendMember(Group group, String userUID){
        if(group.getMembersOfGroupIDS() == null) {
            //if this is the first member of the group being added.
            group.setMembersOfGroupIDS(new ArrayList<>());
            group.getMembersOfGroupIDS().add(userUID);
            appendMembershipCurrentUser(group.getGroupID());
        }
        else{
            group.getMembersOfGroupIDS().add(userUID);
            appendMembershipCurrentUser(group.getGroupID());
        }
    }

    static public void appendMemberRequest(Group group, String userUID){
        if(group.getRequestedMemberIDS() == null) {
            //if this is the first member of the group being added.
            group.setRequestedMemberIDS(new ArrayList<>());
            group.getRequestedMemberIDS().add(userUID);
            appendRequestedMembershipCurrentUser(group.getGroupID());
        }
        else{
            group.getRequestedMemberIDS().add(userUID);
            appendRequestedMembershipCurrentUser(group.getGroupID());
        }
    }

    static public void appendMemberRequestGroupOnly(Group group, String userUID){
        if(group.getRequestedMemberIDS() == null) {
            //if this is the first member of the group being added.
            group.setRequestedMemberIDS(new ArrayList<>());
            group.getRequestedMemberIDS().add(userUID);
        }
        else{
            group.getRequestedMemberIDS().add(userUID);
        }
    }


    static public void appendMemberGroupOnly(Group group, String userUID){
        if(group.getMembersOfGroupIDS() == null) {
            //if this is the first member of the group being added.
            group.setMembersOfGroupIDS(new ArrayList<>());
            group.getMembersOfGroupIDS().add(userUID);
        }
        else{
            group.getMembersOfGroupIDS().add(userUID);

        }
    }

    static public void appendMemberRequest(Group group, FirebaseUser user){
        if(group.getRequestedMemberIDS() == null) {
            //if this is the first member of the group being added create a new array
            group.setRequestedMemberIDS(new ArrayList<>());
            //
            group.getRequestedMemberIDS().add(user.getUid());
            appendRequestedMembershipCurrentUser(group.getGroupID());
        }
        else{
            group.getRequestedMemberIDS().add(user.getUid());
            appendRequestedMembershipCurrentUser(group.getGroupID());
        }
    }

    //for removing a user from a group
    static public boolean removeMember(Group group, FirebaseUser user){
        if(group.getMembersOfGroupIDS() != null){
            return group.getMembersOfGroupIDS().remove(user.getUid());
        }
        else{
            return false;
        }
    }


}
