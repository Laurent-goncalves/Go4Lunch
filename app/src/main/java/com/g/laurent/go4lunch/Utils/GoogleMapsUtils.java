package com.g.laurent.go4lunch.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.ListSearchNearby;
import com.g.laurent.go4lunch.R;
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


@SuppressLint("Registered")
public class GoogleMapsUtils extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private PlaceDetectionClient mPlaceDetectionClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private boolean mLocationPermissionGranted = true;
    private LatLng currentPlaceLatLng;
    private Context context;
    private MultiActivity activity;
    private ToolbarNavigUtils mToolbar_navig_utils;

    public GoogleMapsUtils(Context context, MultiActivity activity, ToolbarNavigUtils toolbar_navig_utils) {

        this.context = context;
        this.activity = activity;
        this.mToolbar_navig_utils =toolbar_navig_utils;

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

                        if(currentPlaceLatLng.longitude==-122.08418830000001)
                            currentPlaceLatLng=new LatLng(48.866667, 2.333333);

                        // Stop swipe to refresh
                        activity.getSwipeRefreshLayout().setEnabled(false);

                    } else {
                        currentPlaceLatLng =null;
                        Toast toast = Toast.makeText(context,context.getResources().getString(R.string.error_current_location),Toast.LENGTH_LONG);
                        toast.show();
                    }

                    if(currentPlaceLatLng!=null) {
                        save_last_current_location(currentPlaceLatLng);
                        activity.setCurrentPlaceLatLng(currentPlaceLatLng);
                    } else
                        set_current_location_by_default();
                });

            } catch (SecurityException e) {

                set_current_location_by_default();

                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.error_current_location)
                        + "\n" + e.toString() ,Toast.LENGTH_LONG);
                toast.show();
            }
        } else
            set_current_location_by_default();

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

    private void set_current_location_by_default(){
        currentPlaceLatLng = find_last_current_location();
        activity.setCurrentPlaceLatLng(currentPlaceLatLng);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast toast = Toast.makeText(context,context.getResources().getString(R.string.error_geolocalization),Toast.LENGTH_SHORT);
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
                    new ListSearchNearby(context,api_key,list_places_nearby,listRestoFragment);
                else if(mapsFragment!=null)
                    new ListSearchNearby(context, api_key,list_places_nearby,mapsFragment);

            } catch (RuntimeExecutionException e) {
                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.error_getting_autocomplete_predic),Toast.LENGTH_SHORT);
                toast.show();
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
                mToolbar_navig_utils.display_list_predictions(al);

            } catch (RuntimeExecutionException e) {
                Toast toast = Toast.makeText(context,context.getResources().getString(R.string.error_getting_autocomplete_predic),Toast.LENGTH_SHORT);
                toast.show();
            }

        });
    }

}