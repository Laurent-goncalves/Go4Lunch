package com.g.laurent.go4lunch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Utils.Maps_API_service;
import com.g.laurent.go4lunch.Utils.Maps_API_stream;
import com.g.laurent.go4lunch.Utils.Search_Nearby.SearchNearby;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static android.content.ContentValues.TAG;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = true;
    private LatLng lastKnownPlace;
    private Place currentPlace;
    private LatLng currentPlaceLatLng;
    private final static String EXTRA_LAST_LATITUDE = "last_latitude";
    private final static String EXTRA_LAST_LONGITUDE = "last_longitude";
    private final static String BUTTON_MAP_SELECTED = "button_map_selected";
    private final static String BUTTON_LIST_SELECTED = "button_list_view_selected";
    private final static String BUTTON_MATES_SELECTED = "button_workmates_selected";
    private String BUTTON_SELECTED;
    @BindView(R.id.map_view_button) Button buttonMap;
    @BindView(R.id.list_view_button) Button buttonList;
    @BindView(R.id.workmates_button) Button buttonMates;

int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        BUTTON_SELECTED=BUTTON_MAP_SELECTED;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/

        lastKnownPlace=findLastPlaceHighestLikelihood(savedInstanceState);


        configure_tabs();

        /*mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();*/

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng corbeil = new LatLng(48.6102599, 2.474805);
        mMap.addMarker(new MarkerOptions().position(corbeil).title("Marker in corbeil"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(corbeil));




 /*     // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        getNumberResults();
*/


    }

    private void configure_tabs(){

        BUTTON_SELECTED=BUTTON_MAP_SELECTED;
        setButtonAsSelected(true, buttonMap);
        setButtonAsSelected(false, buttonList);
        setButtonAsSelected(false, buttonMates);

        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!BUTTON_SELECTED.equals(BUTTON_MAP_SELECTED)) {
                    setButtonAsSelected(true, buttonMap);
                    setButtonAsSelected(false, buttonList);
                    setButtonAsSelected(false, buttonMates);
                    BUTTON_SELECTED=BUTTON_MAP_SELECTED;
                }
            }
        });

        buttonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!BUTTON_SELECTED.equals(BUTTON_LIST_SELECTED)) {
                    setButtonAsSelected(false, buttonMap);
                    setButtonAsSelected(true, buttonList);
                    setButtonAsSelected(false, buttonMates);
                    BUTTON_SELECTED=BUTTON_LIST_SELECTED;
                }
            }
        });

        buttonMates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!BUTTON_SELECTED.equals(BUTTON_MATES_SELECTED)) {
                    setButtonAsSelected(false, buttonMap);
                    setButtonAsSelected(false, buttonList);
                    setButtonAsSelected(true, buttonMates);
                    BUTTON_SELECTED=BUTTON_MATES_SELECTED;
                }
            }
        });

    }

    private void setButtonAsSelected(Boolean select, Button button){

        if(select) {

            button.setTextColor(getResources().getColor(R.color.colorIconSelected));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                button.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),(R.color.colorIconSelected))));
            else {
                Drawable[] wrapDrawable = button.getCompoundDrawables();
                DrawableCompat.setTint(wrapDrawable[0], getResources().getColor(R.color.colorIconSelected));
            }

        } else {

            button.setTextColor(getResources().getColor(R.color.colorIconNotSelected));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                button.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),(R.color.colorIconNotSelected))));
            else {
                Drawable[] wrapDrawable = button.getCompoundDrawables();
                DrawableCompat.setTint(wrapDrawable[0], getResources().getColor(R.color.colorIconNotSelected));
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("eeee    connexion failed !!! ");
    }

    public LatLng getNumberResults() {

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        if (mLocationPermissionGranted) {

            try {

                @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
                placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                        if (task.isSuccessful() && task.getResult() != null) {

                            currentPlace=findPlaceHighestLikelihood(task);

                            if(currentPlace!=null)
                                currentPlaceLatLng=currentPlace.getLatLng();
                            else
                                currentPlaceLatLng = lastKnownPlace;

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return currentPlaceLatLng;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(lastKnownPlace!=null){
            outState.putFloat(EXTRA_LAST_LATITUDE, (float) lastKnownPlace.latitude);
            outState.putFloat(EXTRA_LAST_LONGITUDE, (float) lastKnownPlace.longitude);
        }
        super.onSaveInstanceState(outState);
    }

    private Place findPlaceHighestLikelihood(@NonNull Task<PlaceLikelihoodBufferResponse> task){

        Place placeHighestLikelihood = null;
        float percentage = 0;

        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

        for (PlaceLikelihood placeLikelihood : likelyPlaces) {       // for each placeLikelihood

            if(placeLikelihood.getLikelihood() > percentage) {       // if the likelihood is higher than the other ones,
                placeHighestLikelihood = placeLikelihood.getPlace(); //   set this place as placeLikelihood
                percentage = placeLikelihood.getLikelihood();        //   set this percentage as highest likelihood
            }
        }
        likelyPlaces.release();

        return placeHighestLikelihood;
    }

    private LatLng findLastPlaceHighestLikelihood(Bundle savedInstanceState) {

        float latitude=0;
        float longitude=0;

        if(savedInstanceState!=null){
            latitude = savedInstanceState.getFloat(EXTRA_LAST_LATITUDE,0);
            longitude = savedInstanceState.getFloat(EXTRA_LAST_LONGITUDE,0);
        }

        LatLng lastLatLng = null;

        if(latitude!=0 && longitude!=0 )
            lastLatLng = new LatLng(latitude,longitude);

        return lastLatLng;
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownPlace = null;
                //getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

}
