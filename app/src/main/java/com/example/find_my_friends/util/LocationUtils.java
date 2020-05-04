package com.example.find_my_friends.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class LocationUtils {


    public static float distanceBetweenTwoPointKM(double lat1, double lon1, double lat2,
                                  double lon2) {
        Location location = new Location("");
        location.setLatitude(lat1);
        location.setLongitude(lon1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        return (location.distanceTo(location2)/1000.f);
    }

    public static float distanceBetweenTwoPointMiles(double lat1, double lon1, double lat2,
                                                   double lon2) {
        return distanceBetweenTwoPointKM(lat1,lon1,lat2,lon2)*0.621371f;
    }




    //need to write function that calculates the distance in the range of the lat long from a given number of miles
    public static ArrayList<Double> calculateGpsBounds(LatLng currentLocation, Double rangeInMiles){
        ArrayList<Double> bounds = new ArrayList<>();
        //ratio of relative measurements to miles.
        Double latMileUnit = 0.0144927536231884;
        Double lngMileUnit = 0.0181818181818182;

        bounds.add((currentLocation.latitude-(latMileUnit*rangeInMiles)));
        bounds.add((currentLocation.latitude+(latMileUnit*rangeInMiles)));
        bounds.add((currentLocation.longitude-(lngMileUnit*rangeInMiles)));
        bounds.add((currentLocation.longitude+(lngMileUnit*rangeInMiles)));

        return bounds;
        //returns a square search space containing the values for the circular perimeter.
    }

}
