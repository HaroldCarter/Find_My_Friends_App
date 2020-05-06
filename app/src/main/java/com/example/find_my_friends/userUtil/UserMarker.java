package com.example.find_my_friends.userUtil;

import com.google.android.gms.maps.model.Marker;

public class UserMarker {
    private Marker userMarker;
    private User userMarkerRepresents;


    public UserMarker(Marker userMarker, User userMarkerRepresents) {
        this.userMarker = userMarker;
        this.userMarkerRepresents = userMarkerRepresents;
    }




    public Marker getUserMarker() {
        return userMarker;
    }

    public void setUserMarker(Marker userMarker) {
        this.userMarker = userMarker;
    }

    public User getUserMarkerRepresents() {
        return userMarkerRepresents;
    }

    public void setUserMarkerRepresents(User userMarkerRepresents) {
        this.userMarkerRepresents = userMarkerRepresents;
    }
}
