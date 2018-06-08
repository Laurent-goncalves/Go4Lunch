package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.widget.Toast;

import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT;

public class Google_Maps_Utils extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private PlaceDetectionClient mPlaceDetectionClient;
    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = true;
    private LatLng lastKnownPlace;
    private Place currentPlace;
    private LatLng currentPlaceLatLng;
    private Context context;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public Google_Maps_Utils(Context context) {

        this.context = context;

     /*   mGoogleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();*/
    }

    @Override
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
        updateLocationUI();
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

    private void updateLocationUI() {
    /*    if (mMap == null) {
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
        }*/
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

        System.out.println("eee  api_key=" + api_key + "    query=" + query + "     bounds=" + bounds);

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

                    System.out.println("eee  list_places_nearby.size()=" + list_places_nearby.size());
                    System.out.println("eee  list_places_nearby=" + list_places_nearby.toString());

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

       /* try {
            Thread.sleep(3000);
            // Tasks.await(results, 60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }


}



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