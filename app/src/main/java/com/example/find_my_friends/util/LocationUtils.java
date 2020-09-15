package com.example.find_my_friends.util;

import android.location.Location;

/**
 * A class containing Utility functions useful for calculating the distance between two locations in variable vector units.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class LocationUtils {

    /**
     * calculates the trigonometric hypotenuse distance between two geographical locations, ignoring altitude
     *
     * @param lat1 double for the latitude of the first geopoint
     * @param lon1 double for the longitude of the first geopoint
     * @param lat2 double for the latitude of teh second geopoint
     * @param lon2 double for the longitude of the second geopoint
     * @return a float containing the hypotenuse between the two points in KM
     */
    public static float distanceBetweenTwoPointKM(double lat1, double lon1, double lat2,
                                                  double lon2) {
        Location location = new Location("");
        location.setLatitude(lat1);
        location.setLongitude(lon1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        return (location.distanceTo(location2) / 1000.f);
    }

    /**
     * calculates the trigonometric hypotenuse distance between two geographical locations, ignoring altitude
     *
     * @param lat1 double for the latitude of the first geopoint
     * @param lon1 double for the longitude of the first geopoint
     * @param lat2 double for the latitude of teh second geopoint
     * @param lon2 double for the longitude of the second geopoint
     * @return a float containing the hypotenuse between the two points in Miles
     */
    public static float distanceBetweenTwoPointMiles(double lat1, double lon1, double lat2,
                                                     double lon2) {
        return distanceBetweenTwoPointKM(lat1, lon1, lat2, lon2) * 0.621371f;
    }


}
