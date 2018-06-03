package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Views.MultiFragAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MultiActivity extends AppCompatActivity implements AlarmReceiver.callbackAlarm,
        Callback_resto_fb, NavigationView.OnNavigationItemSelectedListener {

    private LatLng lastKnownPlace;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private static final int SIGN_OUT_TASK = 10;
    private final String EXTRA_API_KEY = "api_key";
    private String api_key;
    private FirebaseUser mCurrentUser;
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_toolbar) Toolbar toolbar;
    private TabLayout tabs;

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

        api_key=getResources().getString(R.string.google_maps_key2);

        //lastKnownPlace=findLastPlaceHighestLikelihood(savedInstanceState);

        Bundle bundle = new Bundle();
        bundle.putDouble(EXTRA_LAT_CURRENT,48.866667);
        bundle.putDouble(EXTRA_LONG_CURRENT,2.333333);

        this.configureDrawerLayout();
        this.configureNavigationView();
        //configureAlarmManager();
        configureViewPagerAndTabs();

        SwipeRefreshLayout swipeRefreshLayout = this.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                this::configureViewPagerAndTabs
        );

    }

    private void configureViewPagerAndTabs(){

        // Get ViewPager from layout
        ViewPager pager = findViewById(R.id.viewpager);

        MultiFragAdapter pageAdapter = new MultiFragAdapter(getSupportFragmentManager(), api_key, getApplicationContext());
        pager.setAdapter(pageAdapter);

        // Get TabLayout from layout
        tabs= findViewById(R.id.activity_multi_tabs);
        tabs.setupWithViewPager(pager);

        // Glue TabLayout and ViewPager together
        configure_tabs();

        // Design purpose. Tabs have the same width
        tabs.setTabMode(TabLayout.MODE_FIXED);
    }

    private void configure_tabs(){

        // Initialize tabs
        Objects.requireNonNull(tabs.getTabAt(0)).setIcon(R.drawable.baseline_map_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconSelected), PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(tabs.getTabAt(1)).setIcon(R.drawable.baseline_view_list_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(1)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconNotSelected),PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(tabs.getTabAt(2)).setIcon(R.drawable.baseline_people_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(2)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconNotSelected),PorterDuff.Mode.SRC_IN);

        // configure toolbar
        configureToolBar("I'm hungry",true);

        // Set on Tab selected listener
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
              if(tab.getIcon()!=null)
              tab.getIcon().setColorFilter(getResources().getColor(R.color.colorIconSelected),PorterDuff.Mode.SRC_IN);

              if(Objects.requireNonNull(tab.getText()).equals(getResources().getString(R.string.workmates)))
                  configureToolBar("Available workmates",true);
              else
                  configureToolBar("I'm hungry!",true);
          }

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {
              if(tab.getIcon()!=null)
              tab.getIcon().setColorFilter(getResources().getColor(R.color.colorIconNotSelected), PorterDuff.Mode.SRC_IN);
          }

          @Override
          public void onTabReselected(TabLayout.Tab tab) {
          }
      });
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

    public void configure_and_show_settings_fragment(){
        configureToolBar("Settings",true);
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
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

            ImageView picture_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_image_drawer);
            TextView name_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_name_drawer);
            TextView email_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_email_drawer);

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

    @Override
    protected void onRestart() {
        super.onRestart();
        configureViewPagerAndTabs();
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


  /*  public void configure_and_show_listmatesfragment(){
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
    }*/