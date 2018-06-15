package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import static android.content.ContentValues.TAG;


public class Google_Maps_Utils extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private PlaceDetectionClient mPlaceDetectionClient;
    private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = true;
    private LatLng lastKnownPlace;
    private Place currentPlace;
    private LatLng currentPlaceLatLng;
    private Context context;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private Location mLastKnownLocation;
    private MultiActivity activity;
    private Toolbar_navig_Utils toolbar_navig_utils;

    public Google_Maps_Utils(Context context, MultiActivity activity, Toolbar_navig_Utils toolbar_navig_utils) {

        this.context = context;
        this.activity = activity;
        this.toolbar_navig_utils=toolbar_navig_utils;
        // Construct a GeoDataClient.
        //mGeoDataClient = Places.getGeoDataClient(context, null);

        // Construct a PlaceDetectionClient.
        //mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        //mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationPermission();

        // Get the current location of the device and set the position of the map.
        //getDeviceLocation();

    }



    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //getNumberResults();

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void getNumberResults(MultiActivity activity) {

        mPlaceDetectionClient = Places.getPlaceDetectionClient(activity);

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

                            Toast toast;

                            if(currentPlaceLatLng!=null)
                                toast = Toast.makeText(context,"current location=" + currentPlaceLatLng.latitude +"   " + currentPlaceLatLng.longitude,Toast.LENGTH_LONG);
                            else
                                toast = Toast.makeText(context,"current location=null" ,Toast.LENGTH_LONG);

                            toast.show();

                            activity.getSwipeRefreshLayout().setEnabled(false);
                            //System.out.println("eee current location=" + currentPlaceLatLng.latitude +"   " + currentPlaceLatLng.longitude);

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

    private void getDeviceLocation() {

        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();

                        } else {
                            Toast toast = Toast.makeText(context,"Problem to get current location",Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Toast toast = Toast.makeText(context,"Problem to get current location\n" + e.getMessage(),Toast.LENGTH_SHORT);
            toast.show();
        }
    }




    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast toast = Toast.makeText(context,"Problem with geolocalization",Toast.LENGTH_SHORT);
        toast.show();
    }


    public void googleplacespredictions(String api_key, String query, LatLngBounds bounds, ListRestoFragment listRestoFragment, MapsFragment mapsFragment){

        List<String> list_places_nearby = new ArrayList<>();
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(context);

        // Filter to select only professionnals addresses
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        Task<AutocompletePredictionBufferResponse> results =
                mGeoDataClient.getAutocompletePredictions(query, bounds, GeoDataClient.BoundsMode.STRICT, typeFilter);

        results.addOnSuccessListener(new OnSuccessListener<AutocompletePredictionBufferResponse>() {
            @Override
            public void onSuccess(AutocompletePredictionBufferResponse autocompletePredictions) {

                try {
                    // Freeze the results immutable representation that can be stored safely.
                    ArrayList<AutocompletePrediction> al = DataBufferUtils.freezeAndClose(autocompletePredictions);

                    for (AutocompletePrediction p : al) {
                        list_places_nearby.add(p.getPlaceId());
                    }

                    if(listRestoFragment!=null)
                        new List_Search_Nearby(api_key,list_places_nearby,listRestoFragment);
                    else if(mapsFragment!=null)
                        new List_Search_Nearby(api_key,list_places_nearby,mapsFragment);

                } catch (RuntimeExecutionException e) {
                    // If the query did not complete successfully return null
                    Log.e(TAG, "Error getting autocomplete prediction API call", e);
                }

            }
        });
    }

    public void get_list_places_prediction(String query, LatLngBounds bounds){

        List<String> list_places_nearby = new ArrayList<>();
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(context);

        // Filter to select only professionnals addresses
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        Task<AutocompletePredictionBufferResponse> results =
                mGeoDataClient.getAutocompletePredictions(query, bounds, GeoDataClient.BoundsMode.STRICT, typeFilter);

        results.addOnSuccessListener(new OnSuccessListener<AutocompletePredictionBufferResponse>() {
            @Override
            public void onSuccess(AutocompletePredictionBufferResponse autocompletePredictions) {

                try {
                    // Freeze the results immutable representation that can be stored safely.
                    ArrayList<AutocompletePrediction> al = DataBufferUtils.freezeAndClose(autocompletePredictions);
                    toolbar_navig_utils.display_list_predictions(al);

                } catch (RuntimeExecutionException e) {
                    // If the query did not complete successfully return null
                    Log.e(TAG, "Error getting autocomplete prediction API call", e);
                }

            }
        });
    }


}


/*  @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

    }*/




   /* private void updateLocationUI() {
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
    }*/




  /*  private LatLng findLastPlaceHighestLikelihood(Bundle savedInstanceState) {

        float latitude=0;
        float longitude=0;

        if(savedInstanceState!=null){
            latitude = savedInstanceState.getFloat(EXTRA_LAT_CURRENT,0);
            longitude = savedInstanceState.getFloat(EXTRA_LONG_CURRENT,0);
        }

        LatLng lastLatLng = null;

        if(latitude!=0 && longitude!=0 )
            lastLatLng = new LatLng(latitude,longitude);

        return lastLatLng;
    }*/