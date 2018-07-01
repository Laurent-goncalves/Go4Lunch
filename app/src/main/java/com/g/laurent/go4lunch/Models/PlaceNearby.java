package com.g.laurent.go4lunch.Models;

import com.g.laurent.go4lunch.Utils.DetailsPlace.Geometry;
import com.g.laurent.go4lunch.Utils.DetailsPlace.OpeningHours;
import java.util.List;

public class PlaceNearby {

    private String name_restaurant;
    private String placeId;
    private Geometry geometry;
    private OpeningHours openingHours;
    private Double rating;
    private List<String> types;
    private String address;
    private String phone_number;
    private String photo_reference;
    private String website;
    private String icon_url;

    public PlaceNearby(String name_restaurant, String placeId, Geometry geometry, OpeningHours openingHours,
                       Double rating, List<String> types, String address, String phone_number, String photo_reference,
                       String website, String icon_url) {
        this.name_restaurant = name_restaurant;
        this.placeId=placeId;
        this.geometry=geometry;
        this.openingHours = openingHours;
        this.rating = rating;
        this.types = types;
        this.address = address;
        this.phone_number=phone_number;
        this.photo_reference=photo_reference;
        this.website=website;
        this.icon_url=icon_url;
    }

    // -----------------------------------------------------------------------------------
    // ----------------------------------- GETTER and SETTER -----------------------------
    // -----------------------------------------------------------------------------------

    public String getName_restaurant() {
        return name_restaurant;
    }

    public void setName_restaurant(String name_restaurant) {
        this.name_restaurant = name_restaurant;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
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

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPhoto_reference() {
        return photo_reference;
    }

    public void setPhoto_reference(String photo_reference) {
        this.photo_reference = photo_reference;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }
}