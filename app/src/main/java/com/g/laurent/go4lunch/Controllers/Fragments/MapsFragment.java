package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Activities.RestoActivity;
import com.g.laurent.go4lunch.Models.PlaceNearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.FirebaseRecover;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MapsFragment extends BaseRestoFragment  {

    @BindView(R.id.mapview) MapView mMapView;
    private final static String EXTRA_RESTO_DETAILS = "resto_details";
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private FirebaseRecover mFirebase_recover;
    private Context context;
    private List<Workmate> list_workmates;
    private LatLng current_place;

    public MapsFragment() {
        // Required empty public constructor
    }

    public static MapsFragment newInstance(List<PlaceNearby> list_restos, LatLng current_place) {

        // Create new fragment
        MapsFragment frag = new MapsFragment();

        // Create bundle and add the list of restaurants to the bundle
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String list_restos_json = gson.toJson(list_restos);
        bundle.putString(EXTRA_LIST_RESTOS_JSON, list_restos_json);
        if(current_place!=null) {
            bundle.putDouble(EXTRA_LAT_CURRENT, current_place.latitude);
            bundle.putDouble(EXTRA_LONG_CURRENT, current_place.longitude);
        }
        frag.setArguments(bundle);

        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this,view);
        mMapView.onCreate(savedInstanceState);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();
        list_places_nearby_OLD = new ArrayList<>();

        if(getArguments()!=null) {

            Gson gson = new Gson();
            String json = getArguments().getString(EXTRA_LIST_RESTOS_JSON,null);
            Type list_places = new TypeToken<ArrayList<PlaceNearby>>(){}.getType();
            list_places_nearby = gson.fromJson(json,list_places);

            current_place = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT,48.866667),
                    getArguments().getDouble(EXTRA_LONG_CURRENT,2.333333));
        }

        recover_list_workmates(list_places_nearby);

        return view;
    }

    public void recover_list_workmates(List<PlaceNearby> list_resto) {

        if(list_places_nearby_OLD!=null && list_places_nearby!=null){
            if(list_places_nearby_OLD.size()==0){ // if there is no place nearby in the old list, it means this method is called for the search
                list_places_nearby_OLD.addAll(list_places_nearby);
            }
        }

        this.list_places_nearby = list_resto;

        mFirebase_recover = new FirebaseRecover(context,this);
        mFirebase_recover.recover_list_workmates();
    }

    private void launch_map_view(){

        try {
            MapsInitializer.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {

            mMapView.onResume();

            if(list_places_nearby!=null){
                // Create marker for each resto on the map
                create_marker_for_each_place_nearby(list_places_nearby,mMap);

                // Set on click listener for markers of the map
                mMap.setOnMarkerClickListener(marker -> {

                    String resto_json = (String) marker.getTag();
                    Intent intent = new Intent(context, RestoActivity.class);
                    intent.putExtra(EXTRA_RESTO_DETAILS,resto_json);
                    startActivity(intent);

                    return false;
                });

                // zoom on current location if number of resto > 1
                if(list_places_nearby.size()>1){

                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(current_place, 16);
                    mMap.animateCamera(yourLocation);

                } else { // else zoom on the resto from the list

                    LatLng location_zoom = current_place;
                    try{
                        location_zoom = new LatLng(list_places_nearby.get(0).getGeometry().getLocation().getLat(),
                                list_places_nearby.get(0).getGeometry().getLocation().getLng());
                    } catch(Throwable e){
                        Toast toast = Toast.makeText(context,context.getResources().getString(R.string.localization_unkown),Toast.LENGTH_LONG);
                        toast.show();
                    }

                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(location_zoom, 16);
                    mMap.animateCamera(yourLocation);
                }
            }
        });
    }

    private void create_marker_for_each_place_nearby(List<PlaceNearby> list_places_nearby, GoogleMap mMap){

        Gson gson = new Gson();
        mMap.clear();
        MarkerOptions markerOptions;

        for(PlaceNearby place_nearby : list_places_nearby){

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
        if(mFirebase_recover !=null)
            mFirebase_recover.recover_list_workmates();
    }
}




        // Recover list of restos nearby
        /*if(getArguments()!=null) {

            // String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS, 500));
            // String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE, "restaurant");

            String radius = "500";
            String type = "restaurant";
            String api_key = getArguments().getString(EXTRA_API_KEY, null);

            if (api_key != null)
                new ListSearchNearby(api_key, currentPlaceLatLng, radius, type, this);
        }*/

