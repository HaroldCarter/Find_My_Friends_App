package com.example.find_my_friends.groupUtil;

import android.icu.util.Calendar;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class GroupList {
    private ArrayList<Group> listofGroups;
    private long numberOfGroups = 0;


    public GroupList(ArrayList<Group> listofGroups, long numberOfGroups) {
        this.listofGroups = listofGroups;
        this.numberOfGroups = numberOfGroups;
    }

    public GroupList(){
        //update from firebase, download and  restore the list and internal variables.
    }


    public Group findGroupByID(Long groupID){
        for (Group G: listofGroups) {
            if(G.groupID.equals(groupID)) {
                return G; //find and return the Group.
            }
        }
        return null;
    }

    public boolean updateGroup(Long groupID, Uri groupPhotoURI, String groupTitle, String groupDesc, Calendar groupCalendar, LatLng groupLocation, FirebaseUser groupCreatorUser){
        if(groupID != null){
            Group groupToUpdate = findGroupByID(groupID);
            if(groupToUpdate != null && groupToUpdate.groupCreatorUser.equals(groupCreatorUser)){
                //update the Group write the update feature. making sure that the updater is the same user as the one that created the Group
            }else{
                if (appendGroup(groupID, groupPhotoURI, groupTitle, groupDesc, groupCalendar, groupLocation, groupCreatorUser)){
                    return true;
                }else{
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public boolean appendGroup(Long groupID, Uri groupPhotoURI, String groupTitle, String groupDesc, Calendar groupCalendar, LatLng groupLocation, FirebaseUser groupCreatorUser){
        if(groupID != null && groupPhotoURI != null && groupTitle != null && groupDesc != null && groupCalendar != null && groupLocation != null && groupCreatorUser !=null){
            this.numberOfGroups++;
            this.listofGroups.add(new Group(groupID,groupPhotoURI,groupTitle,groupDesc,groupCalendar,groupLocation,groupCreatorUser));
            return true;
        }
        return false;
    }



}
