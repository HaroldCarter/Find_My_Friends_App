package com.example.find_my_friends.groupUtil;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.internal.InternalTokenProvider;

public class GroupInfoWindowData {
    private String groupID;
    private String groupPhotoURI;
    private String groupTitle;

    private LatLng userLocation;
    private String modeOfTransportUser;

    private String groupCreatorUID;
    private String groupCreatorUserPhotoURL;
    private String groupCreatorDisplayName;

    private Integer travelDuration = null;


    public GroupInfoWindowData(String groupID, LatLng userLocation , String modeOfTransportUser, @NonNull String groupTitle, @NonNull String groupCreatorUserPhotoURL, @NonNull String groupCreatorDisplayName, @NonNull String groupCreatorUID) {
        this.groupID = groupID;
        this.groupTitle = groupTitle;
        this.userLocation = userLocation;
        this.modeOfTransportUser = modeOfTransportUser;
        this.groupCreatorUserPhotoURL = groupCreatorUserPhotoURL;
        this.groupCreatorDisplayName = groupCreatorDisplayName;
        this.groupCreatorUID = groupCreatorUID;
    }


    public String getGroupCreatorUID() {
        return groupCreatorUID;
    }

    public String getModeOfTransportUser() {
        return modeOfTransportUser;
    }

    public Integer getTravelDuration() {
        return travelDuration;
    }

    public void setTravelDuration(Integer travelDuration) {
        this.travelDuration = travelDuration;
    }

    public LatLng getUserLocation() {
        return userLocation;
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
