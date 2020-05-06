package com.example.find_my_friends.groupUtil;

import androidx.annotation.NonNull;

public class GroupInfoWindowData {
    private String groupID;
    private String groupPhotoURI;
    private String groupTitle;

    private String groupCreatorUserPhotoURL;
    private String groupCreatorDisplayName;


    public GroupInfoWindowData(String groupID, @NonNull String groupPhotoURI, @NonNull String groupTitle, @NonNull String groupCreatorUserPhotoURL, @NonNull String groupCreatorDisplayName) {
        this.groupID = groupID;
        this.groupPhotoURI = groupPhotoURI;
        this.groupTitle = groupTitle;
        this.groupCreatorUserPhotoURL = groupCreatorUserPhotoURL;
        this.groupCreatorDisplayName = groupCreatorDisplayName;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getGroupPhotoURI() {
        return groupPhotoURI;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public String getGroupCreatorUserPhotoURL() {
        return groupCreatorUserPhotoURL;
    }

    public String getGroupCreatorDisplayName() {
        return groupCreatorDisplayName;
    }
}
