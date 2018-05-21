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
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.CallbackMapsActivity;
import com.g.laurent.go4lunch.Models.Callback_DetailResto;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class MultiActivity extends AppCompatActivity implements CallbackMapsActivity,AlarmReceiver.callbackAlarm,
        Callback_DetailResto, Callback_resto_fb, NavigationView.OnNavigationItemSelectedListener {

    /** The activity MultiActivity contains :
     *     - the MapsFragment to display the mapView
     *     - the ListRestoFragment to display the list of restaurant
     *     - the RestoFragment to show the detailed view of a restaurant
     *     **/

    private List_Search_Nearby mList_search_nearby;
    private LatLng currentPlaceLatLng;
    private LatLng lastKnownPlace;
    private List<Place_Nearby> list_search_nearby;
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
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        mCallbackMapsActivity=this;
        mCallback_detailResto = this;
        //lastKnownPlace=findLastPlaceHighestLikelihood(savedInstanceState);



        currentPlaceLatLng=new LatLng(48.866667,2.333333);



       // create_new_list_nearby_places();

        this.configure_tabs();
        this.configureDrawerLayout();
        this.configureNavigationView();
    //  configureAlarmManager();

        configure_and_show_MapsFragment();

    }

    // ---------------------------------------------------------------------------------
    // ------------------------ CREATE NEW LIST OF RESTAURANTS -------------------------
    // ---------------------------------------------------------------------------------

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
    public void create_new_list_nearby_places() {
        mList_search_nearby = new List_Search_Nearby(currentPlaceLatLng,"500",mCallbackMapsActivity);
    }


   /* private void save_current_user_in_Firebase(){

        // Initialization
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("workmates");

        // Create workmates
        if(mCurrentUser!=null){

            String Url_photo = null;

            if(mCurrentUser.getPhotoUrl()!=null)
                Url_photo = mCurrentUser.getPhotoUrl().toString();

            Workmates workmates = new Workmates(
                    mCurrentUser.getDisplayName(),
                    mCurrentUser.getUid(),
                    Url_photo,
                    false, null,null,null,null);

            mDatabase.child(mCurrentUser.getUid()).setValue(workmates);
        }
    }*/



    public void update_list_nearby_places_firebase(List<Place_Nearby> list_search_nearby){


    }

    // ---------------------------------------------------------------------------------
    // -------------------         CONFIGURATION OF FRAGMENTS         ------------------
    // ---------------------------------------------------------------------------------

    public void configure_and_show_listmatesfragment(){
        configureToolBar("available workmates",true);
        ListMatesFragment listMatesFragment = new ListMatesFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map_view, listMatesFragment);
        fragmentTransaction.commit();
    }

    public void configure_and_show_MapsFragment(){

        Bundle bundle = new Bundle();
        bundle.putDouble(EXTRA_LAT_CURRENT,currentPlaceLatLng.latitude);
        bundle.putDouble(EXTRA_LONG_CURRENT,currentPlaceLatLng.longitude);

        configureToolBar("I'm hungry!",true);

        MapsFragment mapsFragment = new MapsFragment();
        mapsFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map_view, mapsFragment);
        fragmentTransaction.commit();
    }

    public void configure_and_show_ListRestoFragment(){
        Bundle bundle = new Bundle();

        bundle.putDouble(EXTRA_LAT_CURRENT,currentPlaceLatLng.latitude);
        bundle.putDouble(EXTRA_LONG_CURRENT,currentPlaceLatLng.longitude);

        configureToolBar("I'm hungry!",true);

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

        // Configuration toolbar
        configureToolBar(null,false);

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        fragmentTransaction.replace(R.id.fragment_map_view, restoFragment);
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    // ---------------------------------------------------------------------------------
    // -----------------     CONFIGURATION OF DRAWER AND TOOLBAR        ----------------
    // ---------------------------------------------------------------------------------

    private void configureToolBar(String title, Boolean bar_display){
        if(bar_display){
            toolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null) {
                actionBar.setTitle(title);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } else
            toolbar.setVisibility(View.GONE);
    }

    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView(){
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected( MenuItem item) {
        //this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("eee button HOME 0");
        switch (item.getItemId()) {
            case android.R.id.home:
                System.out.println("eee button HOME1");
                //drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return true;
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

    // ----------------------------------------------------------------------------------------------------
    // -------------------------------------- CONFIGURE TABS ----------------------------------------------
    // ----------------------------------------------------------------------------------------------------

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



    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(lastKnownPlace!=null){
            outState.putFloat(EXTRA_LAT_CURRENT, (float) lastKnownPlace.latitude);
            outState.putFloat(EXTRA_LONG_CURRENT, (float) lastKnownPlace.longitude);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void update_chosen_list_restos(List<Place_Nearby> list_restos) {

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