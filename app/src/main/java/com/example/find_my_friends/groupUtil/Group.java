package com.example.find_my_friends.groupUtil;

import android.net.Uri;
import android.icu.util.Calendar;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class Group {
    private String TAG = "Group Class :";
    public String groupID;
    public String groupPhotoURI;
    public String groupTitle;
    public String groupDesc;
    public String groupMeetDate;
    public String groupMeetTime;
    public LatLng groupLocation;
    public String groupCreatorUserID;
    public List<String> membersOfGroupIDS;


    public Group(String groupID, Uri groupPhotoURI, String groupTitle, String groupDesc, String groupMeetDate, String groupMeetTime, LatLng groupLocation, String groupCreatorUser, ArrayList<String> membersOfGroupIDS) {
        this.groupID = groupID;
        this.groupPhotoURI = groupPhotoURI.toString();
        this.groupTitle = groupTitle;
        this.groupMeetDate = groupMeetDate;
        this.groupMeetTime = groupMeetTime;
        this.groupLocation = groupLocation;
        this.groupDesc = groupDesc;
        this.groupCreatorUserID = groupCreatorUser;
        this.membersOfGroupIDS = membersOfGroupIDS;
    }


    public Group(){
        this.groupID = UUID.randomUUID().toString();
        this.groupPhotoURI = null;
        this.groupTitle = "Title not set";
        this.groupDesc = "Description not set";
        this.groupLocation = new LatLng(0,0);
        this.groupCreatorUserID = null;
        this.membersOfGroupIDS = new ArrayList<String>();

        //do not call updategroup uploading nulls is not a good idea.
    }

    //for appending users to a group.
    public void appendMember(FirebaseUser user){
        if(this.membersOfGroupIDS == null) {
            //if this is the first member of the group being added.
            this.membersOfGroupIDS = new ArrayList<>();
            this.membersOfGroupIDS.add(user.getUid());
        }
        else{
            this.membersOfGroupIDS.add(user.getUid());
        }
    }


    //for removing a user from a group
    public boolean removeMember(FirebaseUser user){
        if(this.membersOfGroupIDS != null){
                return this.membersOfGroupIDS.remove(user.getUid());
        }
        else{
            return false;
        }
    }


    public boolean uploadGroup(FirebaseFirestore db){
        //do not allow a profile containing nulls to be uploaded, nulls are very bad practise
        if(this.membersOfGroupIDS == null || this.groupCreatorUserID == null || this.groupPhotoURI == null){
            return false;
        }else{
            //save the object to the database.

            db.collection("Groups").document(this.groupID)
                    .set(this, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "group uploaded to fireStore successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing group to the fireStore", e);
                        }
                    });
            return true;
        }


    }



}


