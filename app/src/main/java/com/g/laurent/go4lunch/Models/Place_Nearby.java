package com.g.laurent.go4lunch.Models;

import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Photo;

import java.util.List;

public class Place_Nearby {

    private String name_restaurant;
    private OpeningHours openingHours;
    private List<Photo> photos;
    private Double rating;
    private List<String> types;
    private String address;

    public Place_Nearby(String name_restaurant, OpeningHours openingHours, List<Photo> photos, Double rating, List<String> types, String address) {
        this.name_restaurant = name_restaurant;
        this.openingHours = openingHours;
        this.photos = photos;
        this.rating = rating;
        this.types = types;
        this.address = address;
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
