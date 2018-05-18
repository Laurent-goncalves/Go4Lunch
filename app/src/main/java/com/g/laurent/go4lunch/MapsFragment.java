package com.g.laurent.go4lunch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsFragment extends BaseRestoFragment  {

    private List<Place_Nearby> list_places_nearby;
    @BindView(R.id.mapview) MapView mMapView;
    private LatLng currentPlaceLatLng;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
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

        currentPlaceLatLng = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT),
                                        getArguments().getDouble(EXTRA_LONG_CURRENT));

        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                mMapView.onResume();

                // recover the list of resto on Firebase
                recover_list_resto_firebase(mMap);

                // zoom on current location
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(currentPlaceLatLng, 16);
                mMap.animateCamera(yourLocation);
            }
        });

        return view;
    }

    public void recover_list_resto_firebase(final GoogleMap mMap) {

        list_places_nearby = new ArrayList<>();
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("restaurants");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                create_list_place_nearby(dataSnapshot);
                create_marker_for_each_place_nearby(list_places_nearby,mMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    private void create_list_place_nearby(DataSnapshot dataSnapshot){

        if(dataSnapshot!=null) {
            for (DataSnapshot datas : dataSnapshot.getChildren()){

                getGeometryRestaurant(datas);
                getNameRestaurant(datas);
                getWorkmatesJoining(datas);

                list_places_nearby.add(new Place_Nearby(name_resto,
                        null,geometry,null,null,null,null,list_workmates));

            }
        }
    }

    private void create_marker_for_each_place_nearby(List<Place_Nearby> list_places_nearby,GoogleMap mMap){

        for(Place_Nearby place_nearby : list_places_nearby){

            if(place_nearby!=null){
                if(place_nearby.getGeometry()!=null){
                    if(place_nearby.getGeometry().getLocation()!=null){
                        LatLng city = new LatLng(place_nearby.getGeometry().getLocation().getLat(), place_nearby.getGeometry().getLocation().getLng());
                        String text = place_nearby.getName_restaurant();

                        if(place_nearby.getWorkmatesList().size()>0) {
                            System.out.println("eee VERT");
                            mMap.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(text)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        } else {
                            System.out.println("eee ORANGE");
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

}
