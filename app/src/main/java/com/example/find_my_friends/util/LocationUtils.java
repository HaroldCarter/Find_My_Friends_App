package com.example.find_my_friends.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LocationUtils {


    public static double distanceBetweenTwoPointKM(double lat1, double lon1, double lat2,
                                  double lon2) {
        //radius of earth in km
        final int radiusOfEarth = 6371;

        double lateralDistance = Math.toRadians(lat2 - lat1);
        double longitudinalDistance = Math.toRadians(lon2 - lon1);

        double x = Math.sin(lateralDistance / 2) * Math.sin(lateralDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudinalDistance / 2) * Math.sin(longitudinalDistance / 2);

        double y = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));


        double distance = radiusOfEarth * y * 1000;

        //return the distance in KM
        return Math.sqrt(distance);
    }

    public static double distanceBetweenTwoPointMiles(double lat1, double lon1, double lat2,
                                                   double lon2) {
        return distanceBetweenTwoPointKM(lat1,lon1,lat2,lon2)*0.625;
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
