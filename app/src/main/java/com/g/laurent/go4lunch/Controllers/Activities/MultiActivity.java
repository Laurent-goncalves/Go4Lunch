package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.CallbackMultiActivity;
import com.g.laurent.go4lunch.Models.Callback_alarm;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.DistanceCalculation;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Utils.Google_Maps_Utils;
import com.g.laurent.go4lunch.Views.MultiFragAdapter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;


public class MultiActivity extends AppCompatActivity implements Callback_resto_fb,
        NavigationView.OnNavigationItemSelectedListener, CallbackMultiActivity, SwipeRefreshLayout.OnRefreshListener {

    private LatLng lastKnownPlace;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_USER_ID = "user_id_alarm";
    private static final int SIGN_OUT_TASK = 10;
    private final String EXTRA_API_KEY = "api_key";
    private String api_key;
    private FirebaseUser mCurrentUser;
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_toolbar) Toolbar toolbar;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout tabs;
    private LatLng currentPlaceLatLng;
    private MultiFragAdapter pageAdapter;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private int current_page;
    private ViewPager pager;
    private SearchView searchView;
    private MenuItem searchItem;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private SharedPreferences sharedPreferences;
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_RESTO_JSON = "resto_to_json";
    private final static String EXTRA_RESTO_DETAILS = "resto_details";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";
    private static final String EXTRA_ENABLE_NOTIF = "enable_notif";

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
        sharedPreferences = getSharedPreferences(EXTRA_PREFERENCES, MODE_PRIVATE);
        current_page = 0;

        if (mCurrentUser != null)
            firebase_update.create_new_user_firebase(mCurrentUser);

        api_key = getResources().getString(R.string.google_maps_key2);

        //lastKnownPlace=findLastPlaceHighestLikelihood(savedInstanceState);

        this.configureDrawerLayout();
        this.configureNavigationView();
        this.configureAlarmManager();
        currentPlaceLatLng = new LatLng(48.866667, 2.333333);



        String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS,500));
        String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE,"restaurant");

        new List_Search_Nearby(api_key, currentPlaceLatLng, radius, type, this);

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void configureViewPagerAndTabs(List<Place_Nearby> list_restos) {

        // Get ViewPager from layout
        pager = findViewById(R.id.viewpager);

        pageAdapter = new MultiFragAdapter(getSupportFragmentManager(), api_key, getApplicationContext(), list_restos);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pager.setAdapter(pageAdapter);

                // Get TabLayout from layout
                tabs = findViewById(R.id.activity_multi_tabs);
                tabs.setupWithViewPager(pager);

                // Glue TabLayout and ViewPager together
                configure_tabs();

                // Design purpose. Tabs have the same width
                tabs.setTabMode(TabLayout.MODE_FIXED);

                // Select the last tab selected before eventual refresh and stop the refresh
                Objects.requireNonNull(tabs.getTabAt(current_page)).select();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                current_page = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void configure_tabs() {

        // Initialize tabs
        Objects.requireNonNull(tabs.getTabAt(0)).setIcon(R.drawable.baseline_map_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconSelected), PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(tabs.getTabAt(1)).setIcon(R.drawable.baseline_view_list_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(1)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconNotSelected), PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(tabs.getTabAt(2)).setIcon(R.drawable.baseline_people_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(2)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconNotSelected), PorterDuff.Mode.SRC_IN);

        // configure toolbar
        configureToolBar(getResources().getString(R.string.toolbar_mapview), true);
        swipeRefreshLayout.setEnabled(false);

        // Set on Tab selected listener
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Change color of the tab -> orange
                if (tab.getIcon() != null)
                    tab.getIcon().setColorFilter(getResources().getColor(R.color.colorIconSelected), PorterDuff.Mode.SRC_IN);

                // Change tab title if required
                if (Objects.requireNonNull(tab.getText()).equals(getResources().getString(R.string.workmates)))
                    configureToolBar(getResources().getString(R.string.available_workmates), true);
                else
                    configureToolBar(getResources().getString(R.string.toolbar_mapview), true);

                // Disable pull to refresh when mapView is displayed
                if(tab.getPosition()==0)
                    swipeRefreshLayout.setEnabled(false);
                else
                    swipeRefreshLayout.setEnabled(true);


                if (searchItem != null) {
                    searchItem.collapseActionView();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                // Change color of the tab -> black
                if (tab.getIcon() != null)
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

    public void configureAlarmManager() {

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra(EXTRA_USER_ID, mCurrentUser.getUid());
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        // Set the alarm to start at 12:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);

        // setRepeating() lets you specify a precise custom interval
        if (alarmMgr != null)
            alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);

    }

    // ---------------------------------------------------------------------------------
    // -------------------         CONFIGURATION OF SETTINGS         ------------------
    // ---------------------------------------------------------------------------------

    public void configure_and_show_settings_activity() {
        configureToolBar("Settings", true);
        Intent intent = new Intent(this, SettingActivity.class);
        int requestCode = 0;
        if (sharedPreferences != null) {
            if (sharedPreferences.getBoolean(EXTRA_ENABLE_NOTIF, false)) {
                requestCode = 1;
            } else {
                requestCode = 0;
            }
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // request code 1 = enable notif ; request code 0 = disable notif
        if (requestCode == 1) {
            if (resultCode == RESULT_CANCELED) { // if the user decided to disable notification, whereas it was enabled when opening settings
                if (alarmMgr != null)
                    alarmMgr.cancel(alarmIntent);
            }
        } else {
            if (resultCode == RESULT_FIRST_USER) { // if the user has enabled notif, whereas it was disabled when opening settings
                configureAlarmManager();
            }
        }
    }

    // ---------------------------------------------------------------------------------
    // -----------------     CONFIGURATION OF TOOLBAR  ---------------------------------
    // ---------------------------------------------------------------------------------

    private void configureToolBar(String title, Boolean bar_display) {

        runOnUiThread(() -> {
            if (bar_display) {
                toolbar.setVisibility(View.VISIBLE);
                setSupportActionBar(toolbar);
                android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
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

                if (searchItem != null) {
                    searchItem.collapseActionView();
                }
                return true;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String EXTRA_PREFERENCES = "preferences";
                SharedPreferences sharedPreferences = getSharedPreferences(EXTRA_PREFERENCES, MODE_PRIVATE);
                LatLng current_location = new LatLng(48.866667, 2.333333);
                int radius = sharedPreferences.getInt(EXTRA_PREF_RADIUS, 500);

                // Define bounds for search
                DistanceCalculation tool_calcul_distance = new DistanceCalculation();
                LatLngBounds bounds = tool_calcul_distance.create_LatLngBounds(radius, current_location);

                Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(getApplicationContext());

                switch (current_page) {
                    case 0:
                        google_maps_utils.googleplacespredictions(api_key, query, bounds, null, pageAdapter.getMapsFragment());
                        break;
                    case 1:
                        google_maps_utils.googleplacespredictions(api_key, query, bounds, pageAdapter.getListRestoFragment(), null);
                        break;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchView.setBackgroundColor(Color.WHITE);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                System.out.println("eee    onClose!!");
                switch (current_page) {
                    case 0:
                        pageAdapter.getMapsFragment().recover_previous_state();
                        break;
                    case 1:
                        pageAdapter.getListRestoFragment().recover_previous_state();
                        break;
                }
                return false;
            }
        });
        searchView.setIconifiedByDefault(true);

        return true;
    }

    // ---------------------------------------------------------------------------------
    // -----------------     CONFIGURATION OF DRAWER  ----------------------------------
    // ---------------------------------------------------------------------------------

    private void configureDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView() {
        navigationView.setNavigationItemSelectedListener(this);

        if (mCurrentUser != null) {

            ImageView picture_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_image_drawer);
            TextView name_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_name_drawer);
            TextView email_user = navigationView.getHeaderView(0).findViewById(R.id.current_user_email_drawer);

            name_user.setText(mCurrentUser.getDisplayName());
            email_user.setText(mCurrentUser.getEmail());

            if (mCurrentUser.getPhotoUrl() != null)
                Glide.with(this)
                        .load(mCurrentUser.getPhotoUrl().toString())
                        .apply(RequestOptions.circleCropTransform())
                        .into(picture_user);

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.activity_main_drawer_your_lunch:

                Intent intent = new Intent(getApplicationContext(), RestoActivity.class);
                String resto_json = sharedPreferences.getString(EXTRA_RESTO_JSON, null);
                intent.putExtra(EXTRA_RESTO_DETAILS, resto_json);
                startActivity(intent);

                break;
            case R.id.activity_main_drawer_settings:
                configure_and_show_settings_activity();
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

    private void signOutUserFromFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin) {
        return aVoid -> {
            switch (origin) {
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
        if (lastKnownPlace != null) {
            outState.putFloat(EXTRA_LAT_CURRENT, (float) lastKnownPlace.latitude);
            outState.putFloat(EXTRA_LONG_CURRENT, (float) lastKnownPlace.longitude);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void update_chosen_list_restos(List<Place_Nearby> list_restos) {

    }


    public MultiFragAdapter getPageAdapter() {
        return pageAdapter;
    }

    @Override
    public void onRefresh() {
        String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS,500));
        String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE,"restaurant");
        new List_Search_Nearby(api_key, currentPlaceLatLng, radius, type, this);
    }
}