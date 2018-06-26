package com.g.laurent.go4lunch.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import static android.content.ContentValues.TAG;


@SuppressLint("Registered")
public class Google_Maps_Utils extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private PlaceDetectionClient mPlaceDetectionClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private boolean mLocationPermissionGranted = true;
    private LatLng currentPlaceLatLng;
    private Context context;
    private MultiActivity activity;
    private Toolbar_navig_Utils toolbar_navig_utils;

    public Google_Maps_Utils(Context context, MultiActivity activity, Toolbar_navig_Utils toolbar_navig_utils) {

        this.context = context;
        this.activity = activity;
        this.toolbar_navig_utils=toolbar_navig_utils;

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(context);
    }

    public void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getNumberResults(activity);

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            currentPlaceLatLng = find_last_current_location();
            activity.setCurrentPlaceLatLng(currentPlaceLatLng);
        }
    }

    private void getNumberResults(MultiActivity activity) {

        mPlaceDetectionClient = Places.getPlaceDetectionClient(activity);

        if (mLocationPermissionGranted) {

            try {

                final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
                placeResult.addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {

                        // Recover the latitude and longitude of current location
                        currentPlaceLatLng=findPlaceHighestLikelihood(task);

                        System.out.println("eee  currentPlaceLatLng="+currentPlaceLatLng);

                        // Stop swipe to refresh
                        activity.getSwipeRefreshLayout().setEnabled(false);

                    } else {
                        currentPlaceLatLng =null;
                        Toast toast = Toast.makeText(context,"ERROR to find current location" ,Toast.LENGTH_LONG);
                        toast.show();
                    }

                    if(currentPlaceLatLng!=null)
                        save_last_current_location(currentPlaceLatLng);
                    else
                        currentPlaceLatLng = find_last_current_location();

                    activity.setCurrentPlaceLatLng(currentPlaceLatLng);
                });

            } catch (SecurityException e) {

                currentPlaceLatLng = find_last_current_location();
                activity.setCurrentPlaceLatLng(currentPlaceLatLng);

                Toast toast = Toast.makeText(context,"Error to find current location \n" + e.toString() ,Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            currentPlaceLatLng = find_last_current_location();
            activity.setCurrentPlaceLatLng(currentPlaceLatLng);
        }

        activity.getSwipeRefreshLayout().setEnabled(false);
    }

    private LatLng find_last_current_location(){

        activity.getSharedPreferences().edit().putFloat(EXTRA_LAT_CURRENT,(float) 48.866667).apply();
        activity.getSharedPreferences().edit().putFloat(EXTRA_LONG_CURRENT,(float) 2.333333).apply();

        Float latitude = activity.getSharedPreferences().getFloat(EXTRA_LAT_CURRENT,0);
        Float longitude = activity.getSharedPreferences().getFloat(EXTRA_LONG_CURRENT,0);

        LatLng last_location;

        if(latitude == 0 && longitude == 0)
            last_location=new LatLng(48.866667, 2.333333);
        else
            last_location = new LatLng(latitude,longitude);

        return last_location;
    }

    private void save_last_current_location(LatLng current_loc){
        activity.getSharedPreferences().edit().putFloat(EXTRA_LAT_CURRENT,(float) current_loc.latitude).apply();
        activity.getSharedPreferences().edit().putFloat(EXTRA_LONG_CURRENT,(float) current_loc.longitude).apply();
    }

    private LatLng findPlaceHighestLikelihood(@NonNull Task<PlaceLikelihoodBufferResponse> task){

        Place placeHighestLikelihood = null;
        float percentage = 0;

        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

        for (PlaceLikelihood placeLikelihood : likelyPlaces) {       // for each placeLikelihood

            if(placeLikelihood.getLikelihood() > percentage) {       // if the likelihood is higher than the other ones,
                placeHighestLikelihood = placeLikelihood.getPlace(); //   set this place as placeLikelihood
                percentage = placeLikelihood.getLikelihood();        //   set this percentage as highest likelihood
            }
        }

        LatLng current_location=null;

        if(placeHighestLikelihood!=null)
            current_location = placeHighestLikelihood.getLatLng();

        likelyPlaces.release();

        return current_location;
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

        results.addOnSuccessListener(autocompletePredictions -> {

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

        });
    }

    public void get_list_places_prediction(String query, LatLngBounds bounds){

        GeoDataClient mGeoDataClient = Places.getGeoDataClient(context);

        // Filter to select only professionnals addresses
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .build();

        Task<AutocompletePredictionBufferResponse> results =
                mGeoDataClient.getAutocompletePredictions(query, bounds, GeoDataClient.BoundsMode.STRICT, typeFilter);

        results.addOnSuccessListener(autocompletePredictions -> {

            try {
                // Freeze the results immutable representation that can be stored safely.
                ArrayList<AutocompletePrediction> al = DataBufferUtils.freezeAndClose(autocompletePredictions);
                toolbar_navig_utils.display_list_predictions(al);

            } catch (RuntimeExecutionException e) {
                // If the query did not complete successfully return null
                Log.e(TAG, "Error getting autocomplete prediction API call", e);
            }

        });
    }

}