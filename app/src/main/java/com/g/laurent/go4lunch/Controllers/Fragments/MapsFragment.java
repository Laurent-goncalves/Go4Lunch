package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g.laurent.go4lunch.Controllers.Activities.RestoActivity;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Google_Maps_Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import static android.content.Context.MODE_PRIVATE;


public class MapsFragment extends BaseRestoFragment  {

    @BindView(R.id.mapview) MapView mMapView;


    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_PREF_LANG = "language_preferences";
    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";
    private final static String EXTRA_RESTO_DETAILS = "resto_details";

    private Firebase_recover firebase_recover;
    private Context context;
    private List<Workmate> list_workmates;


    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance(String api_key, List<Place_Nearby> list_restos) {

        // Create new fragment
        MapsFragment frag = new MapsFragment();

        // Create bundle and add the list of restaurants to the bundle
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String list_restos_json = gson.toJson(list_restos);
        bundle.putString(EXTRA_LIST_RESTOS_JSON, list_restos_json);

        frag.setArguments(bundle);

        return(frag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this,view);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(EXTRA_PREFERENCES, MODE_PRIVATE);
        mMapView.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        list_places_nearby_OLD = new ArrayList<>();
        Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(context);

        currentPlaceLatLng = new LatLng(48.866667, 2.333333);


        if(getArguments()!=null) {

            Gson gson = new Gson();
            String json = getArguments().getString(EXTRA_LIST_RESTOS_JSON,null);
            Type list_places = new TypeToken<ArrayList<Place_Nearby>>(){}.getType();
            list_places_nearby = gson.fromJson(json,list_places);
        }

        recover_list_workmates(list_places_nearby);
        // Recover list of restos nearby
        /*if(getArguments()!=null) {

            // String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS, 500));
            // String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE, "restaurant");

            String radius = "500";
            String type = "restaurant";
            String api_key = getArguments().getString(EXTRA_API_KEY, null);

            if (api_key != null)
                new List_Search_Nearby(api_key, currentPlaceLatLng, radius, type, this);
        }*/

        return view;
    }

    public void recover_list_workmates(List<Place_Nearby> list_resto) {

        if(list_places_nearby_OLD!=null && list_places_nearby!=null){
            if(list_places_nearby_OLD.size()==0){ // if there is no place nearby in the old list, it means this method is called for the search
                list_places_nearby_OLD.addAll(list_places_nearby);
            }
        }

        this.list_places_nearby = list_resto;

        firebase_recover = new Firebase_recover(context,this);
        firebase_recover.recover_list_workmates();
    }

    private void launch_map_view(){

        try {
            MapsInitializer.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {

            mMapView.onResume();

            // Create marker for each resto on the map
            create_marker_for_each_place_nearby(list_places_nearby,mMap);

            // Set on click listener for markers of the map
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    String resto_json = (String) marker.getTag();
                    Intent intent = new Intent(context, RestoActivity.class);
                    intent.putExtra(EXTRA_RESTO_DETAILS,resto_json);
                    startActivity(intent);

                    return false;
                }
            });


            // zoom on current location
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(currentPlaceLatLng, 16);
            mMap.animateCamera(yourLocation);
        });

    }

    private void create_marker_for_each_place_nearby(List<Place_Nearby> list_places_nearby,GoogleMap mMap){

        Gson gson = new Gson();
        mMap.clear();
        MarkerOptions markerOptions;

        for(Place_Nearby place_nearby : list_places_nearby){

            if(place_nearby!=null){
                if(place_nearby.getGeometry()!=null){
                    if(place_nearby.getGeometry().getLocation()!=null){
                        LatLng city = new LatLng(place_nearby.getGeometry().getLocation().getLat(), place_nearby.getGeometry().getLocation().getLng());
                        String text = place_nearby.getName_restaurant();

                        if(is_resto_chosen_by_workmates(place_nearby,list_workmates)) {
                            markerOptions = new MarkerOptions()
                                .position(city)
                                .title(text)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_resto_1));
                        } else {
                            markerOptions = new MarkerOptions()
                                .position(city)
                                .title(text)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_resto_0));
                        }
                        Marker marker = mMap.addMarker(markerOptions);
                        String resto_json = gson.toJson(place_nearby);
                        marker.setTag(resto_json);
                    }
                }
            }
        }
    }

    public void set_list_of_workmates(List<Workmate> list_workmates){
        this.list_workmates=list_workmates;
        launch_map_view();
    }

    public void recover_previous_state(){

        if(list_places_nearby_OLD!=null){
            if(list_places_nearby_OLD.size()>0){

                this.list_places_nearby = new ArrayList<>();
                this.list_places_nearby.addAll(list_places_nearby_OLD);
                list_places_nearby_OLD.clear();
                launch_map_view();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(firebase_recover!=null)
            firebase_recover.recover_list_workmates();
    }
}