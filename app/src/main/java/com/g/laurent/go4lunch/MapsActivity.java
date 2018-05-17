package com.g.laurent.go4lunch;

import android.app.AlarmManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.CallbackMapsActivity;
import com.g.laurent.go4lunch.Models.Callback_DetailResto;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import static android.content.ContentValues.TAG;


public class MapsActivity extends AppCompatActivity implements CallbackMapsActivity,AlarmReceiver.callbackAlarm, GoogleApiClient.OnConnectionFailedListener, Callback_DetailResto {//implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = true;
    private List_Search_Nearby mList_search_nearby;
    private LatLng lastKnownPlace;
    private Place currentPlace;
    private LatLng currentPlaceLatLng;
    private List<Place_Nearby> list_search_nearby;
    int count_id;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private final static String BUTTON_MAP_SELECTED = "button_map_selected";
    private final static String BUTTON_LIST_SELECTED = "button_list_view_selected";
    private final static String BUTTON_MATES_SELECTED = "button_workmates_selected";
    private final static String EXTRA_PLACE_ID = "placeId_resto";
    private String BUTTON_SELECTED;
    private CallbackMapsActivity mCallbackMapsActivity;
    private Callback_DetailResto mCallback_detailResto;
    @BindView(R.id.map_view_button) Button buttonMap;
    @BindView(R.id.list_view_button) Button buttonList;
    @BindView(R.id.workmates_button) Button buttonMates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        mCallbackMapsActivity=this;
        mCallback_detailResto = this;
        lastKnownPlace=findLastPlaceHighestLikelihood(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        currentPlaceLatLng=new LatLng(48.866667,2.333333);



       // create_new_list_nearby_places();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        create_new_list_nearby_places();
        configure_tabs();
        //configureAlarmManager();
        //configure_and_show_ListRestoFragment();

        /*
        configure_and_show_MapsFragment();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();*/

    }



    @Override
    public void create_new_list_nearby_places() {
        mList_search_nearby = new List_Search_Nearby(currentPlaceLatLng,"500",mCallbackMapsActivity);
    }

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                return;
            }

            // if success, store the bitmap picture on Firebase storage
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            placePhotoResult.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] data = baos.toByteArray();

            // Create a reference
            String placeId = list_search_nearby.get(count_id).getPlaceId();
            StorageReference mountainsRef = storageRef.child(placeId + ".jpg");
            count_id++;

            UploadTask uploadTask = mountainsRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }
    };


    private void getPlacePhotosAsync(final String placeId) {

        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId)
            .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {

                @Override
                public void onResult(PlacePhotoMetadataResult photos) {
                    if (!photos.getStatus().isSuccess())
                        return;

                    PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                    if (photoMetadataBuffer.getCount() > 0) {
                        photoMetadataBuffer.get(0)
                                .getScaledPhoto(mGoogleApiClient, 100,100)
                                .setResultCallback(mDisplayPhotoResultCallback);
                    }
                    photoMetadataBuffer.release();
                }
            });
    }

    public void update_list_nearby_places_firebase(List<Place_Nearby> list_search_nearby){

        this.list_search_nearby=list_search_nearby;
        FirebaseApp.initializeApp(getApplicationContext());
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("restaurants");
        mDatabase.removeValue();
        count_id=0;

        if(list_search_nearby!=null) {
            for(Place_Nearby place : list_search_nearby) {
                mDatabase.child(place.getPlaceId()).setValue(place);
                getPlacePhotosAsync(place.getPlaceId());
            }
        }
       /* Toast toast = Toast.makeText(getApplicationContext(),"FireBase mis à jour",Toast.LENGTH_SHORT);
        toast.show();*/
    }

    public void configure_and_show_listmatesfragment(){
        ListMatesFragment listMatesFragment = new ListMatesFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map_view, listMatesFragment);
        fragmentTransaction.commit();
    }

    public void configure_and_show_MapsFragment(){
        MapsFragment mapsFragment = new MapsFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map_view, mapsFragment);
        fragmentTransaction.commit();
    }

    public void configure_and_show_ListRestoFragment(){
        Bundle bundle = new Bundle();

        bundle.putDouble(EXTRA_LAT_CURRENT,currentPlaceLatLng.latitude);
        bundle.putDouble(EXTRA_LONG_CURRENT,currentPlaceLatLng.longitude);

        ListRestoFragment listRestoFragment = new ListRestoFragment();
        listRestoFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map_view, listRestoFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void configure_and_show_restofragment(String placeId) {

        // Create new bundle
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PLACE_ID,placeId);

        // Create new fragment and transaction
        RestoFragment restoFragment = new RestoFragment();
        restoFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        fragmentTransaction.replace(R.id.fragment_map_view, restoFragment);
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    private void configureAlarmManager(){

        // Configuration of alarm for saving feeling each day
        AlarmReceiver.callbackAlarm mcallbackAlarm=this;
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        alarmReceiver.createCallbackAlarm(mcallbackAlarm);

        Intent alarmIntent = new Intent(getApplicationContext(), alarmReceiver.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the alarm to start at 12:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE,0);

        // Create alarm to ring it every day at noon
        AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if(manager!=null)
            manager.setRepeating(AlarmManager.RTC,calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        return true;
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

/*    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng corbeil = new LatLng(48.6102599, 2.474805);
        mMap.addMarker(new MarkerOptions().position(corbeil).title("Marker in corbeil"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(corbeil));




      // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        getNumberResults();

    }*/

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
                    configure_and_show_MapsFragment();
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
                    configure_and_show_ListRestoFragment();
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
                    configure_and_show_listmatesfragment();
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
            outState.putFloat(EXTRA_LAT_CURRENT, (float) lastKnownPlace.latitude);
            outState.putFloat(EXTRA_LONG_CURRENT, (float) lastKnownPlace.longitude);
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
            latitude = savedInstanceState.getFloat(EXTRA_LAT_CURRENT,0);
            longitude = savedInstanceState.getFloat(EXTRA_LONG_CURRENT,0);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("eee PROBLEM " );
    }



    /*
    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_main_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        updateUIWithUserInfo(position);
                    }
                });
    }*/

}
