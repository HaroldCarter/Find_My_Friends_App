package com.example.find_my_friends.groupUtil;

import java.util.ArrayList;


/**
 *  A class representative of the groups's of the application, this class stores relevant material in order to provide a platform from which the app can use to store groups, information such as UID, creator UID, title, description and location are all stored locally as an object.
 *
 *  this object is also uploaded to the Firestore Database so that cross-platform consistency is maintained
 *
 * @author Harold Carter
 * @version V6.0
 */
public class Group {
    private ArrayList<String> groupTitleKeywords;

    private String groupID;
    private String groupPhotoURI;
    private String groupTitle;
    private String groupDesc;
    private String groupMeetDate;
    private String groupMeetTime;

    private double groupLatitude;
    private double groupLongitude;
    private String groupCreatorUserID;

    private String groupColor = GroupColors.randomColor().getStringValue();

    private String groupCreatorUserPhotoURL;
    private String groupCreatorDisplayName;
    private String groupCreatorEmail;

    private boolean groupCompletionStatus = false;
    private ArrayList<String> completedMemberIDS;


    private ArrayList<String> membersOfGroupIDS;
    private ArrayList<String> requestedMemberIDS;

    /** default constructor the group takes all the internal variables as parameters and sets aforementioned parameters as the internal variable values
     * @param groupTitleKeywords String ArrayList containing a fragmented collection of the groupTitlesKeywords
     * @param groupID String UUID for the groups unique id
     * @param groupPhotoURI URI representing the groups photo's download url
     * @param groupTitle String for the groups title
     * @param groupDesc String containing the groups description
     * @param groupMeetDate String representing the date the group meeting is arranged for
     * @param groupMeetTime String representing the time the group meeting is arranged for
     * @param groupLatitude Double representation for the users latitude
     * @param groupLongitude double representation for the users longitude
     * @param groupCreatorUserID String for the group creators UUID
     * @param groupCreatorEmail String for the groups creators email address
     * @param groupCreatorUserPhotoURL String representing the group creators profile photo download url
     * @param groupCreatorDisplayName String representing the group creator's username (display name)
     * @param membersOfGroupIDS StringArrayList containing the groups current members, max size of 10
     * @param requestedMemberIDS StringArrayList containing the groups curreent requested memberships
     */
    public Group(ArrayList<String> groupTitleKeywords, String groupID, String groupPhotoURI, String groupTitle, String groupDesc, String groupMeetDate, String groupMeetTime, double groupLatitude, double groupLongitude, String groupCreatorUserID, String groupCreatorEmail, String groupCreatorUserPhotoURL, String groupCreatorDisplayName, ArrayList<String> membersOfGroupIDS, ArrayList<String> requestedMemberIDS) {
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
        this.groupCreatorEmail = groupCreatorEmail;
        this.groupCreatorUserPhotoURL = groupCreatorUserPhotoURL;
        this.groupCreatorDisplayName = groupCreatorDisplayName;
        this.membersOfGroupIDS = membersOfGroupIDS;
        this.requestedMemberIDS = requestedMemberIDS;
    }

    /**
     * overloaded blank constructor for the group
     */
    public Group() {
    }


    /**
     * gets the completed members of a group,  returns the arrayList containing each members UUID (the members that have arrived)
     *
     * @return ArrayList String of the group members UUID's
     */
    public ArrayList<String> getCompletedMemberIDS() {
        return completedMemberIDS;
    }

    /**
     * sets the completed members of a group,  passing the parameter completedmembers as an arrayList containing each members UUID (the members that have arrived)
     *
     * @param completedMemberIDS ArrayList String of the group members UUID's
     */
    public void setCompletedMemberIDS(ArrayList<String> completedMemberIDS) {
        this.completedMemberIDS = completedMemberIDS;
    }

    /**
     * get the group creator email (for display purposes when the creators account is not required to be indexed)(initial loading)
     *
     * @return String representing the groups creator's email address
     */
    public String getGroupCreatorEmail() {
        return groupCreatorEmail;
    }

    /**
     * set the group's variable for the group creators email
     *
     * @param groupCreatorEmail String representing the groups creator's email address
     */
    public void setGroupCreatorEmail(String groupCreatorEmail) {
        this.groupCreatorEmail = groupCreatorEmail;
    }

    /**
     * yet to be implemented in the app, however upon all member being complete, this should return true, else return false
     *
     * @return boolean value representing if the group meeting is concluded
     */
    public boolean isGroupCompletionStatus() {
        return groupCompletionStatus;
    }

    /**
     * set the completion status of a group, takes a boolean that represents if the group meeting is concluded ( if all members have arrived)
     *
     * @param groupCompletionStatus Boolean representing the completion status (true - complete, false- incomplete)
     */
    public void setGroupCompletionStatus(boolean groupCompletionStatus) {
        this.groupCompletionStatus = groupCompletionStatus;
    }


    /**
     * get the group's color, this can return null if the color was never set
     *
     * @return String representing a value from the group color Enum
     */
    public String getGroupColor() {
        return groupColor;
    }

    /**
     * set the group's color to the input color
     *
     * @param groupColor String representing a value from the group color Enum
     */
    public void setGroupColor(String groupColor) {
        this.groupColor = groupColor;
    }

    /**
     * get the array list of strings that contain partial and complete fragments of the group title, this is used for voice and text search
     *
     * @return String ArrayList containing fragments of words contained in the group title.
     */
    public ArrayList<String> getGroupTitleKeywords() {
        return groupTitleKeywords;
    }

    /**
     * set the group's array list for incomplete or fragmented words for the groups title.
     *
     * @param groupTitleKeywords  String ArrayList containing fragments of words contained in the group title.
     */
    public void setGroupTitleKeywords(ArrayList<String> groupTitleKeywords) {
        this.groupTitleKeywords = groupTitleKeywords;
    }

    /**
     * get the group UUID so that the group can be uniquely identified
     *
     * @return String UUID for the groups unique ID
     */
    public String getGroupID() {
        return groupID;
    }

    /**
     *  set the group UUID so that the group can be uniquely identified
     *
     * @param groupID String UUID for the groups unique ID
     */
    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    /**
     * gets the String arraylist that contains the users UID's that have requested to join this group, each UID corresponds to a user document on the database.
     *
     * @return String ArrayList of requested members
     */
    public ArrayList<String> getRequestedMemberIDS() {
        return requestedMemberIDS;
    }

    /**
     * set the String arraylist that contains the users UID's that have requested to join this group, each UID should correspond to a user document on the database.
     *
     * @param requestedMemberIDS  String ArrayList of requested members
     */
    public void setRequestedMemberIDS(ArrayList<String> requestedMemberIDS) {
        this.requestedMemberIDS = requestedMemberIDS;
    }


    /**
     * get the String representing the groups photo's download URI
     *
     * @return String containing the URI for the download URL for the group photo
     */
    public String getGroupPhotoURI() {
        return groupPhotoURI;
    }

    /**
     * set the string representing the groups photo's download URI
     *
     * @param groupPhotoURI String containing the URI for the download URL for the group photo
     */
    public void setGroupPhotoURI(String groupPhotoURI) {
        this.groupPhotoURI = groupPhotoURI;
    }

    /**
     * get the groups title's
     *
     * @return String for the groups title
     */
    public String getGroupTitle() {
        return groupTitle;
    }

    /**
     * set the groups title's
     *
     * @param groupTitle String for the groups title
     */
    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    /**
     * get the string representing the description of the group
     *
     * @return string representing the group's description
     */
    public String getGroupDesc() {
        return groupDesc;
    }

    /**
     * set the string representing the description of the group
     *
     * @param groupDesc string representing the group's description
     */
    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    /**
     * get the string that represents the date the group meeting will occur
     *
     * @return string that represents the date of group meeting
     */
    public String getGroupMeetDate() {
        return groupMeetDate;
    }

    /**
     * set the string that represents the date the group meeting will occur
     *
     * @param groupMeetDate string that represents the date of group meeting
     */
    public void setGroupMeetDate(String groupMeetDate) {
        this.groupMeetDate = groupMeetDate;
    }

    /**
     *  get the string that represents the time the group meeting will occur
     *
     * @return string that represents the time of group meeting
     */
    public String getGroupMeetTime() {
        return groupMeetTime;
    }

    /**
     *  set the string that represents the time the group meeting will occur
     *
     * @param groupMeetTime  string that represents the time of group meeting
     */
    public void setGroupMeetTime(String groupMeetTime) {
        this.groupMeetTime = groupMeetTime;
    }

    /**
     * get the groups last update latitude
     *
     * @return double for the groups latitude (GPS coordinate)
     */
    public double getGroupLatitude() {
        return groupLatitude;
    }

    /**
     * set the groups latitude
     *
     * @param groupLatitude double for the groups latitude (GPS coordinate)
     */
    public void setGroupLatitude(double groupLatitude) {
        this.groupLatitude = groupLatitude;
    }

    /**
     * get the groups last update longitude
     *
     * @return double for the groups longitude (GPS coordinate)
     */
    public double getGroupLongitude() {
        return groupLongitude;
    }

    /**
     * set the groups longitude
     *
     * @param groupLongitude double for the groups longitude (GPS coordinate)
     */
    public void setGroupLongitude(double groupLongitude) {
        this.groupLongitude = groupLongitude;
    }

    /**
     * get the group's creators UID for use when looking up the creators user document
     *
     * @return String containing the group creators UID
     */
    public String getGroupCreatorUserID() {
        return groupCreatorUserID;
    }

    /**
     *  set the group's creators UID
     * @param groupCreatorUserID String containing the group creators UID
     */
    public void setGroupCreatorUserID(String groupCreatorUserID) {
        this.groupCreatorUserID = groupCreatorUserID;
    }

    /**
     * get the group creators photo URL as of the time of creation of the group (for future use (when users are deleted or initial loading))
     *
     * @return String containing the download url for the group creators profile photo
     */
    public String getGroupCreatorUserPhotoURL() {
        return groupCreatorUserPhotoURL;
    }

    /**
     * set the group creators photo URL
     *
     * @param groupCreatorUserPhotoURL String containing the download url for the group creators profile photo
     */
    public void setGroupCreatorUserPhotoURL(String groupCreatorUserPhotoURL) {
        this.groupCreatorUserPhotoURL = groupCreatorUserPhotoURL;
    }

    /**
     * get the groups creator's username / display name as of the time of creation of the group (for use when users are deleted or future use)
     *
     * @return String containing the group creators username
     */
    public String getGroupCreatorDisplayName() {
        return groupCreatorDisplayName;
    }

    /**
     * set the groups creator's username
     *
     * @param groupCreatorDisplayName String containing the group creators username
     */
    public void setGroupCreatorDisplayName(String groupCreatorDisplayName) {
        this.groupCreatorDisplayName = groupCreatorDisplayName;
    }

    /**
     * get's the arraylist of the current members of a group. contains each members UID as a string to be  used for scanning for the users document
     *
     * @return ArrayList String containing the members UID's as string resources
     */
    public ArrayList<String> getMembersOfGroupIDS() {
        return membersOfGroupIDS;
    }

    /**
     * sets the arraylist of the current members of a group
     *
     * @param membersOfGroupIDS ArrayList String containing the members UID's as string resources
     */
    public void setMembersOfGroupIDS(ArrayList<String> membersOfGroupIDS) {
        this.membersOfGroupIDS = membersOfGroupIDS;
    }
}


