package com.example.find_my_friends.groupUtil;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;


public class Group {
    //private String TAG = "Group Class :";
    private String groupID;
    private String groupPhotoURI;
    private String groupTitle;
    private String groupDesc;
    private String groupMeetDate;
    private String groupMeetTime;
    //private GeoPoint groupLocation;
    private double groupLatitude;
    private double groupLongitude;
    private String groupCreatorUserID;

    private String groupCreatorUserPhotoURL;
    private String groupCreatorDisplayName;


    private ArrayList<String> membersOfGroupIDS;


    public Group(String groupID, String groupPhotoURI, String groupTitle, String groupDesc, String groupMeetDate, String groupMeetTime, double groupLatitude, double groupLongitude, String groupCreatorUserID, String groupCreatorUserPhotoURL, String groupCreatorDisplayName, ArrayList<String> membersOfGroupIDS) {
        this.groupID = groupID;
        this.groupPhotoURI = groupPhotoURI;
        this.groupTitle = groupTitle;
        this.groupDesc = groupDesc;
        this.groupMeetDate = groupMeetDate;
        this.groupMeetTime = groupMeetTime;
        this.groupLatitude = groupLatitude;
        this.groupLongitude = groupLongitude;
        this.groupCreatorUserID = groupCreatorUserID;
        this.groupCreatorUserPhotoURL = groupCreatorUserPhotoURL;
        this.groupCreatorDisplayName = groupCreatorDisplayName;
        this.membersOfGroupIDS = membersOfGroupIDS;
    }

    public Group(){
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

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupPhotoURI() {
        return groupPhotoURI;
    }

    public void setGroupPhotoURI(String groupPhotoURI) {
        this.groupPhotoURI = groupPhotoURI;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    public String getGroupMeetDate() {
        return groupMeetDate;
    }

    public void setGroupMeetDate(String groupMeetDate) {
        this.groupMeetDate = groupMeetDate;
    }

    public String getGroupMeetTime() {
        return groupMeetTime;
    }

    public void setGroupMeetTime(String groupMeetTime) {
        this.groupMeetTime = groupMeetTime;
    }

    public double getGroupLatitude() {
        return groupLatitude;
    }

    public void setGroupLatitude(double groupLatitude) {
        this.groupLatitude = groupLatitude;
    }

    public double getGroupLongitude() {
        return groupLongitude;
    }

    public void setGroupLongitude(double groupLongitude) {
        this.groupLongitude = groupLongitude;
    }

    public String getGroupCreatorUserID() {
        return groupCreatorUserID;
    }

    public void setGroupCreatorUserID(String groupCreatorUserID) {
        this.groupCreatorUserID = groupCreatorUserID;
    }

    public String getGroupCreatorUserPhotoURL() {
        return groupCreatorUserPhotoURL;
    }

    public void setGroupCreatorUserPhotoURL(String groupCreatorUserPhotoURL) {
        this.groupCreatorUserPhotoURL = groupCreatorUserPhotoURL;
    }

    public String getGroupCreatorDisplayName() {
        return groupCreatorDisplayName;
    }

    public void setGroupCreatorDisplayName(String groupCreatorDisplayName) {
        this.groupCreatorDisplayName = groupCreatorDisplayName;
    }

    public ArrayList<String> getMembersOfGroupIDS() {
        return membersOfGroupIDS;
    }

    public void setMembersOfGroupIDS(ArrayList<String> membersOfGroupIDS) {
        this.membersOfGroupIDS = membersOfGroupIDS;
    }
}


