package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
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
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
        mMapView.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();

        Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(context);

        currentPlaceLatLng = new LatLng(48.866667, 2.333333);

        // Recover list of restos nearby
        if(getArguments()!=null) {

            // String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS, 500));
            // String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE, "restaurant");

            String radius = "500";
            String type = "restaurant";
            String api_key = getArguments().getString(EXTRA_API_KEY, null);

            if (api_key != null)
                new List_Search_Nearby(api_key, currentPlaceLatLng, radius, type, this);
        }

        return view;
    }

    public void recover_list_workmates(List<Place_Nearby> list_resto) {
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

            create_marker_for_each_place_nearby(list_places_nearby,mMap);

            // zoom on current location
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(currentPlaceLatLng, 16);
            mMap.animateCamera(yourLocation);
        });

    }

    private void create_marker_for_each_place_nearby(List<Place_Nearby> list_places_nearby,GoogleMap mMap){

        mMap.clear();

        for(Place_Nearby place_nearby : list_places_nearby){

            if(place_nearby!=null){
                if(place_nearby.getGeometry()!=null){
                    if(place_nearby.getGeometry().getLocation()!=null){
                        LatLng city = new LatLng(place_nearby.getGeometry().getLocation().getLat(), place_nearby.getGeometry().getLocation().getLng());
                        String text = place_nearby.getName_restaurant();

                        System.out.println("eee  text=" + text);

                        if(is_resto_chosen_by_workmates(place_nearby,list_workmates)) {

                            mMap.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(text)
                               //   .icon(BitmapDescriptorFactory.fromPath(place_nearby.getIcon_url())));
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                        } else {
                            mMap.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(text)
                             //     .icon(BitmapDescriptorFactory.fromPath(place_nearby.getIcon_url())));
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));


                        }
                    }
                }
            }
        }
    }



    public static Bitmap getBitmapFromURL(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        int IO_BUFFER_SIZE = 4 * 1024;
        try {
            URI uri = new URI(url);
            url = uri.toASCIIString();
            in = new BufferedInputStream(new URL(url).openStream(),
                    IO_BUFFER_SIZE);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            int bytesRead;
            byte[] buffer = new byte[IO_BUFFER_SIZE];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            final byte[] data = dataStream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    options);
        } catch (IOException e) {
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in!=null)
                    in.close();
                if(out!=null)
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }



    public void set_list_of_workmates(List<Workmate> list_workmates){
        this.list_workmates=list_workmates;
        launch_map_view();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(firebase_recover!=null)
            firebase_recover.recover_list_workmates();
    }
}


/*



                            if(place_nearby.getIcon_url()!=null){
                                mMap.addMarker(new MarkerOptions()
                                        .position(city)
                                        .title(text)
                                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromURL(place_nearby.getIcon_url()))));
                            } else {
                                mMap.addMarker(new MarkerOptions()
                                        .position(city)
                                        .title(text)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            }

                        } else {
                            if(place_nearby.getIcon_url()!=null){
                                mMap.addMarker(new MarkerOptions()
                                        .position(city)
                                        .title(text)
                                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromURL(place_nearby.getIcon_url()))));
                            } else {
                                mMap.addMarker(new MarkerOptions()
                                        .position(city)
                                        .title(text)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                            }





 */