package com.example.find_my_friends.groupUtil;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * a class which contains the relevant data used to display the information in the infowindowadapter, as this is passed by setting tag on the marker as this datatype; this should contain the information for the group/User that the infowindow is displaying.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class InfoWindowData {
    private String windowTitle;
    private LatLng windowLocation;
    private String windowModeOfTransportForUser;
    private String windowPhotoURL;
    private String windowSecondaryTitle;
    private Integer travelDuration = null;

    /**
     * default constructor for the infowindowData class, which is used to be set as the tag when creating an info window, from this information can be passed between mapOverviewFragment and the infoWindowAdapter class, therefore all relevant internal variables are set using this constructor.
     *
     * @param windowLocation LatLng for the GPS coordinate that the window will be being placed on the map (for geographical changes)
     * @param windowModeOfTransportForUser String representing the mode of transport the users is using (only for when the marker is representing a user), this can be one of three values "Car, Bike, Person"
     * @param windowTitle String for the window's title
     * @param windowPhotoURL String for the download URI used to display a photo on the infowindow
     * @param windowSecondaryTitle String for the window's secondary title (smaller font and vertically under the title)
     */
    public InfoWindowData(LatLng windowLocation, String windowModeOfTransportForUser, @NonNull String windowTitle, @NonNull String windowPhotoURL, @NonNull String windowSecondaryTitle) {
        this.windowTitle = windowTitle;
        this.windowLocation = windowLocation;
        this.windowModeOfTransportForUser = windowModeOfTransportForUser;
        this.windowPhotoURL = windowPhotoURL;
        this.windowSecondaryTitle = windowSecondaryTitle;
    }

    /**
     *
     * @return String representing the mode of transport the users is using (only for when the marker is representing a user), this can be one of three values "Car, Bike, Person"
     */
    public String getWindowModeOfTransportForUser() {
        return windowModeOfTransportForUser;
    }

    /**
     * strictly to be used once the duration is set, this should be used with route planning to give an update an estimated ETA time for the users arrival
     *
     * @return Integer for the estimated duration of travel
     */
    public Integer getTravelDuration() {
        return travelDuration;
    }

    /**
     * strictly to be with route planning, to be used to set an estimated ETA time for the users arrival
     *
     * @param travelDuration Integer for the estimated duration of travel
     */
    public void setTravelDuration(Integer travelDuration) {
        this.travelDuration = travelDuration;
    }

    /**
     * get the Location of from the data object that the infoWindow will be displayed over
     *
     * @return LatLng for the GPS coordinate that the window will be being placed on the map (for geographical changes)
     */
    public LatLng getWindowLocation() {
        return windowLocation;
    }

    /**
     *  get the data object's value for the title
     *
     * @return String for the window's title
     */
    public String getWindowTitle() {
        return windowTitle;
    }

    /**
     *
     * @return String for the download URI used to display a photo on the infowindow
     */
    public String getWindowPhotoURL() {
        return windowPhotoURL;
    }

    /**
     * get the data object's value for the secondary title
     *
     * @return  String for the window's secondary title (smaller font and vertically under the title)
     */
    public String getWindowSecondaryTitle() {
        return windowSecondaryTitle;
    }
}
