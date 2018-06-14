package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.CallbackMultiActivity;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.DistanceCalculation;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Utils.Google_Maps_Utils;
import com.g.laurent.go4lunch.Utils.Toolbar_navig_Utils;
import com.g.laurent.go4lunch.Views.MultiFragAdapter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MultiActivity extends AppCompatActivity implements Callback_resto_fb,
         CallbackMultiActivity, SwipeRefreshLayout.OnRefreshListener {

    private LatLng lastKnownPlace;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_USER_ID = "user_id_alarm";

    private final String EXTRA_API_KEY = "api_key";
    private String api_key;
    private FirebaseUser mCurrentUser;
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    private Toolbar toolbar;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout tabs;
    private LatLng currentPlaceLatLng;
    private MultiFragAdapter pageAdapter;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private int current_page;
    private ViewPager pager;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private SharedPreferences sharedPreferences;
    private static final String EXTRA_PREFERENCES = "preferences";
    private Toolbar_navig_Utils toolbar_navig_utils;
    private final static String EXTRA_RESTO_DETAILS = "resto_details";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";
    private static final String EXTRA_ENABLE_NOTIF = "enable_notif";
    private Google_Maps_Utils google_maps_utils;


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

        google_maps_utils = new Google_Maps_Utils(getApplicationContext(),this,null);
        currentPlaceLatLng = new LatLng(48.866667, 2.333333);
        api_key = getResources().getString(R.string.google_maps_key2);

        // Configure toolbar and navigation drawer
        toolbar_navig_utils = new Toolbar_navig_Utils(this);
        toolbar_navig_utils.configure_toolbar();
        toolbar_navig_utils.configureNavigationView();


        if (mCurrentUser != null)
            firebase_update.create_new_user_firebase(mCurrentUser);



        //lastKnownPlace=findLastPlaceHighestLikelihood(savedInstanceState);


       // this.configureAlarmManager();



        tabs = findViewById(R.id.activity_multi_tabs);
        //tabs.setupWithViewPager(pager);

       // configure_tabs();


        //configureToolBar("I'm hungry!",true);
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

        runOnUiThread(() -> {
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
                    toolbar_navig_utils.getToolbar().setTitle(getResources().getString(R.string.available_workmates));
                else
                    toolbar_navig_utils.getToolbar().setTitle(getResources().getString(R.string.toolbar_mapview));

                // Disable pull to refresh when mapView is displayed
                if(tab.getPosition()==0)
                    swipeRefreshLayout.setEnabled(false);
                else
                    swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                // if the searchView is opened, close it
                if(!toolbar_navig_utils.getSearchView().isIconified())
                    toolbar_navig_utils.getSearchView().setIconified(true);

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

        Boolean enable = sharedPreferences.getBoolean(EXTRA_ENABLE_NOTIF,false);

        if(enable){
            alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            intent.putExtra(EXTRA_USER_ID, mCurrentUser.getUid());
            alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
    }

    // ---------------------------------------------------------------------------------
    // -------------------         CONFIGURATION OF SETTINGS         -------------------
    // ---------------------------------------------------------------------------------

    public void configure_and_show_settings_activity() {

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
                if (alarmMgr != null) {
                    alarmMgr.cancel(alarmIntent);
                }
            }
        } else {
            if (resultCode == RESULT_FIRST_USER) { // if the user has enabled notif, whereas it was disabled when opening settings
                configureAlarmManager();
            }
        }
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

    public int getCurrentPage(){
        return current_page;
    }

    public String get_API_KEY(){
        return api_key;
    }

    public MultiFragAdapter get_Page_Adapter(){
        return pageAdapter;
    }

    public LatLng get_current_position(){
        return currentPlaceLatLng;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public FirebaseUser getCurrentUser() {
        return mCurrentUser;
    }

    public LatLng getCurrentPlaceLatLng() {
        return currentPlaceLatLng;
    }

    @Override
    public void onRefresh() {
        String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS,500));
        String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE,"restaurant");
        new List_Search_Nearby(api_key, currentPlaceLatLng, radius, type, this);
    }


}






    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        searchItem = menu.findItem(R.id.search);


        return true;
    }*/

