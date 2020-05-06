package com.example.find_my_friends.groupUtil;

import com.example.find_my_friends.userUtil.User;
import com.example.find_my_friends.userUtil.UserMarker;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class GroupMarker {
    private Marker groupMarker;
    private Group groupMarkerRepresents;
    private ArrayList<UserMarker> users = new ArrayList<>();

    public GroupMarker(Marker groupMarker, Group groupMarkerRepresents) {
        this.groupMarker = groupMarker;
        this.groupMarkerRepresents = groupMarkerRepresents;
    }



    public Marker getGroupMarker() {
        return groupMarker;
    }

    public void setGroupMarker(Marker groupMarker) {
        this.groupMarker = groupMarker;
    }

    public Group getGroupMarkerRepresents() {
        return groupMarkerRepresents;
    }

    public void setGroupMarkerRepresents(Group groupMarkerRepresents) {
        this.groupMarkerRepresents = groupMarkerRepresents;
    }

    public ArrayList<UserMarker> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<UserMarker> users) {
        this.users = users;
    }

    public void appendUser(UserMarker userMarker) {
        this.users.add(userMarker);
    }

    public UserMarker getUser(UserMarker userMarker){
        return this.users.get(this.users.indexOf(userMarker));
    }

    public int getUserIndex(UserMarker userMarker){
        return this.users.indexOf(userMarker);
    }


    public void removeUser(UserMarker userMarker) {
        this.users.remove(userMarker);
    }

}
