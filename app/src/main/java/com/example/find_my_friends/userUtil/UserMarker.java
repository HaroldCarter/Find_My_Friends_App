package com.example.find_my_friends.userUtil;

import com.google.android.gms.maps.model.Marker;

/**
 * A classification for a user marker which contains the marker that is being displayed on the map and the associated User the marker is representing, this is done to simply the process of deducing which maker has been interacted with later on.
 *
 * @author Harold Carter
 * @version 2.0
 */
public class UserMarker {
    private Marker userMarker;
    private User userMarkerRepresents;


    /**
     * default constructor for the usermarker
     *
     * @param userMarker           the marker on the google map which this usermarker is representing
     * @param userMarkerRepresents the user's information being displayed on the usermarker
     */
    public UserMarker(Marker userMarker, User userMarkerRepresents) {
        this.userMarker = userMarker;
        this.userMarkerRepresents = userMarkerRepresents;
    }

    /**
     * gets the marker representing the user
     *
     * @return the googlemap marker representing the user
     */
    public Marker getUserMarker() {
        return userMarker;
    }

    /**
     * get the user that the current marker is representing / displaying information about
     *
     * @return User the user the marker is representing
     */
    public User getUserMarkerRepresents() {
        return userMarkerRepresents;
    }

}
