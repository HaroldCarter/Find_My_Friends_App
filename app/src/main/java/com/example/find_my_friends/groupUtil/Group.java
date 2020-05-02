package com.example.find_my_friends.groupUtil;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;



import java.util.ArrayList;

import ch.hsr.geohash.GeoHash;
import uk.co.mgbramwell.geofire.android.GeoFire;

import static com.example.find_my_friends.util.Constants.currentUser;


public class Group {
    //private String TAG = "Group Class :";

    //groups need to store an array containing keyword matches of the title so that we can achieve pattern matching.
    private  ArrayList<String> groupTitleKeywords;
    private ArrayList<String> groupGeoHashResolutions;

    private String groupID;
    private String groupPhotoURI;
    private String groupTitle;
    private String groupDesc;
    private String groupMeetDate;
    private String groupMeetTime;

    private double groupLatitude;
    private double groupLongitude;
    private String groupCreatorUserID;

    private String groupCreatorUserPhotoURL;
    private String groupCreatorDisplayName;


    private ArrayList<String> membersOfGroupIDS;
    private ArrayList<String> requestedMemberIDS;


    public Group(ArrayList<String> groupTitleKeywords, ArrayList<String> groupGeoHashResolutions, String groupID, String groupPhotoURI, String groupTitle, String groupDesc, String groupMeetDate, String groupMeetTime, double groupLatitude, double groupLongitude, String groupCreatorUserID, String groupCreatorUserPhotoURL, String groupCreatorDisplayName, ArrayList<String> membersOfGroupIDS, ArrayList<String> requestedMemberIDS) {
        this.groupTitleKeywords = groupTitleKeywords;
        this.groupGeoHashResolutions = groupGeoHashResolutions;
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
        this.requestedMemberIDS = requestedMemberIDS;
    }

    public Group(){
    }


    public void generateGeoHash(){
        int i = 1;
        this.groupGeoHashResolutions = new ArrayList<>();
        while(i <= 12){
            this.groupGeoHashResolutions.add(GeoHash.withCharacterPrecision(this.groupLatitude,this.groupLongitude, i).toBase32());
            i++;
        }
    }

    public ArrayList<String> getGroupGeoHashResolutions() {
        return groupGeoHashResolutions;
    }

    public void setGroupGeoHashResolutions(ArrayList<String> groupGeoHashResolutions) {
        this.groupGeoHashResolutions = groupGeoHashResolutions;
    }


    public ArrayList<String> getGroupTitleKeywords() {
        return groupTitleKeywords;
    }

    public void setGroupTitleKeywords(ArrayList<String> groupTitleKeywords) {
        this.groupTitleKeywords = groupTitleKeywords;
    }

    //this should generate an array of keywords, which can then be appended to the database.
    public void generateKeywords(String enteredText){
        this.groupTitleKeywords = new ArrayList<>();
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
                    this.groupTitleKeywords.add(previousWord.toString() + " " + currentWord.toString());
                }
            }
            //gives the breakdown of the contents of a word
            this.groupTitleKeywords.addAll(accumulator);

            //gives the work itself with a space at the end
            this.groupTitleKeywords.add((S + " "));
            //if the word isn't the first word, it adds the current word to the sentence.
            if(previousWord.length() != 0){
                previousWord.append(" ");
                previousWord.append(S);

                //adds the full sentence up to this point with a space at the end.
                if(previousWord.length() != enteredText.length()) {
                    this.groupTitleKeywords.add(previousWord.toString());
                    this.groupTitleKeywords.add(previousWord.toString() + " ");
                };
            }else{
                previousWord.append(S);
            }


        }
        this.groupTitleKeywords.add(groupTitle);
    }


    //for appending users to a group.
    public void appendMember(FirebaseUser user){
        if(this.membersOfGroupIDS == null) {
            //if this is the first member of the group being added.
            this.membersOfGroupIDS = new ArrayList<>();
            this.membersOfGroupIDS.add(user.getUid());
            currentUser.appendMembership(this.groupID);
        }
        else{
            this.membersOfGroupIDS.add(user.getUid());
        }
    }

    public void appendMember(String userUID){
        if(this.membersOfGroupIDS == null) {
            //if this is the first member of the group being added.
            this.membersOfGroupIDS = new ArrayList<>();
            this.membersOfGroupIDS.add(userUID);
            currentUser.appendMembership(this.groupID);
        }
        else{
            this.membersOfGroupIDS.add(userUID);
            currentUser.appendMembership(this.groupID);
        }
    }

    public void appendMemberGroupOnly(String userUID){
        if(this.membersOfGroupIDS == null) {
            //if this is the first member of the group being added.
            this.membersOfGroupIDS = new ArrayList<>();
            this.membersOfGroupIDS.add(userUID);

        }
        else{
            this.membersOfGroupIDS.add(userUID);

        }
    }

    public void appendMemberRequest(FirebaseUser user){
        if(this.requestedMemberIDS == null) {
            //if this is the first member of the group being added.
            this.requestedMemberIDS = new ArrayList<>();
            this.requestedMemberIDS.add(user.getUid());
            currentUser.appendRequestedMembership(this.groupID);
        }
        else{
            this.requestedMemberIDS.add(user.getUid());
            currentUser.appendRequestedMembership(this.groupID);
        }
    }

    public void appendMemberRequest(String userUID){
        if(this.requestedMemberIDS == null) {
            //if this is the first member of the group being added.
            this.requestedMemberIDS = new ArrayList<>();
            this.requestedMemberIDS.add(userUID);
            currentUser.appendRequestedMembership(this.groupID);
        }
        else{
            this.requestedMemberIDS.add(userUID);
            currentUser.appendRequestedMembership(this.groupID);
        }
    }

    public void appendMemberRequestGroupOnly(String userUID){
        if(this.requestedMemberIDS == null) {
            //if this is the first member of the group being added.
            this.requestedMemberIDS = new ArrayList<>();
            this.requestedMemberIDS.add(userUID);

        }
        else{
            this.requestedMemberIDS.add(userUID);

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


    public boolean uploadGroup(final FirebaseFirestore db){
        //do not allow a profile containing nulls to be uploaded, nulls are very bad practise
        final Group group = this;
        if(this.membersOfGroupIDS == null || this.groupCreatorUserID == null || this.groupPhotoURI == null){
            return false;
        }else{
            //save the object to the database.

            db.collection("Groups").document(this.groupID)
                    .set(this, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //GeoFirestore geoFirestore = new GeoFirestore( db.collection("Groups"));
                            //geoFirestore.setLocation(group.groupID, new GeoPoint(group.getGroupLatitude(), group.getGroupLongitude()));

                            //testing geofire gps search
                            GeoFire geoFire = new GeoFire(db.collection("Groups"));
                            geoFire.setLocation(group.getGroupID(), group.getGroupLatitude(), group.getGroupLongitude());


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

    public ArrayList<String> getRequestedMemberIDS() {
        return requestedMemberIDS;
    }

    public void setRequestedMemberIDS(ArrayList<String> requestedMemberIDS) {
        this.requestedMemberIDS = requestedMemberIDS;
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


