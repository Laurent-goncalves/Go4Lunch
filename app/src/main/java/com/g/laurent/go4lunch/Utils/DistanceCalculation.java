package com.g.laurent.go4lunch.Utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

public class DistanceCalculation {

    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {

        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, radiusInMeters, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, radiusInMeters, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
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
