package com.g.laurent.go4lunch.Views.Resto_List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.TimeCalculation;
import com.g.laurent.go4lunch.Views.GlideApp;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.FirebaseStorage;
import java.lang.ref.WeakReference;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RestoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.name_restaurant) TextView name_resto;
    @BindView(R.id.address_restaurant) TextView address_resto;
    @BindView(R.id.hours_opening_restaurant) TextView opening_hours;
    @BindView(R.id.distance_restaurant) TextView distance;
    @BindView(R.id.number_workmates_restaurant) LinearLayout linearLayout_workmates;
    @BindView(R.id.number_workmates) TextView workmates_num;
    @BindView(R.id.linearlayout_rating) LinearLayout rating;
    @BindView(R.id.image_restaurant) ImageView picture_resto;
    private View view;
    private Place_Nearby place_nearby;
    private List<Workmate> list_workmates;
    private LatLng current_loc;
    private WeakReference<ListViewAdapter.Listener> callbackWeakRef;
    private Context context;

    public RestoViewHolder(View itemView) {
        super(itemView);
        this.view=itemView;
        ButterKnife.bind(this, itemView);
    }

    public void configure_restaurant(LatLng current_loc, Place_Nearby place_nearby, ListViewAdapter.Listener callback, List<Workmate> list_workmates, Context context){

        this.place_nearby=place_nearby;
        this.current_loc=current_loc;
        this.callbackWeakRef = new WeakReference<>(callback);
        this.view.setOnClickListener(this);
        this.context=context;
        this.list_workmates=list_workmates;

        // Name restaurant
        name_restaurant(place_nearby);

        //Address restaurant
        address_restaurant(place_nearby);

        // Opening hours
        display_opening_hours();

        // Distance to restaurant
        display_distance_to_restaurant();

        // Workmate joining
        define_workmates_number();

        // Score resto
        rating_calculation();

        // Put the picture of the resto
        apply_picture_restaurant();
    }

    // ---------------------------------- NAME RESTO ----------------------------------------------
    private void name_restaurant(Place_Nearby place_nearby) {

        if(place_nearby!=null){
            if(place_nearby.getName_restaurant()!=null){
                if(place_nearby.getName_restaurant().length()<=30)
                    name_resto.setText(place_nearby.getName_restaurant());
                else {
                    String text = place_nearby.getName_restaurant().substring(0, 30) + "...";
                    name_resto.setText(text);
                }
            }
        }
    }

    // ---------------------------------- ADDRESS RESTO ----------------------------------------------
    private void address_restaurant(Place_Nearby place_nearby) {

        if(place_nearby!=null){
            if(place_nearby.getAddress()!=null){
                if(place_nearby.getAddress().length()<=25)
                    address_resto.setText(place_nearby.getAddress());
                else {
                    String text = place_nearby.getAddress().substring(0, 25) + "...";
                    address_resto.setText(text);
                }
            }
        }
    }

    // ---------------------------------- RATING RESTO ----------------------------------------------
    private void rating_calculation() {

        int numStars = Math.round(place_nearby.getRating().floatValue());
        rating.removeAllViews();

        if(numStars>=1){
            for (int i = 0; i < numStars-1; i++) {
                ImageView imgView = new ImageView(context);
                imgView.setImageResource(R.drawable.baseline_star_white_24);
                imgView.setColorFilter(ContextCompat.getColor(context,(R.color.colorStars)));
                rating.addView(imgView);
            }
        }
    }
    // ---------------------------------- PICTURE RESTO ----------------------------------------------

    private void apply_picture_restaurant() {

        String link = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + place_nearby.getPhoto_reference()
                + "&key=" + context.getResources().getString(R.string.google_maps_key);

        // Load the image using Glide
        GlideApp.with(view)
                    .load(link)
                    .into(picture_resto);
    }

    // ---------------------------------- WORKMATES JOINING ----------------------------------------------

    @SuppressLint("SetTextI18n")
    private void define_workmates_number(){

        int count = 0;
        // Count number of workmates choosing this restaurant
        if(list_workmates!=null && place_nearby.getPlaceId()!=null){

            for(Workmate workmate : list_workmates){
                if(workmate!=null){
                    if(workmate.getResto_id()!=null){
                        if(workmate.getResto_id().equals(place_nearby.getPlaceId()))
                            count++;
                    }
                }
            }
        }

        // Set text to display
        if(count>0) { // if at least one workmate join the restaurant
            String text = "(" + count + ")";
            workmates_num.setText(text);
        } else // if no workmates join this restaurant, don't display the symbol workmates and the number
                linearLayout_workmates.setVisibility(View.GONE);
    }

    // -------------------------------- OPENING HOURS ---------------------------------------------

    private void display_opening_hours() {
        String opening = null;
        if (place_nearby.getOpeningHours() != null) {
            if (place_nearby.getOpeningHours().getOpenNow()!=null) {
                if (place_nearby.getOpeningHours().getOpenNow()){

                    TimeCalculation timeCalculation = new TimeCalculation();
                    opening = timeCalculation.getTextOpeningHours(place_nearby);

                }else
                    opening = "Closed now";
            }
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

    @Override
    public void onClick(View v) {
        ListViewAdapter.Listener callback = callbackWeakRef.get();
        if (callback != null){
            callback.onClickShowRestoDetails(place_nearby.getPlaceId());
        }
    }
}

