package com.g.laurent.go4lunch.Models;

import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Photo;

import java.util.List;

public class Place_Nearby {

    private String name_restaurant;
    private String placeId;
    private Geometry geometry;
    private OpeningHours openingHours;
    private List<Photo> photos;
    private Double rating;
    private List<String> types;
    private String address;
    private List<Workmates> mWorkmatesList;


    public Place_Nearby(String name_restaurant, String placeId, Geometry geometry, OpeningHours openingHours, Double rating, List<String> types, String address,List<Workmates> mWorkmatesList) {
        this.name_restaurant = name_restaurant;
        this.placeId=placeId;
        this.geometry=geometry;
        this.openingHours = openingHours;
        this.rating = rating;
        this.types = types;
        this.address = address;
        this.mWorkmatesList=mWorkmatesList;
    }

    public List<Workmates> getWorkmatesList() {
        return mWorkmatesList;
    }

    public void setWorkmatesList(List<Workmates> workmatesList) {
        mWorkmatesList = workmatesList;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String id) {
        this.placeId = placeId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getName_restaurant() {
        return name_restaurant;
    }

    public void setName_restaurant(String name_restaurant) {
        this.name_restaurant = name_restaurant;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
