package com.example.find_my_friends.util;

public class LocationUtils {


    public static double distanceBetweenTwoPointKM(double lat1, double lon1, double lat2,
                                  double lon2) {

        final int radiusOfEarth = 6371;

        double lateralDistance = Math.toRadians(lat2 - lat1);
        double longitudaldistance = Math.toRadians(lon2 - lon1);
        double x = Math.sin(lateralDistance / 2) * Math.sin(lateralDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudaldistance / 2) * Math.sin(longitudaldistance / 2);
        double y = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));

       //convert the distance to meters
        double distance = radiusOfEarth * y * 1000;

        //return the distance in KM
        return Math.sqrt(distance)/1000;
    }
}
