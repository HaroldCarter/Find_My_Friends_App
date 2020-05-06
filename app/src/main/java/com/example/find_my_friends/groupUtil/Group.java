package com.example.find_my_friends.groupUtil;
import java.util.ArrayList;



public class Group {
    //private String TAG = "Group Class :";

    //groups need to store an array containing keyword matches of the title so that we can achieve pattern matching.
    private  ArrayList<String> groupTitleKeywords;

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


    public Group(ArrayList<String> groupTitleKeywords, String groupID, String groupPhotoURI, String groupTitle, String groupDesc, String groupMeetDate, String groupMeetTime, double groupLatitude, double groupLongitude, String groupCreatorUserID, String groupCreatorUserPhotoURL, String groupCreatorDisplayName, ArrayList<String> membersOfGroupIDS, ArrayList<String> requestedMemberIDS) {
        this.groupTitleKeywords = groupTitleKeywords;
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

    public ArrayList<String> getGroupTitleKeywords() {
        return groupTitleKeywords;
    }

    public void setGroupTitleKeywords(ArrayList<String> groupTitleKeywords) {
        this.groupTitleKeywords = groupTitleKeywords;
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


