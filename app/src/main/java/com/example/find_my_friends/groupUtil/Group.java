package com.example.find_my_friends.groupUtil;

import android.net.Uri;
import android.icu.util.Calendar;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;


public class Group {
    public Long groupID;
    public Uri groupPhotoURI;
    public String groupTitle;
    public String groupDesc;
    public Calendar groupCalendar;//can get both the date and time.
    public LatLng groupLocation;
    public FirebaseUser groupCreatorUser;


    public Group(Long groupID, Uri groupPhotoURI, String groupTitle, String groupDesc, Calendar groupCalendar, LatLng groupLocation, FirebaseUser groupCreatorUser) {
        this.groupID = groupID;
        this.groupPhotoURI = groupPhotoURI;
        this.groupTitle = groupTitle;
        this.groupCalendar = groupCalendar;
        this.groupLocation = groupLocation;
        this.groupDesc = groupDesc;
        this.groupCreatorUser = groupCreatorUser;
    }


    public Group(){
        this.groupID = 0L;
        this.groupPhotoURI = null;
        this.groupTitle = "Title not set";
        this.groupDesc = "Description not set";
        this.groupCalendar = Calendar.getInstance();//set by default to the current date and time.
        this.groupLocation = new LatLng(0,0);
        this.groupCreatorUser = null;
        //do not call updategroup uploading nulls is not a good idea.
    }




}


