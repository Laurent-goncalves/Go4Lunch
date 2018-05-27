package com.g.laurent.go4lunch;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsFragment extends BaseRestoFragment  {

    private List<Place_Nearby> list_places_nearby;
    @BindView(R.id.mapview) MapView mMapView;
    private LatLng currentPlaceLatLng;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private final static String EXTRA_RADIUS = "radius_for_search";
    private final static String EXTRA_TYPE_PLACE = "type_of_place";
    private Firebase_recover firebase_recover;
    private Context context;
    private List<Workmate> list_workmates;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this,view);
        mMapView.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();

        // Recover list of restos nearby
        if(getArguments()!=null){
            currentPlaceLatLng = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT),
                    getArguments().getDouble(EXTRA_LONG_CURRENT));
            String radius = getArguments().getString(EXTRA_RADIUS,"500");
            String type = getArguments().getString(EXTRA_TYPE_PLACE,"restaurant");

            new List_Search_Nearby(currentPlaceLatLng,radius,type,this);
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
