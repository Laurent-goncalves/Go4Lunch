package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

public class MapsFragment extends BaseRestoFragment  {

    private List<Place_Nearby> list_places_nearby;
    @BindView(R.id.mapview) MapView mMapView;
    private LatLng currentPlaceLatLng;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_PREF_LANG = "language_preferences";
    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";
    private final String EXTRA_API_KEY = "api_key";
    private Firebase_recover firebase_recover;
    private Context context;
    private List<Workmate> list_workmates;

    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance(String api_key) {

        // Create new fragment
        MapsFragment frag = new MapsFragment();
        String EXTRA_API_KEY = "api_key";

        // Create bundle and add it some data
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_API_KEY, api_key);
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
        //mMapView.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();

        // Recover list of restos nearby
        if(getArguments()!=null) {
            currentPlaceLatLng = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT),
                    getArguments().getDouble(EXTRA_LONG_CURRENT));
            String radius = sharedPreferences.getString(EXTRA_PREF_RADIUS, "500");
            String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE, "restaurant");
            String api_key = getArguments().getString(EXTRA_API_KEY, null);

            if (api_key != null) {
                System.out.println("eeee MAPSFRAGMENT api_key = "+api_key);
                new List_Search_Nearby(api_key, currentPlaceLatLng, radius, type, this);
            }
        }

        return view;
    }

    private void create_marker_for_each_place_nearby(List<Place_Nearby> list_places_nearby,GoogleMap mMap){

        for(Place_Nearby place_nearby : list_places_nearby){

            if(place_nearby!=null){
                if(place_nearby.getGeometry()!=null){
                    if(place_nearby.getGeometry().getLocation()!=null){
                        LatLng city = new LatLng(place_nearby.getGeometry().getLocation().getLat(), place_nearby.getGeometry().getLocation().getLng());
                        String text = place_nearby.getName_restaurant();

                        if(is_resto_chosen_by_workmates(place_nearby,list_workmates)) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(text)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        } else {
                            mMap.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(text)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        }
                    }
                }
            }
        }
    }

    public void recover_list_workmates(List<Place_Nearby> list_resto) {
        this.list_places_nearby = list_resto;
        firebase_recover = new Firebase_recover(context,this);
        firebase_recover.recover_list_workmates();
    }

    private void launch_map_view(){

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {

            mMapView.onResume();

            create_marker_for_each_place_nearby(list_places_nearby,mMap);

            // zoom on current location
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(currentPlaceLatLng, 16);
            mMap.animateCamera(yourLocation);
        });

    }

    public void set_list_of_workmates(List<Workmate> list_workmates){
        this.list_workmates=list_workmates;
        launch_map_view();
    }

}
