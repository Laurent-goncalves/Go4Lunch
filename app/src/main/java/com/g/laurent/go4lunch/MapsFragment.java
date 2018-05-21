package com.g.laurent.go4lunch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
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
    private Firebase_recover firebase_recover;
    GoogleMap mMap;


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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebase_recover = new Firebase_recover(getActivity().getApplicationContext(),null,this,null,null);
        if (currentUser != null) {
            firebase_recover.recover_list_restos(currentUser.getUid(),null);
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

                        if(place_nearby.getWorkmatesList().size()>0) {
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

    public void set_resto(List<Place_Nearby> list_resto) {

        this.list_places_nearby = list_resto;
        currentPlaceLatLng = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT),
                getArguments().getDouble(EXTRA_LONG_CURRENT));

        //mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                mMapView.onResume();

                create_marker_for_each_place_nearby(list_places_nearby,mMap);

                // zoom on current location
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(currentPlaceLatLng, 16);
                mMap.animateCamera(yourLocation);
            }
        });
    }

    @Override
    public void configure_and_show_restofragment(String placeId) {

    }


}
