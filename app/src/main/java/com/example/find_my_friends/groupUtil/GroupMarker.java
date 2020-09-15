package com.example.find_my_friends.groupUtil;

import com.example.find_my_friends.userUtil.UserMarker;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * A classification for a group marker which contains the marker that is being displayed on the map and the associated group the marker is representing, this is done to simply the process of deducing which maker has been interacted with later on.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class GroupMarker {
    private Marker groupMarker;
    private Group groupMarkerRepresents;
    private ArrayList<UserMarker> users = new ArrayList<>();

    /**
     * default constructor for the group Marker
     *
     * @param groupMarker           the marker on the google map which this GroupMakrker is representing
     * @param groupMarkerRepresents the Groups's information being displayed on the groupmarker
     */
    public GroupMarker(Marker groupMarker, Group groupMarkerRepresents) {
        this.groupMarker = groupMarker;
        this.groupMarkerRepresents = groupMarkerRepresents;
    }

    /**
     * gets the marker representing the group
     *
     * @return the googlemap marker representing the user
     */
    public Marker getGroupMarker() {
        return groupMarker;
    }

    /**
     * get the group that the current marker is representing / displaying information about
     *
     * @return Group the group the marker is representing
     */
    public Group getGroupMarkerRepresents() {
        return groupMarkerRepresents;
    }

    /**
     * a function for getting a specific user by index from the members of the group and thus groupmarker.
     *
     * @param index int index of user requested to be returned
     * @return User the user found at the index, null if nothing found.
     */
    public UserMarker getUser(Integer index) {
        return this.users.get(index);
    }

    /**
     * get the current list of members
     *
     * @return ArrayList<UserMarker> current list of group members
     */
    public ArrayList<UserMarker> getUsers() {
        return users;
    }

    /**
     * set the current list of members for the group marker
     * @param users ArrayList<UserMarker> the new list of users.
     */
    public void setUsers(ArrayList<UserMarker> users) {
        this.users = users;
    }

    /**
     * a function for adding a new singular usermaker to the current list of members(usermarkers)
     * @param userMarker the Usermarker to be appended to the list of current members.
     */
    public void appendUser(UserMarker userMarker) {
        this.users.add(userMarker);
    }

    /**
     * a function to get the current index of a given usermarker, if the user is contained with current members (UserMarker) of the groupMarker.
     * @param userMarker UserMarker to check index for
     * @return Int index representing the position of the usermarker in the users arraylist.
     */
    public int getUserIndex(UserMarker userMarker) {
        return this.users.indexOf(userMarker);
    }

}
