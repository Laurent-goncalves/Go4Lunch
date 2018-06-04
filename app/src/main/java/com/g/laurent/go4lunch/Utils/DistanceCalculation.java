package com.g.laurent.go4lunch.Utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class DistanceCalculation {


    public LatLngBounds create_LatLngBounds(int radius, LatLng center){

        LatLngBounds bounds;

        double radiusDegrees = (radius*180)/(Math.PI * 61);
        LatLng northEast = new LatLng(center.latitude + radiusDegrees, center.longitude + radiusDegrees);
        LatLng southWest = new LatLng(center.latitude - radiusDegrees, center.longitude - radiusDegrees);

        bounds = LatLngBounds.builder().include(northEast).include(southWest).build();

        return bounds;
    }


    public String calulate_distance(double lat1, double lon1, double lat2, double lon2) {

        String distance;

        Double latitude1 = lat1 * Math.PI / 180;
        Double latitude2 = lat2 * Math.PI / 180;
        Double longitude1 = lon1 * Math.PI / 180;
        Double longitude2 = lon2 * Math.PI / 180;

        Double Radius = 6371d;
        Double d = 1000 * Radius * Math.acos(Math.cos(latitude1) * Math.cos(latitude2) *
                Math.cos(longitude2 - longitude1) + Math.sin(latitude1) *
                Math.sin(latitude2));

        distance = Math.round(d) + " m";

        return distance;
    }

}
