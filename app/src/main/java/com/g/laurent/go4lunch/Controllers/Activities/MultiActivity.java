package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.g.laurent.go4lunch.Controllers.Fragments.ListMatesFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.RestoFragment;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.Callback_DetailResto;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Views.GlideApp;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Calendar;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MultiActivity extends AppCompatActivity implements AlarmReceiver.callbackAlarm,
        Callback_DetailResto, Callback_resto_fb, NavigationView.OnNavigationItemSelectedListener {

    /** The activity MultiActivity contains :
     *     - the MapsFragment to display the mapView
     *     - the ListRestoFragment to display the list of restaurant
     *     - the RestoFragment to show the detailed view of a restaurant
     *     **/

    private LatLng currentPlaceLatLng;
    private Bundle bundle;
    private LatLng lastKnownPlace;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private final static String BUTTON_MAP_SELECTED = "button_map_selected";
    private final static String BUTTON_LIST_SELECTED = "button_list_view_selected";
    private final static String BUTTON_MATES_SELECTED = "button_workmates_selected";
    private final static String EXTRA_PLACE_ID = "placeId_resto";
    private static final int SIGN_OUT_TASK = 10;
    private String BUTTON_SELECTED;
    private Callback_DetailResto mCallback_detailResto;
    private final String EXTRA_API_KEY = "api_key";
    private String api_key;
    private FirebaseUser mCurrentUser;
    private ListRestoFragment listRestoFragment;
    @BindView(R.id.map_view_button) Button buttonMap;
    @BindView(R.id.list_view_button) Button buttonList;
    @BindView(R.id.workmates_button) Button buttonMates;
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_toolbar) Toolbar toolbar;
    private ImageView picture_user;
    private TextView name_user;
    private TextView email_user;
   // @BindView(R.id.current_user_email_drawer)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Context context = getApplicationContext();
        ButterKnife.bind(this);

        // Save current user to Firebase storage
        FirebaseApp.initializeApp(context);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        Firebase_update firebase_update = new Firebase_update(context);

        if(mCurrentUser!=null)
            firebase_update.create_new_user_firebase(mCurrentUser);

        api_key=getResources().getString(R.string.google_maps_key);

        mCallback_detailResto = this;
        //lastKnownPlace=findLastPlaceHighestLikelihood(savedInstanceState);
        currentPlaceLatLng=new LatLng(48.866667,2.333333);
        bundle = new Bundle();
        bundle.putDouble(EXTRA_LAT_CURRENT,48.866667);
        bundle.putDouble(EXTRA_LONG_CURRENT,2.333333);

        this.configure_tabs();
        this.configureDrawerLayout();
        this.configureNavigationView();
        //configureAlarmManager();

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

        // Create new bundle
        bundle.putString(EXTRA_API_KEY,api_key);

        configureToolBar("I'm hungry!",true);

        MapsFragment mapsFragment = new MapsFragment();
        mapsFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map_view, mapsFragment);
        fragmentTransaction.commit();
    }

    public void configure_and_show_ListRestoFragment(){

        configureToolBar("I'm hungry!",true);
        listRestoFragment = new ListRestoFragment();
        listRestoFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map_view, listRestoFragment);
        fragmentTransaction.commit();
    }

    public void configure_and_show_settings_fragment(){
        configureToolBar("Settings",true);
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
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
    // -----------------     CONFIGURATION OF TOOLBAR  ---------------------------------
    // ---------------------------------------------------------------------------------

    private void configureToolBar(String title, Boolean bar_display){

        runOnUiThread(() -> {
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
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
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

    // ---------------------------------------------------------------------------------
    // -----------------     CONFIGURATION OF DRAWER  ----------------------------------
    // ---------------------------------------------------------------------------------

    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView(){
        navigationView.setNavigationItemSelectedListener(this);

        if(mCurrentUser!=null){

            picture_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_image_drawer);
            name_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_name_drawer);
            email_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_email_drawer);

            name_user.setText(mCurrentUser.getDisplayName());
            email_user.setText(mCurrentUser.getEmail());

            if(mCurrentUser.getPhotoUrl()!=null)
                Glide.with(this)
                        .load(mCurrentUser.getPhotoUrl().toString())
                        .into(picture_user);

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.activity_main_drawer_your_lunch :
                Firebase_recover firebase_recover = new Firebase_recover(getApplicationContext(),this,mCurrentUser.getUid());
                firebase_recover.show_lunch_current_user();
                break;
            case R.id.activity_main_drawer_settings:
                configure_and_show_settings_fragment();
                break;
            case R.id.activity_main_drawer_logout:
                signOutUserFromFirebase();
                break;
            default:
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> {
            switch (origin){
                case SIGN_OUT_TASK:
                    finish();
                    break;
                default:
                    break;
            }
        };
    }

    // ----------------------------------------------------------------------------------------------------
    // -------------------------------------- CONFIGURE TABS ----------------------------------------------
    // ----------------------------------------------------------------------------------------------------

    private void configure_tabs(){

        BUTTON_SELECTED=BUTTON_MAP_SELECTED;
        setButtonAsSelected(true, buttonMap);
        setButtonAsSelected(false, buttonList);
        setButtonAsSelected(false, buttonMates);

        buttonMap.setOnClickListener(v -> {

            if(!BUTTON_SELECTED.equals(BUTTON_MAP_SELECTED)) {
                setButtonAsSelected(true, buttonMap);
                setButtonAsSelected(false, buttonList);
                setButtonAsSelected(false, buttonMates);
                BUTTON_SELECTED=BUTTON_MAP_SELECTED;
                configure_and_show_MapsFragment();
            }
        });

        buttonList.setOnClickListener(v -> {

            if(!BUTTON_SELECTED.equals(BUTTON_LIST_SELECTED)) {
                setButtonAsSelected(false, buttonMap);
                setButtonAsSelected(true, buttonList);
                setButtonAsSelected(false, buttonMates);
                BUTTON_SELECTED=BUTTON_LIST_SELECTED;
                configure_and_show_ListRestoFragment();
            }
        });

        buttonMates.setOnClickListener(v -> {

            if(!BUTTON_SELECTED.equals(BUTTON_MATES_SELECTED)) {
                setButtonAsSelected(false, buttonMap);
                setButtonAsSelected(false, buttonList);
                setButtonAsSelected(true, buttonMates);
                BUTTON_SELECTED=BUTTON_MATES_SELECTED;
                configure_and_show_listmatesfragment();
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

    @Override
    public void create_new_list_nearby_places() {

    }

    public ListRestoFragment getListRestoFragment() {
        return listRestoFragment;
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