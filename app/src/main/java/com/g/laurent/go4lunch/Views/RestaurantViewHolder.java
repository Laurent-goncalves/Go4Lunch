package com.g.laurent.go4lunch.Views;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.google.android.gms.maps.model.LatLng;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.name_restaurant) private TextView name_resto;
    @BindView(R.id.address_restaurant) private TextView address_resto;
    @BindView(R.id.hours_opening_restaurant) private TextView opening_hours;
    @BindView(R.id.distance_restaurant) private TextView distance;
    @BindView(R.id.distance_restaurant) private LinearLayout linearLayout_workmates;
    @BindView(R.id.number_workmates) private TextView workmates_num;
    @BindView(R.id.rating_restaurant) private RatingBar rating;
    @BindView(R.id.image_restaurant) private ImageView picture_resto;
    private Place_Nearby place_nearby;
    private LatLng current_loc;
    private View view;

    public RestaurantViewHolder(View itemView, LatLng current_loc, Place_Nearby place_nearby) {
        super(itemView);
        this.place_nearby=place_nearby;
        this.current_loc=current_loc;
        this.view=itemView;
        ButterKnife.bind(this, itemView);

        if(place_nearby!=null)
            configure_restaurant();
    }

    private void configure_restaurant(){

        // Name restaurant
        name_resto.setText(place_nearby.getName_restaurant());

        //Address restaurant
        address_resto.setText(place_nearby.getAddress());

        // Opening hours
        display_opening_hours();

        // Distance to restaurant
        display_distance_to_restaurant();

        // Workmates joining
        define_workmates_number();

        // Score resto
        rating.setRating(place_nearby.getRating().floatValue());

        // Put the picture of the resto
        apply_picture_restaurant();
    }


    // ---------------------------------- PICTURE RESTO ----------------------------------------------

    private void apply_picture_restaurant() {

        if(place_nearby.getPhotos()!=null){

            Glide.with(view)
                    .load(place_nearby.getPhotos().get(0))
                    .into(picture_resto);

        }
    }

    // ---------------------------------- WORKMATES JOINING ----------------------------------------------

    @SuppressLint("SetTextI18n")
    private void define_workmates_number(){

        if(place_nearby.getWorkmatesList().size()>0){
            workmates_num.setText("(" + place_nearby.getWorkmatesList().size() + ")");
        } else // if no workmates join this restaurant, don't display the symbol workmates and the number
            linearLayout_workmates.setVisibility(View.GONE);
    }

    // -------------------------------- OPENING HOURS ---------------------------------------------

    private void display_opening_hours() {
        String opening = null;
        if (place_nearby.getOpeningHours() != null) {
            if (place_nearby.getOpeningHours().getOpenNow())
                opening = "Closed now";
        }
        opening_hours.setText(opening);
    }

    // -------------------------------- CALCULATION DISTANCE ---------------------------------------------

    private void display_distance_to_restaurant(){

        if(current_loc!=null && place_nearby.getGeometry()!=null) {

            if(place_nearby.getGeometry().getLocation()!=null)
                distance.setText(calulate_distance(
                        current_loc.latitude,
                        current_loc.longitude,
                        place_nearby.getGeometry().getLocation().getLat(),
                        place_nearby.getGeometry().getLocation().getLng()));
        }
    }

    private String calulate_distance(double lat1, double lon1, double lat2, double lon2) {

        String distance;

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        if(dist < 1) {
            dist = dist * 1000;
            distance = dist + " m";
        } else
            distance = dist + " km";

            return distance;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
